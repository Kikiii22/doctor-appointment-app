import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AppointmentService } from '../../services/appointment';
import { Appointment } from '../../interfaces/appointment';
import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { User } from '../../interfaces/user';
import { Auth } from '../../services/auth';
import { DoctorService } from '../../services/doctor';
import { Doctor } from '../../interfaces/doctor';

@Component({
  selector: 'app-doctor-past-appointments',
  templateUrl: 'doctor-past-appointments.html',
  styleUrls: ['doctor-past-appointments.css'],
  imports: [
    NgIf,
    NgForOf,
  ],
})
export class DoctorPastAppointmentsComponent implements OnInit {
  doctorId = 0;
  pastAppointments: Appointment[] = [];
  loading = true;
  currentUser: User | null = null;
  doctor: Doctor | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private appointmentService: AppointmentService,
    private authService: Auth,
    private doctorService: DoctorService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.doctorId = Number(this.route.snapshot.paramMap.get('id'));
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: (doctor) => this.doctor = doctor,
      error: (err) => console.error('Error loading doctor:', err)
    });

    this.loadPastAppointments()
  }

  loadPastAppointments() {
    this.loading = true;
    this.appointmentService.getDoctorFinishedAppointments(this.doctorId)
      .subscribe({
        next: appointments => this.pastAppointments = appointments,
        error: err => console.error(err),
        complete: () => this.loading = false
      });
  }

  back() {
    this.router.navigate([`/hospital/doctor-details/${this.doctorId}`]);
  }

  navigateTo(url: string) { this.router.navigateByUrl(url); }


  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

}
