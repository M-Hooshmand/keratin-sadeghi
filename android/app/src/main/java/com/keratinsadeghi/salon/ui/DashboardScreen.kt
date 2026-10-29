package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.R
import com.keratinsadeghi.salon.data.AppointmentItem
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.util.currentJalaliYear
import com.keratinsadeghi.salon.util.formatJalali
import com.keratinsadeghi.salon.util.formatNumber
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.timeLabel
import com.keratinsadeghi.salon.util.todayIso
import com.keratinsadeghi.salon.util.todayJalali
import com.keratinsadeghi.salon.util.toPersianDigits

@Composable
fun DashboardScreen(
    db: Database,
    onAppointments: () -> Unit,
    onCustomers: () -> Unit,
    onAccounting: () -> Unit,
    onMaterials: () -> Unit,
    onNewAppointment: () -> Unit,
    onOpenAppointment: (Long) -> Unit,
    onVisit: (Long) -> Unit,
) {
    val today = todayIso()
    val jalaliToday = todayJalali()
    val month = remember { db.monthSummary(currentJalaliYear(), todayJalali().jm) }
    val todayItems = remember(today) { db.dashboardToday() }
    val waiting = remember(today) { db.dashboardWaiting() }
    val customerCount = remember { db.customerCount() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 18.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(30.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.hair_hero),
                contentDescription = "مدل با موی بلند، صاف و درخشان",
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF4B0D2B).copy(alpha = 0.86f),
                                Color(0xFFA8215D).copy(alpha = 0.62f),
                                Color(0xFFFF4FA5).copy(alpha = 0.3f),
                            ),
                        ),
                    ),
            )
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(18.dp),
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        "SALON PANEL",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "سالن کراتین\nخانم صادقی",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "نوبت‌ها، مشتریان و حساب‌وکتاب سالن، همه در یک پنل",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(
                Modifier.align(Alignment.BottomStart).padding(18.dp),
            ) {
                Text(
                    formatJalali(today, true),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "${toPersianDigits(todayItems.size.toString())} نوبت امروز · ${toPersianDigits(waiting.size.toString())} نفر در انتظار",
                    color = Color.White.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PinkButton("ثبت نوبت جدید", onNewAppointment, icon = Icons.Default.Add)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onAppointments)
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                    ) {
                        Text("مشاهده نوبت‌ها", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    icon = Icons.Default.DateRange,
                    label = "نوبت‌های امروز",
                    value = "${toPersianDigits(todayItems.size.toString())} نوبت",
                    note = "شامل منتظر، انجام‌شده و لغوشده",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Default.Person,
                    label = "در لیست انتظار",
                    value = "${toPersianDigits(waiting.size.toString())} نفر",
                    note = "از امروز به بعد",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    icon = Icons.Default.List,
                    label = "درآمد ${db.monthLabel(jalaliToday.jy, jalaliToday.jm)}",
                    value = formatToman(month.income),
                    note = "${toPersianDigits(month.visits.toString())} کار ثبت‌شده",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Default.Build,
                    label = "سود ناخالص ماه",
                    value = formatToman(month.profit),
                    note = "هزینه مواد ${formatToman(month.materialCost)}",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("برنامه امروز", hint = formatJalali(today, true))
            if (todayItems.isEmpty()) {
                EmptyState("امروز نوبتی ثبت نشده", "برای ثبت نوبت جدید از دکمه بالای صفحه استفاده کنید.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todayItems.forEach { item ->
                        AppointmentMiniCard(
                            item = item,
                            onClick = { onOpenAppointment(item.id) },
                            onVisit = { onVisit(item.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("منتظران آینده", hint = "نوبت‌های در انتظار از امروز به بعد")
            if (waiting.isEmpty()) {
                EmptyState("لیست انتظار خالی است", "نوبت‌های جدید به‌صورت خودکار در لیست انتظار قرار می‌گیرند.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    waiting.forEach { item ->
                        AppointmentMiniCard(
                            item = item,
                            onClick = { onOpenAppointment(item.id) },
                            onVisit = { onVisit(item.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle("میانبرها")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutCard(
                    title = "مشتریان",
                    text = "${toPersianDigits(customerCount.toString())} مشتری ثبت شده",
                    icon = Icons.Default.Person,
                    onClick = onCustomers,
                )
                ShortcutCard(
                    title = "حسابداری",
                    text = "درآمد ماه‌های شمسی، هزینه مواد و سود",
                    icon = Icons.Default.List,
                    onClick = onAccounting,
                )
                ShortcutCard(
                    title = "مواد و قیمت‌ها",
                    text = "قیمت هر میلی‌لیتر و هزینه مصرف",
                    icon = Icons.Default.Build,
                    onClick = onMaterials,
                )
            }
            Spacer(Modifier.height(18.dp))
            PinCard(modifier = Modifier.fillMaxWidth()) {
                Text("یادداشت پین", style = MaterialTheme.typography.labelMedium, color = SalonColors.RoseDeep)
                Text(
                    "داده‌ها فقط روی همین گوشی ذخیره می‌شوند. برای نگهداری مطمئن، از تنظیمات گوشی پشتیبان‌گیری کنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.Muted,
                )
            }
        }
    }
}

@Composable
private fun AppointmentMiniCard(item: AppointmentItem, onClick: () -> Unit, onVisit: () -> Unit) {
    PinCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        padding = PaddingValues(13.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SalonColors.Blush),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        toPersianDigits(com.keratinsadeghi.salon.util.isoToJalali(item.date).jd.toString()),
                        style = MaterialTheme.typography.titleMedium,
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
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.fullName, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Text(
                    "${formatJalali(item.date)} · ${timeLabel(item.time)}",
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
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(item.status)
                if (item.status != com.keratinsadeghi.salon.data.AppointmentStatus.DONE) {
                    Spacer(Modifier.height(5.dp))
                    SoftButton("نتیجه", onVisit)
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalonColors.Line),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.linearGradient(listOf(SalonColors.Hot, SalonColors.Rose))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Text(text, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
            }
        }
    }
}
