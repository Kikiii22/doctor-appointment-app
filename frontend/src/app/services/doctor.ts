import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import {Appointment} from '../interfaces/appointment';
import {Slot} from '../interfaces/slot';
import {Doctor} from '../interfaces/doctor';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private baseUrl = '/api/doctors';

  constructor(private http: HttpClient) {}

  getAllDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(this.baseUrl);
  }

  getDoctorById(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.baseUrl}/${id}`);
  }

  getDoctorSlots(id: number, limit?: number, date?: string): Observable<Slot[]> {
    let params = '';
    if (limit) params += `limit=${limit}`;
    if (date) params += (params ? '&' : '') + `date=${date}`;
    return this.http.get<Slot[]>(`${this.baseUrl}/${id}/slots${params ? '?' + params : ''}`);
  }


  getDoctorAppointments(id: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.baseUrl}/${id}/appointments`);
  }
}
