"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  FileText,
  Pencil,
  Phone,
  Search,
  Trash2,
  UserPlus,
  Users,
} from "lucide-react";
import Modal from "./Modal";
import CustomerForm from "./CustomerForm";
import { EmptyState, PageHeader } from "./ui";
import { Customer } from "@/lib/types";

export default function CustomersClient({ customers }: { customers: Customer[] }) {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Customer | null>(null);

  const filtered = useMemo(() => {
    const q = search.trim();
    if (!q) return customers;
    return customers.filter((c) =>
      `${c.first_name} ${c.last_name} ${c.phone}`.includes(q)
    );
  }, [customers, search]);

  async function remove(customer: Customer) {
    if (
      !window.confirm(
        `مشتری «${customer.first_name} ${customer.last_name}» و همه نوبت‌ها و نتایجش حذف شود؟`
      )
    )
      return;
    try {
      const response = await fetch(`/api/customers/${customer.id}`, { method: "DELETE" });
      const json = await response.json();
      if (!json.ok) throw new Error(json.error);
      router.refresh();
    } catch {
      window.alert("حذف مشتری انجام نشد");
    }
  }

  return (
    <div>
      <PageHeader
        title="مشتریان"
        subtitle="دفترچه مشتریان، پرونده و سابقه مراجعات"
        action={
          <button
            type="button"
            className="btn-primary"
            onClick={() => {
              setEditing(null);
              setOpen(true);
            }}
          >
            <UserPlus size={17} />
            مشتری جدید
          </button>
        }
      />

      <div className="card mb-4">
        <div className="relative">
          <Search size={15} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted" />
          <input
            className="input pr-9"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="جستجوی نام یا شماره تماس..."
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          title="مشتری‌ای پیدا نشد"
          description="می‌توانید همین حالا مشتری جدید اضافه کنید یا از صفحه نوبت‌ها ثبت کنید."
          icon={<Users size={22} />}
        />
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((customer) => (
            <div key={customer.id} className="card flex flex-col justify-between gap-4">
              <div>
                <div className="flex items-start gap-3">
                  <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-blush/70 text-sm font-bold text-rosedeep">
                    {customer.first_name.slice(0, 1)}
                    {customer.last_name.slice(0, 1)}
                  </span>
                  <div className="min-w-0">
                    <Link
                      href={`/customers/${customer.id}`}
                      className="block truncate text-sm font-bold text-ink hover:text-rosedeep"
                    >
                      {customer.first_name} {customer.last_name}
                    </Link>
                    <p className="mt-1 flex items-center gap-1 text-[11px] text-muted">
                      <Phone size={12} />
                      <span dir="ltr">{customer.phone}</span>
                    </p>
                  </div>
                </div>
                {customer.notes ? (
                  <p className="mt-3 line-clamp-2 rounded-xl bg-ivory px-3 py-2 text-[11px] leading-5 text-muted">
                    {customer.notes}
                  </p>
                ) : null}
              </div>
              <div className="flex items-center gap-1.5 border-t border-line pt-3">
                <Link href={`/customers/${customer.id}`} className="btn-ghost btn-sm">
                  <FileText size={14} />
                  پرونده
                </Link>
                <button
                  type="button"
                  className="btn-ghost btn-sm"
                  onClick={() => {
                    setEditing(customer);
                    setOpen(true);
                  }}
                >
                  <Pencil size={14} />
                  ویرایش
                </button>
                <button
                  type="button"
                  className="btn-danger btn-sm mr-auto"
                  onClick={() => remove(customer)}
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={open}
        onClose={() => setOpen(false)}
        title={editing ? "ویرایش مشتری" : "مشتری جدید"}
      >
        <CustomerForm
          customer={editing}
          onCancel={() => setOpen(false)}
          onSaved={() => {
            setOpen(false);
            router.refresh();
          }}
        />
      </Modal>
    </div>
  );
}
