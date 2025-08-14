import {User} from './user';
import {Department} from './Department';
import {Hospital} from './hospital';

export interface Doctor {
  id: number;
  fullName: string;
  phone: string;
  user: User;
  department:Department;
  hospital:Hospital
}
