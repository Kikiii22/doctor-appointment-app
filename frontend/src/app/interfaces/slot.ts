import {Doctor} from './doctor';

export interface Slot { id:number;
  date:string; doctor:Doctor;startTime:string;booked:boolean }
