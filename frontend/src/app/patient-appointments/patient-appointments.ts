import {Component, OnInit} from '@angular/core';
import {Appointment} from '../interfaces/appointment';
import {Auth} from '../services/auth';
import {AppointmentService} from '../services/appointment';
import {DatePipe, NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {User} from '../interfaces/user';

@Component({
  selector: 'app-patient-appointments',
  imports: [
    NgIf,
    NgForOf,

  ],
  templateUrl: './patient-appointments.html',
  styleUrl: './patient-appointments.css'
})
export class PatientAppointments implements OnInit{
  today = new Date();
  activeTab: 'today' | 'upcoming' | 'past' = 'today';
  currentUser: User| null = null;

  stats = { today: 0, upcoming: 0, thisMonth: 0, completed: 0 };

  appointmentsToday: Appointment[] = [];
  upcomingAppointments: Appointment[] = [];
  pastAppointments: Appointment[] = [];

  constructor(
    private appointmentService: AppointmentService,
    private auth: Auth,
    private router:Router
  ) {}

  ngOnInit() {
  this.currentUser = this.auth.getCurrentUser();
    console.log("najnov korisnik",this.currentUser)
    if (this.currentUser?.id) {
      this.loadAppointments(this.currentUser.id);
    }
  }

  private loadAppointments(patientId: number) {
    this.appointmentService.getPatientAppointments(patientId).subscribe({
      next: (appointments) => {
        const todayStr = this.today.toISOString().split('T')[0];
        const todayList = appointments.filter(a => a.slot.date === todayStr);
        const upcomingList = appointments.filter(a =>
          new Date(`${a.slot.date}T${a.slot.startTime}`) > this.today &&
          a.slot.date !== todayStr &&
          a.status !== 'FINISHED'
        );
        const pastList = appointments.filter(a =>
          new Date(`${a.slot.date}T${a.slot.startTime}`) < this.today ||
          a.status === 'FINISHED'
        );

        this.appointmentsToday = todayList;
        this.upcomingAppointments = upcomingList;
        this.pastAppointments = pastList;

        this.stats.today = todayList.length;
        this.stats.upcoming = upcomingList.length;
        this.stats.thisMonth = appointments.filter(a =>
          new Date(a.slot.date).getMonth() === this.today.getMonth()
        ).length;
        this.stats.completed = appointments.filter(a => a.status === 'FINISHED').length;
      },
      error: (err) => console.error('Error loading appointments:', err)
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
          // Remove from UI immediately
          this.upcomingAppointments = this.upcomingAppointments.filter(a => a.id !== apt.id);

          // Optional: toast message
          alert('Appointment cancelled and slot is now available.');
        },
        error: (err) => {
          console.error('Error cancelling appointment:', err);
          alert('Could not cancel the appointment.');
        }
      });
  }
}
