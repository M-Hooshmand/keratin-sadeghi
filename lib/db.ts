import { DatabaseSync } from "node:sqlite";
import fs from "node:fs";
import path from "node:path";

export const DB_PATH =
  process.env.DB_PATH || path.join(process.cwd(), "data", "keratin.db");

const SCHEMA = `
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS customers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  phone TEXT NOT NULL,
  notes TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);

CREATE TABLE IF NOT EXISTS services (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  sort INTEGER NOT NULL DEFAULT 0,
  active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS appointments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  service_id INTEGER REFERENCES services(id) ON DELETE SET NULL,
  appointment_date TEXT NOT NULL,
  appointment_time TEXT NOT NULL,
  deposit INTEGER NOT NULL DEFAULT 0,
  status TEXT NOT NULL DEFAULT 'waiting',
  notes TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);

CREATE TABLE IF NOT EXISTS visits (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  appointment_id INTEGER NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
  customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  service_id INTEGER REFERENCES services(id) ON DELETE SET NULL,
  performed_date TEXT NOT NULL,
  final_amount INTEGER NOT NULL DEFAULT 0,
  hair_length_cm REAL,
  hair_volume TEXT,
  hair_condition TEXT,
  work_done TEXT,
  notes TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);
CREATE INDEX IF NOT EXISTS idx_visits_date ON visits(performed_date);

CREATE TABLE IF NOT EXISTS materials (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  unit TEXT NOT NULL DEFAULT 'ml',
  price_per_unit INTEGER NOT NULL DEFAULT 0,
  notes TEXT,
  active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS material_usages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  visit_id INTEGER NOT NULL REFERENCES visits(id) ON DELETE CASCADE,
  material_id INTEGER NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
  amount REAL NOT NULL DEFAULT 0,
  unit_price INTEGER NOT NULL DEFAULT 0,
  unit TEXT NOT NULL DEFAULT 'ml'
);
CREATE INDEX IF NOT EXISTS idx_usages_visit ON material_usages(visit_id);

CREATE TABLE IF NOT EXISTS holidays (
  date TEXT PRIMARY KEY,
  is_holiday INTEGER NOT NULL DEFAULT 0,
  title TEXT
);

CREATE TABLE IF NOT EXISTS settings (
  key TEXT PRIMARY KEY,
  value TEXT
);
`;

const SERVICES = [
  "کراتین موهای نچرال",
  "کراتین موهای رنگ‌شده",
  "پروتئین تراپی موهای نچرال",
  "پروتئین تراپی موهای آسیب‌دیده",
  "پروتئین تراپی موهای دکلره‌دار",
  "پروتئین تراپی + بسترساز (موهای خیلی آسیب‌دیده و کشسان)",
  "موخوره‌گیری به روش روسی با قیچی",
];

const MATERIALS: Array<{ name: string; price: number; notes: string }> = [
  { name: "پروتئین", price: 2500, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
  { name: "کراتین موهای نچرال", price: 3000, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
  { name: "کراتین موهای رنگ‌شده", price: 3200, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
  { name: "بسترساز", price: 1800, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
  { name: "شامپو", price: 450, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
  { name: "ماسک درمانی", price: 900, notes: "قیمت نمونه — از بخش مواد و قیمت‌ها اصلاح کنید" },
];

declare global {
  // eslint-disable-next-line no-var
  var __keratinDb: DatabaseSync | undefined;
}

function create(): DatabaseSync {
  fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });
  const db = new DatabaseSync(DB_PATH);
  db.exec("PRAGMA journal_mode = WAL;");
  db.exec(SCHEMA);

  const visitColumns = db.prepare("PRAGMA table_info(visits)").all() as Array<{
    name: string;
  }>;
  if (!visitColumns.some((column) => column.name === "hair_volume")) {
    db.exec("ALTER TABLE visits ADD COLUMN hair_volume TEXT");
  }

  const serviceStmt = db.prepare(
    "INSERT OR IGNORE INTO services (name, sort) VALUES (?, ?)"
  );
  SERVICES.forEach((name, i) => serviceStmt.run(name, i));

  const materialsCount = db.prepare("SELECT COUNT(*) AS c FROM materials").get() as {
    c: number;
  };
  if (materialsCount.c === 0) {
    const stmt = db.prepare(
      "INSERT INTO materials (name, price_per_unit, notes) VALUES (?, ?, ?)"
    );
    MATERIALS.forEach((m) => stmt.run(m.name, m.price, m.notes));
  }

  return db;
}

export function getDb(): DatabaseSync {
  if (!global.__keratinDb) {
    global.__keratinDb = create();
  }
  return global.__keratinDb;
}

export type SqlParam = string | number | null;

export function all<T>(sql: string, ...params: SqlParam[]): T[] {
  return getDb()
    .prepare(sql)
    .all(...params)
    .map((row) => ({ ...row })) as T[];
}

export function get<T>(sql: string, ...params: SqlParam[]): T | undefined {
  const row = getDb()
    .prepare(sql)
    .get(...params);
  return row ? ({ ...row } as T) : undefined;
}

export function run(
  sql: string,
  ...params: SqlParam[]
): { lastInsertRowid: number; changes: number } {
  const r = getDb()
    .prepare(sql)
    .run(...params);
  return {
    lastInsertRowid: Number(r.lastInsertRowid),
    changes: Number(r.changes),
  };
}
