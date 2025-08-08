import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {User} from '../interfaces/user';
import {BehaviorSubject, Observable, tap} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = '/api';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  constructor(private http: HttpClient) {}

  getRoles() {
    return this.http.get<string[]>(`${this.api}/roles`);
  }

  getHospitals() {
    return this.http.get<any[]>(`${this.api}/hospitals`);
  }
  login(username: string, password: string): Observable<User> {
    return this.http.post<User>(`${this.api}/auth/login`, { username, password }).pipe(
      tap((res:any)=>{
        localStorage.setItem('jwt', res);
      })
    )
      ;
  }
  getDepartments() {
    return this.http.get<any[]>(`${this.api}/departments`);
  }

  register(data: any) {
    return this.http.post(`${this.api}/auth/register`, data);
  }

  setCurrentUser(user: User): void {
    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }
  logout() {
    localStorage.removeItem('jwt');
  }
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  getToken() {
    return localStorage.getItem('jwt');
  }
}
