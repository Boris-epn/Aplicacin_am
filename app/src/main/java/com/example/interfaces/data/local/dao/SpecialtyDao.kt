package com.example.interfaces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interfaces.data.local.entity.SpecialtyEntity

@Dao
interface SpecialtyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<SpecialtyEntity>): List<Long>

    @Query("SELECT * FROM specialties ORDER BY name ASC")
    fun getAll(): List<SpecialtyEntity>

    @Query("SELECT * FROM specialties WHERE id = :specialtyId LIMIT 1")
    fun getById(specialtyId: Long): SpecialtyEntity?

    @Query("SELECT * FROM specialties WHERE name = :name LIMIT 1")
    fun getByName(name: String): SpecialtyEntity?
}
