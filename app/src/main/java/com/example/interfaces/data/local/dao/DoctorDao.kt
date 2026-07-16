package com.example.interfaces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interfaces.data.local.entity.DoctorEntity

@Dao
interface DoctorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<DoctorEntity>): List<Long>

    @Query("SELECT * FROM doctors WHERE specialty_id = :specialtyId ORDER BY fullName ASC")
    fun getBySpecialty(specialtyId: Long): List<DoctorEntity>

    @Query("SELECT * FROM doctors WHERE id = :doctorId LIMIT 1")
    fun getById(doctorId: Long): DoctorEntity?

    @Query("SELECT * FROM doctors ORDER BY fullName ASC")
    fun getAll(): List<DoctorEntity>

    @Query("SELECT * FROM doctors WHERE fullName = :fullName LIMIT 1")
    fun getByFullName(fullName: String): DoctorEntity?
}
