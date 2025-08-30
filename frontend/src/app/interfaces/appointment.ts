import { Patient } from './patient';
import { Slot } from './slot';

export interface Appointment {
  id: number;
  slot: Slot;
  patient: Patient;
  description?: string;
  status: 'BOOKED' | 'FINISHED'
}

