import { User } from "./user";

export interface Hospital {
  id: number;
  name: string;
  phone: string;
  user: User;
}
