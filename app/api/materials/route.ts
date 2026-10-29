import { fail, num, ok, optionalStr, str } from "@/lib/http";
import { listMaterials } from "@/lib/queries";
import { run } from "@/lib/db";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const includeInactive = url.searchParams.get("all") === "1";
  return ok(listMaterials(includeInactive));
}

export async function POST(request: Request) {
  let body: Record<string, unknown>;
  try {
    body = (await request.json()) as Record<string, unknown>;
  } catch {
    return fail("داده ارسالی معتبر نیست");
  }

  const name = str(body.name);
  if (!name) return fail("نام ماده الزامی است");

  const price = Math.max(0, Math.round(num(body.pricePerUnit ?? body.price_per_unit, 0)));
  const unit = str(body.unit) || "ml";
  const notes = optionalStr(body.notes);

  try {
    const result = run(
      "INSERT INTO materials (name, unit, price_per_unit, notes) VALUES (?, ?, ?, ?)",
      name,
      unit,
      price,
      notes
    );
    return ok({ id: result.lastInsertRowid }, { status: 201 });
  } catch {
    return fail("ماده‌ای با این نام وجود دارد");
  }
}
