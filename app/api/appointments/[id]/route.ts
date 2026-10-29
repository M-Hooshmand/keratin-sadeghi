import { get, run } from "@/lib/db";
import {
  fail,
  intId,
  isoDate,
  num,
  ok,
  optionalStr,
  str,
  timeValue,
} from "@/lib/http";
import { getAppointment } from "@/lib/queries";
import { AppointmentStatus, Customer } from "@/lib/types";

export const dynamic = "force-dynamic";

type Ctx = { params: Promise<{ id: string }> };
const STATUSES: AppointmentStatus[] = ["waiting", "done", "canceled"];

export async function GET(_req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const row = getAppointment(id);
  if (!row) return fail("رزرو پیدا نشد", 404);
  return ok(row);
}

export async function PATCH(req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const current = get<{
    id: number;
    customer_id: number;
    appointment_date: string;
    appointment_time: string;
    deposit: number;
    status: AppointmentStatus;
    notes: string | null;
    service_id: number | null;
  }>("SELECT * FROM appointments WHERE id = ?", id);
  if (!current) return fail("رزرو پیدا نشد", 404);

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return fail("داده ارسالی نامعتبر است");
  }

  const customer = get<Customer>(
    "SELECT * FROM customers WHERE id = ?",
    current.customer_id
  );
  if (!customer) return fail("مشتری پیدا نشد", 404);

  let customerId = current.customer_id;
  const firstName = str(body.firstName) || customer.first_name;
  const lastName = str(body.lastName) || customer.last_name;
  const phone = str(body.phone) || customer.phone;

  if (phone !== customer.phone) {
    const dup = get<Customer>(
      "SELECT * FROM customers WHERE phone = ?",
      phone
    );
    if (dup && dup.id !== customer.id) {
      customerId = dup.id;
      run(
        "UPDATE customers SET first_name = ?, last_name = ? WHERE id = ?",
        firstName,
        lastName,
        dup.id
      );
    } else {
      run("UPDATE customers SET phone = ? WHERE id = ?", phone, customer.id);
    }
  }
  run(
    "UPDATE customers SET first_name = ?, last_name = ? WHERE id = ?",
    firstName,
    lastName,
    customerId
  );

  const date = body.date !== undefined ? isoDate(body.date) : current.appointment_date;
  const time = body.time !== undefined ? timeValue(body.time) : current.appointment_time;
  if (!date) return fail("تاریخ نامعتبر است");
  if (!time) return fail("ساعت نامعتبر است");

  const serviceId =
    body.serviceId === undefined ? current.service_id : intId(body.serviceId);
  const deposit =
    body.deposit === undefined
      ? current.deposit
      : Math.max(0, Math.round(num(body.deposit)));
  const notes =
    body.notes === undefined ? current.notes : optionalStr(body.notes);
  const status = STATUSES.includes(body.status as AppointmentStatus)
    ? (body.status as AppointmentStatus)
    : current.status;

  run(
    `UPDATE appointments SET
      customer_id = ?, service_id = ?, appointment_date = ?, appointment_time = ?,
      deposit = ?, status = ?, notes = ?, updated_at = datetime('now')
     WHERE id = ?`,
    customerId,
    serviceId,
    date,
    time,
    deposit,
    status,
    notes,
    id
  );

  return ok(getAppointment(id));
}

export async function DELETE(_req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const r = run("DELETE FROM appointments WHERE id = ?", id);
  if (!r.changes) return fail("رزرو پیدا نشد", 404);
  return ok({ deleted: true });
}
