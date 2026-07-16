package com.example.interfaces.ui.booking

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object BookingUtils {
    private val displayDateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "EC"))
    private val compactDateFormat = SimpleDateFormat("d MMM", Locale("es", "EC"))
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun todayDisplayDate(): String {
        return displayDateFormat.format(Calendar.getInstance().time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "EC")) else it.toString() }
    }

    fun formatIsoDateForDisplay(isoDate: String): String {
        return try {
            val parsed = isoDateFormat.parse(isoDate)
            if (parsed != null) {
                compactDateFormat.format(parsed)
            } else {
                isoDate
            }
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatCalendarForDisplay(calendar: Calendar): String {
        return displayDateFormat.format(calendar.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "EC")) else it.toString() }
    }

    fun toIsoDate(calendar: Calendar): String {
        return isoDateFormat.format(calendar.time)
    }

    fun parseIsoDate(isoDate: String): Calendar {
        return Calendar.getInstance().apply {
            time = isoDateFormat.parse(isoDate) ?: time
        }
    }

    fun formatTimeLabel(time: String): String {
        return time
    }

    fun generateTimeSlots(): List<String> {
        return listOf(
            "08:00",
            "08:30",
            "09:00",
            "09:30",
            "10:00",
            "10:30",
            "11:00",
            "11:30",
            "12:00",
            "12:30",
            "13:00",
            "13:30",
            "14:00",
            "14:30",
            "15:00",
            "15:30",
            "16:00"
        )
    }

    fun generateUpcomingDates(days: Int = 7): List<String> {
        val calendar = Calendar.getInstance()
        return (0 until days).map {
            val next = calendar.clone() as Calendar
            next.add(Calendar.DAY_OF_MONTH, it)
            toIsoDate(next)
        }
    }
}
