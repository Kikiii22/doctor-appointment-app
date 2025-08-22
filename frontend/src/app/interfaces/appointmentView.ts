import { Appointment } from "./appointment";

export interface AppointmentView extends Appointment {
  showDescriptionForm: boolean;
  newDescription: string;
}
