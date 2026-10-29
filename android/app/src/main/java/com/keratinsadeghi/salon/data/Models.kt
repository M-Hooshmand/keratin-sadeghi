package com.keratinsadeghi.salon.data

enum class AppointmentStatus(val value: String, val label: String) {
    WAITING("waiting", "در لیست انتظار"),
    DONE("done", "انجام‌شده"),
    CANCELED("canceled", "لغو‌شده");

    companion object {
        fun from(value: String?): AppointmentStatus =
            entries.firstOrNull { it.value == value } ?: WAITING
    }
}

data class Customer(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val notes: String? = null,
    val createdAt: String? = null,
)

data class CustomerItem(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val notes: String?,
    val visitsCount: Int,
    val appointmentsCount: Int,
    val totalSpent: Long,
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class Service(
    val id: Long,
    val name: String,
    val sort: Int = 0,
    val active: Boolean = true,
)

data class AppointmentItem(
    val id: Long,
    val customerId: Long,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val serviceId: Long?,
    val serviceName: String?,
    val date: String,
    val time: String,
    val deposit: Long,
    val status: AppointmentStatus,
    val notes: String?,
    val visitId: Long?,
    val finalAmount: Long,
    val materialCost: Long,
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

data class Visit(
    val id: Long,
    val appointmentId: Long,
    val customerId: Long,
    val serviceId: Long?,
    val performedDate: String,
    val finalAmount: Long,
    val durationMinutes: Int?,
    val assistantCount: Int,
    val assistantCost: Long,
    val hairLengthCm: Double?,
    val hairVolume: String?,
    val hairCondition: String?,
    val workDone: String?,
    val notes: String?,
    val materialCost: Long,
    val customerName: String = "",
    val phone: String = "",
    val serviceName: String? = null,
) {
    val staffCount: Int get() = assistantCount + 1
}

data class Material(
    val id: Long,
    val name: String,
    val unit: String = "ml",
    val pricePerUnit: Long = 0,
    val notes: String? = null,
    val active: Boolean = true,
)

data class MaterialUsage(
    val id: Long,
    val visitId: Long,
    val materialId: Long,
    val materialName: String,
    val amount: Double,
    val unitPrice: Long,
    val unit: String,
) {
    val cost: Long get() = (amount * unitPrice).toLong()
}

data class MaterialInput(
    val materialId: Long,
    val amount: Double,
)

data class MonthSummary(
    val jy: Int,
    val jm: Int,
    val income: Long,
    val deposit: Long,
    val materialCost: Long,
    val assistantCost: Long,
    val visits: Int,
) {
    val profit: Long get() = income - materialCost - assistantCost
}

data class ServiceBreakdown(
    val name: String,
    val count: Int,
    val income: Long,
)

data class MaterialBreakdown(
    val name: String,
    val unit: String,
    val amount: Double,
    val cost: Long,
)

const val SPLIT_END_SERVICE_NAME = "موخوره‌گیری به روش روسی با قیچی"
val HAIR_VOLUME_OPTIONS = listOf("کم", "متوسط", "زیاد", "خیلی زیاد")

fun isSplitEndService(name: String?): Boolean = name?.contains("موخوره") == true
