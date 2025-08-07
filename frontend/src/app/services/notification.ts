import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
export interface Notification {
  id: string;
  message: string;
  type: 'info' | 'warning' | 'success' | 'error';
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notifications.asObservable();

  addNotification(message: string, type: 'info' | 'warning' | 'success' | 'error' = 'info'): void {
    const notification: Notification = {
      id: Date.now().toString(),
      message,
      type,
      timestamp: new Date()
    };

    const current = this.notifications.value;
    this.notifications.next([...current, notification]);

    // Auto-remove after 5 seconds
    setTimeout(() => this.removeNotification(notification.id), 5000);
  }

  removeNotification(id: string): void {
    const current = this.notifications.value;
    this.notifications.next(current.filter(n => n.id !== id));
  }

  checkUpcomingAppointments(appointments: any[]): void {
    const now = new Date();
    const oneHourFromNow = new Date(now.getTime() + 60 * 60 * 1000);

    appointments.forEach(appointment => {
      const appointmentDate = new Date(`${appointment.date}T${appointment.time}`);
      if (appointmentDate > now && appointmentDate <= oneHourFromNow) {
        this.addNotification(
          `Appointment with Dr. ${appointment.doctor.fullName} in 1 hour!`,
          'warning'
        );
      }
    });
  }
}
