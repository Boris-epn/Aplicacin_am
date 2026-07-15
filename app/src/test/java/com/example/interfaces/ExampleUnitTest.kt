package com.example.interfaces

import com.example.interfaces.ui.booking.BookingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas de negocio independientes de Android para el flujo de citas.
 */
class ExampleUnitTest {
    @Test
    fun generateTimeSlots_returnsHalfHourSlotsInOrder() {
        val slots = BookingUtils.generateTimeSlots()

        assertEquals("08:00", slots.first())
        assertEquals("16:00", slots.last())
        assertEquals(17, slots.size)
        assertTrue(slots.zipWithNext().all { (current, next) -> current < next })
    }

    @Test
    fun isoDate_roundTripPreservesCalendarDay() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.JULY, 15, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val isoDate = BookingUtils.toIsoDate(calendar)
        val parsed = BookingUtils.parseIsoDate(isoDate)

        assertEquals("2026-07-15", isoDate)
        assertEquals(15, parsed.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(java.util.Calendar.JULY, parsed.get(java.util.Calendar.MONTH))
    }
}
