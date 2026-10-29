import { all, get, run } from "@/lib/db";
import { fail, ok, optionalStr, str } from "@/lib/http";
import { Customer } from "@/lib/types";

export const dynamic = "force-dynamic";

export async function GET(req: Request) {
  const url = new URL(req.url);
  const search = url.searchParams.get("search")?.trim();
  let rows: Customer[];
  if (search) {
    const q = `%${search}%`;
    rows = all<Customer>(
      `SELECT * FROM customers
       WHERE first_name LIKE ? OR last_name LIKE ? OR phone LIKE ?
       ORDER BY id DESC`,
      q,
      q,
      q
    );
  } else {
    rows = all<Customer>("SELECT * FROM customers ORDER BY id DESC");
  }
  return ok(rows);
}

export async function POST(req: Request) {
  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return fail("داده ارسالی نامعتبر است");
  }

  const firstName = str(body.firstName);
  const lastName = str(body.lastName);
  const phone = str(body.phone);
  const notes = optionalStr(body.notes);

  if (!firstName) return fail("نام را وارد کنید");
  if (!lastName) return fail("نام خانوادگی را وارد کنید");
  if (!phone) return fail("شماره تماس را وارد کنید");

  const existing = get<Customer>("SELECT * FROM customers WHERE phone = ?", phone);
  if (existing) {
    run(
      "UPDATE customers SET first_name = ?, last_name = ?, notes = COALESCE(?, notes) WHERE id = ?",
      firstName,
      lastName,
      notes,
      existing.id
    );
    return ok(get<Customer>("SELECT * FROM customers WHERE id = ?", existing.id));
  }

  const r = run(
    "INSERT INTO customers (first_name, last_name, phone, notes) VALUES (?, ?, ?, ?)",
    firstName,
    lastName,
    phone,
    notes
  );
  return ok(get<Customer>("SELECT * FROM customers WHERE id = ?", r.lastInsertRowid), {
    status: 201,
  });
}
