import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../interfaces/appointment';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { NgForOf, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../../interfaces/user';
import { DoctorService } from '../../services/doctor';
import { FormsModule } from '@angular/forms';
import { AppointmentView } from '../../interfaces/appointmentView';

@Component({
  selector: 'app-doctor-appointments',
  imports: [NgIf, NgForOf, FormsModule],
  templateUrl: './doctor-appointments.html',
  styleUrl: './doctor-appointments.css'
})

export class DoctorAppointments implements OnInit {
  today = new Date();
  activeTab: 'today' | 'upcoming' | 'past' = 'today';
  currentUser: User | null = null;
  currentDoctor: any;

  stats = { today: 0, upcoming: 0, thisMonth: 0, completed: 0 };

  appointmentsToday: Appointment[] = [];
  upcomingAppointments: Appointment[] = [];
  pastAppointments: AppointmentView[] = [];

  unfinishedCount = 0;

  editingAppointmentId: number | null = null;
  newDescription: string = '';

  constructor(
    private appointmentService: AppointmentService,
    private auth: Auth,
    private router: Router,
    private doctorService: DoctorService
  ) { }

  ngOnInit() {
    this.currentUser = this.auth.getCurrentUser();
    this.doctorService.getDoctorByUserId(this.currentUser!.id).subscribe({
      next: (doctor) => {
        this.currentDoctor = doctor;
        this.loadAppointments(this.currentDoctor.id);
        this.loadFinishedAppointments(this.currentDoctor.id);
      },
      error: (err) => console.error('Error loading doctor:', err)
    });
  }

  private loadAppointments(doctorId: number) {
    this.appointmentService.getDoctorUpcomingAppointments(doctorId).subscribe({
      next: (appointments) => {
        this.appointmentsToday = appointments.filter(apt => {
          const slotDate = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          return (
            slotDate.getFullYear() === this.today.getFullYear() &&
            slotDate.getMonth() === this.today.getMonth() &&
            slotDate.getDate() === this.today.getDate()
          );
        });

        this.upcomingAppointments = appointments.filter(apt => {
          const slotDate = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          return slotDate > this.today;
        });

        this.stats.today = this.appointmentsToday.length;
        this.stats.upcoming = this.upcomingAppointments.length;
        this.stats.thisMonth = appointments.filter(apt => {
          const slotDate = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          return (
            slotDate.getMonth() === this.today.getMonth() &&
            slotDate.getFullYear() === this.today.getFullYear()
          );
        }).length;
      },
      error: (err) => console.error('Error loading upcoming appointments:', err)
    });
  }

  private loadFinishedAppointments(doctorId: number) {
    this.appointmentService.getDoctorFinishedAppointments(doctorId).subscribe({
      next: (appointments) => {
        this.pastAppointments = appointments.map(a => ({
          ...a,
          showDescriptionForm: false,
          newDescription: ''
        }));
        this.stats.completed = appointments.length;
        this.unfinishedCount = this.pastAppointments.filter(a => !a.description || a.description.trim() === '').length;
      },
      error: (err) => console.error('Error loading finished appointments:', err)
    });
  }


  toggleDescriptionForm(apt: AppointmentView) {
    apt.showDescriptionForm = !apt.showDescriptionForm;
    if (apt.showDescriptionForm) {
      apt.newDescription = apt.description || '';
    }
  }


  startEditing(appointmentId: number) {
    this.editingAppointmentId = appointmentId;
    this.newDescription = '';
  }


  saveDescription(apt: AppointmentView) {
    if (!apt.newDescription || apt.newDescription.trim() === '') return;

    this.appointmentService.finishAppointment(
      apt.id,
      this.currentDoctor.id,
      apt.newDescription
    ).subscribe({
      next: (updated) => {
        apt.description = updated.description;
        apt.newDescription = '';
        apt.showDescriptionForm = false;
        this.unfinishedCount = this.pastAppointments.filter(a => !a.description || a.description.trim() === '').length;
      },
      error: (err) => {
        console.error('Error finishing appointment:', err);
      }
    });
  }



  switchTab(tab: 'today' | 'upcoming' | 'past') {
    this.activeTab = tab;
  }

  logout() {
    this.auth.logout();
  }

  navigateTo(s: string) {
    this.router.navigateByUrl(s);
  }

  cancelAppointment(apt: Appointment) {
    if (!this.currentUser?.id) return;
    this.appointmentService.cancelAppointment(apt.slot.id).subscribe({
      next: () => {
        this.upcomingAppointments = this.upcomingAppointments.filter(a => a.id !== apt.id);
        alert('Appointment cancelled and slot is now available.');
      },
      error: (err) => {
        console.error('Error cancelling appointment:', err);
        alert('Could not cancel the appointment.');
      }
    });
  }
}
