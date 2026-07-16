package com.example.interfaces.ui.booking

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object BookingUtils {
    private val displayDateFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "EC"))
    private val compactDateFormat = DateTimeFormatter.ofPattern("d MMM", Locale("es", "EC"))
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun todayDisplayDate(): String {
        return LocalDate.now().format(displayDateFormat)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "EC")) else it.toString() }
    }

    fun formatDateForDisplay(date: LocalDate): String = compactDateFormat.format(date)

    fun formatTimeForDisplay(time: LocalTime): String = timeFormat.format(time)
}
