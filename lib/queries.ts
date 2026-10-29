import { all, get } from "./db";
import {
  AppointmentRow,
  Customer,
  Material,
  MaterialUsage,
  Service,
  VisitRow,
  VISIT_SELECT,
} from "./types";
import { jalaliToISO, jalaliMonthLength } from "./jalali";

export const APPOINTMENT_SELECT = `
  SELECT a.*, c.first_name, c.last_name, c.phone, s.name AS service_name,
    v.id AS visit_id, v.final_amount,
    COALESCE((SELECT SUM(mu.amount * mu.unit_price) FROM material_usages mu WHERE mu.visit_id = v.id), 0) AS material_cost
  FROM appointments a
  JOIN customers c ON c.id = a.customer_id
  LEFT JOIN services s ON s.id = a.service_id
  LEFT JOIN visits v ON v.appointment_id = a.id
`;

export function listServices(): Service[] {
  return all<Service>(
    "SELECT * FROM services WHERE active = 1 ORDER BY sort, id"
  );
}

export function listCustomers(search?: string): Customer[] {
  if (search && search.trim()) {
    const q = `%${search.trim()}%`;
    return all<Customer>(
      `SELECT * FROM customers
       WHERE first_name LIKE ? OR last_name LIKE ? OR phone LIKE ?
       ORDER BY id DESC`,
      q,
      q,
      q
    );
  }
  return all<Customer>("SELECT * FROM customers ORDER BY id DESC");
}

export function getCustomer(id: number): Customer | undefined {
  return get<Customer>("SELECT * FROM customers WHERE id = ?", id);
}

export function getAppointment(id: number): AppointmentRow | undefined {
  return get<AppointmentRow>(`${APPOINTMENT_SELECT} WHERE a.id = ?`, id);
}

export function listAppointments(opts: {
  status?: string;
  date?: string;
  from?: string;
  to?: string;
  customerId?: number;
  search?: string;
  limit?: number;
} = {}): AppointmentRow[] {
  const where: string[] = [];
  const params: (string | number)[] = [];

  if (opts.status && opts.status !== "all") {
    where.push("a.status = ?");
    params.push(opts.status);
  }
  if (opts.date) {
    where.push("a.appointment_date = ?");
    params.push(opts.date);
  }
  if (opts.from) {
    where.push("a.appointment_date >= ?");
    params.push(opts.from);
  }
  if (opts.to) {
    where.push("a.appointment_date <= ?");
    params.push(opts.to);
  }
  if (opts.customerId) {
    where.push("a.customer_id = ?");
    params.push(opts.customerId);
  }
  if (opts.search && opts.search.trim()) {
    const q = `%${opts.search.trim()}%`;
    where.push("(c.first_name LIKE ? OR c.last_name LIKE ? OR c.phone LIKE ?)");
    params.push(q, q, q);
  }

  let sql = APPOINTMENT_SELECT;
  if (where.length) sql += ` WHERE ${where.join(" AND ")}`;
  sql += " ORDER BY a.appointment_date ASC, a.appointment_time ASC";
  if (opts.limit) {
    sql += " LIMIT ?";
    params.push(opts.limit);
  }
  return all<AppointmentRow>(sql, ...params);
}

export function listMaterials(includeInactive = false): Material[] {
  return all<Material>(
    `SELECT * FROM materials ${includeInactive ? "" : "WHERE active = 1"} ORDER BY id`
  );
}

export function getVisitByAppointment(appointmentId: number): VisitRow | undefined {
  return get<VisitRow>(`${VISIT_SELECT} WHERE v.appointment_id = ?`, appointmentId);
}

export function getVisit(id: number): VisitRow | undefined {
  return get<VisitRow>(`${VISIT_SELECT} WHERE v.id = ?`, id);
}

export function listUsages(visitId: number): MaterialUsage[] {
  return all<MaterialUsage>(
    `SELECT mu.*, m.name FROM material_usages mu
     JOIN materials m ON m.id = mu.material_id
     WHERE mu.visit_id = ? ORDER BY mu.id`,
    visitId
  );
}

export function customerHistory(customerId: number): {
  appointments: AppointmentRow[];
  visits: VisitRow[];
} {
  return {
    appointments: listAppointments({ customerId }),
    visits: all<VisitRow>(
      `${VISIT_SELECT} WHERE v.customer_id = ? ORDER BY v.performed_date DESC`,
      customerId
    ),
  };
}

export interface MonthSummary {
  jy: number;
  jm: number;
  count: number;
  income: number;
  deposit: number;
  materialCost: number;
  profit: number;
  visits: VisitRow[];
}

export function monthSummary(jy: number, jm: number): MonthSummary {
  const start = jalaliToISO(jy, jm, 1);
  const end = jalaliToISO(jy, jm, jalaliMonthLength(jy, jm));
  const visits = all<VisitRow>(
    `${VISIT_SELECT} WHERE v.performed_date BETWEEN ? AND ? ORDER BY v.performed_date DESC`,
    start,
    end
  );
  const deposits = get<{ total: number }>(
    `SELECT COALESCE(SUM(deposit), 0) AS total FROM appointments
     WHERE appointment_date BETWEEN ? AND ? AND status != 'canceled'`,
    start,
    end
  );
  const income = visits.reduce((s, v) => s + v.final_amount, 0);
  const materialCost = visits.reduce((s, v) => s + v.material_cost, 0);
  return {
    jy,
    jm,
    count: visits.length,
    income,
    deposit: deposits?.total ?? 0,
    materialCost,
    profit: income - materialCost,
    visits,
  };
}

export function yearSummary(jy: number): MonthSummary[] {
  return Array.from({ length: 12 }, (_, i) => monthSummary(jy, i + 1));
}

export function serviceBreakdown(visits: VisitRow[]) {
  const map = new Map<
    string,
    { name: string; count: number; income: number; materialCost: number }
  >();
  for (const v of visits) {
    const name = v.service_name || "بدون خدمت";
    const cur = map.get(name) || {
      name,
      count: 0,
      income: 0,
      materialCost: 0,
    };
    cur.count += 1;
    cur.income += v.final_amount;
    cur.materialCost += v.material_cost;
    map.set(name, cur);
  }
  return Array.from(map.values()).sort((a, b) => b.income - a.income);
}

export function materialCostBreakdown(start: string, end: string) {
  return all<{
    name: string;
    total_ml: number;
    cost: number;
    uses: number;
  }>(
    `SELECT m.name AS name,
       COALESCE(SUM(mu.amount), 0) AS total_ml,
       COALESCE(SUM(mu.amount * mu.unit_price), 0) AS cost,
       COUNT(DISTINCT mu.visit_id) AS uses
     FROM material_usages mu
     JOIN materials m ON m.id = mu.material_id
     JOIN visits v ON v.id = mu.visit_id
     WHERE v.performed_date BETWEEN ? AND ?
     GROUP BY m.id
     ORDER BY cost DESC`,
    start,
    end
  );
}
