package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.AppointmentItem
import com.keratinsadeghi.salon.data.AppointmentStatus
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.util.formatJalali
import com.keratinsadeghi.salon.util.formatNumber
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.timeLabel
import com.keratinsadeghi.salon.util.toPersianDigits

private data class StatusFilter(val status: AppointmentStatus?, val label: String)

@Composable
fun AppointmentsScreen(
    db: Database,
    onCreate: () -> Unit,
    onEdit: (Long) -> Unit,
    onVisit: (Long) -> Unit,
    onChanged: () -> Unit,
) {
    var filter by remember { mutableStateOf<AppointmentStatus?>(null) }
    var search by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<AppointmentItem?>(null) }
    val items = remember(filter, search) {
        db.listAppointments(status = filter, search = search.ifBlank { null })
    }
    val all = remember { db.listAppointments() }
    val filters = listOf(
        StatusFilter(null, "همه"),
        StatusFilter(AppointmentStatus.WAITING, "در لیست انتظار"),
        StatusFilter(AppointmentStatus.DONE, "انجام‌شده"),
        StatusFilter(AppointmentStatus.CANCELED, "لغو‌شده"),
    )

    Column(Modifier.fillMaxSize()) {
        GradientHeader(
            title = "نوبت‌ها",
            subtitle = "${toPersianDigits(all.size.toString())} نوبت ثبت شده",
            eyebrow = "BOOKING BOARD",
            action = {
                Surface(
                    onClick = onCreate,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "نوبت جدید",
                        tint = Color.White,
                        modifier = Modifier.padding(9.dp).size(22.dp),
                    )
                }
            },
        )
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            LabeledField(
                label = "جستجو",
                value = search,
                onValueChange = { search = it },
                placeholder = "نام یا شماره تماس",
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                filters.forEach { option ->
                    val count = when (option.status) {
                        null -> all.size
                        else -> all.count { it.status == option.status }
                    }
                    val active = filter == option.status
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(if (active) SalonColors.RoseDeep else Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                    ) {
                        Text(
                            "${option.label} (${toPersianDigits(count.toString())})",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) Color.White else SalonColors.RoseDeep,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
        if (items.isEmpty()) {
            Box(Modifier.padding(16.dp)) {
                EmptyState("نوبتی پیدا نشد", "فیلتر را تغییر دهید یا یک نوبت جدید ثبت کنید.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    AppointmentCard(
                        item = item,
                        onEdit = { onEdit(item.id) },
                        onVisit = { onVisit(item.id) },
                        onCancel = {
                            val next = if (item.status == AppointmentStatus.CANCELED)
                                AppointmentStatus.WAITING else AppointmentStatus.CANCELED
                            db.setAppointmentStatus(item.id, next)
                            onChanged()
                        },
                        onDelete = { deleting = item },
                    )
                }
            }
        }
    }

    val deleteTarget = deleting
    if (deleteTarget != null) {
        ConfirmDialog(
            title = "حذف نوبت",
            text = "نوبت ${deleteTarget.fullName} حذف شود؟ نتیجه ثبت‌شده هم حذف خواهد شد.",
            confirmLabel = "حذف",
            danger = true,
            onConfirm = {
                db.deleteAppointment(deleteTarget.id)
                deleting = null
                onChanged()
            },
            onDismiss = { deleting = null },
        )
    }
}

@Composable
private fun AppointmentCard(
    item: AppointmentItem,
    onEdit: () -> Unit,
    onVisit: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    PinCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SalonColors.Blush),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        toPersianDigits(com.keratinsadeghi.salon.util.isoToJalali(item.date).jd.toString()),
                        style = MaterialTheme.typography.titleLarge,
                        color = SalonColors.RoseDeep,
                    )
                    Text(
                        com.keratinsadeghi.salon.util.WEEKDAYS_SHORT[
                            com.keratinsadeghi.salon.util.weekdayIndex(item.date)
                        ],
                        style = MaterialTheme.typography.bodySmall,
                        color = SalonColors.Muted,
                    )
                }
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = SalonColors.Ink,
                        modifier = Modifier.weight(1f),
                    )
                    StatusChip(item.status)
                }
                Text(
                    "${formatJalali(item.date, true)} · ${timeLabel(item.time)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
                Text(
                    item.serviceName ?: "خدمت انتخاب نشده",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.RoseDeep,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "بیعانه ${formatToman(item.deposit)}" +
                        if (item.visitId != null) " · نهایی ${formatToman(item.finalAmount)}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
                Text(
                    item.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = SalonColors.RoseDeep, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SalonColors.Danger, modifier = Modifier.size(19.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            PinkButton(
                text = if (item.visitId != null) "مشاهده نتیجه" else "ثبت نتیجه",
                onClick = onVisit,
            )
            SoftButton(
                text = if (item.status == AppointmentStatus.CANCELED) "بازگردانی" else "لغو",
                onClick = onCancel,
                danger = item.status != AppointmentStatus.CANCELED,
            )
        }
        if (item.status == AppointmentStatus.DONE) {
            Spacer(Modifier.height(6.dp))
            Text(
                "درآمد نهایی ${formatToman(item.finalAmount)} · هزینه مواد ${formatToman(item.materialCost)}",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.MintDeep,
            )
        }
    }
}
