import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-login-success',
  imports: [],
  templateUrl: './login-success.html',
  styleUrl: './login-success.css'
})
export class LoginSuccess implements OnInit {
  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      const userJson = params['user'];

      if (token && userJson) {
        localStorage.setItem('jwt', token);
        try {
          const user = JSON.parse(decodeURIComponent(userJson));
          localStorage.setItem('currentUser', JSON.stringify(user));

          switch (user.role) {
            case 'PATIENT':
              this.router.navigate(['/patient/dashboard']);
              break;
            case 'DOCTOR':
              this.router.navigate(['/doctor/dashboard']);
              break;
            case 'ADMIN':
              this.router.navigate(['/hospital/dashboard']);
              break;
            default:
              this.router.navigate(['/login']);
              break;
          }
        } catch (e) {
          console.log('Error parsing user', e);
          this.router.navigate(['/login']);
        }
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}
