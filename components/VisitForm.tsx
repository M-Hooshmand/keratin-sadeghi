"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  ArrowRight,
  Calculator,
  FlaskConical,
  Loader2,
  Plus,
  Save,
  Scissors,
  Trash2,
  Wallet,
} from "lucide-react";
import JalaliDatePicker from "./JalaliDatePicker";
import {
  AppointmentRow,
  HAIR_VOLUME_OPTIONS,
  isSplitEndService,
  Material,
  MaterialUsage,
  Service,
  VisitRow,
} from "@/lib/types";
import {
  formatMl,
  formatNumber,
  formatToman,
  parseNumber,
  toEnglishDigits,
} from "@/lib/jalali";

interface Row {
  key: number;
  materialId: string;
  amount: string;
}

export default function VisitForm({
  appointment,
  services,
  materials,
  initialVisit,
  initialUsages,
}: {
  appointment: AppointmentRow;
  services: Service[];
  materials: Material[];
  initialVisit: VisitRow | null;
  initialUsages: MaterialUsage[];
}) {
  const router = useRouter();
  const [visitId, setVisitId] = useState<number | null>(initialVisit?.id ?? null);
  const [serviceId, setServiceId] = useState(
    String(initialVisit?.service_id ?? appointment.service_id ?? "")
  );
  const [performedDate, setPerformedDate] = useState(
    initialVisit?.performed_date ?? appointment.appointment_date
  );
  const [finalAmount, setFinalAmount] = useState(
    initialVisit ? String(initialVisit.final_amount) : ""
  );
  const [hairLength, setHairLength] = useState(
    initialVisit?.hair_length_cm != null ? String(initialVisit.hair_length_cm) : ""
  );
  const [hairVolume, setHairVolume] = useState(initialVisit?.hair_volume ?? "");
  const [hairCondition, setHairCondition] = useState(initialVisit?.hair_condition ?? "");
  const [workDone, setWorkDone] = useState(initialVisit?.work_done ?? "");
  const [notes, setNotes] = useState(initialVisit?.notes ?? "");
  const [rows, setRows] = useState<Row[]>(() =>
    initialUsages.map((usage, index) => ({
      key: index + 1,
      materialId: String(usage.material_id),
      amount: String(usage.amount),
    }))
  );
  const [nextKey, setNextKey] = useState(initialUsages.length + 1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);

  const materialMap = useMemo(
    () => new Map(materials.map((material) => [material.id, material])),
    [materials]
  );

  const materialCost = useMemo(() => {
    return rows.reduce((sum, row) => {
      const material = materialMap.get(Number(row.materialId));
      if (!material) return sum;
      return sum + parseNumber(row.amount) * material.price_per_unit;
    }, 0);
  }, [rows, materialMap]);

  const selectedService = services.find((service) => String(service.id) === serviceId);
  const splitEnd = isSplitEndService(selectedService?.name);
  const effectiveMaterialCost = splitEnd ? 0 : materialCost;

  const total = parseNumber(finalAmount);
  const profit = total - effectiveMaterialCost;
  const deposit = appointment.deposit;

  function addRow() {
    const used = new Set(rows.map((row) => row.materialId));
    const firstFree = materials.find((material) => !used.has(String(material.id)));
    setRows((prev) => [
      ...prev,
      {
        key: nextKey,
        materialId: firstFree ? String(firstFree.id) : "",
        amount: "",
      },
    ]);
    setNextKey((v) => v + 1);
  }

  function updateRow(key: number, patch: Partial<Row>) {
    setRows((prev) => prev.map((row) => (row.key === key ? { ...row, ...patch } : row)));
  }

  function removeRow(key: number) {
    setRows((prev) => prev.filter((row) => row.key !== key));
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setSaved(false);
    if (splitEnd) {
      if (!hairLength || parseNumber(hairLength) <= 0) {
        setError("برای موخوره‌گیری، قد مو الزامی است");
        return;
      }
      if (!hairVolume) {
        setError("برای موخوره‌گیری، حجم مو الزامی است");
        return;
      }
    }
    setSaving(true);
    try {
      const payload = {
        appointmentId: appointment.id,
        serviceId: serviceId ? Number(serviceId) : null,
        performedDate,
        finalAmount: Math.round(total),
        hairLengthCm: hairLength === "" ? null : parseNumber(hairLength),
        hairVolume,
        hairCondition,
        workDone,
        notes,
        materials: splitEnd
          ? []
          : rows
              .filter((row) => row.materialId && parseNumber(row.amount) > 0)
              .map((row) => ({
                materialId: Number(row.materialId),
                amount: parseNumber(row.amount),
              })),
      };
      const response = await fetch(visitId ? `/api/visits/${visitId}` : "/api/visits", {
        method: visitId ? "PATCH" : "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(payload),
      });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error || "ثبت نتیجه انجام نشد");
      if (json.data?.id) setVisitId(json.data.id);
      setSaved(true);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "خطای نامشخص");
    } finally {
      setSaving(false);
    }
  }

  async function removeVisit() {
    if (!visitId) return;
    if (!window.confirm("نتیجه این نوبت حذف شود و نوبت به لیست انتظار برگردد؟")) return;
    setSaving(true);
    try {
      const response = await fetch(`/api/visits/${visitId}`, { method: "DELETE" });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      router.push("/appointments");
    } catch {
      setError("حذف نتیجه انجام نشد");
      setSaving(false);
    }
  }

  return (
    <form onSubmit={submit} className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link href="/appointments" className="btn-ghost btn-sm">
          <ArrowRight size={14} />
          بازگشت به نوبت‌ها
        </Link>
        <div className="text-left text-xs text-muted">
          {appointment.first_name} {appointment.last_name} —{" "}
          <span dir="ltr">{appointment.phone}</span>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1fr_340px]">
        <div className="space-y-4">
          <section className="card space-y-4">
            <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
              <Scissors size={16} className="text-hot" />
              مشخصات کار انجام‌شده
            </h2>

            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label className="label">خدمت انجام‌شده</label>
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
              </div>
              <div>
                <label className="label">تاریخ انجام کار</label>
                <JalaliDatePicker value={performedDate} onChange={setPerformedDate} />
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              <div>
                <label className="label">طول موی مشتری (سانتی‌متر){splitEnd ? " *" : ""}</label>
                <input
                  className="input text-left"
                  dir="ltr"
                  inputMode="decimal"
                  value={hairLength}
                  onChange={(e) => setHairLength(e.target.value)}
                  placeholder="مثلاً 45"
                />
              </div>
              <div>
                <label className="label">حجم مو{splitEnd ? " *" : ""}</label>
                <select
                  className="input"
                  value={hairVolume}
                  onChange={(e) => setHairVolume(e.target.value)}
                >
                  <option value="">انتخاب کنید</option>
                  {HAIR_VOLUME_OPTIONS.map((option) => (
                    <option key={option} value={option}>
                      {option}
                    </option>
                  ))}
                  {hairVolume && !HAIR_VOLUME_OPTIONS.includes(hairVolume) ? (
                    <option value={hairVolume}>{hairVolume}</option>
                  ) : null}
                </select>
              </div>
              <div>
                <label className="label">مشکل / وضعیت مو</label>
                <input
                  className="input"
                  value={hairCondition}
                  onChange={(e) => setHairCondition(e.target.value)}
                  placeholder="خشکی، دکلره، کشسانی، آسیب..."
                />
              </div>
            </div>

            {splitEnd ? (
              <p className="rounded-xl border border-[#f3c5d8] bg-blush/40 px-3 py-2 text-[11px] leading-5 text-rosedeep">
                برای موخوره‌گیری، ثبت قد و حجم مو الزامی است. این خدمت نرخ واحد و مصرف
                مواد ندارد؛ مبلغ نهایی را بر اساس مبلغ دریافت‌شده وارد کنید.
              </p>
            ) : null}

            <div>
              <label className="label">شرح کاری که انجام شد</label>
              <textarea
                className="input min-h-24 resize-y"
                value={workDone}
                onChange={(e) => setWorkDone(e.target.value)}
                placeholder="مثلاً کراتین نچرال روی موی رنگ‌شده، بدون دکلره..."
              />
            </div>

            <div>
              <label className="label">یادداشت نتیجه</label>
              <textarea
                className="input min-h-16 resize-y"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="توصیه‌ها، نکات جلسه بعد..."
              />
            </div>
          </section>

          {splitEnd ? (
            <section className="card">
              <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
                <FlaskConical size={16} className="text-hot" />
                مصرف مواد
              </h2>
              <p className="mt-3 rounded-xl border border-dashed border-line bg-ivory px-3 py-4 text-center text-[11px] leading-5 text-muted">
                موخوره‌گیری به روش روسی با قیچی مواد مصرفی ندارد و هزینه مواد برای این
                خدمت صفر ثبت می‌شود.
              </p>
            </section>
          ) : (
          <section className="card">
            <div className="mb-3 flex items-center justify-between">
              <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
                <FlaskConical size={16} className="text-hot" />
                مصرف مواد
              </h2>
              <button type="button" className="btn-ghost btn-sm" onClick={addRow}>
                <Plus size={14} />
                افزودن ماده
              </button>
            </div>

            {rows.length === 0 ? (
              <p className="rounded-xl border border-dashed border-line bg-ivory px-3 py-4 text-center text-[11px] text-muted">
                هنوز ماده‌ای ثبت نشده است. با دکمه «افزودن ماده» مصرف پروتئین، کراتین،
                بسترساز و شامپو را وارد کنید.
              </p>
            ) : (
              <div className="space-y-2">
                {rows.map((row) => {
                  const material = materialMap.get(Number(row.materialId));
                  const rowCost = material
                    ? parseNumber(row.amount) * material.price_per_unit
                    : 0;
                  return (
                    <div
                      key={row.key}
                      className="grid grid-cols-[1fr_88px_auto] items-center gap-2 rounded-xl border border-line bg-white p-2"
                    >
                      <select
                        className="input py-2 text-xs"
                        value={row.materialId}
                        onChange={(e) => updateRow(row.key, { materialId: e.target.value })}
                      >
                        <option value="">انتخاب ماده</option>
                        {materials.map((material) => (
                          <option key={material.id} value={material.id}>
                            {material.name}
                            {material.active ? "" : " (غیرفعال)"}
                          </option>
                        ))}
                      </select>
                      <input
                        className="input py-2 text-left text-xs"
                        dir="ltr"
                        inputMode="decimal"
                        value={row.amount}
                        onChange={(e) => updateRow(row.key, { amount: e.target.value })}
                        placeholder="میل"
                      />
                      <div className="flex items-center gap-1">
                        <span className="w-16 text-left text-[10px] text-muted" dir="ltr">
                          {material ? formatNumber(rowCost) : "-"}
                        </span>
                        <button
                          type="button"
                          className="rounded-lg p-1.5 text-[#8e3b3b] hover:bg-[#fbeaea]"
                          onClick={() => removeRow(row.key)}
                          aria-label="حذف ماده"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                      {material ? (
                        <p className="col-span-3 -mt-1 pr-1 text-[10px] text-muted">
                          {formatMl(parseNumber(row.amount) || 0)} ×{" "}
                          {formatNumber(material.price_per_unit)} تومان هر{" "}
                          {material.unit === "ml" ? "میل" : material.unit}
                        </p>
                      ) : null}
                    </div>
                  );
                })}
              </div>
            )}
            <p className="mt-3 text-[10px] leading-5 text-muted">
              قیمت هر ماده در لحظه ثبت در سابقه ذخیره می‌شود؛ تغییر قیمت‌ها در بخش مواد،
              حسابداری گذشته را تغییر نمی‌دهد.
            </p>
          </section>
          )}
        </div>

        <div className="space-y-4">
          <section className="card space-y-3">
            <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
              <Wallet size={16} className="text-hot" />
              مبلغ نهایی
            </h2>
            <div>
              <label className="label">مبلغ نهایی دریافت‌شده (تومان)</label>
              <input
                className="input text-left text-base font-bold"
                dir="ltr"
                inputMode="numeric"
                value={finalAmount}
                onChange={(e) => setFinalAmount(e.target.value)}
                placeholder="0"
              />
              <p className="mt-1 text-[11px] text-muted">
                {formatNumber(total)} تومان
              </p>
              {splitEnd ? (
                <p className="mt-1 text-[10px] leading-5 text-rosedeep">
                  مبلغ موخوره‌گیری نرخ ثابت ندارد؛ مبلغ دریافت‌شده را وارد کنید.
                </p>
              ) : null}
            </div>
            <div className="rounded-xl bg-ivory p-3 text-[11px] leading-6 text-muted">
              بیعانه ثبت‌شده: {formatToman(deposit)}
              <br />
              مانده تقریبی: {formatToman(Math.max(0, total - deposit))}
            </div>
          </section>

          <section className="card space-y-2 bg-blush/30">
            <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
              <Calculator size={16} className="text-rosedeep" />
              جمع‌بندی این نوبت
            </h2>
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted">هزینه مواد</span>
              <span className="font-medium">
                {splitEnd ? "ندارد" : formatToman(effectiveMaterialCost)}
              </span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-muted">مبلغ نهایی</span>
              <span className="font-medium">{formatToman(total)}</span>
            </div>
            <div className="mt-1 flex items-center justify-between border-t border-line pt-2 text-sm">
              <span className="font-medium text-ink">سود ناخالص</span>
              <span className={profit >= 0 ? "font-bold text-[#2f6b4f]" : "font-bold text-[#8e3b3b]"}>
                {formatToman(profit)}
              </span>
            </div>
          </section>

          {error ? (
            <p className="rounded-xl border border-[#efc9c9] bg-[#fbeaea] px-3 py-2 text-xs text-[#8e3b3b]">
              {error}
            </p>
          ) : null}
          {saved ? (
            <p className="rounded-xl border border-[#c4e0cf] bg-[#e8f3ec] px-3 py-2 text-xs text-[#2f6b4f]">
              نتیجه با موفقیت ذخیره شد.
            </p>
          ) : null}

          <button type="submit" className="btn-primary w-full" disabled={saving}>
            {saving ? <Loader2 size={16} className="animate-spin" /> : <Save size={16} />}
            ذخیره نتیجه نهایی
          </button>

          {visitId ? (
            <button
              type="button"
              className="btn-danger w-full"
              disabled={saving}
              onClick={removeVisit}
            >
              <Trash2 size={16} />
              حذف نتیجه و بازگشت به انتظار
            </button>
          ) : null}

          <p className="text-center text-[10px] leading-5 text-muted">
            با ذخیره نتیجه، وضعیت نوبت به «انجام‌شده» تغییر می‌کند.
          </p>
        </div>
      </div>
    </form>
  );
}
