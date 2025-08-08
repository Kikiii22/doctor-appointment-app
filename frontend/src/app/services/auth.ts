import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = '/api';

  constructor(private http: HttpClient) {}

  getRoles() {
    return this.http.get<string[]>(`${this.api}/roles`);
  }

  getHospitals() {
    return this.http.get<any[]>(`${this.api}/hospitals`);
  }

  getDepartments() {
    return this.http.get<any[]>(`${this.api}/departments`);
  }

  register(data: any) {
    return this.http.post(`${this.api}/auth/register`, data);
  }
}
