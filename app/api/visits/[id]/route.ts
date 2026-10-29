import { getDb, all, get, run } from "@/lib/db";
import { fail, intId, isoDate, num, ok, optionalStr } from "@/lib/http";
import { getVisit, listUsages } from "@/lib/queries";
import { isSplitEndService, Material } from "@/lib/types";

export const dynamic = "force-dynamic";

type Params = { params: Promise<{ id: string }> };

export async function GET(_request: Request, { params }: Params) {
  const { id } = await params;
  const visitId = intId(id);
  if (!visitId) return fail("شناسه نتیجه معتبر نیست");
  const visit = getVisit(visitId);
  if (!visit) return fail("نتیجه پیدا نشد", 404);
  return ok({ ...visit, usages: listUsages(visitId) });
}

export async function PATCH(request: Request, { params }: Params) {
  const { id } = await params;
  const visitId = intId(id);
  if (!visitId) return fail("شناسه نتیجه معتبر نیست");

  const current = getVisit(visitId);
  if (!current) return fail("نتیجه پیدا نشد", 404);

  let body: Record<string, unknown>;
  try {
    body = (await request.json()) as Record<string, unknown>;
  } catch {
    return fail("داده ارسالی معتبر نیست");
  }

  const performedDate =
    body.performedDate === undefined ? current.performed_date : isoDate(body.performedDate);
  if (!performedDate) return fail("تاریخ انجام کار معتبر نیست");

  const serviceId =
    body.serviceId === undefined ? current.service_id : intId(body.serviceId) ?? current.service_id;
  const service = serviceId
    ? get<{ name: string }>("SELECT name FROM services WHERE id = ?", serviceId)
    : undefined;
  const splitEnd = isSplitEndService(service?.name);
  const finalAmount =
    body.finalAmount === undefined
      ? current.final_amount
      : Math.max(0, Math.round(num(body.finalAmount, 0)));
  const hairLength =
    body.hairLengthCm === undefined
      ? current.hair_length_cm
      : body.hairLengthCm === null || body.hairLengthCm === ""
        ? null
        : num(body.hairLengthCm, 0);
  const hairVolume =
    body.hairVolume === undefined ? current.hair_volume : optionalStr(body.hairVolume);
  const hairCondition =
    body.hairCondition === undefined ? current.hair_condition : optionalStr(body.hairCondition);
  const workDone = body.workDone === undefined ? current.work_done : optionalStr(body.workDone);
  const notes = body.notes === undefined ? current.notes : optionalStr(body.notes);

  if (splitEnd) {
    if (!hairLength) return fail("برای موخوره‌گیری، قد مو الزامی است");
    if (!hairVolume) return fail("برای موخوره‌گیری، حجم مو الزامی است");
  }

  const hasMaterials = body.materials !== undefined;
  const usages: Array<{ materialId: number; amount: number }> = [];
  if (hasMaterials && !splitEnd) {
    if (!Array.isArray(body.materials)) return fail("فهرست مواد معتبر نیست");
    for (const item of body.materials) {
      if (!item || typeof item !== "object") continue;
      const row = item as Record<string, unknown>;
      const materialId = intId(row.materialId ?? row.material_id);
      const amount = num(row.amount, 0);
      if (materialId && amount > 0) usages.push({ materialId, amount });
    }
  }

  const db = getDb();
  db.exec("BEGIN IMMEDIATE");
  try {
    run(
      `UPDATE visits SET service_id = ?, performed_date = ?, final_amount = ?,
         hair_length_cm = ?, hair_volume = ?, hair_condition = ?, work_done = ?, notes = ?,
         updated_at = datetime('now')
       WHERE id = ?`,
      serviceId,
      performedDate,
      finalAmount,
      hairLength,
      hairVolume,
      hairCondition,
      workDone,
      notes,
      visitId
    );

    if (hasMaterials || splitEnd) {
      const ids = Array.from(new Set(usages.map((u) => u.materialId)));
      let materials: Material[] = [];
      if (ids.length) {
        const placeholders = ids.map(() => "?").join(",");
        materials = all<Material>(
          `SELECT * FROM materials WHERE id IN (${placeholders})`,
          ...ids
        );
      }
      const map = new Map(materials.map((m) => [m.id, m]));
      db.prepare("DELETE FROM material_usages WHERE visit_id = ?").run(visitId);
      const insert = db.prepare(
        `INSERT INTO material_usages (visit_id, material_id, amount, unit_price, unit)
         VALUES (?, ?, ?, ?, ?)`
      );
      for (const usage of usages) {
        const material = map.get(usage.materialId);
        if (!material) continue;
        insert.run(visitId, material.id, usage.amount, material.price_per_unit, material.unit);
      }
    }

    db.prepare(
      `UPDATE appointments SET service_id = ?, updated_at = datetime('now') WHERE id = ?`
    ).run(serviceId, current.appointment_id);
    db.exec("COMMIT");
  } catch {
    db.exec("ROLLBACK");
    return fail("ویرایش نتیجه انجام نشد", 500);
  }

  return ok(getVisit(visitId));
}

export async function DELETE(_request: Request, { params }: Params) {
  const { id } = await params;
  const visitId = intId(id);
  if (!visitId) return fail("شناسه نتیجه معتبر نیست");

  const visit = get<{ appointment_id: number }>(
    "SELECT appointment_id FROM visits WHERE id = ?",
    visitId
  );
  if (!visit) return fail("نتیجه پیدا نشد", 404);

  const db = getDb();
  db.exec("BEGIN IMMEDIATE");
  try {
    db.prepare("DELETE FROM visits WHERE id = ?").run(visitId);
    db.prepare(
      `UPDATE appointments SET status = 'waiting', updated_at = datetime('now') WHERE id = ?`
    ).run(visit.appointment_id);
    db.exec("COMMIT");
  } catch {
    db.exec("ROLLBACK");
    return fail("حذف نتیجه انجام نشد", 500);
  }

  return ok({ id: visitId });
}
