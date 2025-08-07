import { Routes } from '@angular/router';
import {Login} from './login/login';
import {Register} from './register/register';
import {Test} from './test/test';


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
    path:'test',
    component:Test
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' }


];
