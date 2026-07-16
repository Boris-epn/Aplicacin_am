package com.example.interfaces.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specialties")
data class SpecialtyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val colorHex: String,
    val iconRes: Int
)
