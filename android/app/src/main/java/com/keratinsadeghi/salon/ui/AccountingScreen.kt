package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.data.MaterialBreakdown
import com.keratinsadeghi.salon.data.MonthSummary
import com.keratinsadeghi.salon.data.ServiceBreakdown
import com.keratinsadeghi.salon.util.MONTH_NAMES
import com.keratinsadeghi.salon.util.formatDecimal
import com.keratinsadeghi.salon.util.formatNumber
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.toPersianDigits

@Composable
fun AccountingScreen(db: Database) {
    val years = remember { db.yearOptions() }
    var year by remember { mutableIntStateOf(years.first()) }
    val months = remember(year) { db.yearSummary(year) }
    val services = remember(year) { db.serviceBreakdown(year) }
    val materials = remember(year) { db.materialBreakdown(year) }
    val income = months.sumOf { it.income }
    val materialCost = months.sumOf { it.materialCost }
    val deposits = months.sumOf { it.deposit }
    val profit = income - materialCost
    val visits = months.sumOf { it.visits }
    val maxIncome = (months.maxOfOrNull { it.income } ?: 0L).coerceAtLeast(1L)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp),
    ) {
        GradientHeader(
            title = "حسابداری",
            subtitle = "درآمد، هزینه مواد و سود به تفکیک ماه شمسی",
            eyebrow = "SALON LEDGER",
        )
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                years.forEach { option ->
                    val active = option == year
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(if (active) SalonColors.RoseDeep else Color.White.copy(alpha = 0.9f))
                            .padding(horizontal = 15.dp, vertical = 8.dp),
                    ) {
                        Text(
                            toPersianDigits(option.toString()),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) Color.White else SalonColors.RoseDeep,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatCard(
                    icon = Icons.Default.DateRange,
                    label = "درآمد سال",
                    value = formatToman(income),
                    note = "${toPersianDigits(visits.toString())} کار",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Default.ShoppingCart,
                    label = "هزینه مواد",
                    value = formatToman(materialCost),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatCard(
                    icon = Icons.Default.List,
                    label = "سود ناخالص",
                    value = formatToman(profit),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Default.Build,
                    label = "بیعانه‌های ثبت‌شده",
                    value = formatToman(deposits),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("درآمد ماه‌به‌ماه", hint = "سال ${toPersianDigits(year.toString())}")
            PinCard(Modifier.fillMaxWidth()) {
                months.forEach { month ->
                    MonthRow(month = month, maxIncome = maxIncome)
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("جزئیات ماه‌ها", hint = "درآمد، هزینه مواد و سود")
            PinCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(10.dp)) {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    TableHead("ماه", 1.1f)
                    TableHead("کار", 0.6f)
                    TableHead("درآمد", 1.2f)
                    TableHead("مواد", 1.0f)
                    TableHead("سود", 1.0f)
                }
                months.forEach { month ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        TableCell(MONTH_NAMES[month.jm - 1], 1.1f, bold = true)
                        TableCell(toPersianDigits(month.visits.toString()), 0.6f)
                        TableCell(formatNumber(month.income), 1.2f)
                        TableCell(formatNumber(month.materialCost), 1.0f)
                        TableCell(formatNumber(month.profit), 1.0f, color = SalonColors.MintDeep)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("تفکیک خدمات", hint = "پرتکرارترین خدمات و درآمدشان")
            if (services.isEmpty()) {
                EmptyState("کاری ثبت نشده", "با ثبت نتیجه نوبت‌ها، گزارش خدمات ساخته می‌شود.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    services.forEach { service ->
                        ServiceRow(service)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("مصرف مواد", hint = "مقدار و هزینه مواد در سال")
            if (materials.isEmpty()) {
                EmptyState("مصرفی ثبت نشده", "در فرم نتیجه، مواد مصرفی هر کار را وارد کنید.")
            } else {
                PinCard(Modifier.fillMaxWidth()) {
                    materials.forEach { material ->
                        MaterialRow(material)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthRow(month: MonthSummary, maxIncome: Long) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            MONTH_NAMES[month.jm - 1],
            style = MaterialTheme.typography.bodySmall,
            color = SalonColors.Ink,
            modifier = Modifier.width(58.dp),
        )
        Column(Modifier.weight(1f)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(CircleShape)
                    .background(SalonColors.Blush),
            ) {
                val fraction = (month.income.toFloat() / maxIncome.toFloat()).coerceIn(0f, 1f)
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(11.dp)
                        .clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(SalonColors.Hot, SalonColors.Rose))),
                )
            }
            Text(
                "درآمد ${formatToman(month.income)} · سود ${formatToman(month.profit)}",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.Muted,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            toPersianDigits(month.visits.toString()),
            style = MaterialTheme.typography.labelMedium,
            color = SalonColors.RoseDeep,
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableHead(text: String, weight: Float) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = SalonColors.Muted,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String,
    weight: Float,
    bold: Boolean = false,
    color: Color = SalonColors.Ink,
) {
    Text(
        text,
        style = if (bold) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        color = color,
        modifier = Modifier.weight(weight),
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
    )
}

@Composable
private fun ServiceRow(service: ServiceBreakdown) {
    PinCard(Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Text(
                    "${toPersianDigits(service.count.toString())} کار",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
            }
            Text(formatToman(service.income), style = MaterialTheme.typography.labelMedium, color = SalonColors.RoseDeep)
        }
    }
}

@Composable
private fun MaterialRow(material: MaterialBreakdown) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(material.name, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
            Text(
                "${formatDecimal(material.amount)} ${material.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = SalonColors.Muted,
            )
        }
        Text(formatToman(material.cost), style = MaterialTheme.typography.labelMedium, color = SalonColors.RoseDeep)
    }
}
