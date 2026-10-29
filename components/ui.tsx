import { ReactNode } from "react";
import { Sparkles } from "lucide-react";
import { STATUS_LABELS, STATUS_STYLES, AppointmentStatus } from "@/lib/types";

export function cn(...classes: Array<string | false | null | undefined>) {
  return classes.filter(Boolean).join(" ");
}

export function PageHeader({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <span className="barbie-sticker mb-2.5">
          <Sparkles size={11} />
          pin board
        </span>
        <h1 className="text-xl font-extrabold tracking-tight text-ink md:text-2xl">
          {title}
        </h1>
        {subtitle ? (
          <p className="mt-1.5 text-sm text-muted">{subtitle}</p>
        ) : null}
      </div>
      {action}
    </div>
  );
}

const TONES: Record<string, string> = {
  rose: "bg-gradient-to-br from-[#ff8fc4] via-[#ff4fa5] to-[#b02a66] text-white shadow-[0_12px_24px_-12px_rgba(255,44,146,0.9)]",
  gold: "bg-gradient-to-br from-[#ffe1ef] to-[#f3d6ff] text-[#a62a68]",
  green: "bg-gradient-to-br from-[#e5f7ee] to-[#d8f0e6] text-[#2f6b4f]",
  plain: "bg-gradient-to-br from-white to-[#ffe9f3] text-hot",
};

export function StatCard({
  label,
  value,
  hint,
  icon,
  tone = "plain",
}: {
  label: string;
  value: string;
  hint?: string;
  icon?: ReactNode;
  tone?: keyof typeof TONES | string;
}) {
  return (
    <div className="pin-card group overflow-hidden p-4">
      <span className="pointer-events-none absolute -left-10 -top-10 h-24 w-24 rounded-full bg-[#ff8fc4]/15 blur-2xl" />
      <div className="relative flex items-start justify-between gap-3">
        <div>
          <p className="text-[11px] font-medium text-muted">{label}</p>
          <p className="mt-2 text-lg font-extrabold text-ink md:text-xl">
            {value}
          </p>
          {hint ? (
            <p className="mt-1 text-[10px] text-muted">{hint}</p>
          ) : null}
        </div>
        {icon ? (
          <span
            className={cn(
              "flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl ring-2 ring-white/70 transition duration-300 group-hover:-rotate-6",
              TONES[tone] ?? TONES.plain
            )}
          >
            {icon}
          </span>
        ) : null}
      </div>
    </div>
  );
}

export function StatusBadge({ status }: { status: AppointmentStatus }) {
  return (
    <span className={cn("chip", STATUS_STYLES[status])}>
      {STATUS_LABELS[status]}
    </span>
  );
}

export function EmptyState({
  title,
  description,
  icon,
  action,
}: {
  title: string;
  description?: string;
  icon?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="relative flex flex-col items-center justify-center overflow-hidden rounded-[26px] border border-dashed border-hot/25 bg-white/50 px-6 py-12 text-center backdrop-blur-sm">
      <span className="checker-soft pointer-events-none absolute inset-0 opacity-30" />
      {icon ? (
        <span className="relative mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-[#ff8fc4] to-[#b02a66] text-white shadow-[0_14px_28px_-14px_rgba(255,44,146,0.9)]">
          {icon}
        </span>
      ) : null}
      <p className="relative text-sm font-bold text-ink">{title}</p>
      {description ? (
        <p className="relative mt-1 max-w-sm text-xs leading-6 text-muted">
          {description}
        </p>
      ) : null}
      {action ? <div className="relative mt-4">{action}</div> : null}
    </div>
  );
}
