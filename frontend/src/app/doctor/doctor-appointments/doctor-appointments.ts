import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../interfaces/appointment';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../../interfaces/user';
import { DoctorService } from '../../services/doctor';

@Component({
  selector: 'app-doctor-appointments',
  imports: [
    NgIf,
    NgForOf,

  ],
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
  pastAppointments: Appointment[] = [];

  constructor(
    private appointmentService: AppointmentService,
    private auth: Auth,
    private router: Router,
    private doctorService: DoctorService
  ) { }

  ngOnInit() {
    this.currentUser = this.auth.getCurrentUser();
    console.log("najnov korisnik", this.currentUser)
    this.doctorService.getDoctorByUserId(this.currentUser!.id).subscribe({
      next: (doctor) => {
        this.currentDoctor = doctor
        this.loadAppointments(this.currentDoctor.id)
        this.loadFinishedAppointments(this.currentDoctor.id);
      },
      error: (err) => console.error('Error loading patient:', err)
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
          return slotDate.getMonth() === this.today.getMonth() &&
            slotDate.getFullYear() === this.today.getFullYear();
        }).length;
      },
      error: (err) => console.error('Error loading upcoming appointments:', err)
    });
  }



  private loadFinishedAppointments(doctorId: number) {
    this.appointmentService.getDoctorFinishedAppointments(doctorId).subscribe({
      next: (appointments) => {
        this.pastAppointments = appointments;
        this.stats.completed = appointments.length;
      },
      error: (err) => console.error('Error loading finished appointments:', err)
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

  rescheduleAppointment(apt: Appointment) {

  }

  cancelAppointment(apt: Appointment) {
    if (!this.currentUser?.id) return;
    this.appointmentService.cancelAppointment(apt.slot.id)
      .subscribe({
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
