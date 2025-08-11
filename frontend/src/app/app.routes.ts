import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Register} from './register/register';
import { PatientDashboardComponent} from './patient/dashboard/dashboard';


export const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: 'register',
    component: Register
  },
  {
    path:'patient/dashboard',
    component:PatientDashboardComponent
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' }


];
