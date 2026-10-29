import { fail, intId, num, ok, optionalStr, str } from "@/lib/http";
import { all, get, run } from "@/lib/db";
import { Material } from "@/lib/types";

export const dynamic = "force-dynamic";

type Params = { params: Promise<{ id: string }> };

export async function PATCH(request: Request, { params }: Params) {
  const { id } = await params;
  const materialId = intId(id);
  if (!materialId) return fail("شناسه ماده معتبر نیست");

  const current = get<Material>("SELECT * FROM materials WHERE id = ?", materialId);
  if (!current) return fail("ماده پیدا نشد", 404);

  let body: Record<string, unknown>;
  try {
    body = (await request.json()) as Record<string, unknown>;
  } catch {
    return fail("داده ارسالی معتبر نیست");
  }

  const name = body.name === undefined ? current.name : str(body.name) || current.name;
  const price =
    body.pricePerUnit === undefined && body.price_per_unit === undefined
      ? current.price_per_unit
      : Math.max(0, Math.round(num(body.pricePerUnit ?? body.price_per_unit, 0)));
  const unit = body.unit === undefined ? current.unit : str(body.unit) || current.unit;
  const notes = body.notes === undefined ? current.notes : optionalStr(body.notes);
  const active =
    body.active === undefined ? current.active : body.active === false || body.active === 0 ? 0 : 1;

  try {
    run(
      "UPDATE materials SET name = ?, price_per_unit = ?, unit = ?, notes = ?, active = ? WHERE id = ?",
      name,
      price,
      unit,
      notes,
      active,
      materialId
    );
  } catch {
    return fail("ماده‌ای با این نام وجود دارد");
  }

  return ok(get<Material>("SELECT * FROM materials WHERE id = ?", materialId));
}

export async function DELETE(_request: Request, { params }: Params) {
  const { id } = await params;
  const materialId = intId(id);
  if (!materialId) return fail("شناسه ماده معتبر نیست");

  const used = get<{ c: number }>(
    "SELECT COUNT(*) AS c FROM material_usages WHERE material_id = ?",
    materialId
  );
  if (used && used.c > 0) {
    run("UPDATE materials SET active = 0 WHERE id = ?", materialId);
    return ok({ id: materialId, archived: true });
  }

  run("DELETE FROM materials WHERE id = ?", materialId);
  return ok({ id: materialId, archived: false });
}
