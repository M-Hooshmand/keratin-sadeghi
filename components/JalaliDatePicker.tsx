"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";
import {
  MONTH_NAMES,
  WEEKDAYS_SHORT,
  addJalaliMonths,
  formatJalali,
  isoToJalali,
  jalaliMonthLength,
  jalaliToISO,
  toPersianDigits,
  todayISO,
  todayJalali,
  weekdayIndex,
} from "@/lib/jalali";

interface Props {
  value: string;
  onChange: (iso: string) => void;
  disabled?: boolean;
}

type DayInfo = { isHoliday: boolean; title: string | null };

export default function JalaliDatePicker({ value, onChange, disabled }: Props) {
  const [open, setOpen] = useState(false);
  const [view, setView] = useState(() => {
    const j = value ? isoToJalali(value) : todayJalali();
    return { jy: j.jy, jm: j.jm };
  });
  const [days, setDays] = useState<Record<string, DayInfo>>({});
  const [loading, setLoading] = useState(false);
  const boxRef = useRef<HTMLDivElement>(null);
  const today = todayISO();

  useEffect(() => {
    if (!open) return;
    const onClick = (event: MouseEvent) => {
      if (!boxRef.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    let active = true;
    setLoading(true);
    fetch(`/api/holidays?year=${view.jy}&month=${view.jm}`)
      .then((res) => res.json())
      .then((json) => {
        if (!active || !json?.ok) return;
        const map: Record<string, DayInfo> = {};
        for (const day of json.data.days || []) {
          map[day.date] = { isHoliday: day.isHoliday, title: day.title };
        }
        setDays((prev) => ({ ...prev, ...map }));
      })
      .catch(() => undefined)
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [open, view.jy, view.jm]);

  const firstISO = jalaliToISO(view.jy, view.jm, 1);
  const leading = weekdayIndex(firstISO);
  const length = jalaliMonthLength(view.jy, view.jm);

  const cells = useMemo(() => {
    const result: Array<{ iso: string; jd: number } | null> = [];
    for (let i = 0; i < leading; i += 1) result.push(null);
    for (let d = 1; d <= length; d += 1) {
      result.push({ iso: jalaliToISO(view.jy, view.jm, d), jd: d });
    }
    return result;
  }, [leading, length, view.jy, view.jm]);

  return (
    <div className="relative" ref={boxRef}>
      <button
        type="button"
        disabled={disabled}
        onClick={() => {
          if (!open) {
            const j = value ? isoToJalali(value) : todayJalali();
            setView({ jy: j.jy, jm: j.jm });
          }
          setOpen((v) => !v);
        }}
        className="input flex items-center justify-between gap-2 text-right disabled:opacity-60"
      >
        <span className={`truncate ${value ? "text-ink" : "text-muted/60"}`}>
          {value ? formatJalali(value, true) : "انتخاب تاریخ"}
        </span>
        <CalendarDays size={16} className="shrink-0 text-hot" />
      </button>

      {open ? (
        <div className="fade-in absolute z-40 mt-2 w-[19rem] rounded-2xl border border-line bg-white p-3 shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <button
              type="button"
              className="rounded-lg p-1.5 text-muted hover:bg-blush/60 hover:text-rosedeep"
              onClick={() => setView((v) => addJalaliMonths(v.jy, v.jm, -1))}
              aria-label="ماه قبل"
            >
              <ChevronRight size={17} />
            </button>
            <div className="text-center">
              <p className="text-sm font-bold text-ink">
                {MONTH_NAMES[view.jm - 1]} {toPersianDigits(view.jy)}
              </p>
              <p className="text-[10px] text-muted">
                {loading ? "در حال دریافت تعطیلات..." : "شنبه تا جمعه"}
              </p>
            </div>
            <button
              type="button"
              className="rounded-lg p-1.5 text-muted hover:bg-blush/60 hover:text-rosedeep"
              onClick={() => setView((v) => addJalaliMonths(v.jy, v.jm, 1))}
              aria-label="ماه بعد"
            >
              <ChevronLeft size={17} />
            </button>
          </div>

          <div className="grid grid-cols-7 gap-1 text-center">
            {WEEKDAYS_SHORT.map((d, i) => (
              <span
                key={d}
                className={`py-1 text-[10px] ${i === 6 ? "text-rose" : "text-muted"}`}
              >
                {d}
              </span>
            ))}
            {cells.map((cell, index) => {
              if (!cell) return <span key={`blank-${index}`} />;
              const isFriday = weekdayIndex(cell.iso) === 6;
              const isToday = cell.iso === today;
              const selected = cell.iso === value;
              const info = days[cell.iso];
              return (
                <button
                  key={cell.iso}
                  type="button"
                  title={info?.title || undefined}
                  onClick={() => {
                    onChange(cell.iso);
                    setOpen(false);
                  }}
                  className={`relative flex h-9 items-center justify-center rounded-lg text-xs transition ${
                    selected
                      ? "bg-rosedeep font-bold text-white"
                      : isToday
                        ? "bg-blush text-rosedeep font-bold"
                        : "hover:bg-blush/50"
                  } ${isFriday && !selected ? "text-rose" : ""}`}
                >
                  {toPersianDigits(cell.jd)}
                  {info?.isHoliday ? (
                    <span
                      className={`absolute bottom-1 h-1 w-1 rounded-full ${
                        selected ? "bg-white" : "bg-hot"
                      }`}
                    />
                  ) : null}
                </button>
              );
            })}
          </div>

          <div className="mt-2 flex items-center justify-between border-t border-line pt-2">
            <button
              type="button"
              className="rounded-lg px-2 py-1 text-[11px] text-rosedeep hover:bg-blush/50"
              onClick={() => {
                const j = todayJalali();
                setView({ jy: j.jy, jm: j.jm });
                onChange(today);
                setOpen(false);
              }}
            >
              امروز
            </button>
            <span className="text-[10px] text-muted">تعطیلات رسمی با نقطه طلایی</span>
          </div>
        </div>
      ) : null}
    </div>
  );
}
