import { Component } from '@angular/core';
import {Doctor} from '../../interfaces/doctor';
import {Slot} from '../../interfaces/slot';
import {ActivatedRoute, Router} from '@angular/router';
import {DoctorService} from '../../services/doctor';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Auth} from '../../services/auth';
import {User} from '../../interfaces/user';
type DayTab = { iso: string; label: string };

@Component({
  selector: 'app-doctor-details',
  imports: [
    NgClass,
    NgIf,
    NgForOf,
    DatePipe
  ],
  templateUrl: './doctor-details.html',
  styleUrl: './doctor-details.css'
})
export class DoctorDetails {
  doctorId = 0;

  doctor: Doctor | null = null;
  currentUser:User | null = null;

  earliestSlot: Slot | null = null;

  // Calendar (next 14 days)
  days: DayTab[] = [];
  selectedDateISO = '';

  // Slots for the selected date
  loadingSlots = false;
  slots: Slot[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private authService: Auth
  ) {}
  ngOnInit(): void {
  this.currentUser = this.authService.getCurrentUser();

  this.doctorId = Number(this.route.snapshot.paramMap.get('id'));
    this.buildDays(14);
    this.selectedDateISO = this.days[0]?.iso || this.todayISO();

    this.loadDoctorDetails();
    this.loadEarliestSlot();
    this.loadSlotsForDate(this.selectedDateISO);
  }

  goBack(): void {
    this.router.navigate(['/patient/doctors']);
  }

  // ----- Data loads -----
  private loadDoctorDetails(): void {
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: (doctor) => (this.doctor = doctor),
      error: (err) => console.error('Error loading doctor:', err),
    });
  }

  private loadEarliestSlot(): void {
    this.doctorService.getDoctorSlots(this.doctorId, 1).subscribe({
      next: (slots) => {
        const s = (slots ?? [])
          .map(x => this.normalizeSlot(x))
          .find(x => !(x as any).booked && (x as any).isAvailable !== false) || null;
        this.earliestSlot = s;
      },
      error: (err) => console.error('Error loading earliest slot:', err),
    });
  }

  loadSlotsForDate(dateISO: string): void {
    this.selectedDateISO = dateISO;
    this.loadingSlots = true;
    this.slots = [];

    this.doctorService.getDoctorSlots(this.doctorId, undefined, dateISO).subscribe({
      next: (slots) => {
        const list = (slots ?? []).map(s => this.normalizeSlot(s));
        this.slots = list.sort((a, b) => this.slotTs(a) - this.slotTs(b));
      },
      error: (err) => console.error('Error loading slots:', err),
      complete: () => (this.loadingSlots = false),
    });
  }

  // ----- UI helpers -----
  get hospitalName(): string {
    // Assumes department has hospital prop; guard if undefined
    // @ts-ignore – depends on your Department interface
    return (this.doctor as any)?.department?.hospital?.name || '—';
  }

  dayLabel(d: DayTab): string {
    const today = this.todayISO();
    const tomorrow = this.addDaysISO(today, 1);
    if (d.iso === today) return 'Today';
    if (d.iso === tomorrow) return 'Tomorrow';
    // e.g. Mon 12 Aug
    const dt = new Date(d.iso);
    return dt.toLocaleDateString(undefined, {
      weekday: 'short', day: '2-digit', month: 'short'
    });
  }

  isAvailable(slot: Slot): boolean {
    const booked = (slot as any).booked === true;
    const isAvail = (slot as any).isAvailable;
    return !booked && (isAvail === undefined || isAvail === true);
  }

  timeOf(slot: Slot): string {
    // supports startTime or time
    return (slot as any).startTime || (slot as any).time || '';
  }

  bookSlot(slot: Slot): void {
    if (!this.isAvailable(slot)) return;
    // Navigate to your booking flow or open modal
    // Example:
    this.router.navigate(['/patient/doctor', this.doctorId], {
      queryParams: { date: slot.date, time: this.timeOf(slot) }
    });
  }

  // ----- Utils -----
  private normalizeSlot(s: Slot): Slot {
    // normalize startTime if backend uses "time"
    if (!(s as any).startTime && (s as any).time) {
      (s as any).startTime = (s as any).time;
    }
    return s;
  }

  private slotTs(s: Slot): number {
    const t = this.timeOf(s) || '00:00';
    return new Date(`${s.date}T${t}`).getTime();
  }

  private buildDays(n: number) {
    const base = this.todayISO();
    this.days = Array.from({ length: n }, (_, i) => {
      const iso = this.addDaysISO(base, i);
      return { iso, label: iso };
    });
  }

  private todayISO(): string {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
    // If you need local TZ handling beyond this, consider date-fns
  }
 logout(){
    this.authService.logout();
    this.router.navigate(['/login'],{ replaceUrl: true });
}
  private addDaysISO(iso: string, add: number): string {
    const d = new Date(iso);
    d.setDate(d.getDate() + add);
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }
}
