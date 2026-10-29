import AccountingClient from "@/components/AccountingClient";
import { currentJalaliYear } from "@/lib/jalali";

export const dynamic = "force-dynamic";

export default function AccountingPage() {
  return <AccountingClient initialYear={currentJalaliYear()} />;
}
