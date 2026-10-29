"use client";

import { useEffect, useMemo, useState } from "react";
import {
  BarChart3,
  Loader2,
  Package,
  TrendingUp,
  Wallet,
} from "lucide-react";
import { PageHeader, StatCard } from "./ui";
import {
  formatMl,
  formatNumber,
  formatToman,
  MONTH_NAMES,
  toPersianDigits,
} from "@/lib/jalali";

interface MonthRow {
  jm: number;
  count: number;
  income: number;
  deposit: number;
  materialCost: number;
  profit: number;
}

interface ServiceRow {
  name: string;
  count: number;
  income: number;
  materialCost: number;
}

interface MaterialRow {
  name: string;
  total_ml: number;
  cost: number;
  uses: number;
}

interface Data {
  year: number;
  months: MonthRow[];
  totals: {
    income: number;
    materialCost: number;
    profit: number;
    count: number;
    deposit: number;
  };
  services: ServiceRow[];
  materials: MaterialRow[];
}

export default function AccountingClient({ initialYear }: { initialYear: number }) {
  const years = useMemo(
    () => Array.from({ length: 6 }, (_, i) => initialYear - i),
    [initialYear]
  );
  const [year, setYear] = useState(initialYear);
  const [data, setData] = useState<Data | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    fetch(`/api/accounting?year=${year}`)
      .then((response) => response.json())
      .then((json) => {
        if (!active) return;
        if (!json.ok) throw new Error(json.error);
        setData(json.data);
      })
      .catch(() => {
        if (active) setError("دریافت گزارش انجام نشد");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [year]);

  const maxIncome = useMemo(
    () => Math.max(1, ...(data?.months.map((m) => m.income) ?? [1])),
    [data]
  );

  return (
    <div>
      <PageHeader
        title="حسابداری"
        subtitle="درآمد، هزینه مواد و سود به تفکیک هر ماه شمسی"
        action={
          <select
            className="input w-36 py-2"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
          >
            {years.map((y) => (
              <option key={y} value={y}>
                سال {toPersianDigits(y)}
              </option>
            ))}
          </select>
        }
      />

      {loading && !data ? (
        <div className="card flex items-center justify-center gap-2 py-16 text-sm text-muted">
          <Loader2 size={18} className="animate-spin" />
          در حال محاسبه گزارش...
        </div>
      ) : error ? (
        <p className="rounded-xl border border-[#efc9c9] bg-[#fbeaea] px-3 py-2 text-xs text-[#8e3b3b]">
          {error}
        </p>
      ) : data ? (
        <div className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard
              label={`درآمد سال ${toPersianDigits(data.year)}`}
              value={formatToman(data.totals.income)}
              hint={`${formatNumber(data.totals.count)} نوبت انجام‌شده`}
              icon={<Wallet size={18} />}
              tone="rose"
            />
            <StatCard
              label="هزینه مواد مصرفی"
              value={formatToman(data.totals.materialCost)}
              hint="بر اساس قیمت ثبت‌شده در هر نوبت"
              icon={<Package size={18} />}
              tone="gold"
            />
            <StatCard
              label="سود ناخالص"
              value={formatToman(data.totals.profit)}
              hint="درآمد منهای هزینه مواد"
              icon={<TrendingUp size={18} />}
              tone="green"
            />
            <StatCard
              label="بیعانه‌های ثبت‌شده"
              value={formatToman(data.totals.deposit)}
              hint="نوبت‌های لغو‌نشده سال"
              icon={<BarChart3 size={18} />}
              tone="plain"
            />
          </div>

          <div className="card">
            <h2 className="mb-4 text-sm font-bold text-ink">درآمد ماه‌به‌ماه</h2>
            <div className="space-y-2">
              {data.months.map((month) => (
                <div key={month.jm} className="flex items-center gap-3">
                  <span className="w-16 shrink-0 text-xs text-muted">
                    {MONTH_NAMES[month.jm - 1]}
                  </span>
                  <div className="h-6 flex-1 overflow-hidden rounded-lg bg-ivory">
                    <div
                      className={`h-full rounded-lg transition-all ${
                        month.income > 0 ? "bg-rosedeep/80" : "bg-transparent"
                      }`}
                      style={{ width: `${(month.income / maxIncome) * 100}%` }}
                    />
                  </div>
                  <span className="w-28 shrink-0 text-left text-[11px] text-ink/75" dir="rtl">
                    {month.income > 0 ? formatToman(month.income) : "—"}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className="card overflow-x-auto">
            <h2 className="mb-3 text-sm font-bold text-ink">جدول ماهانه</h2>
            <table className="w-full min-w-[560px] text-right text-xs">
              <thead>
                <tr className="border-b border-line text-[11px] text-muted">
                  <th className="py-2 font-medium">ماه</th>
                  <th className="py-2 font-medium">تعداد کار</th>
                  <th className="py-2 font-medium">درآمد</th>
                  <th className="py-2 font-medium">هزینه مواد</th>
                  <th className="py-2 font-medium">سود</th>
                </tr>
              </thead>
              <tbody>
                {data.months.map((month) => (
                  <tr
                    key={month.jm}
                    className={`border-b border-line/60 ${
                      month.count > 0 ? "text-ink" : "text-muted/70"
                    }`}
                  >
                    <td className="py-2.5 font-medium">{MONTH_NAMES[month.jm - 1]}</td>
                    <td className="py-2.5">{formatNumber(month.count)}</td>
                    <td className="py-2.5">{formatNumber(month.income)}</td>
                    <td className="py-2.5">{formatNumber(month.materialCost)}</td>
                    <td className="py-2.5">{formatNumber(month.profit)}</td>
                  </tr>
                ))}
                <tr className="bg-blush/40 font-bold text-ink">
                  <td className="py-2.5">جمع سال</td>
                  <td className="py-2.5">{formatNumber(data.totals.count)}</td>
                  <td className="py-2.5">{formatNumber(data.totals.income)}</td>
                  <td className="py-2.5">{formatNumber(data.totals.materialCost)}</td>
                  <td className="py-2.5">{formatNumber(data.totals.profit)}</td>
                </tr>
              </tbody>
            </table>
            <p className="mt-2 text-[10px] text-muted">مبالغ به تومان است.</p>
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <div className="card">
              <h2 className="mb-3 text-sm font-bold text-ink">درآمد به تفکیک خدمت</h2>
              {data.services.length === 0 ? (
                <p className="py-6 text-center text-xs text-muted">کاری ثبت نشده است.</p>
              ) : (
                <div className="space-y-2">
                  {data.services.map((service) => (
                    <div
                      key={service.name}
                      className="rounded-xl border border-line bg-white px-3 py-2"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="text-xs font-medium text-ink">{service.name}</span>
                        <span className="text-[11px] text-muted">
                          {formatNumber(service.count)} نوبت
                        </span>
                      </div>
                      <div className="mt-1 flex items-center justify-between text-[11px]">
                        <span className="text-muted">
                          هزینه مواد {formatNumber(service.materialCost)}
                        </span>
                        <span className="font-medium text-[#2f6b4f]">
                          {formatToman(service.income)}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="card">
              <h2 className="mb-3 text-sm font-bold text-ink">مصرف مواد در سال</h2>
              {data.materials.length === 0 ? (
                <p className="py-6 text-center text-xs text-muted">مصرفی ثبت نشده است.</p>
              ) : (
                <table className="w-full text-right text-xs">
                  <thead>
                    <tr className="border-b border-line text-[11px] text-muted">
                      <th className="py-2 font-medium">ماده</th>
                      <th className="py-2 font-medium">مقدار</th>
                      <th className="py-2 font-medium">هزینه</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.materials.map((material) => (
                      <tr key={material.name} className="border-b border-line/60">
                        <td className="py-2.5">{material.name}</td>
                        <td className="py-2.5">{formatMl(material.total_ml)}</td>
                        <td className="py-2.5">{formatNumber(material.cost)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
