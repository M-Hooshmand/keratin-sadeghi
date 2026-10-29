package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.keratinsadeghi.salon.data.AppointmentStatus
import com.keratinsadeghi.salon.data.HAIR_VOLUME_OPTIONS
import com.keratinsadeghi.salon.data.isSplitEndService
import com.keratinsadeghi.salon.util.MONTH_NAMES
import com.keratinsadeghi.salon.util.WEEKDAYS_SHORT
import com.keratinsadeghi.salon.util.addJalaliMonths
import com.keratinsadeghi.salon.util.formatJalali
import com.keratinsadeghi.salon.util.isoToJalali
import com.keratinsadeghi.salon.util.jalaliMonthLength
import com.keratinsadeghi.salon.util.jalaliToIso
import com.keratinsadeghi.salon.util.todayIso
import com.keratinsadeghi.salon.util.toPersianDigits
import com.keratinsadeghi.salon.util.weekdayIndex

@Composable
fun AppBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SalonColors.Ivory,
                        Color(0xFFFFF8FC),
                        SalonColors.Lilac.copy(alpha = 0.45f),
                    ),
                ),
            )
            .drawBehind {
                val step = 42.dp.toPx()
                val radius = 1.15.dp.toPx()
                var y = step / 2
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) step / 2 else step
                    while (x < size.width) {
                        drawCircle(
                            color = SalonColors.Bubble.copy(alpha = 0.16f),
                            radius = radius,
                            center = Offset(x, y),
                        )
                        x += step
                    }
                    y += step
                    row++
                }
            },
    ) {
        content()
    }
}

@Composable
fun GradientHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF4B0D2B), Color(0xFFA8215D), Color(0xFFFF4FA5)),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White,
                            modifier = Modifier.padding(9.dp).size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    if (eyebrow != null) {
                        Text(
                            text = eyebrow,
                            color = SalonColors.Bubble,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (action != null) action()
            }
        }
    }
}

@Composable
fun PinCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = SalonColors.Rose.copy(alpha = 0.16f),
                spotColor = SalonColors.Rose.copy(alpha = 0.14f),
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalonColors.Line.copy(alpha = 0.85f)),
    ) {
        Column(Modifier.padding(padding)) { content() }
    }
}

@Composable
fun SectionTitle(title: String, hint: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
            if (hint != null) {
                Text(hint, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
            }
        }
        if (action != null) action()
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    note: String? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalonColors.Line.copy(alpha = 0.8f)),
        shadowElevation = 5.dp,
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(listOf(SalonColors.Hot, SalonColors.Rose)),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = SalonColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note != null) {
                Text(note, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
            }
        }
    }
}

@Composable
fun StatusChip(status: AppointmentStatus) {
    val (bg, fg) = when (status) {
        AppointmentStatus.WAITING -> SalonColors.Blush to SalonColors.RoseDeep
        AppointmentStatus.DONE -> SalonColors.Mint to SalonColors.MintDeep
        AppointmentStatus.CANCELED -> SalonColors.DangerBg to SalonColors.Danger
    }
    Box(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(status.label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
fun PinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(SalonColors.Hot, SalonColors.RoseDeep))
                else Brush.horizontalGradient(listOf(Color(0xFFE7BFD2), Color(0xFFD6A4BC))),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    danger: Boolean = false,
) {
    val fg = if (danger) SalonColors.Danger else SalonColors.RoseDeep
    val bg = if (danger) SalonColors.DangerBg else Color.White.copy(alpha = 0.9f)
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, if (danger) SalonColors.Danger.copy(alpha = 0.25f) else SalonColors.Line, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null,
    required: Boolean = false,
) {
    Column(modifier) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
            color = SalonColors.Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SalonColors.Hot,
                unfocusedBorderColor = SalonColors.Line,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                cursorColor = SalonColors.Hot,
            ),
        )
    }
}

@Composable
fun PickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.DateRange,
) {
    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = SalonColors.Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, SalonColors.Line),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SalonColors.Ink,
                    modifier = Modifier.weight(1f),
                )
                Icon(icon, contentDescription = null, tint = SalonColors.Hot, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SalonDropdown(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "انتخاب کنید",
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = SalonColors.Muted,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        ExposedDropdownMenuBox(
            expanded = expanded && enabled,
            onExpandedChange = { if (enabled) expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selected?.let(optionLabel) ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SalonColors.Hot,
                    unfocusedBorderColor = SalonColors.Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = 0.92f),
                ),
            )
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option), style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorText(text: String?) {
    if (!text.isNullOrBlank()) {
        Text(
            text,
            color = SalonColors.Danger,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SalonColors.DangerBg)
                .padding(10.dp),
        )
    }
}

