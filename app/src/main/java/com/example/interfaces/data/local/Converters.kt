package com.example.interfaces.data.local

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): Int = time.hour * 60 + time.minute

    @TypeConverter
    fun toLocalTime(value: Int): LocalTime = LocalTime.of(value / 60, value % 60)

    @TypeConverter
    fun fromWorkDays(days: Set<DayOfWeek>): String = days.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun toWorkDays(value: String): Set<DayOfWeek> =
        value.split(",").filter { it.isNotBlank() }.map { DayOfWeek.of(it.toInt()) }.toSet()
}
