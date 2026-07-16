package com.example.interfaces.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SpecialtyEntity::class,
            parentColumns = ["id"],
            childColumns = ["specialty_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctor_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("user_id"), Index("specialty_id"), Index("doctor_id")]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "specialty_id")
    val specialtyId: Long,
    @ColumnInfo(name = "doctor_id")
    val doctorId: Long,
    @ColumnInfo(name = "appointment_date")
    val appointmentDate: String,
    @ColumnInfo(name = "appointment_time")
    val appointmentTime: String,
    val room: String,
    val status: String,
    val reason: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
