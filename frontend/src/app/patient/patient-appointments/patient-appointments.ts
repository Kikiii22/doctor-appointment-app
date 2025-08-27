import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../interfaces/appointment';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { User } from '../../interfaces/user';
import { PatientService } from '../../services/patient';

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

  stats = { today: 0, upcoming: 0, thisMonth: 0, completed: 0 };

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
    console.log("najnov korisnik", this.currentUser)
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
