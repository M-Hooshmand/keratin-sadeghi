"use client";

import { useMemo, useState } from "react";
import { Loader2, Save } from "lucide-react";
import JalaliDatePicker from "./JalaliDatePicker";
import {
  AppointmentRow,
  AppointmentStatus,
  Service,
  isSplitEndService,
} from "@/lib/types";
import {
  formatNumber,
  parseNumber,
  toEnglishDigits,
  todayISO,
} from "@/lib/jalali";

const TIMES = (() => {
  const list: string[] = [];
  for (let h = 9; h <= 21; h += 1) {
    for (const m of ["00", "30"]) {
      if (h === 21 && m === "30") continue;
      list.push(`${String(h).padStart(2, "0")}:${m}`);
    }
  }
  return list;
})();

export default function AppointmentForm({
  appointment,
  services,
  defaultDate,
  onSaved,
  onCancel,
}: {
  appointment: AppointmentRow | null;
  services: Service[];
  defaultDate?: string;
  onSaved: () => void;
  onCancel: () => void;
}) {
  const [firstName, setFirstName] = useState(appointment?.first_name ?? "");
  const [lastName, setLastName] = useState(appointment?.last_name ?? "");
  const [phone, setPhone] = useState(appointment?.phone ?? "");
  const [serviceId, setServiceId] = useState(
    appointment?.service_id ? String(appointment.service_id) : ""
  );
  const [date, setDate] = useState(
    appointment?.appointment_date ?? defaultDate ?? todayISO()
  );
  const [time, setTime] = useState(appointment?.appointment_time ?? "10:00");
  const [deposit, setDeposit] = useState(
    appointment?.deposit ? String(appointment.deposit) : ""
  );
  const [notes, setNotes] = useState(appointment?.notes ?? "");
  const [status, setStatus] = useState<AppointmentStatus>(
    appointment?.status ?? "waiting"
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const times = useMemo(() => {
    if (time && !TIMES.includes(time)) return [...TIMES, time].sort();
    return TIMES;
  }, [time]);

  const selectedService = services.find(
    (service) => String(service.id) === serviceId
  );
  const splitEnd = isSplitEndService(selectedService?.name);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const response = await fetch(
        appointment ? `/api/appointments/${appointment.id}` : "/api/appointments",
        {
          method: appointment ? "PATCH" : "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify({
            firstName,
            lastName,
            phone: toEnglishDigits(phone),
            serviceId: serviceId ? Number(serviceId) : null,
            date,
            time,
            deposit: Math.round(parseNumber(deposit)),
            notes,
            status,
          }),
        }
      );
      const json = await response.json();
      if (!json.ok) throw new Error(json.error || "ثبت نوبت انجام نشد");
      onSaved();
    } catch (err) {
      setError(err instanceof Error ? err.message : "خطای نامشخص");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label">نام</label>
          <input
            className="input"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            placeholder="مثلاً سارا"
            required
          />
        </div>
        <div>
          <label className="label">نام خانوادگی</label>
          <input
            className="input"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            placeholder="مثلاً محمدی"
            required
          />
        </div>
      </div>

      <div>
        <label className="label">شماره تماس</label>
        <input
          className="input text-left"
          dir="ltr"
          inputMode="tel"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          placeholder="09xxxxxxxxx"
          required
        />
      </div>

      <div>
        <label className="label">خدمت</label>
        <select
          className="input"
          value={serviceId}
          onChange={(e) => setServiceId(e.target.value)}
        >
          <option value="">انتخاب نشده</option>
          {services.map((service) => (
            <option key={service.id} value={service.id}>
              {service.name}
            </option>
          ))}
        </select>
        {splitEnd ? (
          <p className="mt-2 rounded-xl border border-[#f3c5d8] bg-blush/40 px-3 py-2 text-[11px] leading-5 text-rosedeep">
            موخوره‌گیری نرخ واحد و مواد اولیه ندارد؛ مبلغ نهایی بعد از انجام کار و بر
            اساس مبلغ دریافت‌شده ثبت می‌شود. قد و حجم مو در نتیجه نوبت الزامی است.
          </p>
        ) : null}
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label">تاریخ مراجعه</label>
          <JalaliDatePicker value={date} onChange={setDate} />
        </div>
        <div>
          <label className="label">ساعت</label>
          <select
            className="input"
            value={time}
            onChange={(e) => setTime(e.target.value)}
          >
            {times.map((slot) => (
              <option key={slot} value={slot}>
                {toEnglishDigits(slot)}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <label className="label">بیعانه پرداختی (تومان)</label>
          <input
            className="input text-left"
            dir="ltr"
            inputMode="numeric"
            value={deposit}
            onChange={(e) => setDeposit(e.target.value)}
            placeholder="0"
          />
          {deposit ? (
            <p className="mt-1 text-[11px] text-muted">
              {formatNumber(parseNumber(deposit))} تومان
            </p>
          ) : null}
        </div>
        {appointment ? (
          <div>
            <label className="label">وضعیت</label>
            <select
              className="input"
              value={status}
              onChange={(e) => setStatus(e.target.value as AppointmentStatus)}
            >
              <option value="waiting">در لیست انتظار</option>
              <option value="done">انجام‌شده</option>
              <option value="canceled">لغو‌شده</option>
            </select>
          </div>
        ) : (
          <div className="rounded-xl border border-[#ead7ac] bg-[#fbf1dd] px-3 py-2 text-[11px] leading-5 text-[#8a6a1f]">
            نوبت جدید به‌صورت خودکار در لیست انتظار ثبت می‌شود.
          </div>
        )}
      </div>

      <div>
        <label className="label">یادداشت</label>
        <textarea
          className="input min-h-20 resize-y"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="توضیحات، حساسیت، درخواست خاص..."
        />
      </div>

      {error ? (
        <p className="rounded-xl border border-[#efc9c9] bg-[#fbeaea] px-3 py-2 text-xs text-[#8e3b3b]">
          {error}
        </p>
      ) : null}

      <div className="flex items-center justify-end gap-2 border-t border-line pt-3">
        <button type="button" className="btn-ghost" onClick={onCancel}>
          انصراف
        </button>
        <button type="submit" className="btn-primary" disabled={saving}>
          {saving ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
          {appointment ? "ذخیره تغییرات" : "ثبت نوبت"}
        </button>
      </div>
    </form>
  );
}
