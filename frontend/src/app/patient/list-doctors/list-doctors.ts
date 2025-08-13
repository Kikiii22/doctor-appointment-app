import {Component, OnInit} from '@angular/core';
import { DoctorService} from '../../services/doctor';
import {Slot} from '../../interfaces/slot';
import {Router} from '@angular/router';
import {Doctor} from '../../interfaces/doctor';
import {FormsModule} from '@angular/forms';
import {NgForOf, NgIf} from '@angular/common';
import {Auth} from '../../services/auth';

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
export class ListDoctors implements OnInit{
  currentUser: { username: string } | null = null; // wire to your auth user if you have one
  searchTerm = '';
  loading = false;
  error: string | null = null;

  doctors: Doctor[] = [];
  earliest: Record<number, Slot | null> = {};

  constructor(
    private doctorsService: DoctorService,
    private authService: Auth,
    protected router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDoctors();
  }

  navigateTo(url: string) {
    this.router.navigateByUrl(url);
  }

  logout() {
    // hook up to your auth service
    this.router.navigateByUrl('/login');
  }

  private loadDoctors(): void {
    this.loading = true;
    this.error = null;

    this.doctorsService.getAllDoctors().subscribe({
      next: (docs) => {
        this.doctors = docs ?? [];
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

  get filteredDoctors(): Doctor[] {
    const q = this.searchTerm.trim().toLowerCase();
    if (!q) return this.doctors;
    return this.doctors.filter(d =>
      d.fullName.toLowerCase().includes(q) ||
      d.department.name.toLowerCase().includes(q)
    );
  }

  bookAppointment(id: number) {
    this.router.navigate(['/patient/doctors', id]);
  }

  viewDoctor(id: number) {
    this.router.navigate(['/patient/doctors', id]);
  }
}
