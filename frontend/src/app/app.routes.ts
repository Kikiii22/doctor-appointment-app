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
import { DoctorAppointments } from './doctor/doctor-appointments/doctor-appointments';
import {AuthGuard} from './auth.guard';



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
    component: PatientDashboardComponent,
    canActivate:[AuthGuard]
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
    component: PatientAppointments,
    canActivate:[AuthGuard]

  },
  {
    path: 'login-success',
    component: LoginSuccess
  },
  {
    path: 'doctor/dashboard',
    component: DoctorDashboardComponent,
    canActivate:[AuthGuard]

  },
  {
    path: 'doctor/appointments',
    component: DoctorAppointments
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
