import { notFound } from "next/navigation";
import VisitForm from "@/components/VisitForm";
import { PageHeader } from "@/components/ui";
import { formatJalali, timeLabel } from "@/lib/jalali";
import {
  getAppointment,
  getVisitByAppointment,
  listMaterials,
  listServices,
  listUsages,
} from "@/lib/queries";

export const dynamic = "force-dynamic";

type Params = { params: Promise<{ appointmentId: string }> };

export default async function VisitPage({ params }: Params) {
  const { appointmentId } = await params;
  const id = Number(appointmentId);
  if (!Number.isInteger(id)) notFound();

  const appointment = getAppointment(id);
  if (!appointment) notFound();

  const visit = getVisitByAppointment(id) ?? null;
  const usages = visit ? listUsages(visit.id) : [];
  const services = listServices();
  const materials = listMaterials(true);

  return (
    <div>
      <PageHeader
        title={visit ? "ویرایش نتیجه نهایی" : "ثبت نتیجه نهایی"}
        subtitle={`${appointment.first_name} ${appointment.last_name} — ${formatJalali(
          appointment.appointment_date,
          true
        )} ساعت ${timeLabel(appointment.appointment_time)}`}
      />
      <VisitForm
        appointment={appointment}
        services={services}
        materials={materials}
        initialVisit={visit}
        initialUsages={usages}
      />
    </div>
  );
}
