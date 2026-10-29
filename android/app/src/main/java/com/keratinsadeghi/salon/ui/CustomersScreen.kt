package com.keratinsadeghi.salon.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.CustomerItem
import com.keratinsadeghi.salon.data.Database
import com.keratinsadeghi.salon.util.formatToman
import com.keratinsadeghi.salon.util.toPersianDigits

@Composable
fun CustomersScreen(
    db: Database,
    onDetail: (Long) -> Unit,
    onCreate: () -> Unit,
    onChanged: () -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val customers = remember(search) { db.listCustomers(search.ifBlank { null }) }

    Column(Modifier.fillMaxSize()) {
        GradientHeader(
            title = "مشتریان",
            subtitle = "${toPersianDigits(customers.size.toString())} مشتری",
            eyebrow = "CLIENT BOOK",
            action = {
                Surface(onClick = onCreate, shape = CircleShape, color = Color.White.copy(alpha = 0.18f)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "مشتری جدید",
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
        }
        if (customers.isEmpty()) {
            Box(Modifier.padding(16.dp)) {
                EmptyState("مشتری پیدا نشد", "یک مشتری جدید اضافه کنید یا عبارت جستجو را تغییر دهید.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(customers, key = { it.id }) { customer ->
                    CustomerCard(customer = customer, onClick = { onDetail(customer.id) })
                }
            }
        }
    }
}

@Composable
private fun CustomerCard(customer: CustomerItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SalonColors.Line),
        shadowElevation = 4.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(47.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(SalonColors.Hot, SalonColors.Rose))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(customer.fullName, style = MaterialTheme.typography.titleMedium, color = SalonColors.Ink)
                Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = SalonColors.Muted)
                Text(
                    "${toPersianDigits(customer.visitsCount.toString())} کار · ${formatToman(customer.totalSpent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SalonColors.RoseDeep,
                )
            }
            Text(
                "${toPersianDigits(customer.appointmentsCount.toString())} نوبت",
                style = MaterialTheme.typography.labelMedium,
                color = SalonColors.Muted,
            )
        }
    }
}
