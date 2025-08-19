import { Component, OnInit } from '@angular/core';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { NotificationService } from '../../services/notification';
import { Router } from '@angular/router';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { Appointment } from '../../interfaces/appointment';
import { DoctorService } from '../../services/doctor';

@Component({
  selector: 'app-doctor-dashboard',
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: 'dashboard.html',
  styleUrl: 'dashboard.css'
})
export class DoctorDashboardComponent implements OnInit {
  currentUser: any
  currentDoctor: any
  upcomingAppointments: Appointment[] = [];

  constructor(
    private authService: Auth,
    private appointmentService: AppointmentService,
    private notificationService: NotificationService,
    private router: Router,
    private doctorService: DoctorService
  ) { }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;
    this.currentUser = user;

    this.doctorService.getDoctorByUserId(user.id).subscribe({
      next: (doctor) => {
        console.log('Doctor from API:', doctor);
        this.currentDoctor = doctor;
        this.loadUpcomingAppointments();
      },
      error: (err) => console.error(err)
    });

  }


  loadUpcomingAppointments(): void {
    console.log('Loading appointments for doctor:', this.currentUser?.id);
    console.log(localStorage.getItem('jwt'));
    console.log(localStorage.getItem('currentUser'));

    if (this.currentUser?.id) {
      this.appointmentService.getDoctorAppointments(this.currentUser.id).subscribe({
        next: (appointments) => {
          console.log("Appointments", appointments);
          this.upcomingAppointments = appointments
            .filter(apt => new Date(`${apt.slot.date}T${apt.slot.startTime}`) > new Date())
            .slice(0, 5);
          this.notificationService.checkUpcomingAppointments(appointments);
        },
        error: (error) => console.error('Error loading doctor appointments:', error)
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
