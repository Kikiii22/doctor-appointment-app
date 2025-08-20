import { Component, OnInit } from '@angular/core';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { NotificationService } from '../../services/notification';
import { Router } from '@angular/router';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { Appointment } from '../../interfaces/appointment';
import { PatientService } from '../../services/patient';

@Component({
  selector: 'app-dashboard',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class PatientDashboardComponent implements OnInit {
  currentUser: any
  currentPatient: any
  upcomingAppointments: Appointment[] = [];

  constructor(
    private authService: Auth,
    private appointmentService: AppointmentService,
    private notificationService: NotificationService,
    private router: Router,
    private patientService: PatientService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.patientService.getPatientByUserId(this.currentUser.id).subscribe({
      next: (patient) => {
        console.log('Patient from API:', patient);
        this.currentPatient = patient;
        this.loadUpcomingAppointments();
      },
      error: (err) => console.error(err)
    });
  }

  loadUpcomingAppointments(): void {
    console.log('Loading appointments for patient:', this.currentPatient?.id);
    console.log(localStorage.getItem('jwt'))
    console.log(localStorage.getItem('currentUser'))

    if (this.currentPatient?.id) {
      this.appointmentService.getPatientUpcomingAppointments(this.currentPatient.id).subscribe({
        next: (appointments) => {
          console.log("Appointments", appointments)
          console.log(appointments[0])
          this.upcomingAppointments = appointments.slice(0, 5);
          this.notificationService.checkUpcomingAppointments(appointments);
        },
        error: (error) => console.error('Error loading appointments:', error)
      });
    }
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
