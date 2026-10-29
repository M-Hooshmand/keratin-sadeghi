"use client";

import { useState } from "react";
import { Loader2, Save } from "lucide-react";
import { Customer } from "@/lib/types";
import { toEnglishDigits } from "@/lib/jalali";

export default function CustomerForm({
  customer,
  onSaved,
  onCancel,
}: {
  customer: Customer | null;
  onSaved: () => void;
  onCancel: () => void;
}) {
  const [firstName, setFirstName] = useState(customer?.first_name ?? "");
  const [lastName, setLastName] = useState(customer?.last_name ?? "");
  const [phone, setPhone] = useState(customer?.phone ?? "");
  const [notes, setNotes] = useState(customer?.notes ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const response = await fetch(
        customer ? `/api/customers/${customer.id}` : "/api/customers",
        {
          method: customer ? "PATCH" : "POST",
          headers: { "content-type": "application/json" },
          body: JSON.stringify({
            firstName,
            lastName,
            phone: toEnglishDigits(phone),
            notes,
          }),
        }
      );
      const json = await response.json();
      if (!json.ok) throw new Error(json.error || "ثبت مشتری انجام نشد");
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
            required
          />
        </div>
        <div>
          <label className="label">نام خانوادگی</label>
          <input
            className="input"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
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
        <label className="label">یادداشت</label>
        <textarea
          className="input min-h-20 resize-y"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          placeholder="نوع مو، حساسیت، توضیحات..."
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
          {customer ? "ذخیره تغییرات" : "افزودن مشتری"}
        </button>
      </div>
    </form>
  );
}
