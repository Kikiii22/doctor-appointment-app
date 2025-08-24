import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Appointment } from '../interfaces/appointment';
import { Slot } from '../interfaces/slot';
import { Hospital } from '../interfaces/hospital';
import { Doctor } from '../interfaces/doctor';

@Injectable({
  providedIn: 'root'
})
export class HospitalService {
  private baseUrl = '/api/hospitals';

  constructor(private http: HttpClient) { }

  getAllHospitals(): Observable<Hospital[]> {
    return this.http.get<Hospital[]>(this.baseUrl);
  }

  getHospitalById(id: number): Observable<Hospital> {
    return this.http.get<Hospital>(`${this.baseUrl}/${id}`);
  }

  getHospitalDoctors(id: number): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.baseUrl}/${id}/doctors`);
  }

}
