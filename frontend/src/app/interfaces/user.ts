export interface User {
  id: number;
  username: string;
  email: string;
  role: 'PATIENT' | 'DOCTOR' | 'ADMIN';
  password: string;
}
