"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import {
  CalendarDays,
  FlaskConical,
  Heart,
  LayoutDashboard,
  Menu,
  Scissors,
  Sparkles,
  Users,
  Wallet,
  X,
} from "lucide-react";

const NAV = [
  { href: "/", label: "داشبورد", icon: LayoutDashboard },
  { href: "/appointments", label: "نوبت‌ها", icon: CalendarDays },
  { href: "/customers", label: "مشتریان", icon: Users },
  { href: "/accounting", label: "حسابداری", icon: Wallet },
  { href: "/materials", label: "مواد و قیمت‌ها", icon: FlaskConical },
];

function Brand() {
  return (
    <Link href="/" className="flex items-center gap-3">
      <span className="relative flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-[#ff7bb8] via-hot to-[#8f1f50] text-white shadow-[0_12px_26px_-12px_rgba(255,44,146,0.95)] ring-2 ring-white/70">
        <Scissors size={19} />
        <span className="absolute -left-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-white text-hot shadow-sm">
          <Sparkles size={9} />
        </span>
      </span>
      <span className="leading-tight">
        <span className="block text-sm font-extrabold text-ink">
          سالن کراتین خانم صادقی
        </span>
        <span className="mt-1 flex items-center gap-1 text-[10px] font-medium text-hot">
          <Sparkles size={10} />
          پنل باربی‌کور
        </span>
      </span>
    </Link>
  );
}

function NavLinks({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  return (
    <nav className="flex flex-col gap-1.5">
      {NAV.map((item) => {
        const active =
          item.href === "/"
            ? pathname === "/"
            : pathname.startsWith(item.href);
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            onClick={onNavigate}
            className={`group flex items-center gap-3 rounded-2xl px-3.5 py-2.5 text-sm transition ${
              active
                ? "bg-gradient-to-l from-[#ff4fa5] via-[#f2338d] to-[#a8215d] font-bold text-white shadow-[0_14px_26px_-14px_rgba(255,44,146,0.95)]"
                : "text-ink/75 hover:bg-white/75 hover:text-rosedeep"
            }`}
          >
            <Icon
              size={18}
              className={
                active ? "text-white" : "text-hot/75 group-hover:text-rosedeep"
              }
            />
            <span>{item.label}</span>
            {active ? (
              <Heart size={12} className="mr-auto fill-white/80 text-white/80" />
            ) : null}
          </Link>
        );
      })}
    </nav>
  );
}

function PromoCard() {
  return (
    <Link
      href="/appointments"
      className="group relative mb-3 block overflow-hidden rounded-[24px] border border-white/70 bg-gradient-to-br from-[#ff4fa5] via-[#d42c7a] to-[#7d1745] p-4 text-white shadow-[0_20px_36px_-24px_rgba(176,42,102,0.9)] transition duration-300 hover:-translate-y-0.5"
    >
      <span className="checker-soft pointer-events-none absolute inset-0 opacity-30" />
      <span className="relative block">
        <span className="flex items-center justify-between gap-2">
          <span className="text-[9px] font-semibold uppercase tracking-[0.18em] text-[#ffd3e6]">
            pin of the day
          </span>
          <span className="flex h-7 w-7 items-center justify-center rounded-full border border-white/50 bg-white/15 backdrop-blur">
            <Heart size={12} className="fill-white/90" />
          </span>
        </span>
        <span className="mt-5 block text-sm font-extrabold leading-6">
          نوبت بعدی‌ات را
          <br />
          صورتی کن
        </span>
        <span className="mt-1 block text-[10px] leading-5 text-white/80">
          رزرو سریع برای درخشش موهایت
        </span>
        <span className="mt-4 flex items-center gap-1 text-[10px] font-bold text-[#ffd6e8]">
          ثبت نوبت
          <Sparkles
            size={12}
            className="transition group-hover:-translate-x-0.5"
          />
        </span>
      </span>
    </Link>
  );
}

function AsideBody({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <>
      <Brand />
      <div className="mt-8">
        <NavLinks onNavigate={onNavigate} />
      </div>
      <div className="mt-auto pt-6">
        <PromoCard />
        <div className="relative overflow-hidden rounded-2xl border border-white/80 bg-white/60 p-3 text-[10px] leading-6 text-muted backdrop-blur">
          <span className="mb-1 flex items-center gap-1 text-[10px] font-bold text-hot">
            <Heart size={11} className="fill-hot/20" />
            یادداشت پین
          </span>
          داده‌ها روی همین دستگاه ذخیره می‌شوند. برای پشتیبان‌گیری از پوشه
          <span className="mx-1 font-mono text-ink/70">data</span>
          کپی بگیرید.
        </div>
      </div>
    </>
  );
}

export default function Sidebar() {
  const [open, setOpen] = useState(false);

  return (
    <>
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-line/80 bg-gradient-to-l from-white/90 via-[#fff4fa]/90 to-blush/80 px-4 py-3 backdrop-blur-xl lg:hidden">
        <Brand />
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="rounded-2xl border border-white/80 bg-gradient-to-br from-white to-blush p-2 text-hot shadow-sm"
          aria-label="باز کردن منو"
        >
          <Menu size={20} />
        </button>
      </header>

      <aside className="fixed inset-y-0 right-0 z-30 hidden w-64 flex-col overflow-hidden border-l border-white/80 bg-gradient-to-b from-white/95 via-[#fff4fa]/90 to-[#f3e3ff]/80 p-5 backdrop-blur-xl lg:flex">
        <span className="checker-soft pointer-events-none absolute inset-x-0 bottom-0 h-32 opacity-45" />
        <span className="pointer-events-none absolute -left-16 top-40 h-48 w-48 rounded-full bg-[#ff8fc4]/20 blur-3xl" />
        <div className="relative flex h-full flex-col">
          <AsideBody />
        </div>
      </aside>

      {open ? (
        <div className="fixed inset-0 z-40 lg:hidden">
          <button
            type="button"
            aria-label="بستن منو"
            className="absolute inset-0 bg-[#3b2030]/40 backdrop-blur-sm"
            onClick={() => setOpen(false)}
          />
          <div className="fade-in absolute inset-y-0 right-0 flex w-72 flex-col overflow-hidden border-l border-white/80 bg-gradient-to-b from-white via-[#fff8fc] to-blush/70 p-5 shadow-2xl">
            <span className="checker-soft pointer-events-none absolute inset-x-0 bottom-0 h-28 opacity-40" />
            <div className="relative flex h-full flex-col">
              <div className="flex items-center justify-between">
                <Brand />
                <button
                  type="button"
                  onClick={() => setOpen(false)}
                  className="rounded-full p-1.5 text-muted hover:bg-blush/70"
                  aria-label="بستن"
                >
                  <X size={20} />
                </button>
              </div>
              <div className="mt-8">
                <NavLinks onNavigate={() => setOpen(false)} />
              </div>
              <div className="mt-auto pt-6">
                <PromoCard />
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
