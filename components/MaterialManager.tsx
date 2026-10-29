"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  Archive,
  Check,
  FlaskConical,
  Loader2,
  Plus,
  Save,
  Trash2,
} from "lucide-react";
import { PageHeader } from "./ui";
import { Material } from "@/lib/types";
import { formatNumber, parseNumber } from "@/lib/jalali";

interface Row {
  id: number;
  name: string;
  price: string;
  unit: string;
  active: boolean;
}

export default function MaterialManager({ materials }: { materials: Material[] }) {
  const router = useRouter();
  const [rows, setRows] = useState<Row[]>(() =>
    materials.map((material) => ({
      id: material.id,
      name: material.name,
      price: String(material.price_per_unit),
      unit: material.unit,
      active: material.active === 1,
    }))
  );
  const [savingId, setSavingId] = useState<number | null>(null);
  const [savedId, setSavedId] = useState<number | null>(null);
  const [newName, setNewName] = useState("");
  const [newPrice, setNewPrice] = useState("");
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function update(id: number, patch: Partial<Row>) {
    setRows((prev) => prev.map((row) => (row.id === id ? { ...row, ...patch } : row)));
  }

  async function save(row: Row) {
    setSavingId(row.id);
    setError(null);
    try {
      const response = await fetch(`/api/materials/${row.id}`, {
        method: "PATCH",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          name: row.name,
          pricePerUnit: Math.round(parseNumber(row.price)),
          unit: row.unit,
          active: row.active,
        }),
      });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      setSavedId(row.id);
      window.setTimeout(() => setSavedId((v) => (v === row.id ? null : v)), 1800);
    } catch (err) {
      setError(err instanceof Error ? err.message : "ذخیره ماده انجام نشد");
    } finally {
      setSavingId(null);
    }
  }

  async function remove(row: Row) {
    if (!window.confirm(`ماده «${row.name}» حذف یا بایگانی شود؟`)) return;
    try {
      const response = await fetch(`/api/materials/${row.id}`, { method: "DELETE" });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      if (json.data.archived) {
        update(row.id, { active: false });
      } else {
        setRows((prev) => prev.filter((item) => item.id !== row.id));
      }
      router.refresh();
    } catch {
      setError("حذف ماده انجام نشد");
    }
  }

  async function add(event: React.FormEvent) {
    event.preventDefault();
    setAdding(true);
    setError(null);
    try {
      const response = await fetch("/api/materials", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          name: newName,
          pricePerUnit: Math.round(parseNumber(newPrice)),
          unit: "ml",
        }),
      });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      setRows((prev) => [
        ...prev,
        {
          id: json.data.id,
          name: newName,
          price: String(Math.round(parseNumber(newPrice))),
          unit: "ml",
          active: true,
        },
      ]);
      setNewName("");
      setNewPrice("");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "افزودن ماده انجام نشد");
    } finally {
      setAdding(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="مواد و قیمت‌ها"
        subtitle="قیمت هر میل هر ماده را دقیق وارد کنید تا هزینه مواد و سود درست محاسبه شود"
      />

      <div className="card mb-4 border-hot/20 bg-blush/50 text-[11px] leading-6 text-rosedeep">
        قیمت هر میل در لحظه «ثبت نتیجه نهایی» برای همان نوبت ذخیره می‌شود. تغییر قیمت
        امروز، حسابداری گذشته را تغییر نمی‌دهد.
      </div>

      {error ? (
        <p className="mb-3 rounded-xl border border-[#efc9c9] bg-[#fbeaea] px-3 py-2 text-xs text-[#8e3b3b]">
          {error}
        </p>
      ) : null}

      <div className="space-y-2.5">
        {rows.map((row) => (
          <div
            key={row.id}
            className={`card flex flex-col gap-3 p-3 md:flex-row md:items-end ${
              row.active ? "" : "opacity-60"
            }`}
          >
            <div className="flex-1">
              <label className="label">نام ماده</label>
              <input
                className="input py-2"
                value={row.name}
                onChange={(e) => update(row.id, { name: e.target.value })}
              />
            </div>
            <div className="w-full md:w-44">
              <label className="label">قیمت هر میل (تومان)</label>
              <input
                className="input py-2 text-left"
                dir="ltr"
                inputMode="numeric"
                value={row.price}
                onChange={(e) => update(row.id, { price: e.target.value })}
              />
              <p className="mt-1 text-[10px] text-muted">
                {formatNumber(parseNumber(row.price))} تومان
              </p>
            </div>
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                className={`btn btn-sm ${
                  row.active
                    ? "border border-[#c4e0cf] bg-[#e8f3ec] text-[#2f6b4f]"
                    : "border border-line bg-white text-muted"
                }`}
                onClick={() => update(row.id, { active: !row.active })}
              >
                <Check size={13} />
                {row.active ? "فعال" : "غیرفعال"}
              </button>
              <button
                type="button"
                className="btn-primary btn-sm"
                onClick={() => save(row)}
                disabled={savingId === row.id}
              >
                {savingId === row.id ? (
                  <Loader2 size={13} className="animate-spin" />
                ) : savedId === row.id ? (
                  <Check size={13} />
                ) : (
                  <Save size={13} />
                )}
                ذخیره
              </button>
              <button
                type="button"
                className="btn-danger btn-sm"
                onClick={() => remove(row)}
                aria-label="حذف ماده"
              >
                {row.active ? <Trash2 size={13} /> : <Archive size={13} />}
              </button>
            </div>
          </div>
        ))}
      </div>

      <form onSubmit={add} className="card mt-4 space-y-3">
        <h2 className="flex items-center gap-2 text-sm font-bold text-ink">
          <FlaskConical size={16} className="text-gold" />
          افزودن ماده جدید
        </h2>
        <div className="grid gap-3 sm:grid-cols-[1fr_180px_auto] sm:items-end">
          <div>
            <label className="label">نام ماده</label>
            <input
              className="input"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              placeholder="مثلاً روغن آرگان"
              required
            />
          </div>
          <div>
            <label className="label">قیمت هر میل (تومان)</label>
            <input
              className="input text-left"
              dir="ltr"
              inputMode="numeric"
              value={newPrice}
              onChange={(e) => setNewPrice(e.target.value)}
              placeholder="0"
            />
          </div>
          <button type="submit" className="btn-gold" disabled={adding}>
            {adding ? <Loader2 size={16} className="animate-spin" /> : <Plus size={16} />}
            افزودن
          </button>
        </div>
      </form>
    </div>
  );
}
