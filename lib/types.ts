export type AppointmentStatus = "waiting" | "done" | "canceled";

export const STATUS_LABELS: Record<AppointmentStatus, string> = {
  waiting: "در لیست انتظار",
  done: "انجام‌شده",
  canceled: "لغو‌شده",
};

export const STATUS_STYLES: Record<AppointmentStatus, string> = {
  waiting: "bg-[#FFE3F1] text-[#B02A66] border-[#FFB8D8]",
  done: "bg-[#E8F3EC] text-[#2F6B4F] border-[#C4E0CF]",
  canceled: "bg-[#FBEAEA] text-[#8E3B3B] border-[#EFC9C9]",
};

export const SPLIT_END_SERVICE_NAME = "موخوره‌گیری به روش روسی با قیچی";
export const HAIR_VOLUME_OPTIONS = ["کم", "متوسط", "زیاد", "خیلی زیاد"];

export function isSplitEndService(name: string | null | undefined): boolean {
  return Boolean(name && name.includes("موخوره"));
}

export interface Customer {
  id: number;
  first_name: string;
  last_name: string;
  phone: string;
  notes: string | null;
  created_at: string;
}

export interface Service {
  id: number;
  name: string;
  sort: number;
  active: number;
}

export interface AppointmentRow {
  id: number;
  customer_id: number;
  service_id: number | null;
  appointment_date: string;
  appointment_time: string;
  deposit: number;
  status: AppointmentStatus;
  notes: string | null;
  created_at: string;
  first_name: string;
  last_name: string;
  phone: string;
  service_name: string | null;
  visit_id: number | null;
  final_amount: number | null;
  material_cost: number | null;
}

export interface VisitRow {
  id: number;
  appointment_id: number;
  customer_id: number;
  service_id: number | null;
  performed_date: string;
  final_amount: number;
  hair_length_cm: number | null;
  hair_volume: string | null;
  hair_condition: string | null;
  work_done: string | null;
  notes: string | null;
  created_at: string;
  first_name: string;
  last_name: string;
  phone: string;
  service_name: string | null;
  material_cost: number;
}

export interface Material {
  id: number;
  name: string;
  unit: string;
  price_per_unit: number;
  notes: string | null;
  active: number;
}

export interface MaterialUsage {
  id: number;
  visit_id: number;
  material_id: number;
  amount: number;
  unit_price: number;
  unit: string;
  name?: string;
}

export const VISIT_SELECT = `
  SELECT v.*, c.first_name, c.last_name, c.phone, s.name AS service_name,
    COALESCE((SELECT SUM(mu.amount * mu.unit_price) FROM material_usages mu WHERE mu.visit_id = v.id), 0) AS material_cost
  FROM visits v
  JOIN customers c ON c.id = v.customer_id
  LEFT JOIN services s ON s.id = v.service_id
`;
