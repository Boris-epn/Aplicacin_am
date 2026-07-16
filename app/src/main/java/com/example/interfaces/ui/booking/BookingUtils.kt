package com.example.interfaces.ui.booking

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object BookingUtils {
    private val displayDateFormat = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "EC"))
    private val compactDateFormat = DateTimeFormatter.ofPattern("d MMM", Locale("es", "EC"))
    private val isoDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun todayDisplayDate(): String {
        return LocalDate.now().format(displayDateFormat)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "EC")) else it.toString() }
    }

    fun formatIsoDateForDisplay(isoDate: String): String {
        return try {
            val parsed = LocalDate.parse(isoDate, isoDateFormat)
            compactDateFormat.format(parsed)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun toIsoDate(date: LocalDate): String {
        return date.format(isoDateFormat)
    }

    fun parseIsoDate(isoDate: String): LocalDate {
        return try {
            LocalDate.parse(isoDate, isoDateFormat)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    fun generateUpcomingDates(days: Int = 7): List<String> {
        val today = LocalDate.now()
        return (0 until days).map {
            toIsoDate(today.plusDays(it.toLong()))
        }
    }
}
