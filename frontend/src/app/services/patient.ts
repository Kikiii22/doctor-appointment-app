import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patient } from '../interfaces/patient';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private baseUrl = '/api/patients';

  constructor(private http: HttpClient) { }

  getPatientByUserId(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.baseUrl}/user/${id}`);
  }
}
