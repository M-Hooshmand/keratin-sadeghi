import Link from "next/link";
import { SearchX } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <span className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-blush/70 text-rosedeep">
        <SearchX size={24} />
      </span>
      <h1 className="text-lg font-bold text-ink">صفحه پیدا نشد</h1>
      <p className="mt-2 max-w-sm text-xs leading-6 text-muted">
        آدرسی که وارد کرده‌اید وجود ندارد یا حذف شده است.
      </p>
      <Link href="/" className="btn-primary mt-5">
        بازگشت به داشبورد
      </Link>
    </div>
  );
}
