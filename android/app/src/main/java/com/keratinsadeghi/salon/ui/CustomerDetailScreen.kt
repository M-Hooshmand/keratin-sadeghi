package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.AppointmentStatus
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.util.formatJalali
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.timeLabel
import com.keratinsadeghi.salon.util.toPersianDigits

@Composable
fun CustomerDetailScreen(
    db: Database,
    customerId: Long,
    onBack: () -> Unit,
    onEditAppointment: (Long) -> Unit,
    onVisit: (Long) -> Unit,
    onChanged: () -> Unit,
) {
    val isNew = customerId < 0
    val customer = remember(customerId) { if (isNew) null else db.getCustomer(customerId) }
    val appointments = remember(customerId) {
        if (isNew) emptyList() else db.listAppointments(customerId = customerId)
    }
    val visits = remember(customerId) {
        if (isNew) emptyList() else db.listCustomerVisits(customerId)
    }
    var editing by remember { mutableStateOf(isNew) }
    var firstName by remember { mutableStateOf(customer?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(customer?.lastName.orEmpty()) }
    var phone by remember { mutableStateOf(customer?.phone.orEmpty()) }
    var notes by remember { mutableStateOf(customer?.notes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    if (editing || customer == null) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 18.dp),
        ) {
            GradientHeader(
                title = if (isNew) "مشتری جدید" else "ویرایش مشتری",
                subtitle = "مشخصات تماس مشتری",
                eyebrow = "CLIENT CARD",
                onBack = if (isNew) onBack else ({ editing = false }),
            )
            Column(Modifier.padding(16.dp)) {
                PinCard(Modifier.fillMaxWidth()) {
                    LabeledField("نام", firstName, { firstName = it }, required = true)
                    Spacer(Modifier.height(8.dp))
                    LabeledField("نام خانوادگی", lastName, { lastName = it }, required = true)
                    Spacer(Modifier.height(8.dp))
                    LabeledField(
                        "شماره تماس",
                        phone,
                        { phone = it },
                        keyboardType = KeyboardType.Phone,
                        required = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    LabeledField(
                        "یادداشت",
                        notes,
                        { notes = it },
                        singleLine = false,
                        minLines = 3,
                    )
                }
                Spacer(Modifier.height(10.dp))
                ErrorText(error)
                Spacer(Modifier.height(10.dp))
                PinkButton(
                    text = "ذخیره مشتری",
                    onClick = {
                        error = null
                        val result = db.saveCustomer(
                            id = if (isNew) null else customerId,
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            notes = notes,
                        )
                        if (result.second == null) {
                            onChanged()
                            if (isNew) onBack() else editing = false
                        } else {
                            error = result.second
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!isNew) {
                    Spacer(Modifier.height(9.dp))
                    SoftButton(
                        text = "حذف مشتری",
                        onClick = { showDelete = true },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Delete,
                        danger = true,
                    )
                }
            }
        }
    } else {
        val totalSpent = visits.sumOf { it.finalAmount }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp),
        ) {
            GradientHeader(
                title = "${customer.firstName} ${customer.lastName}",
                subtitle = customer.phone,
                eyebrow = "CLIENT PROFILE",
                onBack = onBack,
                action = {
                    Surface(
                        onClick = { editing = true },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = Color.White,
                            modifier = Modifier.padding(9.dp).size(21.dp),
                        )
                    }
                },
            )
            Column(Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    StatCard(
                        icon = Icons.Default.Person,
                        label = "کار انجام‌شده",
                        value = "${toPersianDigits(visits.size.toString())} بار",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon = Icons.Default.List,
                        label = "مجموع پرداخت",
                        value = formatToman(totalSpent),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    StatCard(
                        icon = Icons.Default.List,
                        label = "نوبت‌ها",
                        value = "${toPersianDigits(appointments.size.toString())} نوبت",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        icon = Icons.Default.Person,
                        label = "در انتظار",
                        value = "${toPersianDigits(appointments.count { it.status == AppointmentStatus.WAITING }.toString())} نوبت",
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!customer.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    PinCard(Modifier.fillMaxWidth()) {
                        Text("یادداشت", style = MaterialTheme.typography.labelMedium, color = SalonColors.RoseDeep)
                        Text(customer.notes, style = MaterialTheme.typography.bodyMedium, color = SalonColors.Ink)
                    }
                }
                Spacer(Modifier.height(18.dp))
                SectionTitle("تاریخچه نوبت‌ها", hint = "برای ویرایش یا ثبت نتیجه لمس کنید")
                if (appointments.isEmpty()) {
                    EmptyState("نوبتی ثبت نشده", "از صفحه نوبت‌ها برای این مشتری نوبت ایجاد کنید.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        appointments.forEach { appointment ->
                            PinCard(
                                Modifier.fillMaxWidth(),
                                padding = PaddingValues(12.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "${formatJalali(appointment.date, true)} · ${timeLabel(appointment.time)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = SalonColors.Ink,
                                        )
                                        Text(
                                            appointment.serviceName ?: "خدمت انتخاب نشده",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SalonColors.RoseDeep,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    StatusChip(appointment.status)
                                }
                                Spacer(Modifier.height(7.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    SoftButton("ویرایش", { onEditAppointment(appointment.id) })
                                    PinkButton(
                                        if (appointment.visitId != null) "مشاهده نتیجه" else "ثبت نتیجه",
                                        { onVisit(appointment.id) },
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                SectionTitle("نتیجه کارها", hint = "قد، حجم و وضعیت موی ثبت‌شده")
                if (visits.isEmpty()) {
                    EmptyState("نتیجه‌ای ثبت نشده", "پس از انجام کار، قد و حجم مو، مشکل و مواد مصرفی را ثبت کنید.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        visits.forEach { visit ->
                            PinCard(Modifier.fillMaxWidth(), padding = PaddingValues(13.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        visit.serviceName ?: "بدون خدمت",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SalonColors.Ink,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        formatToman(visit.finalAmount),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SalonColors.RoseDeep,
                                    )
                                }
                                Text(
                                    formatJalali(visit.performedDate, true),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SalonColors.Muted,
                                )
                                Spacer(Modifier.height(5.dp))
                                val duration = visit.durationMinutes?.let { "مدت ${toPersianDigits(it.toString())} دقیقه" }
                                    ?: "مدت ثبت نشده"
                                val staff = if (visit.assistantCount > 0) {
                                    "${toPersianDigits(visit.staffCount.toString())} نفره · ${toPersianDigits(visit.assistantCount.toString())} دستیار"
                                } else {
                                    "یک‌نفره"
                                }
                                Text(
                                    "$duration · $staff",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SalonColors.Muted,
                                )
                                val length = visit.hairLengthCm?.let {
                                    "قد مو ${toPersianDigits(it.toInt().toString())} سانتی‌متر"
                                } ?: "قد مو ثبت نشده"
                                val volume = visit.hairVolume?.let { " · حجم مو $it" } ?: ""
                                Text(
                                    length + volume,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SalonColors.RoseDeep,
                                )
                                if (!visit.hairCondition.isNullOrBlank()) {
                                    Text(
                                        "وضعیت مو: ${visit.hairCondition}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SalonColors.Muted,
                                    )
                                }
                                if (!visit.workDone.isNullOrBlank()) {
                                    Text(
                                        "کار انجام‌شده: ${visit.workDone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SalonColors.Ink,
                                    )
                                }
                                Text(
                                    "مواد ${formatToman(visit.materialCost)} · دستیار ${formatToman(visit.assistantCost)} · سود ${
                                        formatToman(visit.finalAmount - visit.materialCost - visit.assistantCost)
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SalonColors.MintDeep,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                SoftButton(
                    "حذف مشتری",
                    { showDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Delete,
                    danger = true,
                )
            }
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "حذف مشتری",
            text = "مشتری و همه نوبت‌ها و نتیجه‌های او حذف شوند؟",
            confirmLabel = "حذف",
            danger = true,
            onConfirm = {
                db.deleteCustomer(customerId)
                showDelete = false
                onChanged()
                onBack()
            },
            onDismiss = { showDelete = false },
        )
    }
}
