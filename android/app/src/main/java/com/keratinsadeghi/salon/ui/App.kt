package com.keratinsadeghi.salon.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.keratinsadeghi.salon.data.Database

sealed interface Screen {
    data object Dashboard : Screen
    data object Appointments : Screen
    data class AppointmentForm(val id: Long? = null) : Screen
    data object Customers : Screen
    data class CustomerDetail(val id: Long) : Screen
    data class VisitForm(val appointmentId: Long) : Screen
    data object Accounting : Screen
    data object Materials : Screen
}

private data class Tab(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val db = remember { Database(context.applicationContext) }
    val stack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    var version by remember { mutableIntStateOf(0) }
    var unlocked by remember { mutableStateOf(false) }
    if (!unlocked) {
        AppBackground {
            AuthScreen(db = db, onAuthenticated = { unlocked = true })
        }
        return
    }
    val current = stack.last()

    fun openTab(screen: Screen) {
        stack.clear()
        stack.add(screen)
    }

    fun push(screen: Screen) {
        stack.add(screen)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun changed(popAfter: Boolean = true) {
        version++
        if (popAfter) pop()
    }

    BackHandler(enabled = stack.size > 1) { pop() }

    val tabs = listOf(
        Tab(Screen.Dashboard, "داشبورد", Icons.Default.Home),
        Tab(Screen.Appointments, "نوبت‌ها", Icons.Default.DateRange),
        Tab(Screen.Customers, "مشتریان", Icons.Default.Person),
        Tab(Screen.Accounting, "حسابداری", Icons.Default.List),
        Tab(Screen.Materials, "مواد", Icons.Default.Build),
    )
    val showBar = current is Screen.Dashboard || current is Screen.Appointments ||
        current is Screen.Customers || current is Screen.Accounting || current is Screen.Materials

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBar) {
                    NavigationBar(
                        containerColor = Color.White.copy(alpha = 0.96f),
                        tonalElevation = 0.dp,
                    ) {
                        tabs.forEach { tab ->
                            val selected = when (tab.screen) {
                                Screen.Dashboard -> current is Screen.Dashboard
                                Screen.Appointments -> current is Screen.Appointments
                                Screen.Customers -> current is Screen.Customers
                                Screen.Accounting -> current is Screen.Accounting
                                else -> current is Screen.Materials
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = { openTab(tab.screen) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = SalonColors.RoseDeep,
                                    indicatorColor = SalonColors.Hot,
                                    unselectedIconColor = SalonColors.Muted,
                                    unselectedTextColor = SalonColors.Muted,
                                ),
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding)) {
                key(version) {
                    when (val screen = current) {
                        Screen.Dashboard -> DashboardScreen(
                            db = db,
                            onAppointments = { openTab(Screen.Appointments) },
                            onCustomers = { openTab(Screen.Customers) },
                            onAccounting = { openTab(Screen.Accounting) },
                            onMaterials = { openTab(Screen.Materials) },
                            onNewAppointment = { push(Screen.AppointmentForm()) },
                            onOpenAppointment = { push(Screen.AppointmentForm(it)) },
                            onVisit = { push(Screen.VisitForm(it)) },
                            onLogout = {
                                stack.clear()
                                stack.add(Screen.Dashboard)
                                unlocked = false
                            },
                        )

                        Screen.Appointments -> AppointmentsScreen(
                            db = db,
                            onCreate = { push(Screen.AppointmentForm()) },
                            onEdit = { push(Screen.AppointmentForm(it)) },
                            onVisit = { push(Screen.VisitForm(it)) },
                            onChanged = { version++ },
                        )

                        is Screen.AppointmentForm -> AppointmentFormScreen(
                            db = db,
                            appointmentId = screen.id,
                            onBack = { pop() },
                            onSaved = { changed() },
                        )

                        Screen.Customers -> CustomersScreen(
                            db = db,
                            onDetail = { push(Screen.CustomerDetail(it)) },
                            onCreate = { push(Screen.CustomerDetail(-1)) },
                            onChanged = { version++ },
                        )

                        is Screen.CustomerDetail -> CustomerDetailScreen(
                            db = db,
                            customerId = screen.id,
                            onBack = { pop() },
                            onEditAppointment = { push(Screen.AppointmentForm(it)) },
                            onVisit = { push(Screen.VisitForm(it)) },
                            onChanged = { version++ },
                        )

                        is Screen.VisitForm -> VisitFormScreen(
                            db = db,
                            appointmentId = screen.appointmentId,
                            onBack = { pop() },
                            onSaved = { changed() },
                        )

                        Screen.Accounting -> AccountingScreen(db = db)

                        Screen.Materials -> MaterialsScreen(db = db, onChanged = { version++ })
                    }
                }
            }
        }
    }
}
