import { Component, OnInit } from '@angular/core';
import { Auth } from '../../services/auth';
import { AppointmentService } from '../../services/appointment';
import { Router } from '@angular/router';
import {  NgForOf, NgIf } from '@angular/common';
import { Appointment } from '../../interfaces/appointment';
import { DoctorService } from '../../services/doctor';
import { User } from '../../interfaces/user';

@Component({
  selector: 'app-doctor-dashboard',
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: 'dashboard.html',
  styleUrl: 'dashboard.css'
})
export class DoctorDashboardComponent implements OnInit {
  currentUser: User | null = null
  currentDoctor: any
  currentAppointment:Appointment | null = null;
  upcomingAppointments: Appointment[] = [];
  appointmentsToday: Appointment[] = [];
  today = new Date();
  timeUntilCurrent: string = '';

  constructor(
    private authService: Auth,
    private appointmentService: AppointmentService,
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
        this.loadAppointments(doctor.id);
      },
      error: (err) => console.error(err)
    });

  }


  private loadAppointments(doctorId: number) {
    this.appointmentService.getDoctorUpcomingAppointments(doctorId).subscribe({
      next: (appointments) => {
       console.log("appointments", appointments)
        this.upcomingAppointments = appointments.filter(apt => {
          const slotDate = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          return slotDate > this.today;
        });
        this.appointmentsToday = appointments.filter(apt => {
          const slotDate2 = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
          return slotDate2.toDateString() === this.today.toDateString();})
        console.log("appointments today", this.appointmentsToday)
        this.updateCurrentAppointment();
        setInterval(() => this.updateCurrentAppointment(), 60 * 1000);
      },
      error: (err) => console.error('Error loading upcoming appointments:', err)
    });
  }

  private updateCurrentAppointment() {
    const now = new Date();

    this.currentAppointment = this.appointmentsToday.find(apt => {
      const start = new Date(`${apt.slot.date}T${apt.slot.startTime}`);
      const end = new Date(start.getTime() + 30 * 60000); //
      return end > now;
    }) || null;

    if (this.currentAppointment) {
      this.timeUntilCurrent = this.getTimeUntil(`${this.currentAppointment.slot.date}T${this.currentAppointment.slot.startTime}`);
    } else {
      this.timeUntilCurrent = '';
    }
  }

  private getTimeUntil(startTime: string): string {
    const now = new Date();
    const start = new Date(startTime);
    const diffMs = start.getTime() - now.getTime();

    if (diffMs > 0) {
      const mins = Math.floor(diffMs / 60000);
      const hours = Math.floor(mins / 60);
      if (hours > 0) return `in ${hours}h ${mins % 60}m`;
      return `in ${mins} mins`;
    } else {
      return 'ongoing';
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
