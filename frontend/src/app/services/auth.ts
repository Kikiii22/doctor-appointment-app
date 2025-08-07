import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {User} from '../interfaces/user';
import {HttpClient} from '@angular/common/http';
import {Patient} from '../interfaces/patient';

@Injectable({
  providedIn: 'root'
})
export class Auth {

  constructor(private http: HttpClient) {}

  getDepartments(hospitalId: number): Observable<any[]> {
    return this.http.get<any[]>('/api/departments');
  }



}
