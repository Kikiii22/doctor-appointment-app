import {Slot} from './slot';

export interface Appointment
{ id:number; slot:Slot; patient:number;  description?:string; status:'BOOKED'|'AVAILABLE'|'CANCELLED' }
