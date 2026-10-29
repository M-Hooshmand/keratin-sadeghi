import { ok } from "@/lib/http";
import {
  formatJalali,
  todayISO,
  todayJalali,
  weekdayIndex,
  WEEKDAYS,
} from "@/lib/jalali";

export const dynamic = "force-dynamic";

export async function GET() {
  const iso = todayISO();
  const jalali = todayJalali();
  let source = "local";
  let title: string | null = null;

  const key = process.env.TIME_IR_API_KEY;
  if (key) {
    try {
      const controller = new AbortController();
      const timer = setTimeout(() => controller.abort(), 6000);
      const response = await fetch(
        `https://api.time.ir/v1/event/fa/events/${jalali.jy}/${jalali.jm}/${jalali.jd}`,
        {
          signal: controller.signal,
          headers: { "x-api-key": key },
          cache: "no-store",
        }
      );
      clearTimeout(timer);
      if (response.ok) {
        const data = (await response.json()) as {
          data?: Array<{ title?: string }>;
        };
        const titles = (data.data || [])
          .map((event) => event.title)
          .filter((v): v is string => Boolean(v));
        title = titles.length ? titles.join("، ") : null;
        source = "time.ir";
      }
    } catch {
      source = "local";
    }
  }

  return ok({
    iso,
    jalali,
    weekday: WEEKDAYS[weekdayIndex(iso)],
    label: formatJalali(iso, true),
    title,
    source,
  });
}
