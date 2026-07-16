package com.example.interfaces.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.example.interfaces.data.local.entity.AppointmentEntity

data class AppointmentSummary(
    @Embedded val appointment: AppointmentEntity,
    @ColumnInfo(name = "specialty_name")
    val specialtyName: String,
    @ColumnInfo(name = "doctor_name")
    val doctorName: String
)
