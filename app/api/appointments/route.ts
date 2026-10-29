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
import { getAppointment, listAppointments } from "@/lib/queries";
import { AppointmentStatus, Customer } from "@/lib/types";

export const dynamic = "force-dynamic";

const STATUSES: AppointmentStatus[] = ["waiting", "done", "canceled"];

export async function GET(req: Request) {
  const url = new URL(req.url);
  const rows = listAppointments({
    status: url.searchParams.get("status") || undefined,
    date: url.searchParams.get("date") || undefined,
    from: url.searchParams.get("from") || undefined,
    to: url.searchParams.get("to") || undefined,
    customerId: Number(url.searchParams.get("customerId")) || undefined,
    search: url.searchParams.get("search") || undefined,
    limit: Number(url.searchParams.get("limit")) || undefined,
  });
  return ok(rows);
}

function findOrCreateCustomer(body: Record<string, unknown>): Customer | string {
  const customerId = intId(body.customerId);
  if (customerId) {
    const c = get<Customer>("SELECT * FROM customers WHERE id = ?", customerId);
    if (!c) return "مشتری پیدا نشد";
    return c;
  }

  const firstName = str(body.firstName);
  const lastName = str(body.lastName);
  const phone = str(body.phone);
  if (!firstName) return "نام را وارد کنید";
  if (!lastName) return "نام خانوادگی را وارد کنید";
  if (!phone) return "شماره تماس را وارد کنید";

  const existing = get<Customer>("SELECT * FROM customers WHERE phone = ?", phone);
  if (existing) {
    run(
      "UPDATE customers SET first_name = ?, last_name = ? WHERE id = ?",
      firstName,
      lastName,
      existing.id
    );
    return { ...existing, first_name: firstName, last_name: lastName };
  }
  const r = run(
    "INSERT INTO customers (first_name, last_name, phone) VALUES (?, ?, ?)",
    firstName,
    lastName,
    phone
  );
  return {
    id: r.lastInsertRowid,
    first_name: firstName,
    last_name: lastName,
    phone,
    notes: null,
    created_at: new Date().toISOString(),
  };
}

export async function POST(req: Request) {
  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return fail("داده ارسالی نامعتبر است");
  }

  const customer = findOrCreateCustomer(body);
  if (typeof customer === "string") return fail(customer);

  const date = isoDate(body.date);
  const time = timeValue(body.time);
  if (!date) return fail("تاریخ مراجعه را انتخاب کنید");
  if (!time) return fail("ساعت مراجعه را انتخاب کنید");

  const serviceId = intId(body.serviceId);
  const deposit = Math.max(0, num(body.deposit));
  const notes = optionalStr(body.notes);
  const status = STATUSES.includes(body.status as AppointmentStatus)
    ? (body.status as AppointmentStatus)
    : "waiting";

  const r = run(
    `INSERT INTO appointments
      (customer_id, service_id, appointment_date, appointment_time, deposit, status, notes)
     VALUES (?, ?, ?, ?, ?, ?, ?)`,
    customer.id,
    serviceId,
    date,
    time,
    Math.round(deposit),
    status,
    notes
  );

  return ok(getAppointment(r.lastInsertRowid), { status: 201 });
}
