import { getDb, all, get } from "@/lib/db";
import { fail, intId, isoDate, num, ok, optionalStr, str } from "@/lib/http";
import { getVisit, getVisitByAppointment, listUsages } from "@/lib/queries";
import { isSplitEndService, Material } from "@/lib/types";

export const dynamic = "force-dynamic";

interface UsageInput {
  materialId: number;
  amount: number;
}

function parseUsages(value: unknown): UsageInput[] | null {
  if (!Array.isArray(value)) return [];
  const result: UsageInput[] = [];
  for (const item of value) {
    if (!item || typeof item !== "object") continue;
    const row = item as Record<string, unknown>;
    const materialId = intId(row.materialId ?? row.material_id);
    const amount = num(row.amount, 0);
    if (!materialId || amount <= 0) continue;
    result.push({ materialId, amount });
  }
  return result;
}

export async function GET(request: Request) {
  const url = new URL(request.url);
  const appointmentId = intId(url.searchParams.get("appointmentId"));
  const visitId = intId(url.searchParams.get("visitId"));

  if (appointmentId) {
    const visit = getVisitByAppointment(appointmentId);
    return ok(visit ?? null);
  }
  if (visitId) {
    const visit = getVisit(visitId);
    if (!visit) return fail("نتیجه پیدا نشد", 404);
    return ok({ ...visit, usages: listUsages(visitId) });
  }
  return fail("شناسه نوبت یا نتیجه الزامی است");
}

export async function POST(request: Request) {
  let body: Record<string, unknown>;
  try {
    body = (await request.json()) as Record<string, unknown>;
  } catch {
    return fail("داده ارسالی معتبر نیست");
  }

  const appointmentId = intId(body.appointmentId);
  if (!appointmentId) return fail("نوبت انتخاب نشده است");

  const appointment = get<{
    id: number;
    customer_id: number;
    service_id: number | null;
  }>("SELECT id, customer_id, service_id FROM appointments WHERE id = ?", appointmentId);
  if (!appointment) return fail("نوبت پیدا نشد", 404);

  const performedDate = isoDate(body.performedDate);
  if (!performedDate) return fail("تاریخ انجام کار معتبر نیست");

  const serviceId = intId(body.serviceId) ?? appointment.service_id;
  const service = serviceId
    ? get<{ name: string }>("SELECT name FROM services WHERE id = ?", serviceId)
    : undefined;
  const splitEnd = isSplitEndService(service?.name);
  const finalAmount = Math.max(0, Math.round(num(body.finalAmount, 0)));
  const hairLengthValue =
    body.hairLengthCm === null || body.hairLengthCm === ""
      ? null
      : num(body.hairLengthCm, 0);
  const hairLength = hairLengthValue && hairLengthValue > 0 ? hairLengthValue : null;
  const hairVolume = optionalStr(body.hairVolume);
  const hairCondition = optionalStr(body.hairCondition);
  const workDone = optionalStr(body.workDone);
  const notes = optionalStr(body.notes);
  let usages = parseUsages(body.materials);
  if (usages === null) return fail("فهرست مواد معتبر نیست");

  if (splitEnd) {
    if (!hairLength) return fail("برای موخوره‌گیری، قد مو الزامی است");
    if (!hairVolume) return fail("برای موخوره‌گیری، حجم مو الزامی است");
    usages = [];
  }

  if (usages.length > 0) {
    const ids = Array.from(new Set(usages.map((u) => u.materialId)));
    const placeholders = ids.map(() => "?").join(",");
    const found = all<Material>(
      `SELECT * FROM materials WHERE id IN (${placeholders})`,
      ...ids
    );
    if (found.length !== ids.length) return fail("یکی از مواد انتخاب‌شده پیدا نشد");
  }

  const materialRows = new Map<number, Material>();
  if (usages.length > 0) {
    const ids = Array.from(new Set(usages.map((u) => u.materialId)));
    const placeholders = ids.map(() => "?").join(",");
    const found = all<Material>(
      `SELECT * FROM materials WHERE id IN (${placeholders})`,
      ...ids
    );
    found.forEach((m) => materialRows.set(m.id, m));
  }

  const db = getDb();
  db.exec("BEGIN IMMEDIATE");
  try {
    const existing = get<{ id: number }>(
      "SELECT id FROM visits WHERE appointment_id = ?",
      appointmentId
    );

    let visitId: number;
    if (existing) {
      db.prepare(
        `UPDATE visits SET service_id = ?, performed_date = ?, final_amount = ?,
           hair_length_cm = ?, hair_volume = ?, hair_condition = ?, work_done = ?, notes = ?,
           updated_at = datetime('now')
         WHERE id = ?`
      ).run(
        serviceId,
        performedDate,
        finalAmount,
        hairLength,
        hairVolume,
        hairCondition,
        workDone,
        notes,
        existing.id
      );
      db.prepare("DELETE FROM material_usages WHERE visit_id = ?").run(existing.id);
      visitId = existing.id;
    } else {
      const result = db
        .prepare(
          `INSERT INTO visits (appointment_id, customer_id, service_id, performed_date,
             final_amount, hair_length_cm, hair_volume, hair_condition, work_done, notes)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
        )
        .run(
          appointmentId,
          appointment.customer_id,
          serviceId,
          performedDate,
          finalAmount,
          hairLength,
          hairVolume,
          hairCondition,
          workDone,
          notes
        );
      visitId = Number(result.lastInsertRowid);
    }

    const insertUsage = db.prepare(
      `INSERT INTO material_usages (visit_id, material_id, amount, unit_price, unit)
       VALUES (?, ?, ?, ?, ?)`
    );
    for (const usage of usages) {
      const material = materialRows.get(usage.materialId);
      if (!material) continue;
      insertUsage.run(
        visitId,
        usage.materialId,
        usage.amount,
        material.price_per_unit,
        material.unit
      );
    }

    db.prepare(
      `UPDATE appointments SET status = 'done', service_id = ?, updated_at = datetime('now')
       WHERE id = ?`
    ).run(serviceId, appointmentId);
    db.exec("COMMIT");
  } catch {
    db.exec("ROLLBACK");
    return fail("ثبت نتیجه نهایی انجام نشد", 500);
  }

  const visit = getVisitByAppointment(appointmentId);
  return ok(visit, { status: 201 });
}

export async function PUT(request: Request) {
  return POST(request);
}
