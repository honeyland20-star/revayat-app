package ir.revayat.app.util

import android.icu.util.Calendar
import android.icu.util.ULocale

object Jalali {
    private val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )
    private val weekDays = listOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")

    /** Basij week = 29 Aban .. 5 Azar. */
    const val WEEK_START_DOY = 245 // 29 Aban
    const val WEEK_END_DOY = 251   // 5 Azar

    private fun now(): Calendar = Calendar.getInstance(ULocale("fa_IR@calendar=persian"))

    /** Day of the Jalali year (1..366). */
    fun dayOfYear(): Int {
        val c = now()
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return (if (m <= 6) (m - 1) * 31 else 186 + (m - 7) * 30) + d
    }

    /** 1..7 during Basij week, 0 before it, 8 after it. */
    fun basijWeekDay(): Int {
        val doy = dayOfYear()
        return when {
            doy < WEEK_START_DOY -> 0
            doy > WEEK_END_DOY -> 8
            else -> doy - WEEK_START_DOY + 1
        }
    }

    /** Days remaining until Basij week starts (0 if started or passed). */
    fun daysUntilWeek(): Int = (WEEK_START_DOY - dayOfYear()).coerceAtLeast(0)

    fun todayString(): String {
        val c = now()
        val wd = weekDays[c.get(Calendar.DAY_OF_WEEK) - 1]
        val d = c.get(Calendar.DAY_OF_MONTH)
        val m = months[c.get(Calendar.MONTH)]
        val y = c.get(Calendar.YEAR)
        return "$wd ${d.fa()} $m ${y.fa()}"
    }

    /** Label of the given Basij-week day, e.g. "۲۹ آبان". */
    fun weekDayLabel(day: Int): String = when (day) {
        1 -> "۲۹ آبان"
        2 -> "۳۰ آبان"
        else -> "${(day - 2).fa()} آذر"
    }
}

fun Int.fa(): String = toString().fa()

fun String.fa(): String {
    val sb = StringBuilder(length)
    for (ch in this) sb.append(if (ch in '0'..'9') ('۰' + (ch - '0')) else ch)
    return sb.toString()
}
