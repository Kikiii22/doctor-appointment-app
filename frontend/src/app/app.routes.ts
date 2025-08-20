import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { PatientDashboardComponent } from './patient/dashboard/dashboard';
import { ListDoctors } from './patient/list-doctors/list-doctors';
import { DoctorDetails } from './patient/doctor-details/doctor-details';
import { PatientAppointments } from './patient/patient-appointments/patient-appointments';
import { LoginSuccess } from './login-success/login-success';
import { DoctorDashboardComponent } from './doctor/dashboard/dashboard';
import {GoogleSuccess} from './patient/google-success/google-success';


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
    path: 'patient/dashboard',
    component: PatientDashboardComponent
  },
  {
    path: 'patient/doctors',
    component: ListDoctors
  },
  {
    path: 'patient/doctors/:id',
    component: DoctorDetails
  },
  {
    path: 'patient/appointments',
    component: PatientAppointments
  },
  {
    path: 'login-success',
    component: LoginSuccess
  },
  {
    path: 'doctor/dashboard',
    component: DoctorDashboardComponent
  },
  {
    path: '',
    redirectTo: 'login', pathMatch: 'full'
  },
  {
    path:'google-success',
    component:GoogleSuccess
  }


];
