import {Component, OnInit} from '@angular/core';
import {Auth} from '../../services/auth';
import {AppointmentService} from '../../services/appointment';
import {NotificationService} from '../../services/notification';
import {Router} from '@angular/router';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Appointment} from '../../interfaces/appointment';

@Component({
  selector: 'app-dashboard',
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class PatientDashboardComponent implements OnInit {
  currentUser: any;
  upcomingAppointments: Appointment[] = [];

  constructor(
    private authService: Auth,
    private appointmentService: AppointmentService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadUpcomingAppointments();
  }

  loadUpcomingAppointments(): void {
    // This would need patient ID - you might need to modify your user model
    // For now, assuming the user object has an id
    console.log('Loading appointments for patient:', this.currentUser?.id);
    console.log(localStorage.getItem('jwt'))
    console.log(localStorage.getItem('currentUser'))
    if (this.currentUser?.id) {
      this.appointmentService.getPatientAppointments(this.currentUser.id).subscribe({
        next: (appointments) => {
          console.log("Appointments",appointments)
          console.log(appointments[0])
          this.upcomingAppointments = appointments
            .filter(apt => new Date(`${apt.slot.date}T${apt.slot.startTime}`) > new Date())
            .slice(0, 3);
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
    this.router.navigate(['/login'],{ replaceUrl: true });
  }
}
