import { Component } from '@angular/core';
import {Doctor} from '../../interfaces/doctor';
import {Slot} from '../../interfaces/slot';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {DoctorService} from '../../services/doctor';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Auth} from '../../services/auth';
import {User} from '../../interfaces/user';
import {AppointmentService} from '../../services/appointment';

type DayTab = { iso: string; label: string };

interface CalendarDay {
  dayNumber: number;
  dateISO: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  slots: Slot[];
  loading: boolean;
}
declare var bootstrap: any;

@Component({
  selector: 'app-doctor-details',
  imports: [

    NgIf,
    NgForOf,
    DatePipe,

  ],
  templateUrl: './doctor-details.html',
  styleUrl: './doctor-details.css'
})
export class DoctorDetails {
  doctorId = 0;
  doctor: Doctor | null = null;
  currentUser: User | null = null;
  earliestSlot: Slot | null = null;
  currentCalendarDate = new Date();
  calendarDays: CalendarDay[] = [];
  days: DayTab[] = [];
  selectedDateISO = '';
  loadingSlots = false;
  selectedSlot:Slot|null=null;
  slots: Slot[] = [];
  private slotsCache = new Map<string, Slot[]>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private authService: Auth,
    private appointmentService: AppointmentService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.doctorId = Number(this.route.snapshot.paramMap.get('id'));
    this.buildDays(14);
    this.selectedDateISO = this.days[0]?.iso || this.todayISO();
    this.loadDoctorDetails();
    this.generateCalendar();
    this.loadEarliestSlot();
    this.loadSlotsForDate(this.selectedDateISO);
  }
  openConfirmModal(slot: Slot) {
    console.log('Opening modal for slot:', slot); // Debug log
    this.selectedSlot = slot;

    try {
      const modalEl = document.getElementById('confirmBookingModal');

      if (!modalEl) {
        console.error('Modal element not found!');
        alert('Modal not found. Please check your template.');
        return;
      }

      // Check if Bootstrap is available
      if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap is not loaded!');
        alert('Bootstrap is not loaded properly.');
        return;
      }

      const modal = new bootstrap.Modal(modalEl, {
        backdrop: 'static', // Optional: prevent closing by clicking backdrop
        keyboard: true
      });

      modal.show();
      console.log('Modal should be showing now'); // Debug log

    } catch (error) {
      console.error('Error opening modal:', error);
      // Fallback to confirm dialog
      if (confirm(`Book appointment with ${this.doctor?.fullName} on ${slot.date} at ${this.timeOf(slot)}?`)) {
        this.confirmBooking();
      }
    }
  }
  confirmBooking() {
    if (!this.selectedSlot) return;

    this.appointmentService.bookAppointment(this.selectedSlot.id).subscribe({
      next: () => {
        this.markSlotAsBooked(this.selectedSlot!);
        alert('Booking confirmed!');
        const modalEl = document.getElementById('confirmBookingModal');
        const modal = bootstrap.Modal.getInstance(modalEl!);
        modal.hide();
        this.refreshSlotData();
      },
      error: err => {
        console.error(err);
        alert('Failed to book slot.');
      }
    });
  }
  private markSlotAsBooked(slot: Slot): void {
    // Update in main slots array
    const mainSlotIndex = this.slots.findIndex(s => s.id === slot.id);
    if (mainSlotIndex !== -1) {
      (this.slots[mainSlotIndex] as any).booked = true;
      (this.slots[mainSlotIndex] as any).isAvailable = false;
    }

    // Update in calendar days
    this.calendarDays.forEach(day => {
      const calendarSlotIndex = day.slots.findIndex(s => s.id === slot.id);
      if (calendarSlotIndex !== -1) {
        (day.slots[calendarSlotIndex] as any).booked = true;
        (day.slots[calendarSlotIndex] as any).isAvailable = false;
      }
    });
    this.slotsCache.forEach((cachedSlots, dateKey) => {
      const cachedSlotIndex = cachedSlots.findIndex(s => s.id === slot.id);
      if (cachedSlotIndex !== -1) {
        (cachedSlots[cachedSlotIndex] as any).booked = true;
        (cachedSlots[cachedSlotIndex] as any).isAvailable = false;
      }
    });

    // Update earliest slot if it was the one booked
    if (this.earliestSlot && this.earliestSlot.id === slot.id) {
      this.loadEarliestSlot(); // Reload to find next available
    }}
  goBack(): void {
    this.router.navigate(['/patient/doctors']);
  }
  private refreshSlotData(): void {
    // Clear cache for the booked date to force fresh data
    this.slotsCache.delete(this.selectedSlot?.date || '');

    // Reload slots for current selected date
    this.loadSlotsForDate(this.selectedDateISO);

    // Reload calendar day slots
    const calendarDay = this.calendarDays.find(day => day.dateISO === this.selectedSlot?.date);
    if (calendarDay) {
      this.loadSlotsForCalendarDay(calendarDay);
    }

    // Reload earliest slot
    this.loadEarliestSlot();
  }
  generateCalendar(): void {
    const year = this.currentCalendarDate.getFullYear();
    const month = this.currentCalendarDate.getMonth();

    const firstDay = new Date(year, month, 1);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    this.calendarDays = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(startDate);
      date.setDate(startDate.getDate() + i);

      const dateISO = this.dateToISO(date);
      const calendarDay: CalendarDay = {
        dayNumber: date.getDate(),
        dateISO: dateISO,
        isCurrentMonth: date.getMonth() === month,
        isToday: dateISO === this.todayISO(),
        slots: [],
        loading: false
      };

      this.calendarDays.push(calendarDay);
    }

    this.loadSlotsForVisibleDays();
  }

  private loadSlotsForVisibleDays(): void {
    const currentMonthDays = this.calendarDays.filter(day => day.isCurrentMonth);
    const futureDays = currentMonthDays.filter(day => !this.isDateInPast(day.dateISO));
    futureDays.forEach(day => {
      this.loadSlotsForCalendarDay(day);
    });
  }

  private loadSlotsForCalendarDay(day: CalendarDay): void {
    if (this.isDateInPast(day.dateISO)) {
      day.slots = [];
      return;
    }

    if (this.slotsCache.has(day.dateISO)) {
      day.slots = this.filterFutureSlots(this.slotsCache.get(day.dateISO) || []);
      return;
    }

    day.loading = true;
    this.doctorService.getDoctorSlots(this.doctorId, undefined, day.dateISO).subscribe({
      next: (slots) => {
        const normalizedSlots = (slots ?? []).map(s => this.normalizeSlot(s));
        const sortedSlots = normalizedSlots.sort((a, b) => this.slotTs(a) - this.slotTs(b));

        this.slotsCache.set(day.dateISO, sortedSlots);
        day.slots = this.filterFutureSlots(sortedSlots);
      },
      error: (err) => {
        console.error('Error loading slots for', day.dateISO, err);
        day.slots = [];
      },
      complete: () => {
        day.loading = false;
      }
    });
  }

  private loadDoctorDetails(): void {
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: (doctor) => (this.doctor = doctor),
      error: (err) => console.error('Error loading doctor:', err),
    });
  }

  private loadEarliestSlot(): void {
    this.doctorService.getDoctorSlots(this.doctorId, 1).subscribe({
      next: (slots) => {
        const futureSlots = (slots ?? [])
          .map(x => this.normalizeSlot(x))
          .filter(slot => !this.isSlotInPast(slot))
          .filter(x => !(x as any).booked && (x as any).isAvailable !== false);

        this.earliestSlot = futureSlots[0] || null;
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
        const sortedSlots = list.sort((a, b) => this.slotTs(a) - this.slotTs(b));

        this.slots = this.filterFutureSlots(sortedSlots);
      },
      error: (err) => console.error('Error loading slots:', err),
      complete: () => (this.loadingSlots = false),
    });
  }

  isAvailable(slot: Slot): boolean {
    const booked = (slot as any).booked === true;
    const isAvail = (slot as any).isAvailable;

    if (this.isSlotInPast(slot)) {
      return false;
    }

    return !booked && (isAvail === undefined || isAvail === true);
  }

  protected isSlotInPast(slot: Slot): boolean {
    const now = new Date();
    const slotDateTime = this.getSlotDateTime(slot);
    return slotDateTime <= now;
  }

  private getSlotDateTime(slot: Slot): Date {
    const timeStr = this.timeOf(slot) || '00:00';
    return new Date(`${slot.date}T${timeStr}`);
  }

  timeOf(slot: Slot): string {
    return (slot as any).startTime || (slot as any).time || '';
  }
  navigateTo(url: string) {
    this.router.navigateByUrl(url);
  }
  previousMonth(): void {
    this.currentCalendarDate = new Date(
      this.currentCalendarDate.getFullYear(),
      this.currentCalendarDate.getMonth() - 1,
      1
    );
    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentCalendarDate = new Date(
      this.currentCalendarDate.getFullYear(),
      this.currentCalendarDate.getMonth() + 1,
      1
    );
    this.generateCalendar();
  }

  get currentMonthLabel(): string {
    return this.currentCalendarDate.toLocaleDateString(undefined, {
      month: 'long',
      year: 'numeric'
    });
  }
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  private normalizeSlot(s: Slot): Slot {
    if (!(s as any).startTime && (s as any).time) {
      (s as any).startTime = (s as any).time;
    }
    return s;
  }

  private slotTs(s: Slot): number {
    const t = this.timeOf(s) || '00:00';
    return new Date(`${s.date}T${t}`).getTime();
  }

  private filterFutureSlots(slots: Slot[]): Slot[] {
    return slots.filter(slot => !this.isSlotInPast(slot));
  }

  protected isDateInPast(dateISO: string): boolean {
    const today = this.todayISO();
    return dateISO < today;
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
    return this.dateToISO(d);
  }

  private dateToISO(date: Date): string {
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${m}-${day}`;
  }

  private addDaysISO(iso: string, add: number): string {
    const d = new Date(iso);
    d.setDate(d.getDate() + add);
    return this.dateToISO(d);
  }
}
