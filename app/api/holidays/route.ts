import { all, get, run } from "@/lib/db";
import { ok } from "@/lib/http";
import { isoToJalali, jalaliMonthLength, jalaliToISO } from "@/lib/jalali";

export const dynamic = "force-dynamic";

interface HolidayRow {
  date: string;
  is_holiday: number;
  title: string | null;
}

interface Result {
  date: string;
  isHoliday: boolean;
  title: string | null;
}

async function fetchDay(date: string, attempt = 0): Promise<Result | null> {
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);
    const j = isoToJalali(date);
    const jalaliPath = `${j.jy}/${String(j.jm).padStart(2, "0")}/${String(j.jd).padStart(2, "0")}`;
    const response = await fetch(`https://holidayapi.ir/jalali/${jalaliPath}`, {
      signal: controller.signal,
      headers: { accept: "application/json" },
      cache: "no-store",
    });
    clearTimeout(timer);
    if (!response.ok) {
      if (attempt < 2) {
        await new Promise((resolve) => setTimeout(resolve, 400 * (attempt + 1)));
        return fetchDay(date, attempt + 1);
      }
      return null;
    }
    const data = (await response.json()) as {
      is_holiday?: boolean;
      events?: Array<{ description?: string; is_holiday?: boolean }>;
    };
    const titles = (data.events || [])
      .filter((e) => e.is_holiday)
      .map((e) => e.description)
      .filter((v): v is string => Boolean(v));
    return {
      date,
      isHoliday: Boolean(data.is_holiday),
      title: titles.length ? titles.join("، ") : null,
    };
  } catch {
    if (attempt < 2) {
      await new Promise((resolve) => setTimeout(resolve, 400 * (attempt + 1)));
      return fetchDay(date, attempt + 1);
    }
    return null;
  }
}

export async function GET(request: Request) {
  const url = new URL(request.url);
  const now = new Date();
  const currentYear = Number(
    new Intl.DateTimeFormat("en-US-u-ca-persian", { year: "numeric" }).format(now)
  );
  const currentMonth = Number(
    new Intl.DateTimeFormat("en-US-u-ca-persian", { month: "numeric" }).format(now)
  );

  const yearParam = Number(url.searchParams.get("year"));
  const monthParam = Number(url.searchParams.get("month"));
  const jy =
    Number.isInteger(yearParam) && yearParam > 1300 && yearParam < 1600
      ? yearParam
      : currentYear;
  const jm =
    Number.isInteger(monthParam) && monthParam >= 1 && monthParam <= 12
      ? monthParam
      : currentMonth;

  const dates = Array.from({ length: jalaliMonthLength(jy, jm) }, (_, i) =>
    jalaliToISO(jy, jm, i + 1)
  );

  const cached = new Map<string, Result>();
  const placeholders = dates.map(() => "?").join(",");
  const rows = all<HolidayRow>(
    `SELECT date, is_holiday, title FROM holidays WHERE date IN (${placeholders})`,
    ...dates
  );
  rows.forEach((row) =>
    cached.set(row.date, {
      date: row.date,
      isHoliday: row.is_holiday === 1,
      title: row.title,
    })
  );

  const missing = dates.filter((d) => !cached.has(d));
  for (let index = 0; index < missing.length; index += 4) {
    const batch = missing.slice(index, index + 4);
    const fetched = await Promise.all(batch.map((d) => fetchDay(d)));
    fetched.forEach((item) => {
      if (!item) return;
      cached.set(item.date, item);
      run(
        `INSERT INTO holidays (date, is_holiday, title) VALUES (?, ?, ?)
         ON CONFLICT(date) DO UPDATE SET is_holiday = excluded.is_holiday, title = excluded.title`,
        item.date,
        item.isHoliday ? 1 : 0,
        item.title
      );
    });
  }

  const days = dates
    .map((date) => cached.get(date))
    .filter((item): item is Result => Boolean(item))
    .map((item) => ({
      date: item.date,
      isHoliday: item.isHoliday,
      title: item.title,
    }));

  return ok({ year: jy, month: jm, days });
}
