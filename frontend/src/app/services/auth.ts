import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import { User } from '../interfaces/user';
import {BehaviorSubject, catchError, Observable, tap, throwError} from 'rxjs';
import { AuthResponse } from '../interfaces/AuthResponse';
import { Router } from '@angular/router';
import { Doctor } from '../interfaces/doctor';

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = '/api';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  constructor(private http: HttpClient, private router: Router) {
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
      this.currentUserSubject.next(JSON.parse(storedUser));
    }
  }

  getRoles() {
    return this.http.get<string[]>(`${this.api}/roles`);
  }

  getHospitals() {
    return this.http.get<any[]>(`${this.api}/hospitals`);
  }
  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, { username, password }).pipe(
      tap((res) => {
        localStorage.setItem('jwt', res.token);
        localStorage.setItem('currentUser', JSON.stringify(res.user));
        this.currentUserSubject.next(res.user);
      }),
      catchError((error: HttpErrorResponse) => {
        let message = 'An unknown error occurred';
        if (error.status === 401) {
          message = 'Username or password is incorrect';
        } else if (error.status === 404) {
          message = 'User not found';
        } else if (error.error?.message) {
          // Backend may send a custom message
          message = error.error.message;
        }
        return throwError(() => new Error(message));
      })
    );
  }
  getDepartments() {
    return this.http.get<any[]>(`${this.api}/departments`);
  }
  register(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/auth/register`, data).pipe(
      tap(res => {
        localStorage.setItem('jwt', res.token);
        localStorage.setItem('currentUser', JSON.stringify(res.user));
        this.currentUserSubject.next(res.user);
      })
    );
  }

  setCurrentUser(user: User | null): void {
    if (user) {
      localStorage.setItem('currentUser', JSON.stringify(user));
    } else {
      localStorage.removeItem('currentUser');
    }
    this.currentUserSubject.next(user);
  }
  logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    if (localStorage.getItem('isGoogleUser')) {
      const googleEmail = localStorage.getItem('googleUserEmail');
     const isGoogle=localStorage.getItem('isGoogleUser')
      if (googleEmail && (window as any).google?.accounts?.id) {
        (window as any).google.accounts.id.revoke(googleEmail, () => {
          console.log("Google session revoked");
        });
        console.log("Google session revoked",googleEmail, (window as any).google?.accounts?.id);
      }
      localStorage.removeItem('isGoogleUser');
      localStorage.removeItem('googleUserEmail');
    }
    this.http.post('/api/auth/logout', {}).subscribe(() => {
      this.router.navigate(['/login'], { replaceUrl: true });
    });
  }
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  getToken(): string | null {
    return localStorage.getItem('jwt');
  }
}
