import Image from "next/image";
import Link from "next/link";
import {
  ArrowLeft,
  CalendarDays,
  CalendarPlus,
  ClipboardList,
  Clock3,
  Heart,
  Scissors,
  Sparkles,
  TrendingUp,
  Users,
  Wallet,
} from "lucide-react";
import { EmptyState, StatCard, StatusBadge } from "@/components/ui";
import {
  currentJalaliYear,
  formatJalali,
  formatNumber,
  formatToman,
  MONTH_NAMES,
  timeLabel,
  todayISO,
  todayJalali,
} from "@/lib/jalali";
import {
  listAppointments,
  listCustomers,
  monthSummary,
} from "@/lib/queries";

export const dynamic = "force-dynamic";

export default function DashboardPage() {
  const today = todayISO();
  const jalali = todayJalali();
  const todayAppointments = listAppointments({ date: today });
  const waiting = listAppointments({ status: "waiting", from: today });
  const month = monthSummary(jalali.jy, jalali.jm);
  const customers = listCustomers();

  return (
    <div>
      <section className="relative mb-4 overflow-hidden rounded-[34px] border-2 border-white/60 bg-gradient-to-l from-[#4b0d2b] via-[#a8215d] to-[#ff4fa5] text-white shadow-[0_36px_70px_-44px_rgba(125,23,69,0.95)]">
        <span className="checker-soft pointer-events-none absolute inset-0 opacity-25" />
        <span className="pointer-events-none absolute -right-16 -top-20 h-56 w-56 rounded-full bg-white/15 blur-2xl" />
        <span className="pointer-events-none absolute -bottom-24 left-1/3 h-64 w-64 rounded-full bg-[#ffc0dc]/25 blur-3xl" />
        <span className="twinkle pointer-events-none absolute right-[42%] top-6 text-xl text-white/35">
          ✦
        </span>
        <div className="relative grid items-stretch md:grid-cols-[1.15fr_0.85fr]">
          <div className="flex flex-col justify-center p-6 md:p-8">
            <span className="barbie-sticker w-fit border-white/40 bg-white/15 tracking-[0.2em] shadow-none backdrop-blur">
              <Sparkles size={12} />
              barbiecore salon
            </span>
            <h1 className="mt-3 text-2xl font-extrabold leading-snug md:text-[30px]">
              درخشش موهایت را
              <br className="hidden sm:block" /> به سبک{" "}
              <span className="text-[#ffd6e8]">باربی</span> بسپار
            </h1>
            <p className="mt-2 max-w-md text-xs leading-6 text-[#ffe4ef]/90 md:text-sm">
              نوبت‌ها، مشتریان، نتیجه کار، مصرف مواد و حساب‌وکتاب ماهانه؛ همه در
              یک پین‌بورد مرتب و صورتی.
            </p>
            <div className="mt-4 flex flex-wrap items-center gap-2">
              <Link
                href="/appointments"
                className="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2.5 text-sm font-bold text-rosedeep shadow-lg shadow-black/10 transition hover:bg-[#fff0f6]"
              >
                <CalendarPlus size={16} />
                ثبت نوبت جدید
              </Link>
              <Link
                href="/appointments"
                className="inline-flex items-center gap-2 rounded-full border border-white/35 bg-white/10 px-4 py-2.5 text-sm font-medium text-white backdrop-blur transition hover:bg-white/20"
              >
                مشاهده نوبت‌ها
                <ArrowLeft size={15} />
              </Link>
            </div>
            <div className="mt-5 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[11px] text-[#ffe4ef]/85">
              <span className="inline-flex items-center gap-1.5">
                <CalendarDays size={13} />
                {formatJalali(today, true)}
              </span>
              <span className="h-3 w-px bg-white/30" />
              <span>{formatNumber(todayAppointments.length)} نوبت امروز</span>
              <span className="h-3 w-px bg-white/30" />
              <span>{formatNumber(waiting.length)} نفر در انتظار</span>
            </div>
          </div>

          <div className="relative min-h-56 p-3 md:min-h-full md:p-4">
            <div className="relative h-full min-h-52 overflow-hidden rounded-[28px] border-2 border-white/65 shadow-2xl shadow-black/20">
              <Image
                src="/hair-hero-2026.jpg"
                alt="مدل با موی بلند، صاف و درخشان"
                fill
                priority
                sizes="(max-width: 768px) 100vw, 420px"
                className="object-cover object-[center_28%]"
              />
              <span className="absolute inset-0 bg-gradient-to-l from-[#4b0d2b]/65 via-[#a8215d]/10 to-transparent" />
              <span className="tape z-10" />
              <span className="absolute right-3 top-3 flex h-8 w-8 items-center justify-center rounded-full border border-white/60 bg-white/20 text-white backdrop-blur">
                <Heart size={14} className="fill-white/90" />
              </span>
              <span className="absolute bottom-3 right-3 inline-flex items-center gap-1.5 rounded-full bg-white/20 px-3 py-1 text-[10px] font-medium text-white backdrop-blur">
                <Scissors size={12} />
                کراتین · پروتئین · ترمیم
              </span>
            </div>
          </div>
        </div>
      </section>

      <section className="mb-5 mt-5">
        <div className="mb-3 flex items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <span className="barbie-sticker">
              <Sparkles size={11} />
              mood board
            </span>
            <h2 className="text-sm font-extrabold text-ink">برد الهام سالن</h2>
          </div>
          <span className="hidden text-[10px] text-muted sm:block">
            پین‌ها را ورق بزن
          </span>
        </div>

        <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <Link
            href="/appointments"
            className="pin-card group col-span-2 overflow-hidden p-5"
          >
            <span className="tape" />
            <span className="checker-soft pointer-events-none absolute inset-0 opacity-30" />
            <span className="relative flex h-full min-h-44 flex-col">
              <span className="flex items-center justify-between gap-2">
                <span className="barbie-sticker">
                  <Scissors size={11} />
                  service menu
                </span>
                <span className="flex h-9 w-9 items-center justify-center rounded-2xl bg-blush text-rosedeep ring-1 ring-white">
                  <Sparkles size={16} />
                </span>
              </span>
              <span className="mt-4 block text-lg font-extrabold leading-8 text-ink">
                درخشش، از یک نوبت شروع می‌شود
              </span>
              <span className="mt-1 block text-[11px] leading-6 text-muted">
                کراتین، پروتئین‌تراپی و موخوره‌گیری روسی؛ با ثبت دقیق قد و حجم
                مو.
              </span>
              <span className="mt-3 flex flex-wrap gap-1.5">
                <span className="chip border-[#ffc1dc] bg-[#ffe3f1] text-rosedeep">
                  کراتین
                </span>
                <span className="chip border-[#e9d1ff] bg-[#f3e3ff] text-[#7b3c9e]">
                  پروتئین
                </span>
                <span className="chip border-[#ffd3c4] bg-[#fff0e9] text-[#a26036]">
                  موخوره‌گیری
                </span>
              </span>
              <span className="mt-auto flex items-center gap-1 pt-4 text-[10px] font-bold text-hot">
                رزرو نوبت
                <ArrowLeft
                  size={12}
                  className="transition group-hover:-translate-x-0.5"
                />
              </span>
            </span>
          </Link>

          <div className="pin-card relative overflow-hidden p-4">
            <span className="tape" />
            <Heart size={18} className="mb-2 fill-hot/15 text-hot" />
            <p className="text-sm font-extrabold leading-7 text-ink">
              «موهای سالم، اعتماد به نفسِ بیشتر.»
            </p>
            <p className="mt-1 text-[10px] leading-5 text-muted">
              پین امروز برای یک شروع باربی‌وار
            </p>
            <div className="mt-3 flex flex-wrap gap-1.5">
              <span className="chip border-[#ffc1dc] bg-[#ffe3f1] text-rosedeep">
                کراتین
              </span>
              <span className="chip border-[#e9d1ff] bg-[#f3e3ff] text-[#7b3c9e]">
                پروتئین
              </span>
              <span className="chip border-[#ffd3c4] bg-[#fff0e9] text-[#a26036]">
                ترمیم
              </span>
            </div>
          </div>

          <Link
            href="/materials"
            className="group relative overflow-hidden rounded-[28px] border border-white/60 bg-gradient-to-br from-[#ff4fa5] via-[#d42c7a] to-[#7d1745] p-4 text-white shadow-[0_24px_48px_-30px_rgba(176,42,102,0.9)] transition duration-300 hover:-translate-y-1"
          >
            <span className="checker-soft pointer-events-none absolute inset-0 opacity-30" />
            <span className="relative flex h-full flex-col">
              <span className="flex h-9 w-9 items-center justify-center rounded-2xl bg-white/20 ring-1 ring-white/40">
                <Wallet size={16} />
              </span>
              <p className="mt-3 text-sm font-extrabold">قیمت مواد</p>
              <p className="mt-1 text-[10px] leading-5 text-white/80">
                هر میل را دقیق حساب کن تا سود واقعی بماند.
              </p>
              <span className="mt-auto flex items-center gap-1 pt-3 text-[10px] font-medium text-[#ffd6e8]">
                مدیریت قیمت‌ها
                <ArrowLeft
                  size={12}
                  className="transition group-hover:-translate-x-0.5"
                />
              </span>
            </span>
          </Link>
        </div>
      </section>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="نوبت‌های امروز"
          value={`${formatNumber(todayAppointments.length)} نوبت`}
          hint="شامل منتظر، انجام‌شده و لغوشده"
          icon={<CalendarDays size={18} />}
          tone="rose"
        />
        <StatCard
          label="در لیست انتظار"
          value={`${formatNumber(waiting.length)} نفر`}
          hint="از امروز به بعد"
          icon={<Clock3 size={18} />}
          tone="gold"
        />
        <StatCard
          label={`درآمد ${MONTH_NAMES[jalali.jm - 1]}`}
          value={formatToman(month.income)}
          hint={`${formatNumber(month.count)} کار انجام‌شده`}
          icon={<Wallet size={18} />}
          tone="green"
        />
        <StatCard
          label="سود ناخالص ماه"
          value={formatToman(month.profit)}
          hint={`هزینه مواد ${formatToman(month.materialCost)}`}
          icon={<TrendingUp size={18} />}
          tone="plain"
        />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-[1.4fr_1fr]">
        <section className="card">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
              <CalendarDays size={16} className="text-hot" />
              برنامه امروز
            </h2>
            <Link
              href="/appointments"
              className="flex items-center gap-1 text-[11px] text-rosedeep hover:underline"
            >
              همه نوبت‌ها
              <ArrowLeft size={12} />
            </Link>
          </div>

          {todayAppointments.length === 0 ? (
            <EmptyState
              title="امروز نوبتی ثبت نشده"
              description="برای امروز وقت خالی است؛ می‌توانید نوبت جدید ثبت کنید."
              icon={<CalendarDays size={22} />}
              action={
                <Link href="/appointments" className="btn-ghost btn-sm">
                  <CalendarPlus size={14} />
                  ثبت نوبت
                </Link>
              }
            />
          ) : (
            <div className="space-y-2">
              {todayAppointments.map((item) => (
                <div
                  key={item.id}
                  className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-line bg-white px-3 py-2.5"
                >
                  <div className="flex items-center gap-3">
                    <span className="rounded-lg bg-ivory px-2 py-1 text-[11px] font-medium text-rosedeep">
                      {timeLabel(item.appointment_time)}
                    </span>
                    <div>
                      <Link
                        href={`/customers/${item.customer_id}`}
                        className="text-xs font-bold text-ink hover:text-rosedeep"
                      >
                        {item.first_name} {item.last_name}
                      </Link>
                      <p className="mt-0.5 text-[10px] text-muted">
                        {item.service_name || "بدون خدمت مشخص"}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <StatusBadge status={item.status} />
                    {item.status !== "done" ? (
                      <Link href={`/visits/${item.id}`} className="btn-ghost btn-sm">
                        ثبت نتیجه
                      </Link>
                    ) : (
                      <Link href={`/visits/${item.id}`} className="btn-ghost btn-sm">
                        نتیجه
                      </Link>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <div className="space-y-4">
          <section className="card">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-bold text-ink">
              <ClipboardList size={16} className="text-hot" />
              منتظران آینده
            </h2>
            {waiting.length === 0 ? (
              <p className="py-4 text-center text-[11px] text-muted">
                لیست انتظار خالی است.
              </p>
            ) : (
              <div className="space-y-2">
                {waiting.slice(0, 5).map((item) => (
                  <div
                    key={item.id}
                    className="flex items-center justify-between gap-2 rounded-xl bg-ivory px-3 py-2"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-xs font-medium text-ink">
                        {item.first_name} {item.last_name}
                      </p>
                      <p className="mt-0.5 text-[10px] text-muted">
                        {formatJalali(item.appointment_date)} ·{" "}
                        {timeLabel(item.appointment_time)}
                      </p>
                    </div>
                    <Link
                      href="/appointments"
                      className="shrink-0 text-[10px] text-rosedeep hover:underline"
                    >
                      مدیریت
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="card">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-bold text-ink">
              <Users size={16} className="text-hot" />
              دفترچه مشتریان
            </h2>
            <div className="flex items-end justify-between">
              <div>
                <p className="text-2xl font-bold text-rosedeep">
                  {formatNumber(customers.length)}
                </p>
                <p className="mt-1 text-[11px] text-muted">مشتری ثبت‌شده</p>
              </div>
              <Link href="/customers" className="btn-ghost btn-sm">
                مشاهده
                <ArrowLeft size={13} />
              </Link>
            </div>
          </section>

          <section className="card bg-blush/30">
            <h2 className="mb-2 flex items-center gap-2 text-sm font-bold text-ink">
              <Scissors size={16} className="text-rosedeep" />
              میان‌بر
            </h2>
            <div className="flex flex-wrap gap-1.5">
              <Link href="/appointments" className="btn-ghost btn-sm">
                نوبت‌ها
              </Link>
              <Link href="/appointments" className="btn-ghost btn-sm">
                ثبت نتیجه
              </Link>
              <Link href="/accounting" className="btn-ghost btn-sm">
                حسابداری
              </Link>
              <Link href="/materials" className="btn-ghost btn-sm">
                قیمت مواد
              </Link>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
