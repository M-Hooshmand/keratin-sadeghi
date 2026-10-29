import { get, run } from "@/lib/db";
import { fail, intId, ok, optionalStr, str } from "@/lib/http";
import { customerHistory, getCustomer } from "@/lib/queries";

export const dynamic = "force-dynamic";

type Ctx = { params: Promise<{ id: string }> };

export async function GET(_req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const customer = getCustomer(id);
  if (!customer) return fail("مشتری پیدا نشد", 404);
  return ok({ customer, ...customerHistory(id) });
}

export async function PATCH(req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const customer = getCustomer(id);
  if (!customer) return fail("مشتری پیدا نشد", 404);

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return fail("داده ارسالی نامعتبر است");
  }

  const firstName = str(body.firstName) || customer.first_name;
  const lastName = str(body.lastName) || customer.last_name;
  const phone = str(body.phone) || customer.phone;
  const notes =
    body.notes === undefined ? customer.notes : optionalStr(body.notes);

  if (phone !== customer.phone) {
    const dup = get<{ id: number }>("SELECT id FROM customers WHERE phone = ?", phone);
    if (dup && dup.id !== id) return fail("این شماره قبلاً برای مشتری دیگری ثبت شده است");
  }

  run(
    "UPDATE customers SET first_name = ?, last_name = ?, phone = ?, notes = ? WHERE id = ?",
    firstName,
    lastName,
    phone,
    notes,
    id
  );
  return ok(getCustomer(id));
}

export async function DELETE(_req: Request, ctx: Ctx) {
  const id = intId((await ctx.params).id);
  if (!id) return fail("شناسه نامعتبر است", 404);
  const r = run("DELETE FROM customers WHERE id = ?", id);
  if (!r.changes) return fail("مشتری پیدا نشد", 404);
  return ok({ deleted: true });
}
