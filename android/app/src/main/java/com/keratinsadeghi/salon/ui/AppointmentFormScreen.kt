package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.AppointmentStatus
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.data.Service
import com.keratinsadeghi.salon.data.isSplitEndService
import com.keratinsadeghi.salon.util.formatJalali
import com.keratinsadeghi.salon.util.parseNumber
import com.keratinsadeghi.salon.util.timeLabel
import com.keratinsadeghi.salon.util.todayIso

@Composable
fun AppointmentFormScreen(
    db: Database,
    appointmentId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val existing = remember(appointmentId) { appointmentId?.let { db.getAppointment(it) } }
    val services = remember { db.services() }
    var firstName by remember { mutableStateOf(existing?.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(existing?.lastName.orEmpty()) }
    var phone by remember { mutableStateOf(existing?.phone.orEmpty()) }
    var serviceId by remember { mutableStateOf(existing?.serviceId) }
    var date by remember { mutableStateOf(existing?.date ?: todayIso()) }
    var time by remember { mutableStateOf(existing?.time ?: "09:00") }
    var deposit by remember { mutableStateOf(existing?.deposit?.takeIf { it > 0 }?.toString().orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var status by remember { mutableStateOf(existing?.status ?: AppointmentStatus.WAITING) }
    var error by remember { mutableStateOf<String?>(null) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val selectedService = services.firstOrNull { it.id == serviceId }
    val splitEnd = isSplitEndService(selectedService?.name)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 18.dp),
    ) {
        GradientHeader(
            title = if (existing == null) "ثبت نوبت جدید" else "ویرایش نوبت",
            subtitle = "مشتری را در لیست انتظار سالن قرار بده",
            eyebrow = "BOOKING",
            onBack = onBack,
        )
        Column(Modifier.padding(16.dp)) {
            PinCard(Modifier.fillMaxWidth()) {
                Text("مشخصات مشتری", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Spacer(Modifier.height(10.dp))
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
                    placeholder = "۰۹۱۲...",
                )
            }
            Spacer(Modifier.height(12.dp))
            PinCard(Modifier.fillMaxWidth()) {
                Text("زمان و خدمت", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Spacer(Modifier.height(10.dp))
                SalonDropdown(
                    label = "خدمت",
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
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    PickerField(
                        label = "تاریخ شمسی",
                        value = formatJalali(date, true),
                        onClick = { showDate = true },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DateRange,
                    )
                    PickerField(
                        label = "ساعت",
                        value = timeLabel(time),
                        onClick = { showTime = true },
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.DateRange,
                    )
                }
                Spacer(Modifier.height(10.dp))
                LabeledField(
                    label = "بیعانه پرداختی (تومان)",
                    value = deposit,
                    onValueChange = { deposit = it },
                    keyboardType = KeyboardType.Number,
                )
                Spacer(Modifier.height(10.dp))
                SalonDropdown(
                    label = "وضعیت",
                    selected = status,
                    options = AppointmentStatus.entries,
                    optionLabel = { it.label },
                    onSelect = { status = it },
                )
                Spacer(Modifier.height(10.dp))
                LabeledField(
                    label = "یادداشت",
                    value = notes,
                    onValueChange = { notes = it },
                    singleLine = false,
                    minLines = 3,
                )
            }
            Spacer(Modifier.height(12.dp))
            ErrorText(error)
            Spacer(Modifier.height(10.dp))
            PinkButton(
                text = if (existing == null) "ثبت در لیست انتظار" else "ذخیره تغییرات",
                onClick = {
                    error = null
                    val result = db.saveAppointment(
                        appointmentId = appointmentId,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        serviceId = serviceId,
                        date = date,
                        time = time,
                        deposit = parseNumber(deposit),
                        notes = notes,
                        status = status,
                    )
                    val message = result.second
                    if (message == null) onSaved() else error = message
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (existing != null) {
                Spacer(Modifier.height(9.dp))
                SoftButton(
                    text = "حذف نوبت",
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Delete,
                    danger = true,
                )
            }
        }
    }

    if (showDate) {
        JalaliDateDialog(
            initialIso = date,
            onSelect = {
                date = it
                showDate = false
            },
            onDismiss = { showDate = false },
        )
    }
    if (showTime) {
        TimeDialog(
            initial = time,
            onSelect = {
                time = it
                showTime = false
            },
            onDismiss = { showTime = false },
        )
    }
    if (showDelete && appointmentId != null) {
        ConfirmDialog(
            title = "حذف نوبت",
            text = "این نوبت و نتیجه ثبت‌شده آن حذف شوند؟",
            confirmLabel = "حذف",
            danger = true,
            onConfirm = {
                db.deleteAppointment(appointmentId)
                showDelete = false
                onSaved()
            },
            onDismiss = { showDelete = false },
        )
    }
}
