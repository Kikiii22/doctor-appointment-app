import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {User} from '../interfaces/user';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {AuthResponse} from '../interfaces/AuthResponse';
import {Router} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class Auth {
  private api = '/api';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  constructor(private http: HttpClient,private router:Router) {
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
    return this.http.post<User>(`${this.api}/auth/login`, { username, password }).pipe(
      tap((res:any)=>{
        localStorage.setItem('jwt', res.token);
        this.currentUser$ = res.user;
        localStorage.setItem('currentUser', JSON.stringify(res.user));
      })
    )
      ;
  }
  getDepartments() {
    return this.http.get<any[]>(`${this.api}/departments`);
  }

  register(data: any):Observable<AuthResponse> {
    return this.http.post(`${this.api}/auth/register`, data).pipe(
      tap((res:any)=>{
        localStorage.setItem('jwt', res.token);
        this.currentUser$ = res.user;
        localStorage.setItem('currentUser', JSON.stringify(res.user));
      })
    )
      ;
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
    this.currentUserSubject.next(null);                      // ✅ notify subscribers
    this.router.navigate(['/login'], { replaceUrl: true });
  }
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
  getToken():string|null {
    return localStorage.getItem('jwt');
  }
}
