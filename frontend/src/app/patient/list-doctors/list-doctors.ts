import { Component, OnInit } from '@angular/core';
import { DoctorService } from '../../services/doctor';
import { Slot } from '../../interfaces/slot';
import { Router } from '@angular/router';
import { Doctor } from '../../interfaces/doctor';
import { FormsModule } from '@angular/forms';
import { NgForOf, NgIf, NgStyle } from '@angular/common';
import { Auth } from '../../services/auth';
import { DepartmentService } from '../../services/department';

@Component({
  selector: 'app-list-doctors',
  imports: [
    FormsModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './list-doctors.html',
  styleUrl: './list-doctors.css'
})
export class ListDoctors implements OnInit {
  currentUser: { username: string } | null = null
  searchTerm = ''
  loading = false
  error: string | null = null
  searchBySymptoms = false

  doctors: Doctor[] = [];
  earliest: Record<number, Slot | null> = {};

  constructor(
    private doctorsService: DoctorService,
    private authService: Auth,
    protected router: Router,
    private departmentService: DepartmentService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDoctors();
  }

  navigateTo(url: string) {
    this.router.navigateByUrl(url);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });

  }

  private loadDoctors(): void {
    this.loading = true;
    this.error = null;

    this.doctorsService.getAllDoctors().subscribe({
      next: (docs) => {
        this.doctors = docs ?? [];
        this.filteredDoctors = this.doctors;
        for (const d of this.doctors) this.loadEarliestSlot(d.id);
      },
      error: (e) => {
        console.error(e);
        this.error = 'Failed to load doctors.';
      },
      complete: () => (this.loading = false),
    });
  }

  private loadEarliestSlot(doctorId: number) {
    this.earliest[doctorId] = null;
    this.doctorsService.getDoctorSlots(doctorId, 1).subscribe({
      next: (slots) => {
        const sorted = [...(slots ?? [])].sort((a, b) => {
          const ta = new Date(`${a.date}T${(a as any).startTime ?? (a as any).time ?? '00:00'}`).getTime();
          const tb = new Date(`${b.date}T${(b as any).startTime ?? (b as any).time ?? '00:00'}`).getTime();
          return ta - tb;
        });
        this.earliest[doctorId] = sorted[0] ?? null;
      },
      error: () => (this.earliest[doctorId] = null),
    });
  }

  filteredDoctors: Doctor[] = [];

  searchDoctors() {
    if (this.searchBySymptoms) {
      const query = this.searchTerm.trim();
      if (!query) {
        this.filteredDoctors = this.doctors;
        return;
      }

      this.departmentService.searchDoctorsBySymptoms(query).subscribe(doctors => {
        this.filteredDoctors = doctors;
      });
    } else {
      const q = this.searchTerm.trim().toLowerCase();
      this.filteredDoctors = !q
        ? this.doctors
        : this.doctors.filter(d =>
          d.fullName.toLowerCase().includes(q) ||
          d.department.name.toLowerCase().includes(q)
        );
    }
  }

  bookAppointment(id: number) {
    this.router.navigate(['/patient/doctors', id]);
  }

  viewDoctor(id: number) {
    this.router.navigate(['/patient/doctors', id]);
  }

  toggleSearchBySymptoms() {
    this.searchBySymptoms = !this.searchBySymptoms;
  }
}
