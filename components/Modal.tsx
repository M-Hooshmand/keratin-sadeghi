"use client";

import { ReactNode, useEffect } from "react";
import { X } from "lucide-react";

export default function Modal({
  open,
  onClose,
  title,
  description,
  children,
  size = "md",
}: {
  open: boolean;
  onClose: () => void;
  title: string;
  description?: string;
  children: ReactNode;
  size?: "md" | "lg";
}) {
  useEffect(() => {
    if (!open) return;
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKey);
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = previous;
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center overflow-y-auto bg-ink/35 p-0 backdrop-blur-sm sm:items-center sm:p-4">
      <button
        type="button"
        aria-label="بستن"
        className="absolute inset-0 cursor-default"
        onClick={onClose}
      />
      <div
        className={`fade-in relative z-10 my-0 w-full rounded-t-3xl border border-line bg-ivory shadow-2xl sm:my-6 sm:rounded-3xl ${
          size === "lg" ? "max-w-3xl" : "max-w-xl"
        }`}
      >
        <div className="flex items-start justify-between gap-4 border-b border-line px-5 py-4">
          <div>
            <h2 className="text-base font-bold text-ink">{title}</h2>
            {description ? (
              <p className="mt-1 text-xs text-muted">{description}</p>
            ) : null}
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1.5 text-muted transition hover:bg-blush/60 hover:text-rosedeep"
            aria-label="بستن"
          >
            <X size={18} />
          </button>
        </div>
        <div className="max-h-[78vh] overflow-y-auto px-5 py-4">{children}</div>
      </div>
    </div>
  );
}
