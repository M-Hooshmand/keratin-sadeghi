package com.keratinsadeghi.salon.util

import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

data class JDate(val jy: Int, val jm: Int, val jd: Int)

private val BREAKS = intArrayOf(
    -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
    1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
)

val MONTH_NAMES = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

val WEEKDAYS = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
val WEEKDAYS_SHORT = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

private data class JalCal(val leap: Int, val gy: Int, val march: Int)

private fun div(a: Int, b: Int): Int = a / b
private fun mod(a: Int, b: Int): Int = a - (a / b) * b

private fun jalCal(jy: Int): JalCal {
    val gy = jy + 621
    var leapJ = -14
    var jp = BREAKS[0]
    var jump = 0
    var i = 1
    while (i < BREAKS.size) {
        val jm = BREAKS[i]
        jump = jm - jp
        if (jy < jm) break
        leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
        jp = jm
        i++
    }
    var n = jy - jp
    leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
    if (mod(jump, 33) == 4 && jump - n == 4) leapJ++
    val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
    val march = 20 + leapJ - leapG
    if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
    var leap = mod(mod(n + 1, 33) - 1, 4)
    if (leap == -1) leap = 4
    return JalCal(leap, gy, march)
}

private fun g2d(gy: Int, gm: Int, gd: Int): Int {
    var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
        div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
    d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752
    return d
}

private data class GDate(val gy: Int, val gm: Int, val gd: Int)

private fun d2g(jdn: Int): GDate {
    var j = 4 * jdn + 139361631
    j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
    val i = div(mod(j, 1461), 4) * 5 + 308
    val gd = div(mod(i, 153), 5) + 1
    val gm = mod(div(i, 153), 12) + 1
    val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
    return GDate(gy, gm, gd)
}

private fun j2d(jy: Int, jm: Int, jd: Int): Int {
    val r = jalCal(jy)
    return g2d(r.gy, 3, r.march) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
}

private fun d2j(jdn: Int): JDate {
    val gy = d2g(jdn).gy
    var jy = gy - 621
    val r = jalCal(jy)
    var k = jdn - g2d(gy, 3, r.march)
    if (k >= 0) {
        if (k <= 185) return JDate(jy, 1 + k / 31, mod(k, 31) + 1)
        k -= 186
    } else {
        jy -= 1
        k += 179
        if (r.leap == 1) k += 1
    }
    return JDate(jy, 7 + k / 30, mod(k, 30) + 1)
}

fun isLeapJalaliYear(jy: Int): Boolean = jalCal(jy).leap == 0

fun jalaliMonthLength(jy: Int, jm: Int): Int = when {
    jm <= 6 -> 31
    jm <= 11 -> 30
    isLeapJalaliYear(jy) -> 30
    else -> 29
}

fun toJalali(date: LocalDate): JDate = d2j(g2d(date.year, date.monthValue, date.dayOfMonth))

fun jalaliToIso(jy: Int, jm: Int, jd: Int): String {
    val g = d2g(j2d(jy, jm, jd))
    return "%04d-%02d-%02d".format(g.gy, g.gm, g.gd)
}

fun fromJalali(jy: Int, jm: Int, jd: Int): LocalDate = LocalDate.parse(jalaliToIso(jy, jm, jd))

fun isoToJalali(iso: String): JDate {
    val parts = iso.split("-")
    if (parts.size != 3) return toJalali(LocalDate.now())
    return toJalali(LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
}

fun todayIso(): String = LocalDate.now().toString()

fun todayJalali(): JDate = toJalali(LocalDate.now())

fun currentJalaliYear(): Int = todayJalali().jy

fun weekdayIndex(iso: String): Int = (LocalDate.parse(iso).dayOfWeek.value + 1) % 7

fun weekdayName(iso: String): String = WEEKDAYS[weekdayIndex(iso)]

fun monthRange(jy: Int, jm: Int): Pair<String, String> {
    val start = jalaliToIso(jy, jm, 1)
    val end = jalaliToIso(jy, jm, jalaliMonthLength(jy, jm))
    return start to end
}

fun yearRange(jy: Int): Pair<String, String> =
    jalaliToIso(jy, 1, 1) to jalaliToIso(jy, 12, jalaliMonthLength(jy, 12))

fun addJalaliMonths(jy: Int, jm: Int, delta: Int): Pair<Int, Int> {
    val total = jy * 12 + (jm - 1) + delta
    return total / 12 to (total % 12) + 1
}

fun toPersianDigits(value: String): String {
    val fa = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return buildString {
        for (ch in value) append(if (ch in '0'..'9') fa[ch - '0'] else ch)
    }
}

fun toEnglishDigits(value: String): String {
    val en = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    return buildString {
        for (ch in value) {
            val idx = "۰۱۲۳۴۵۶۷۸۹".indexOf(ch)
            append(if (idx >= 0) en[idx] else ch)
        }
    }
}

fun parseNumber(value: String?): Long {
    if (value.isNullOrBlank()) return 0
    val cleaned = toEnglishDigits(value).replace(",", "").replace("٬", "")
        .replace("٫", ".").filter { it.isDigit() || it == '.' || it == '-' }
    return cleaned.toDoubleOrNull()?.toLong() ?: 0
}

fun parseDouble(value: String?): Double {
    if (value.isNullOrBlank()) return 0.0
    val cleaned = toEnglishDigits(value).replace(",", "").replace("٬", "")
        .replace("٫", ".").filter { it.isDigit() || it == '.' || it == '-' }
    return cleaned.toDoubleOrNull() ?: 0.0
}

fun formatNumber(value: Long): String =
    toPersianDigits(String.format(Locale.US, "%,d", value))

fun formatNumber(value: Int): String = formatNumber(value.toLong())

fun formatDecimal(value: Double): String {
    val text = if (abs(value - value.toLong()) < 0.001) value.toLong().toString()
    else String.format(Locale.US, "%.1f", value)
    return toPersianDigits(text)
}

fun formatToman(value: Long): String = "${formatNumber(value)} تومان"

fun formatMl(value: Double): String = "${formatDecimal(value)} میلی‌لیتر"

fun timeLabel(time: String): String = toPersianDigits(time)

fun formatJalali(iso: String, withWeekday: Boolean = false): String {
    val j = isoToJalali(iso)
    val base = "${toPersianDigits(j.jd.toString())} ${MONTH_NAMES[j.jm - 1]} ${toPersianDigits(j.jy.toString())}"
    return if (withWeekday) "${weekdayName(iso)}، $base" else base
}

fun formatJalali(j: JDate, withWeekday: Boolean = false): String {
    return formatJalali(jalaliToIso(j.jy, j.jm, j.jd), withWeekday)
}
