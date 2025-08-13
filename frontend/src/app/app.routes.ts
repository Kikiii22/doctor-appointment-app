import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Register} from './register/register';
import { PatientDashboardComponent} from './patient/dashboard/dashboard';
import {ListDoctors} from './patient/list-doctors/list-doctors';
import {DoctorDetails} from './patient/doctor-details/doctor-details';


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
  {
    path:'patient/doctors',
    component:ListDoctors
  },
  {
    path:'patient/doctors/:id',
    component:DoctorDetails
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' }


];
