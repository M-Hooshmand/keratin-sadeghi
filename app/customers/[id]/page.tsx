import Link from "next/link";
import { notFound } from "next/navigation";
import {
  ArrowRight,
  CalendarDays,
  ClipboardPlus,
  Hash,
  Phone,
  Scissors,
  StickyNote,
  TrendingUp,
  Wallet,
} from "lucide-react";
import { EmptyState, PageHeader, StatCard, StatusBadge } from "@/components/ui";
import {
  formatJalali,
  formatNumber,
  formatToman,
  timeLabel,
} from "@/lib/jalali";
import { customerHistory, getCustomer } from "@/lib/queries";

export const dynamic = "force-dynamic";

type Params = { params: Promise<{ id: string }> };

export default async function CustomerPage({ params }: Params) {
  const { id } = await params;
  const customerId = Number(id);
  if (!Number.isInteger(customerId)) notFound();
  const customer = getCustomer(customerId);
  if (!customer) notFound();

  const { appointments, visits } = customerHistory(customer.id);
  const totalSpent = visits.reduce((sum, visit) => sum + visit.final_amount, 0);
  const totalMaterial = visits.reduce((sum, visit) => sum + visit.material_cost, 0);
  const lastVisit = visits[0];

  return (
    <div>
      <div className="mb-4">
        <Link
          href="/customers"
          className="inline-flex items-center gap-1 text-xs text-muted hover:text-rosedeep"
        >
          <ArrowRight size={14} />
          بازگشت به مشتریان
        </Link>
      </div>

      <PageHeader
        title={`${customer.first_name} ${customer.last_name}`}
        subtitle="پرونده مشتری و سابقه کامل مراجعات"
        action={
          <Link href="/appointments" className="btn-primary">
            <CalendarDays size={16} />
            ثبت نوبت
          </Link>
        }
      />

      <div className="grid gap-4 lg:grid-cols-[320px_1fr]">
        <aside className="space-y-4">
          <section className="card space-y-3">
            <div className="flex items-center gap-3">
              <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blush/70 text-base font-bold text-rosedeep">
                {customer.first_name.slice(0, 1)}
                {customer.last_name.slice(0, 1)}
              </span>
              <div>
                <p className="text-sm font-bold text-ink">
                  {customer.first_name} {customer.last_name}
                </p>
                <p className="mt-0.5 flex items-center gap-1 text-[11px] text-muted">
                  <Phone size={12} />
                  <span dir="ltr">{customer.phone}</span>
                </p>
              </div>
            </div>
            <div className="rounded-xl bg-ivory p-3 text-[11px] leading-6 text-muted">
              <p className="flex items-center gap-1">
                <Hash size={12} />
                شناسه مشتری: {formatNumber(customer.id)}
              </p>
              <p className="mt-1">
                {formatNumber(appointments.length)} نوبت ثبت‌شده ·{" "}
                {formatNumber(visits.length)} کار انجام‌شده
              </p>
            </div>
            {customer.notes ? (
              <div className="rounded-xl border border-line p-3 text-[11px] leading-6 text-ink/75">
                <p className="mb-1 flex items-center gap-1 text-muted">
                  <StickyNote size={12} />
                  یادداشت
                </p>
                {customer.notes}
              </div>
            ) : null}
          </section>

          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
            <StatCard
              label="مجموع پرداختی"
              value={formatToman(totalSpent)}
              icon={<Wallet size={18} />}
              tone="green"
            />
            <StatCard
              label="سود ناخالص از این مشتری"
              value={formatToman(totalSpent - totalMaterial)}
              icon={<TrendingUp size={18} />}
              tone="rose"
            />
          </div>

          {lastVisit ? (
            <section className="card">
              <p className="text-xs font-bold text-ink">آخرین مراجعه</p>
              <p className="mt-1 text-[11px] text-muted">
                {formatJalali(lastVisit.performed_date, true)}
              </p>
              <p className="mt-2 text-xs text-ink/80">
                {lastVisit.service_name || "بدون خدمت مشخص"}
              </p>
            </section>
          ) : null}
        </aside>

        <div className="space-y-4">
          <section className="card">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-bold text-ink">
              <Scissors size={16} className="text-hot" />
              سابقه نوبت‌ها
            </h2>
            {appointments.length === 0 ? (
              <EmptyState title="نوبتی برای این مشتری ثبت نشده" icon={<CalendarDays size={22} />} />
            ) : (
              <div className="space-y-2">
                {appointments.map((item) => (
                  <div
                    key={item.id}
                    className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-line bg-white px-3 py-2.5"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-20 text-[11px] text-muted">
                        {formatJalali(item.appointment_date)}
                      </div>
                      <div>
                        <p className="text-xs font-medium text-ink">
                          {item.service_name || "بدون خدمت مشخص"}
                        </p>
                        <p className="mt-0.5 text-[10px] text-muted">
                          ساعت {timeLabel(item.appointment_time)} · بیعانه{" "}
                          {formatNumber(item.deposit)} تومان
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <StatusBadge status={item.status} />
                      <Link href={`/visits/${item.id}`} className="btn-ghost btn-sm">
                        <ClipboardPlus size={13} />
                        {item.status === "done" ? "نتیجه" : "ثبت نتیجه"}
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="card">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-bold text-ink">
              <ClipboardPlus size={16} className="text-hot" />
              نتایج ثبت‌شده
            </h2>
            {visits.length === 0 ? (
              <EmptyState
                title="هنوز نتیجه‌ای ثبت نشده"
                description="پس از انجام کار، از صفحه نوبت نتیجه، قد و حجم مو، مشکل و مواد مصرفی را ثبت کنید."
                icon={<ClipboardPlus size={22} />}
              />
            ) : (
              <div className="space-y-2">
                {visits.map((visit) => (
                  <div key={visit.id} className="rounded-xl border border-line bg-white p-3">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <p className="text-xs font-bold text-ink">
                          {visit.service_name || "بدون خدمت مشخص"}
                        </p>
                        <p className="mt-0.5 text-[10px] text-muted">
                          {formatJalali(visit.performed_date, true)}
                          {visit.hair_length_cm != null
                            ? ` · طول مو ${formatNumber(visit.hair_length_cm)} سانتی‌متر`
                            : ""}
                          {visit.hair_volume
                            ? ` · حجم مو ${visit.hair_volume}`
                            : ""}
                        </p>
                      </div>
                      <div className="text-left text-[11px]">
                        <p className="font-bold text-ink">{formatToman(visit.final_amount)}</p>
                        <p className="text-muted">
                          هزینه مواد {formatNumber(visit.material_cost)}
                        </p>
                      </div>
                    </div>
                    {visit.hair_condition ? (
                      <p className="mt-2 text-[11px] text-ink/70">
                        وضعیت مو: {visit.hair_condition}
                      </p>
                    ) : null}
                    {visit.work_done ? (
                      <p className="mt-1 text-[11px] leading-5 text-muted">
                        {visit.work_done}
                      </p>
                    ) : null}
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
