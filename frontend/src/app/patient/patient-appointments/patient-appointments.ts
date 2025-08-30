import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import { Appointment } from '../../interfaces/appointment';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import {  NgForOf, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../../interfaces/user';
import { PatientService } from '../../services/patient';
import { Toast } from 'bootstrap';
import {Slot} from '../../interfaces/slot';
declare var bootstrap: any;

@Component({
  selector: 'app-patient-appointments',
  imports: [
    NgIf,
    NgForOf,

  ],
  templateUrl: './patient-appointments.html',
  styleUrl: './patient-appointments.css'
})
export class PatientAppointments implements OnInit {
  today = new Date();
  activeTab: 'today' | 'upcoming' | 'past'|'cancelled' = 'today';
  currentUser: User | null = null;
  currentPatient: any;
  toastMessage = '';
  @ViewChild('successToast', { static: true }) successToastRef!: ElementRef;
  stats = { today: 0, upcoming: 0, thisMonth: 0, completed: 0 };
  selectedAppointment: Appointment | null = null;
  appointmentsToday: Appointment[] = [];
  upcomingAppointments: Appointment[] = [];
  pastAppointments: Appointment[] = [];
  cancelledAppointments: Appointment[] = [];
  constructor(
    private appointmentService: AppointmentService,
    private auth: Auth,
    private router: Router,
    private patientService: PatientService
  ) { }

  ngOnInit() {
    this.currentUser = this.auth.getCurrentUser();
    this.patientService.getPatientByUserId(this.currentUser!.id).subscribe({
      next: (patient) => {
        this.currentPatient = patient;
        this.loadAppointments(this.currentPatient.id)
        this.loadFinishedAppointments(this.currentPatient.id);
        this.loadCancelledAppointments(this.currentPatient.id);
      },
      error: (err) => console.error('Error loading patient:', err)
    });
  }
private loadCancelledAppointments(patientId: number) {
    this.appointmentService.getCancelledAppointments(patientId).subscribe({
      next: (appointments) => {
        this.cancelledAppointments = appointments;
      },
      error: (err) => console.error('Error loading cancelled appointments:', err)
    })
}
  private loadAppointments(patientId: number) {
    this.appointmentService.getPatientUpcomingAppointments(patientId).subscribe({
      next: (appointments) => {
        const todayAppointments: Appointment[] = [];
        const upcomingAppointments: Appointment[] = [];

        appointments.forEach(apt => {
          const slotDate = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          if (
            slotDate.getFullYear() === this.today.getFullYear() &&
            slotDate.getMonth() === this.today.getMonth() &&
            slotDate.getDate() === this.today.getDate()
          ) {
            todayAppointments.push(apt);
          }
          if (slotDate.getMonth() == this.today.getMonth()) {
            this.stats.thisMonth++;
          }
          upcomingAppointments.push(apt);

        });

        this.appointmentsToday = todayAppointments;
        this.upcomingAppointments = upcomingAppointments;

        this.stats.today = todayAppointments.length;
        this.stats.upcoming = upcomingAppointments.length;
      },
      error: (err) => console.error('Error loading upcoming appointments:', err)
    });
  }


  private loadFinishedAppointments(patientId: number) {
    this.appointmentService.getPatientFinishedAppointments(patientId).subscribe({
      next: (appointments) => {
        this.pastAppointments = appointments;
        this.stats.completed = appointments.length;
      },
      error: (err) => console.error('Error loading finished appointments:', err)
    });
  }


  switchTab(tab: 'today' | 'upcoming' | 'past'|'cancelled') {
    this.activeTab = tab;
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login'], { replaceUrl: true });

  }

  navigateTo(s: string) {
    this.router.navigateByUrl(s);
  }

  rescheduleAppointment(apt: Appointment) {
    this.router.navigate(['/patient/doctors', apt.slot.doctor.id], {
      queryParams: { reschedule: apt.slot.id }
    });
  }
  openCancelModal(appointment: Appointment) {
    this.selectedAppointment = appointment;

    try {
      const modalEl = document.getElementById('cancelAppointmentModal');

      if (!modalEl) {
        alert('Modal not found. Please check your template.');
        return;
      }

      if (typeof bootstrap === 'undefined') {
        alert('Bootstrap is not loaded properly.');
        return;
      }

      const modal = new bootstrap.Modal(modalEl, {
        backdrop: 'static',
        keyboard: true
      });

      modal.show();
    } catch (error) {
      if (confirm(`Cancel appointment with Dr. ${appointment.slot.doctor.fullName} on ${appointment.slot.date} at ${this.timeOf(appointment.slot)}?`)) {
        this.cancelAppointment();
      }
    }
  }
  timeOf(slot: Slot): string {
    return (slot as any).startTime || (slot as any).time || '';
  }
  cancelAppointment() {
    if (!this.selectedAppointment ||!this.currentUser?.id) return;
    this.appointmentService.cancelAppointment(this.selectedAppointment.slot.id)
      .subscribe({
        next: () => {
          this.upcomingAppointments = this.upcomingAppointments.filter(a => a.id !== this.selectedAppointment!.id);
          this.appointmentsToday = this.appointmentsToday.filter(a => a.id !== this.selectedAppointment!.id);

          const modalEl = document.getElementById('cancelAppointmentModal');
          const modal = bootstrap.Modal.getInstance(modalEl!);
          this.toastMessage = 'Appointment cancelled successfully and slot is now available';
          const toast = new Toast(this.successToastRef.nativeElement, { delay: 3000 });
          toast.show();
          if (modalEl) {
            const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
            modal.hide();
          }

          },
        error: (err) => {
          console.error('Error cancelling appointment:', err);
                 }
      });
  }
}
