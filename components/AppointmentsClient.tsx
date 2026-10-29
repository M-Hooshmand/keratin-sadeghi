"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  CalendarCheck2,
  CalendarPlus,
  ClipboardPlus,
  Clock3,
  Pencil,
  Phone,
  Search,
  Trash2,
  Wallet,
  XCircle,
} from "lucide-react";
import Modal from "./Modal";
import AppointmentForm from "./AppointmentForm";
import { EmptyState, PageHeader, StatusBadge, cn } from "./ui";
import { AppointmentRow, AppointmentStatus, Service } from "@/lib/types";
import {
  formatJalali,
  formatNumber,
  isoToJalali,
  timeLabel,
  weekdayOfISO,
} from "@/lib/jalali";

const TABS: Array<{ key: AppointmentStatus | "all"; label: string }> = [
  { key: "all", label: "همه" },
  { key: "waiting", label: "در انتظار" },
  { key: "done", label: "انجام‌شده" },
  { key: "canceled", label: "لغو‌شده" },
];

export default function AppointmentsClient({
  appointments,
  services,
}: {
  appointments: AppointmentRow[];
  services: Service[];
}) {
  const router = useRouter();
  const [tab, setTab] = useState<AppointmentStatus | "all">("all");
  const [search, setSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<AppointmentRow | null>(null);
  const [busy, setBusy] = useState<number | null>(null);

  const counts = useMemo(() => {
    return {
      all: appointments.length,
      waiting: appointments.filter((a) => a.status === "waiting").length,
      done: appointments.filter((a) => a.status === "done").length,
      canceled: appointments.filter((a) => a.status === "canceled").length,
    };
  }, [appointments]);

  const filtered = useMemo(() => {
    const q = search.trim();
    return appointments.filter((item) => {
      if (tab !== "all" && item.status !== tab) return false;
      if (!q) return true;
      const haystack = `${item.first_name} ${item.last_name} ${item.phone}`;
      return haystack.includes(q);
    });
  }, [appointments, tab, search]);

  function openNew() {
    setEditing(null);
    setModalOpen(true);
  }

  function openEdit(item: AppointmentRow) {
    setEditing(item);
    setModalOpen(true);
  }

  async function patchStatus(item: AppointmentRow, status: AppointmentStatus) {
    if (status === "canceled" && !window.confirm("این نوبت لغو شود؟")) return;
    setBusy(item.id);
    try {
      const response = await fetch(`/api/appointments/${item.id}`, {
        method: "PATCH",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ status }),
      });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      router.refresh();
    } catch {
      window.alert("تغییر وضعیت انجام نشد");
    } finally {
      setBusy(null);
    }
  }

  async function remove(item: AppointmentRow) {
    if (!window.confirm("این نوبت برای همیشه حذف شود؟")) return;
    setBusy(item.id);
    try {
      const response = await fetch(`/api/appointments/${item.id}`, { method: "DELETE" });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      router.refresh();
    } catch {
      window.alert("حذف نوبت انجام نشد");
    } finally {
      setBusy(null);
    }
  }

  return (
    <div>
      <PageHeader
        title="نوبت‌ها"
        subtitle="لیست انتظار، تغییر تاریخ، لغو و ثبت نتیجه نهایی"
        action={
          <button type="button" className="btn-primary" onClick={openNew}>
            <CalendarPlus size={17} />
            نوبت جدید
          </button>
        }
      />

      <div className="card mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div className="flex flex-wrap gap-1.5">
          {TABS.map((item) => (
            <button
              key={item.key}
              type="button"
              onClick={() => setTab(item.key)}
              className={cn(
                "rounded-xl px-3 py-2 text-xs font-medium transition",
                tab === item.key
                  ? "bg-rosedeep text-white"
                  : "bg-white text-muted hover:bg-blush/50 hover:text-rosedeep"
              )}
            >
              {item.label}
              <span className="mr-1.5 rounded-md bg-black/5 px-1.5 py-0.5 text-[10px]">
                {formatNumber(counts[item.key])}
              </span>
            </button>
          ))}
        </div>
        <div className="relative w-full md:w-64">
          <Search size={15} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted" />
          <input
            className="input pr-9"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="جستجوی نام یا شماره..."
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="نوبتی پیدا نشد"
          description="با تغییر فیلترها جستجو کنید یا یک نوبت جدید ثبت کنید."
          icon={<CalendarCheck2 size={22} />}
          action={
            <button type="button" className="btn-ghost" onClick={openNew}>
              <CalendarPlus size={16} />
              ثبت نوبت
            </button>
          }
        />
      ) : (
        <div className="space-y-2.5">
          {filtered.map((item) => {
            const isBusy = busy === item.id;
            return (
              <div
                key={item.id}
                className="card flex flex-col gap-4 p-4 md:flex-row md:items-center md:justify-between"
              >
                <div className="flex items-start gap-3">
                  <div className="flex w-16 shrink-0 flex-col items-center rounded-xl border border-line bg-ivory px-2 py-2 text-center">
                    <span className="text-base font-bold text-rosedeep">
                      {formatNumber(isoToJalali(item.appointment_date).jd)}
                    </span>
                    <span className="text-[10px] text-muted">
                      {weekdayOfISO(item.appointment_date)}
                    </span>
                    <span className="mt-0.5 text-[9px] text-muted">
                      {formatJalali(item.appointment_date).split(" ").slice(1, 3).join(" ")}
                    </span>
                  </div>
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <Link
                        href={`/customers/${item.customer_id}`}
                        className="text-sm font-bold text-ink hover:text-rosedeep"
                      >
                        {item.first_name} {item.last_name}
                      </Link>
                      <StatusBadge status={item.status} />
                    </div>
                    <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-[11px] text-muted">
                      <span className="inline-flex items-center gap-1">
                        <Phone size={12} />
                        <span dir="ltr">{item.phone}</span>
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <Clock3 size={12} />
                        {timeLabel(item.appointment_time)}
                      </span>
                      <span className="inline-flex items-center gap-1">
                        <Wallet size={12} />
                        بیعانه {formatNumber(item.deposit)} تومان
                      </span>
                    </p>
                    <p className="mt-1 text-[11px] text-ink/70">
                      {item.service_name || "خدمت انتخاب نشده"}
                      {item.notes ? ` — ${item.notes}` : ""}
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-1.5 border-t border-line pt-3 md:border-0 md:pt-0">
                  {item.status === "done" && item.visit_id ? (
                    <Link
                      href={`/visits/${item.id}`}
                      className="btn-ghost btn-sm"
                    >
                      <ClipboardPlus size={14} />
                      مشاهده نتیجه
                    </Link>
                  ) : (
                    <Link href={`/visits/${item.id}`} className="btn-primary btn-sm">
                      <ClipboardPlus size={14} />
                      ثبت نتیجه
                    </Link>
                  )}
                  <button
                    type="button"
                    className="btn-ghost btn-sm"
                    onClick={() => openEdit(item)}
                  >
                    <Pencil size={14} />
                    ویرایش
                  </button>
                  {item.status !== "canceled" ? (
                    <button
                      type="button"
                      className="btn-danger btn-sm"
                      disabled={isBusy}
                      onClick={() => patchStatus(item, "canceled")}
                    >
                      <XCircle size={14} />
                      لغو
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="btn-ghost btn-sm"
                      disabled={isBusy}
                      onClick={() => patchStatus(item, "waiting")}
                    >
                      بازگردانی
                    </button>
                  )}
                  <button
                    type="button"
                    className="btn-danger btn-sm"
                    disabled={isBusy}
                    onClick={() => remove(item)}
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? "ویرایش نوبت" : "ثبت نوبت جدید"}
        description="مشتری با شماره تماس شناسایی می‌شود؛ اگر قبلاً ثبت شده باشد، اطلاعاتش به‌روز می‌شود."
        size="lg"
      >
        <AppointmentForm
          appointment={editing}
          services={services}
          onCancel={() => setModalOpen(false)}
          onSaved={() => {
            setModalOpen(false);
            router.refresh();
          }}
        />
      </Modal>
    </div>
  );
}
