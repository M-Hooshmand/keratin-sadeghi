export type JDate = { jy: number; jm: number; jd: number };

const BREAKS = [
  -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097,
  2192, 2262, 2324, 2394, 2456, 3178,
];

const div = (a: number, b: number) => ~~(a / b);
const mod = (a: number, b: number) => a - ~~(a / b) * b;

function jalCal(jy: number): { leap: number; gy: number; march: number } {
  const bl = BREAKS.length;
  const gy = jy + 621;
  let leapJ = -14;
  let jp = BREAKS[0];
  let jm = 0;
  let jump = 0;
  let n: number;

  if (jy < jp || jy >= BREAKS[bl - 1]) throw new Error("سال جلالی نامعتبر است");

  for (let i = 1; i < bl; i += 1) {
    jm = BREAKS[i];
    jump = jm - jp;
    if (jy < jm) break;
    leapJ = leapJ + div(jump, 33) * 8 + div(mod(jump, 33), 4);
    jp = jm;
  }
  n = jy - jp;

  leapJ = leapJ + div(n, 33) * 8 + div(mod(n, 33) + 3, 4);
  if (mod(jump, 33) === 4 && jump - n === 4) leapJ += 1;

  const leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150;
  const march = 20 + leapJ - leapG;

  if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33;
  let leap = mod(mod(n + 1, 33) - 1, 4);
  if (leap === -1) leap = 4;

  return { leap, gy, march };
}

function g2d(gy: number, gm: number, gd: number): number {
  let d =
    div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
    div(153 * mod(gm + 9, 12) + 2, 5) +
    gd -
    34840408;
  d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752;
  return d;
}

function d2g(jdn: number): { gy: number; gm: number; gd: number } {
  let j = 4 * jdn + 139361631;
  j = j + div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908;
  const i = div(mod(j, 1461), 4) * 5 + 308;
  const gd = div(mod(i, 153), 5) + 1;
  const gm = mod(div(i, 153), 12) + 1;
  const gy = div(j, 1461) - 100100 + div(8 - gm, 6);
  return { gy, gm, gd };
}

function j2d(jy: number, jm: number, jd: number): number {
  const r = jalCal(jy);
  return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1;
}

function d2j(jdn: number): JDate {
  const gy = d2g(jdn).gy;
  let jy = gy - 621;
  const r = jalCal(jy);
  const jdn1f = g2d(gy, 3, r.march);
  let k = jdn - jdn1f;
  if (k >= 0) {
    if (k <= 185) {
      return { jy, jm: 1 + div(k, 31), jd: mod(k, 31) + 1 };
    }
    k -= 186;
  } else {
    jy -= 1;
    k += 179;
    if (r.leap === 1) k += 1;
  }
  return { jy, jm: 7 + div(k, 30), jd: mod(k, 30) + 1 };
}

export function isLeapJalaliYear(jy: number): boolean {
  return jalCal(jy).leap === 0;
}

export function jalaliMonthLength(jy: number, jm: number): number {
  if (jm <= 6) return 31;
  if (jm <= 11) return 30;
  return isLeapJalaliYear(jy) ? 30 : 29;
}

export const MONTH_NAMES = [
  "فروردین",
  "اردیبهشت",
  "خرداد",
  "تیر",
  "مرداد",
  "شهریور",
  "مهر",
  "آبان",
  "آذر",
  "دی",
  "بهمن",
  "اسفند",
];

export const WEEKDAYS = [
  "شنبه",
  "یکشنبه",
  "دوشنبه",
  "سه‌شنبه",
  "چهارشنبه",
  "پنجشنبه",
  "جمعه",
];

export const WEEKDAYS_SHORT = ["ش", "ی", "د", "س", "چ", "پ", "ج"];

const pad = (n: number) => String(n).padStart(2, "0");

export function toJalali(date: Date): JDate {
  const r = d2j(g2d(date.getFullYear(), date.getMonth() + 1, date.getDate()));
  return r;
}

export function fromJalali(jy: number, jm: number, jd: number): Date {
  const g = d2g(j2d(jy, jm, jd));
  return new Date(g.gy, g.gm - 1, g.gd, 12, 0, 0, 0);
}

export function isoToJalali(iso: string): JDate {
  const [y, m, d] = iso.split("-").map(Number);
  return d2j(g2d(y, m, d));
}

export function jalaliToISO(jy: number, jm: number, jd: number): string {
  const g = d2g(j2d(jy, jm, jd));
  return `${g.gy}-${pad(g.gm)}-${pad(g.gd)}`;
}

export function todayISO(): string {
  const n = new Date();
  return `${n.getFullYear()}-${pad(n.getMonth() + 1)}-${pad(n.getDate())}`;
}

export function todayJalali(): JDate {
  return toJalali(new Date());
}

export function weekdayIndex(iso: string): number {
  const [y, m, d] = iso.split("-").map(Number);
  return (new Date(y, m - 1, d, 12).getDay() + 1) % 7;
}

export function weekdayOfISO(iso: string): string {
  return WEEKDAYS[weekdayIndex(iso)];
}

export function formatJalali(iso: string, withWeekday = false): string {
  const j = isoToJalali(iso);
  const base = `${toPersianDigits(j.jd)} ${MONTH_NAMES[j.jm - 1]} ${toPersianDigits(j.jy)}`;
  return withWeekday ? `${weekdayOfISO(iso)}، ${base}` : base;
}

export function formatJalaliShort(iso: string): string {
  const j = isoToJalali(iso);
  return `${toPersianDigits(j.jy)}/${toPersianDigits(j.jm)}/${toPersianDigits(j.jd)}`;
}

export function currentJalaliYear(): number {
  return todayJalali().jy;
}

export function monthRange(y: number, m: number): { start: string; end: string } {
  const start = jalaliToISO(y, m, 1);
  const end = jalaliToISO(y, m, jalaliMonthLength(y, m));
  return { start, end };
}

export function addJalaliMonths(
  y: number,
  m: number,
  delta: number
): { jy: number; jm: number } {
  const total = y * 12 + (m - 1) + delta;
  return { jy: Math.floor(total / 12), jm: (total % 12) + 1 };
}

const FA_DIGITS = "۰۱۲۳۴۵۶۷۸۹";
const AR_DIGITS = "٠١٢٣٤٥٦٧٨٩";

export function toPersianDigits(input: string | number): string {
  return String(input).replace(/[0-9]/g, (d) => FA_DIGITS[Number(d)]);
}

export function toEnglishDigits(input: string): string {
  return input
    .replace(/[۰-۹]/g, (d) => String(FA_DIGITS.indexOf(d)))
    .replace(/[٠-٩]/g, (d) => String(AR_DIGITS.indexOf(d)));
}

export function parseNumber(input: string | null | undefined): number {
  if (input == null) return 0;
  const cleaned = toEnglishDigits(String(input))
    .replace(/[,٬\s]/g, "")
    .replace(/[^\d.-]/g, "");
  const n = Number(cleaned);
  return Number.isFinite(n) ? n : 0;
}

export function formatNumber(n: number): string {
  return toPersianDigits(new Intl.NumberFormat("en-US").format(Math.round(n)));
}

export function formatToman(n: number): string {
  return `${formatNumber(n)} تومان`;
}

export function formatMl(n: number): string {
  return `${toPersianDigits(Number(n).toLocaleString("en-US", { maximumFractionDigits: 1 }))} میلی‌لیتر`;
}

export function nowTime(): string {
  const d = new Date();
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function timeLabel(t: string): string {
  return toPersianDigits(t);
}
