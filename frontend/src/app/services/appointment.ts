import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Appointment} from '../interfaces/appointment';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private baseUrl = '/api/appointments';

  constructor(private http: HttpClient) {}

  bookAppointment(slotId: number, patientId: number): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.baseUrl}/book`, { slotId, patientId });
  }

  cancelAppointment(slotId: number, patientId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/cancel`, { slotId, patientId });
  }

  finishAppointment(id: number, doctorId: number, description: string): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.baseUrl}/${id}/finish?doctorId=${doctorId}`,
      { description });
  }

  getPatientAppointments(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`/api/patients/${patientId}/appointments`);
  }
}

