import { NextResponse } from "next/server";

export function ok<T>(data: T, init?: ResponseInit) {
  return NextResponse.json({ ok: true, data }, init);
}

export function fail(message: string, status = 400) {
  return NextResponse.json({ ok: false, error: message }, { status });
}

export function num(v: unknown, fallback = 0): number {
  if (typeof v === "number" && Number.isFinite(v)) return v;
  if (typeof v === "string") {
    const n = Number(v.replace(/[^\d.-]/g, ""));
    return Number.isFinite(n) ? n : fallback;
  }
  return fallback;
}

export function str(v: unknown): string {
  return typeof v === "string" ? v.trim() : "";
}

export function optionalStr(v: unknown): string | null {
  const s = str(v);
  return s ? s : null;
}

export function intId(v: unknown): number | null {
  const n = Number(v);
  return Number.isInteger(n) && n > 0 ? n : null;
}

export function isoDate(v: unknown): string | null {
  const s = str(v);
  return /^\d{4}-\d{2}-\d{2}$/.test(s) ? s : null;
}

export function timeValue(v: unknown): string | null {
  const s = str(v);
  return /^\d{2}:\d{2}$/.test(s) ? s : null;
}
