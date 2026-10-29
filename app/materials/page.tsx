import MaterialManager from "@/components/MaterialManager";
import { listMaterials } from "@/lib/queries";

export const dynamic = "force-dynamic";

export default function MaterialsPage() {
  const materials = listMaterials(true);
  return <MaterialManager materials={materials} />;
}
