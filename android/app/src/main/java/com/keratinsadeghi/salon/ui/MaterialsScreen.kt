package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.data.Material
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.parseNumber
import com.keratinsadeghi.salon.util.toPersianDigits

@Composable
fun MaterialsScreen(db: Database, onChanged: () -> Unit) {
    val materials = remember { db.listMaterials(includeInactive = true) }
    var editing by remember { mutableStateOf<Material?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Material?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp),
    ) {
        GradientHeader(
            title = "مواد و قیمت‌ها",
            subtitle = "قیمت هر میلی‌لیتر برای حسابداری دقیق",
            eyebrow = "MATERIAL SHELF",
            action = {
                Surface(
                    onClick = { adding = true },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "ماده جدید",
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp).size(22.dp),
                    )
                }
            },
        )
        Column(Modifier.padding(16.dp)) {
            PinCard(Modifier.fillMaxWidth()) {
                Text("قیمت‌ها قابل ویرایش‌اند", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Text(
                    "هنگام ثبت نتیجه، قیمت هر ماده داخل همان کار ذخیره می‌شود. تغییر قیمت بعدی، حساب‌های قبلی را تغییر نمی‌دهد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
            }
            Spacer(Modifier.height(12.dp))
            if (materials.isEmpty()) {
                EmptyState("ماده‌ای ثبت نشده", "اولین ماده و قیمت هر واحد آن را اضافه کنید.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    materials.forEach { material ->
                        MaterialCard(
                            material = material,
                            onEdit = { editing = material },
                            onToggle = {
                                db.setMaterialActive(material.id, !material.active)
                                onChanged()
                            },
                            onDelete = { deleting = material },
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "قیمت‌های اولیه نمونه هستند؛ آن‌ها را با قیمت واقعی مواد سالن جایگزین کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.Muted,
            )
        }
    }

    if (adding || editing != null) {
        MaterialEditDialog(
            material = editing,
            onDismiss = {
                adding = false
                editing = null
            },
            onSave = { id, name, price, unit, notes ->
                db.saveMaterial(id, name, price, unit, notes)
                adding = false
                editing = null
                onChanged()
            },
        )
    }
    val deleteTarget = deleting
    if (deleteTarget != null) {
        ConfirmDialog(
            title = "حذف ماده",
            text = "اگر این ماده در کاری مصرف شده باشد، به‌جای حذف، بایگانی می‌شود.",
            confirmLabel = "حذف / بایگانی",
            danger = true,
            onConfirm = {
                db.deleteMaterial(deleteTarget.id)
                deleting = null
                onChanged()
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun MaterialCard(
    material: Material,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    PinCard(Modifier.fillMaxWidth(), padding = PaddingValues(13.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(material.name, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                    if (!material.active) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "بایگانی",
                            style = MaterialTheme.typography.labelMedium,
                            color = SalonColors.Muted,
                        )
                    }
                }
                Text(
                    "هر ${material.unit} ${formatToman(material.pricePerUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.RoseDeep,
                )
                if (!material.notes.isNullOrBlank()) {
                    Text(material.notes, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = SalonColors.RoseDeep)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SalonColors.Danger)
            }
        }
        SoftButton(
            text = if (material.active) "بایگانی کن" else "فعال کن",
            onClick = onToggle,
        )
    }
}

@Composable
private fun MaterialEditDialog(
    material: Material?,
    onDismiss: () -> Unit,
    onSave: (Long?, String, Long, String, String?) -> Unit,
) {
    var name by remember(material) { mutableStateOf(material?.name.orEmpty()) }
    var price by remember(material) {
        mutableStateOf(material?.pricePerUnit?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var unit by remember(material) { mutableStateOf(material?.unit ?: "ml") }
    var notes by remember(material) { mutableStateOf(material?.notes.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Text(
                if (material == null) "ماده جدید" else "ویرایش ماده",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).imePadding()) {
                LabeledField("نام ماده", name, { name = it }, required = true)
                Spacer(Modifier.height(8.dp))
                LabeledField(
                    "قیمت هر واحد (تومان)",
                    price,
                    { price = it },
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(8.dp))
                LabeledField("واحد", unit, { unit = it })
                Spacer(Modifier.height(8.dp))
                LabeledField("یادداشت", notes, { notes = it }, singleLine = false, minLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(material?.id, name, parseNumber(price), unit.ifBlank { "ml" }, notes)
                }
            }) {
                Text("ذخیره", color = SalonColors.RoseDeep)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = SalonColors.Muted) }
        },
    )
}
