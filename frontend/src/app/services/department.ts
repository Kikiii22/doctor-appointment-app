import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor } from '../interfaces/doctor';

@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  private baseUrl = '/api/departments';

  constructor(private http: HttpClient) { }

  searchDoctorsBySymptoms(symptoms: string): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.baseUrl}/search`, {
      params: { symptoms }
    });
  }
}
