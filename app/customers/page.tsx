import CustomersClient from "@/components/CustomersClient";
import { listCustomers } from "@/lib/queries";

export const dynamic = "force-dynamic";

export default function CustomersPage() {
  const customers = listCustomers();
  return <CustomersClient customers={customers} />;
}