@Composable
fun EmptyState(title: String, text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SalonColors.Blush.copy(alpha = 0.55f))
            .border(1.dp, SalonColors.Line, RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = SalonColors.RoseDeep)
            Text(text, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(confirmLabel, color = if (danger) SalonColors.Danger else SalonColors.RoseDeep)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = SalonColors.Muted)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
    )
}

@Composable
fun JalaliDateDialog(
    initialIso: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val start = if (initialIso.isBlank()) isoToJalali(todayIso()) else isoToJalali(initialIso)
    var year by remember { mutableIntStateOf(start.jy) }
    var month by remember { mutableIntStateOf(start.jm) }
    val selected = if (initialIso.isBlank()) "" else initialIso

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val prev = addJalaliMonths(year, month, -1)
                        year = prev.first
                        month = prev.second
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "ماه قبل")
                    }
                    Text(
                        "${MONTH_NAMES[month - 1]} ${toPersianDigits(year.toString())}",
                        style = MaterialTheme.typography.titleMedium,
                        color = SalonColors.Ink,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    IconButton(onClick = {
                        val next = addJalaliMonths(year, month, 1)
                        year = next.first
                        month = next.second
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "ماه بعد")
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    WEEKDAYS_SHORT.forEach { day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (day == "ج") SalonColors.RoseDeep else SalonColors.Muted,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                val firstIso = jalaliToIso(year, month, 1)
                val offset = weekdayIndex(firstIso)
                val length = jalaliMonthLength(year, month)
                val cells = offset + length
                val rows = (cells + 6) / 7
                Column {
                    for (row in 0 until rows) {
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val index = row * 7 + col
                                val day = index - offset + 1
                                Box(
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (day in 1..length) {
                                        val iso = jalaliToIso(year, month, day)
                                        val isSelected = iso == selected
                                        val isToday = iso == todayIso()
                                        val friday = weekdayIndex(iso) == 6
                                        Box(
                                            Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    when {
                                                        isSelected -> Modifier.background(
                                                            Brush.linearGradient(
                                                                listOf(SalonColors.Hot, SalonColors.RoseDeep),
                                                            ),
                                                        )
                                                        isToday -> Modifier.border(1.dp, SalonColors.Hot, CircleShape)
                                                        else -> Modifier
                                                    },
                                                )
                                                .clickable { onSelect(iso) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                toPersianDigits(day.toString()),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = when {
                                                    isSelected -> Color.White
                                                    friday -> SalonColors.RoseDeep
                                                    else -> SalonColors.Ink
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { onSelect(todayIso()) }) {
                        Text("امروز", color = SalonColors.RoseDeep)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("بستن", color = SalonColors.Muted)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDialog(
    initial: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val slots = remember(initial) {
        val base = buildList {
            for (hour in 9..21) {
                add("%02d:00".format(hour))
                if (hour < 21) add("%02d:30".format(hour))
            }
        }.toMutableList()
        if (initial.isNotBlank() && initial !in base) base.add(initial)
        base
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 16.dp) {
            Column(Modifier.padding(16.dp)) {
                Text("ساعت نوبت", style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(330.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(slots) { slot ->
                        val selected = slot == initial
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) Brush.linearGradient(
                                        listOf(SalonColors.Hot, SalonColors.RoseDeep),
                                    ) else Brush.linearGradient(
                                        listOf(SalonColors.Blush, SalonColors.Blush),
                                    ),
                                )
                                .clickable { onSelect(slot) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                toPersianDigits(slot),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selected) Color.White else SalonColors.RoseDeep,
                            )
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("بستن", color = SalonColors.Muted)
                }
            }
        }
    }
}

@Composable
fun HairVolumeOptions(
    selected: String?,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HAIR_VOLUME_OPTIONS.forEach { option ->
            val active = option == selected
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(if (active) SalonColors.RoseDeep else SalonColors.Blush)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 13.dp, vertical = 8.dp),
            ) {
                Text(
                    option,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) Color.White else SalonColors.RoseDeep,
                )
            }
        }
    }
}


@Composable
fun SplitEndNote() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SalonColors.Blush.copy(alpha = 0.65f))
            .border(1.dp, SalonColors.Line, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = SalonColors.RoseDeep,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "این خدمت نرخ واحد و مصرف مواد ندارد؛ مبلغ نهایی را بر اساس مبلغ دریافت‌شده وارد کنید. ثبت قد و حجم مو الزامی است.",
            style = MaterialTheme.typography.bodySmall,
            color = SalonColors.RoseDeep,
        )
    }
}
