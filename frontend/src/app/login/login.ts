import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {NotificationService} from '../services/notification';
import {Auth} from '../services/auth';
import {NgIf} from '@angular/common';

class AuthService {
}

@Component({
  selector: 'app-login',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    NgIf
  ],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  loading = false;
  showPassword=false;
  loginForm: FormGroup;



  constructor(
    private fb: FormBuilder,
    private authService: Auth,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      const { username, password } = this.loginForm.value;

      this.authService.login(username, password).subscribe({
        next: (user) => {
          this.authService.setCurrentUser(user);
alert("Logged in")
          if (user.role === 'PATIENT') {
            this.router.navigate(['/dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (error) => {
alert("Invalid credentials")
          this.loading = false;
        }
      });
    }
  }
}
