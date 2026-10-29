import AppointmentsClient from "@/components/AppointmentsClient";
import { listAppointments, listServices } from "@/lib/queries";

export const dynamic = "force-dynamic";

export default function AppointmentsPage() {
  const appointments = listAppointments({ limit: 1000 });
  const services = listServices();

  return <AppointmentsClient appointments={appointments} services={services} />;
}
