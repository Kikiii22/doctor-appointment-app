import { Component, OnInit } from '@angular/core';
import { Auth } from '../../services/auth';
import { HospitalService } from '../../services/hospital';
import { Router } from '@angular/router';
import { NgForOf, NgIf } from '@angular/common';
import { Hospital } from '../../interfaces/hospital';
import { Doctor } from '../../interfaces/doctor';
import { User } from '../../interfaces/user';

@Component({
  selector: 'app-hospital-dashboard',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  templateUrl: 'dashboard.html',
  styleUrl: 'dashboard.css'
})
export class HospitalDashboardComponent implements OnInit {
  currentUser: User | null = null;
  currentHospital: Hospital | null = null;
  doctors: Doctor[] = [];

  constructor(
    private authService: Auth,
    private hospitalService: HospitalService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    this.currentUser = user;

    this.hospitalService.getAllHospitals().subscribe({
      next: (hospitals) => {
        const hospital = hospitals.find(h => h.user.id === user.id);
        if (hospital) {
          this.currentHospital = hospital;
          this.loadDoctors(hospital.id);
        }
      },
      error: (err) => console.error('Error loading hospitals:', err)
    });
  }

  loadDoctors(hospitalId: number): void {
    this.hospitalService.getHospitalDoctors(hospitalId).subscribe({
      next: (doctors) => {
        this.doctors = doctors;
      },
      error: (err) => console.error('Error loading doctors:', err)
    });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
