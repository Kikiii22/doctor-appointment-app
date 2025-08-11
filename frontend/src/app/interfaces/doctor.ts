import {User} from './user';
import {Department} from './Department';

export interface Doctor {
  id: number;
  fullName: string;
  phone: string;
  user: User;
  department:Department
}
