package com.example.interfaces.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interfaces.data.local.entity.AppointmentEntity
import com.example.interfaces.data.local.model.AppointmentSummary

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appointment: AppointmentEntity): Long

    @Query(
        """
        SELECT a.id,
               a.user_id,
               a.specialty_id,
               a.doctor_id,
               a.appointment_date,
               a.appointment_time,
               a.room,
               a.status,
               a.reason,
               a.created_at,
               s.name AS specialty_name,
               d.fullName AS doctor_name
        FROM appointments a
        INNER JOIN specialties s ON s.id = a.specialty_id
        INNER JOIN doctors d ON d.id = a.doctor_id
        WHERE a.user_id = :userId AND a.status = 'ACTIVA'
        ORDER BY a.appointment_date ASC, a.appointment_time ASC
        LIMIT 1
        """
    )
    fun getNextSummaryForUser(userId: Long): AppointmentSummary?

    @Query(
        """
        SELECT a.id,
               a.user_id,
               a.specialty_id,
               a.doctor_id,
               a.appointment_date,
               a.appointment_time,
               a.room,
               a.status,
               a.reason,
               a.created_at,
               s.name AS specialty_name,
               d.fullName AS doctor_name
        FROM appointments a
        INNER JOIN specialties s ON s.id = a.specialty_id
        INNER JOIN doctors d ON d.id = a.doctor_id
        WHERE a.user_id = :userId
        ORDER BY a.appointment_date ASC, a.appointment_time ASC
        """
    )
    fun getAllSummariesForUser(userId: Long): List<AppointmentSummary>

    @Query(
        """
        SELECT a.id,
               a.user_id,
               a.specialty_id,
               a.doctor_id,
               a.appointment_date,
               a.appointment_time,
               a.room,
               a.status,
               a.reason,
               a.created_at,
               s.name AS specialty_name,
               d.fullName AS doctor_name
        FROM appointments a
        INNER JOIN specialties s ON s.id = a.specialty_id
        INNER JOIN doctors d ON d.id = a.doctor_id
        WHERE a.id = :appointmentId
        LIMIT 1
        """
    )
    fun getSummaryById(appointmentId: Long): AppointmentSummary?

    @Query(
        """
        SELECT appointment_time
        FROM appointments
        WHERE doctor_id = :doctorId
          AND appointment_date = :appointmentDate
          AND status = 'ACTIVA'
        ORDER BY appointment_time ASC
        """
    )
    fun getBookedTimesForDoctorAndDate(doctorId: Long, appointmentDate: String): List<String>

    @Query("UPDATE appointments SET status = :status WHERE id = :appointmentId")
    fun updateStatus(appointmentId: Long, status: String): Int
}
