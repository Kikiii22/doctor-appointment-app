import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-google-success',
  imports: [],
  templateUrl: './google-success.html',
  styleUrl: './google-success.css'
})
export class GoogleSuccess implements OnInit{

      constructor(private route: ActivatedRoute, private router: Router)
      {}

      ngOnInit():
      void {
        this.route.queryParams.subscribe(params => {
          const token = params['token'];
          const userJson = params['user'];
          if (token && userJson) {
            localStorage.setItem('jwt', token);
            try {
              const user = JSON.parse(decodeURIComponent(userJson));
              localStorage.setItem('currentUser', JSON.stringify(user));
                localStorage.setItem('isGoogleUser', 'true');
                localStorage.setItem('googleUserEmail', user.email);
                console.log("Google", user);
                console.log("Google email", user.email);

            } catch (e) {
              console.log("Error parsing user");
            }
            this.router.navigate(['/patient/dashboard']);
          } else {
            this.router.navigate(['/login']);
          }
        });
      }
    }


