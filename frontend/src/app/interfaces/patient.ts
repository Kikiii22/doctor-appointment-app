import {User} from './user';

export interface Patient {
  id: number;
  fullName: string;
  phone: string;
  user: User;
}
