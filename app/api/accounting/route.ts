import { ok } from "@/lib/http";
import { jalaliMonthLength, jalaliToISO } from "@/lib/jalali";
import {
  materialCostBreakdown,
  monthSummary,
  serviceBreakdown,
  yearSummary,
} from "@/lib/queries";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const now = new Date();
  const localYear = Number(
    new Intl.DateTimeFormat("en-US-u-ca-persian", { year: "numeric" }).format(now)
  );
  const yearParam = Number(url.searchParams.get("year"));
  const year =
    Number.isInteger(yearParam) && yearParam > 1300 && yearParam < 1600
      ? yearParam
      : localYear;
  const monthParam = Number(url.searchParams.get("month"));
  const month =
    Number.isInteger(monthParam) && monthParam >= 1 && monthParam <= 12 ? monthParam : null;

  const months = yearSummary(year);
  const allVisits = months.flatMap((m) => m.visits);
  const start = jalaliToISO(year, 1, 1);
  const end = jalaliToISO(year, 12, jalaliMonthLength(year, 12));

  const totals = months.reduce(
    (acc, m) => {
      acc.income += m.income;
      acc.materialCost += m.materialCost;
      acc.profit += m.profit;
      acc.count += m.count;
      acc.deposit += m.deposit;
      return acc;
    },
    { income: 0, materialCost: 0, profit: 0, count: 0, deposit: 0 }
  );

  return ok({
    year,
    months,
    totals,
    services: serviceBreakdown(allVisits),
    materials: materialCostBreakdown(start, end),
    month: month ? monthSummary(year, month) : null,
  });
}
