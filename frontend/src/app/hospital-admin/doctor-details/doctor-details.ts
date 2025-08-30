import { Component, ElementRef, ViewChild } from '@angular/core';
import { Doctor } from '../../interfaces/doctor';
import { Slot } from '../../interfaces/slot';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DoctorService } from '../../services/doctor';
import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { Auth } from '../../services/auth';
import { User } from '../../interfaces/user';
import { AppointmentService } from '../../services/appointment';

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
export class HospitalDoctorDetails {
  doctorId = 0;
  doctor: Doctor | null = null;
  currentUser: User | null = null;
  earliestSlot: Slot | null = null;
  currentCalendarDate = new Date();
  calendarDays: CalendarDay[] = [];
  days: DayTab[] = [];
  selectedDateISO = '';
  loadingSlots = false;
  selectedSlot: Slot | null = null;
  appointment: any = null;
  slots: Slot[] = [];
  toastMessage = '';
  @ViewChild('successToast', { static: true }) successToastRef!: ElementRef;
  @ViewChild('errorToast', { static: true }) errorToastRef!: ElementRef;
  private slotsCache = new Map<string, Slot[]>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private authService: Auth,
    private appointmentService: AppointmentService
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.doctorId = Number(this.route.snapshot.paramMap.get('id'));
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: (doctor) => this.doctor = doctor,
      error: (err) => console.error('Error loading doctor:', err)
    });

    this.buildDays(14);
    this.selectedDateISO = this.days[0]?.iso || this.todayISO();
    this.loadDoctorDetails();
    this.generateCalendar();
    this.loadEarliestSlot();
    this.loadSlotsForDate(this.selectedDateISO);
  }

  navigateToPastAppointments(): void {
    this.router.navigate(['/hospital/doctors', this.doctorId, 'past']);
  }


  openSlotPatientModal(slot: Slot) {
    this.selectedSlot = slot;
    this.appointment = null;

    this.appointmentService.getAppointmentBySlot(slot.id).subscribe({
      next: (appointment) => {
        this.appointment = appointment;
        this.showModal();
      },
      error: (err) => {
        console.error('Error fetching appointment:', err);
        this.showModal();
      }
    });
  }


  private showModal() {
    const modalEl = document.getElementById('slotPatientModal');
    if (!modalEl) return;

    const modal = new bootstrap.Modal(modalEl, {
      backdrop: 'static',
      keyboard: true
    });

    modal.show();
  }


  goBack(): void {
    this.router.navigate(['/hospital/doctors']);
  }

  private refreshSlotData(): void {
    this.slotsCache.delete(this.selectedSlot?.date || '');
    this.loadSlotsForDate(this.selectedDateISO);
    const calendarDay = this.calendarDays.find(day => day.dateISO === this.selectedSlot?.date);
    if (calendarDay) this.loadSlotsForCalendarDay(calendarDay);
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
      this.calendarDays.push({
        dayNumber: date.getDate(),
        dateISO: dateISO,
        isCurrentMonth: date.getMonth() === month,
        isToday: dateISO === this.todayISO(),
        slots: [],
        loading: false
      });
    }
    this.loadSlotsForVisibleDays();
  }

  private loadSlotsForVisibleDays(): void {
    const currentMonthDays = this.calendarDays.filter(day => day.isCurrentMonth);
    const futureDays = currentMonthDays.filter(day => !this.isDateInPast(day.dateISO));
    futureDays.forEach(day => this.loadSlotsForCalendarDay(day));
  }

  private loadSlotsForCalendarDay(day: CalendarDay): void {
    if (this.slotsCache.has(day.dateISO)) {
      day.slots = this.slotsCache.get(day.dateISO) || [];
      return;
    }
    day.loading = true;
    this.doctorService.getDoctorSlots(this.doctorId, undefined, day.dateISO).subscribe({
      next: (slots) => {
        const normalized = (slots ?? []).map(s => this.normalizeSlot(s)).sort((a, b) => this.slotTs(a) - this.slotTs(b));
        this.slotsCache.set(day.dateISO, normalized);
        day.slots = normalized;
      },
      error: (err) => { console.error('Error loading slots:', err); day.slots = []; },
      complete: () => day.loading = false
    });
  }


  private loadDoctorDetails(): void {
    this.doctorService.getDoctorById(this.doctorId).subscribe({
      next: d => this.doctor = d,
      error: err => console.error('Error loading doctor:', err)
    });
  }

  private loadEarliestSlot(): void {
    this.doctorService.getDoctorSlots(this.doctorId, 1).subscribe({
      next: slots => {
        const future = (slots ?? [])
          .map(s => this.normalizeSlot(s))
          .filter(slot => !this.isSlotInPast(slot))
          .filter(x => !(x as any).booked && (x as any).isAvailable !== false);
        this.earliestSlot = future[0] || null;
      },
      error: err => console.error('Error loading earliest slot:', err)
    });
  }

  loadSlotsForDate(dateISO: string): void {
    this.selectedDateISO = dateISO;
    this.loadingSlots = true;
    this.slots = [];
    this.doctorService.getDoctorSlots(this.doctorId, undefined, dateISO).subscribe({
      next: s => this.slots = this.filterFutureSlots((s ?? []).map(this.normalizeSlot.bind(this)).sort((a, b) => this.slotTs(a) - this.slotTs(b))),
      error: err => console.error('Error loading slots:', err),
      complete: () => this.loadingSlots = false
    });
  }

  isAvailable(slot: Slot): boolean {
    if (this.isSlotInPast(slot)) return false;
    const booked = (slot as any).booked === true;
    const avail = (slot as any).isAvailable;
    return !booked && (avail === undefined || avail === true);
  }

  protected isSlotInPast(slot: Slot): boolean {
    return this.getSlotDateTime(slot) <= new Date();
  }

  private getSlotDateTime(slot: Slot): Date {
    return new Date(`${slot.date}T${this.timeOf(slot) || '00:00'}`);
  }

  timeOf(slot: Slot): string {
    return (slot as any).startTime || (slot as any).time || '';
  }

  navigateTo(url: string) { this.router.navigateByUrl(url); }

  previousMonth(): void {
    this.currentCalendarDate = new Date(this.currentCalendarDate.getFullYear(), this.currentCalendarDate.getMonth() - 1, 1);
    this.generateCalendar();
  }

  nextMonth(): void {
    this.currentCalendarDate = new Date(this.currentCalendarDate.getFullYear(), this.currentCalendarDate.getMonth() + 1, 1);
    this.generateCalendar();
  }

  get currentMonthLabel(): string {
    return this.currentCalendarDate.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }

  private normalizeSlot(s: Slot): Slot {
    if (!(s as any).startTime && (s as any).time) (s as any).startTime = (s as any).time;
    return s;
  }

  private slotTs(s: Slot): number {
    return new Date(`${s.date}T${this.timeOf(s) || '00:00'}`).getTime();
  }

  private filterFutureSlots(slots: Slot[]): Slot[] {
    return slots.filter(s => !this.isSlotInPast(s));
  }

  protected isDateInPast(dateISO: string): boolean {
    return dateISO < this.todayISO();
  }

  private buildDays(n: number) {
    const base = this.todayISO();
    this.days = Array.from({ length: n }, (_, i) => ({ iso: this.addDaysISO(base, i), label: this.addDaysISO(base, i) }));
  }

  private todayISO(): string { return this.dateToISO(new Date()); }
  private dateToISO(date: Date): string { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; }
  private addDaysISO(iso: string, add: number): string { const d = new Date(iso); d.setDate(d.getDate() + add); return this.dateToISO(d); }
}
