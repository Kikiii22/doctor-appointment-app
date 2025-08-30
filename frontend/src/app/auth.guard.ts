import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) { }

  canActivate(): boolean {
    const token = localStorage.getItem('jwt');
    if (token) {
      return true;
    }
    this.router.navigate(['/login'], { state: { message: 'You must be logged in to access this page.' } });
    return false;
  }
}
