import { Component } from '@angular/core';

import {Router, RouterOutlet} from '@angular/router';



@Component({
  selector: 'app-root',
  imports: [RouterOutlet ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  standalone:true
})
export class App {
  constructor(private router:Router) { this.router.events

  }



}
