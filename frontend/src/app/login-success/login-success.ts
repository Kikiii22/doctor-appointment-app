import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

@Component({
  selector: 'app-login-success',
  imports: [],
  templateUrl: './login-success.html',
  styleUrl: './login-success.css'
})
export class LoginSuccess implements OnInit{
  constructor(private route: ActivatedRoute, private router: Router){}
    ngOnInit(): void {
      this.route.queryParams.subscribe(params => {
        const token = params['token'];
        const userJson = params['user'];
        if (token&&userJson) {
          localStorage.setItem('jwt', token);
          try{
            const user = JSON.parse(decodeURIComponent(userJson));
            localStorage.setItem('currentUser', JSON.stringify(user));
          }
          catch(e){
            console.log("Error parsing user");
          }
          this.router.navigate(['/patient/dashboard']);
        } else {
          this.router.navigate(['/login']);
        }
      });
    }




}
