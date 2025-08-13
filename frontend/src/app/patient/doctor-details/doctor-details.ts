import { Component } from '@angular/core';
import {Doctor} from '../../interfaces/doctor';
import {Slot} from '../../interfaces/slot';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {DoctorService} from '../../services/doctor';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {Auth} from '../../services/auth';
import {User} from '../../interfaces/user';

type DayTab = { iso: string; label: string };

interface CalendarDay {
  dayNumber: number;
  dateISO: string;
  isCurrentMonth: boolean;
  isToday: boolean;
  slots: Slot[];
  loading: boolean;
}

@Component({
  selector: 'app-doctor-details',
  imports: [
    NgClass,
    NgIf,
    NgForOf,
    DatePipe,
    RouterLink
  ],
  templateUrl: './doctor-details.html',
  styleUrl: './doctor-details.css'
})
export class DoctorDetails {
  doctorId = 0;

  doctor: Doctor | null = null;
  currentUser: User | null = null;

  earliestSlot: Slot | null = null;

  // Calendar navigation
  currentCalendarDate = new Date();
  calendarDays: CalendarDay[] = [];

  // Legacy support for existing functionality
  days: DayTab[] = [];
  selectedDateISO = '';

  // Slots for the selected date
  loadingSlots = false;
  slots: Slot[] = [];

  // Cache for loaded slots to avoid repeated API calls
  private slotsCache = new Map<string, Slot[]>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private authService: Auth
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.doctorId = Number(this.route.snapshot.paramMap.get('id'));

    // Keep existing functionality
    this.buildDays(14);
    this.selectedDateISO = this.days[0]?.iso || this.todayISO();

    // Initialize calendar
    this.generateCalendar();

    this.loadDoctorDetails();
    this.loadEarliestSlot();
    this.loadSlotsForDate(this.selectedDateISO);
  }

  goBack(): void {
    this.router.navigate(['/patient/doctors']);
  }

  // ----- Calendar Navigation -----
  previousMonth(): void {
    this.currentCalendarDate.setMonth(this.currentCalendarDate.getMonth() - 1);
    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentCalendarDate.setMonth(this.currentCalendarDate.getMonth() + 1);
    this.generateCalendar();
  }

  generateCalendar(): void {
    const year = this.currentCalendarDate.getFullYear();
    const month = this.currentCalendarDate.getMonth();

    // Get first day of month and calculate start date (beginning of week)
    const firstDay = new Date(year, month, 1);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay()); // Start from Sunday

    // Generate 42 days (6 weeks)
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

    // Load slots for all visible days in current month
    this.loadSlotsForVisibleDays();
  }

  private loadSlotsForVisibleDays(): void {
    const currentMonthDays = this.calendarDays.filter(day => day.isCurrentMonth);

    // Only load slots for current and future dates
    const futureDays = currentMonthDays.filter(day => !this.isDateInPast(day.dateISO));

    futureDays.forEach(day => {
      this.loadSlotsForCalendarDay(day);
    });
  }

  private loadSlotsForCalendarDay(day: CalendarDay): void {
    // Don't load slots for past dates
    if (this.isDateInPast(day.dateISO)) {
      day.slots = [];
      return;
    }

    // Check cache first
    if (this.slotsCache.has(day.dateISO)) {
      day.slots = this.filterFutureSlots(this.slotsCache.get(day.dateISO) || []);
      return;
    }

    day.loading = true;

    this.doctorService.getDoctorSlots(this.doctorId, undefined, day.dateISO).subscribe({
      next: (slots) => {
        const normalizedSlots = (slots ?? []).map(s => this.normalizeSlot(s));
        const sortedSlots = normalizedSlots.sort((a, b) => this.slotTs(a) - this.slotTs(b));

        // Cache all slots (including past ones for consistency)
        this.slotsCache.set(day.dateISO, sortedSlots);

        // But only show future slots
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

  // ----- Existing Data Load Methods -----
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
          .filter(slot => !this.isSlotInPast(slot)) // Filter past slots
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

        // Filter to show only future slots
        this.slots = this.filterFutureSlots(sortedSlots);
      },
      error: (err) => console.error('Error loading slots:', err),
      complete: () => (this.loadingSlots = false),
    });
  }

  // ----- UI Helpers -----
  get hospitalName(): string {
    return (this.doctor as any)?.department?.hospital?.name || '—';
  }

  dayLabel(d: DayTab): string {
    const today = this.todayISO();
    const tomorrow = this.addDaysISO(today, 1);
    if (d.iso === today) return 'Today';
    if (d.iso === tomorrow) return 'Tomorrow';
    const dt = new Date(d.iso);
    return dt.toLocaleDateString(undefined, {
      weekday: 'short', day: '2-digit', month: 'short'
    });
  }

  isAvailable(slot: Slot): boolean {
    const booked = (slot as any).booked === true;
    const isAvail = (slot as any).isAvailable;

    // Check if slot is in the past
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

  bookSlot(slot: Slot): void {
    if (!this.isAvailable(slot)) return;

    // Update selected date when booking from calendar
    this.selectedDateISO = slot.date;

    this.router.navigate(['/patient/doctor', this.doctorId], {
      queryParams: { date: slot.date, time: this.timeOf(slot) }
    });
  }

  getSlotTooltip(slot: Slot): string {
    if (this.isSlotInPast(slot)) {
      return 'This time slot has passed';
    }
    if (!(slot as any).booked && ((slot as any).isAvailable === undefined || (slot as any).isAvailable === true)) {
      return 'Click to book this appointment';
    }
    return 'This slot is not available';
  }



  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  // ----- Utility Methods -----
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
