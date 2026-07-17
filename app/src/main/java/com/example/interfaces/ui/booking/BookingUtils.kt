package com.example.interfaces.ui.booking

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object BookingUtils {
    private val ecuador = Locale("es", "EC")
    private val displayDateFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ecuador)
    private val compactDateFormat = DateTimeFormatter.ofPattern("d MMM", ecuador)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val fullMonthFormat = DateTimeFormatter.ofPattern("MMMM", ecuador)
    private val shortMonthFormat = DateTimeFormatter.ofPattern("MMM", ecuador)

    fun todayDisplayDate(): String {
        return LocalDate.now().format(displayDateFormat).capitalized()
    }

    fun formatDateForDisplay(date: LocalDate): String = compactDateFormat.format(date)

    /** Encabezado de la tira de días: "Julio 2026", o "Jul – Ago 2026" si la semana cruza de mes. */
    fun formatWeekLabel(monday: LocalDate, friday: LocalDate): String {
        val year = friday.year
        return if (monday.month == friday.month) {
            "${fullMonthFormat.format(monday).capitalized()} $year"
        } else {
            "${shortMonthFormat.format(monday).capitalized()} – ${shortMonthFormat.format(friday).capitalized()} $year"
        }
    }

    private fun String.capitalized(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(ecuador) else it.toString() }

    fun formatTimeForDisplay(time: LocalTime): String = timeFormat.format(time)
}
