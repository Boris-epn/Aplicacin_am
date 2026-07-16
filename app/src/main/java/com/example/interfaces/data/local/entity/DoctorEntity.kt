package com.example.interfaces.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    tableName = "doctors",
    foreignKeys = [
        ForeignKey(
            entity = SpecialtyEntity::class,
            parentColumns = ["id"],
            childColumns = ["specialty_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("specialty_id")]
)
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    @ColumnInfo(name = "specialty_id")
    val specialtyId: Long,
    val workDays: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    @ColumnInfo(name = "consultation_room")
    val consultationRoom: String,
    val phone: String,
    val photoRes: Int
)
