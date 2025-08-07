import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {User} from '../interfaces/user';
import {HttpClient} from '@angular/common/http';
import {Patient} from '../interfaces/patient';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private baseUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  login(username: string, password: string): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, { username, password });
  }

  register(userData: any): Observable<Patient> {
    return this.http.post<Patient>(`${this.baseUrl}/register`, userData);
  }
  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  setCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return !!this.getCurrentUser();
  }

  isPatient(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'PATIENT';
  }

  isDoctor(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'DOCTOR';
  }
}
