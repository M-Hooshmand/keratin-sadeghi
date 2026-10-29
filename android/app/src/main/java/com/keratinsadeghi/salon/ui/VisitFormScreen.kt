package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.data.HAIR_VOLUME_OPTIONS
import com.keratinsadeghi.salon.data.Material
import com.keratinsadeghi.salon.data.MaterialInput
import com.keratinsadeghi.salon.data.isSplitEndService
import com.keratinsadeghi.salon.util.formatDecimal
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.parseDouble
import com.keratinsadeghi.salon.util.parseNumber
import com.keratinsadeghi.salon.util.todayIso
import com.keratinsadeghi.salon.util.toPersianDigits

private data class UsageRow(
    val key: Int,
    val materialId: Long?,
    val amount: String,
)

@Composable
fun VisitFormScreen(
    db: Database,
    appointmentId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val appointment = remember(appointmentId) { db.getAppointment(appointmentId) }
    val existing = remember(appointmentId) { db.getVisitForAppointment(appointmentId) }
    val services = remember { db.services() }
    val materials = remember { db.listMaterials(includeInactive = true) }
    val usages = remember(appointmentId) { existing?.let { db.usages(it.id) } ?: emptyList() }
    val deposit = appointment?.deposit ?: 0L

    var serviceId by remember {
        mutableStateOf(existing?.serviceId ?: appointment?.serviceId)
    }
    var performedDate by remember { mutableStateOf(existing?.performedDate ?: todayIso()) }
    var finalAmount by remember {
        mutableStateOf(existing?.finalAmount?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var duration by remember {
        mutableStateOf(existing?.durationMinutes?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var withAssistant by remember {
        mutableStateOf((existing?.assistantCount ?: 0) > 0)
    }
    var assistantCount by remember {
        mutableStateOf(existing?.assistantCount?.takeIf { it > 0 }?.toString() ?: "1")
    }
    var assistantCost by remember {
        mutableStateOf(existing?.assistantCost?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var hairLength by remember {
        mutableStateOf(existing?.hairLengthCm?.let { formatDecimal(it).replace("٫", ".") } ?: "")
    }
    var hairVolume by remember { mutableStateOf(existing?.hairVolume.orEmpty()) }
    var hairCondition by remember { mutableStateOf(existing?.hairCondition.orEmpty()) }
    var workDone by remember { mutableStateOf(existing?.workDone.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var rows by remember {
        mutableStateOf(
            usages.mapIndexed { index, usage ->
                UsageRow(index, usage.materialId, formatDecimal(usage.amount).replace("٫", "."))
            },
        )
    }
    var nextKey by remember { mutableIntStateOf(rows.size + 1) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val selectedService = services.firstOrNull { it.id == serviceId }
    val splitEnd = isSplitEndService(selectedService?.name)
    val selectedIds = rows.mapNotNull { it.materialId }.toSet()
    val addable = materials.filter { it.active && it.id !in selectedIds }
    val materialCost = if (splitEnd) 0L else rows.sumOf { row ->
        val material = materials.firstOrNull { it.id == row.materialId }
        ((material?.pricePerUnit ?: 0L) * parseDouble(row.amount)).toLong()
    }
    val amountValue = parseNumber(finalAmount)
    val assistantCostValue = if (withAssistant) parseNumber(assistantCost) else 0L
    val profit = amountValue - materialCost - assistantCostValue

    if (appointment == null) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            GradientHeader(
                title = "نتیجه کار",
                subtitle = "نوبت پیدا نشد",
                onBack = onBack,
            )
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 18.dp),
    ) {
        GradientHeader(
            title = if (existing == null) "ثبت نتیجه نهایی" else "ویرایش نتیجه",
            subtitle = "${appointment.fullName} · ${appointment.phone}",
            eyebrow = "FINAL RESULT",
            onBack = onBack,
        )
        Column(Modifier.padding(16.dp)) {
            PinCard(Modifier.fillMaxWidth()) {
                Text("مشخصات کار", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Spacer(Modifier.height(9.dp))
                SalonDropdown(
                    label = "خدمت انجام‌شده",
                    selected = selectedService,
                    options = services,
                    optionLabel = { it.name },
                    onSelect = { serviceId = it.id },
                    placeholder = "انتخاب خدمت",
                )
                if (splitEnd) {
                    Spacer(Modifier.height(8.dp))
                    SplitEndNote()
                }
                Spacer(Modifier.height(9.dp))
                PickerField(
                    label = "تاریخ انجام",
                    value = com.keratinsadeghi.salon.util.formatJalali(performedDate, true),
                    onClick = { showDate = true },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(9.dp))
                LabeledField(
                    label = "مدت زمان انجام کار (دقیقه)",
                    value = duration,
                    onValueChange = { duration = it },
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "نفرات انجام کار",
                    style = MaterialTheme.typography.labelMedium,
                    color = SalonColors.Muted,
                )
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StaffOption("یک‌نفره", !withAssistant) {
                        withAssistant = false
                        assistantCount = "1"
                        assistantCost = ""
                    }
                    StaffOption("با دستیار", withAssistant) { withAssistant = true }
                }
                if (withAssistant) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledField(
                            label = "تعداد دستیار",
                            value = assistantCount,
                            onValueChange = { assistantCount = it },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                        LabeledField(
                            label = "هزینه دستیار (تومان)",
                            value = assistantCost,
                            onValueChange = { assistantCost = it },
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Text(
                        "هزینه دستیار را بر اساس مبلغ پرداختی همان کار وارد کنید؛ در سود کسر می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalonColors.Muted,
                    )
                }
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledField(
                        label = "قد مو (سانتی‌متر)",
                        value = hairLength,
                        onValueChange = { hairLength = it },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f),
                        required = splitEnd,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (splitEnd) "حجم مو *" else "حجم مو",
                            style = MaterialTheme.typography.labelMedium,
                            color = SalonColors.Muted,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        HairVolumeOptions(selected = hairVolume.ifBlank { null }, onSelect = { hairVolume = it })
                    }
                }
                Spacer(Modifier.height(9.dp))
                LabeledField(
                    label = "مشکل یا وضعیت مو",
                    value = hairCondition,
                    onValueChange = { hairCondition = it },
                    singleLine = false,
                    minLines = 2,
                    placeholder = "مثلاً دکلره‌شده، خشک، چند سال رنگ",
                )
                Spacer(Modifier.height(9.dp))
                LabeledField(
                    label = "چه کاری انجام شد",
                    value = workDone,
                    onValueChange = { workDone = it },
                    singleLine = false,
                    minLines = 2,
                )
                Spacer(Modifier.height(9.dp))
                LabeledField(
                    label = "یادداشت",
                    value = notes,
                    onValueChange = { notes = it },
                    singleLine = false,
                    minLines = 2,
                )
            }

            Spacer(Modifier.height(12.dp))
            if (splitEnd) {
                PinCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SalonColors.RoseDeep)
                        Spacer(Modifier.width(8.dp))
                        Text("مصرف مواد", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "این خدمت مصرف مواد ندارد و هزینه مواد آن صفر ثبت می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalonColors.Muted,
                    )
                }
            } else {
                PinCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SalonColors.RoseDeep)
                        Spacer(Modifier.width(8.dp))
                        Text("مصرف مواد", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            if (addable.isNotEmpty()) {
                                rows = rows + UsageRow(nextKey, addable.first().id, "")
                                nextKey++
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "افزودن ماده", tint = SalonColors.RoseDeep)
                        }
                    }
                    if (rows.isEmpty()) {
                        Text(
                            "برای ثبت هزینه مواد، یک ماده اضافه کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SalonColors.Muted,
                        )
                    } else {
                        rows.forEach { row ->
                            val material = materials.firstOrNull { it.id == row.materialId }
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.weight(1.5f)) {
                                    SalonDropdown(
                                        label = "",
                                        selected = material,
                                        options = materials.filter {
                                            it.active || it.id == row.materialId
                                        },
                                        optionLabel = { it.name },
                                        onSelect = { selected ->
                                            rows = rows.map {
                                                if (it.key == row.key) it.copy(materialId = selected.id) else it
                                            }
                                        },
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                LabeledField(
                                    label = "مقدار",
                                    value = row.amount,
                                    onValueChange = { value ->
                                        rows = rows.map {
                                            if (it.key == row.key) it.copy(amount = value) else it
                                        }
                                    },
                                    keyboardType = KeyboardType.Decimal,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { rows = rows.filterNot { it.key == row.key } }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "حذف ماده",
                                        tint = SalonColors.Danger,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Text(
                                "${material?.name ?: "ماده"} · هر ${material?.unit ?: "ml"} ${formatToman(material?.pricePerUnit ?: 0)} · هزینه ${
                                    formatToman(((material?.pricePerUnit ?: 0L) * parseDouble(row.amount)).toLong())
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = SalonColors.Muted,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            PinCard(Modifier.fillMaxWidth()) {
                Text("مبلغ نهایی", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                LabeledField(
                    label = "مبلغ دریافت‌شده (تومان)",
                    value = finalAmount,
                    onValueChange = { finalAmount = it },
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    "بیعانه ثبت‌شده: ${formatToman(deposit)} · مانده: ${
                        formatToman((amountValue - deposit).coerceAtLeast(0))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
                if (splitEnd) {
                    Text(
                        "مبلغ موخوره‌گیری نرخ ثابت ندارد؛ مبلغ دریافت‌شده را وارد کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SalonColors.RoseDeep,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PinCard(Modifier.fillMaxWidth()) {
                Text("جمع‌بندی", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Spacer(Modifier.height(6.dp))
                SummaryLine("هزینه مواد", if (splitEnd) "ندارد" else formatToman(materialCost))
                SummaryLine("هزینه دستیار", if (withAssistant) formatToman(assistantCostValue) else "ندارد")
                SummaryLine("درآمد نهایی", formatToman(amountValue))
                SummaryLine("سود ناخالص", formatToman(profit), SalonColors.MintDeep)
            }

            Spacer(Modifier.height(10.dp))
            ErrorText(error)
            Spacer(Modifier.height(8.dp))
            PinkButton(
                text = if (existing == null) "ثبت نتیجه و انجام‌شدن" else "ذخیره تغییرات",
                onClick = {
                    error = null
                    val length = parseDouble(hairLength)
                    val assistants = if (withAssistant) parseNumber(assistantCount).toInt() else 0
                    if (withAssistant && assistants < 1) {
                        error = "تعداد دستیار را وارد کنید"
                        return@PinkButton
                    }
                    if (withAssistant && parseNumber(assistantCost) < 0) {
                        error = "هزینه دستیار نمی‌تواند منفی باشد"
                        return@PinkButton
                    }
                    if (splitEnd && length <= 0) {
                        error = "برای موخوره‌گیری، قد مو الزامی است"
                        return@PinkButton
                    }
                    if (splitEnd && hairVolume.isBlank()) {
                        error = "برای موخوره‌گیری، حجم مو الزامی است"
                        return@PinkButton
                    }
                    val items = if (splitEnd) emptyList() else rows
                        .filter { it.materialId != null && parseDouble(it.amount) > 0 }
                        .map { MaterialInput(it.materialId!!, parseDouble(it.amount)) }
                    val message = db.saveVisit(
                        appointmentId = appointmentId,
                        serviceId = serviceId,
                        performedDate = performedDate,
                        finalAmount = amountValue,
                        durationMinutes = parseNumber(duration).toInt().takeIf { it > 0 },
                        assistantCount = assistants,
                        assistantCost = if (withAssistant) parseNumber(assistantCost) else 0L,
                        hairLengthCm = length.takeIf { it > 0 },
                        hairVolume = hairVolume.ifBlank { null },
                        hairCondition = hairCondition,
                        workDone = workDone,
                        notes = notes,
                        materials = items,
                    )
                    if (message == null) onSaved() else error = message
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (existing != null) {
                Spacer(Modifier.height(8.dp))
                SoftButton(
                    "حذف نتیجه و بازگشت به انتظار",
                    { showDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Delete,
                    danger = true,
                )
            }
        }
    }

    if (showDate) {
        JalaliDateDialog(
            initialIso = performedDate,
            onSelect = {
                performedDate = it
                showDate = false
            },
            onDismiss = { showDate = false },
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = "حذف نتیجه",
            text = "نتیجه این کار حذف شود و نوبت به لیست انتظار برگردد؟",
            confirmLabel = "حذف نتیجه",
            danger = true,
            onConfirm = {
                db.deleteVisitByAppointment(appointmentId)
                showDelete = false
                onSaved()
            },
            onDismiss = { showDelete = false },
        )
    }
}

@Composable
private fun StaffOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) SalonColors.RoseDeep else SalonColors.Blush)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else SalonColors.RoseDeep,
        )
    }
}

@Composable
private fun SummaryLine(label: String, value: String, valueColor: Color = SalonColors.RoseDeep) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = valueColor)
    }
}
