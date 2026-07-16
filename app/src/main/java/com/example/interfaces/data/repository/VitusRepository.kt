package com.example.interfaces.data.repository

import android.content.Context
import com.example.interfaces.R
import com.example.interfaces.data.local.VitusDatabase
import com.example.interfaces.data.local.entity.AppointmentEntity
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.local.entity.SpecialtyEntity
import com.example.interfaces.data.local.entity.UserEntity
import com.example.interfaces.data.local.model.AppointmentSummary
import java.util.Calendar
import java.util.Locale

class VitusRepository private constructor(context: Context) {
    private val database = VitusDatabase.getInstance(context)
    private val userDao = database.userDao()
    private val specialtyDao = database.specialtyDao()
    private val doctorDao = database.doctorDao()
    private val appointmentDao = database.appointmentDao()

    suspend fun ensureSeedData() {
        val specialtyIds = ensureSpecialtiesSeeded()
        ensureDoctorsSeeded(specialtyIds)
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        documentNumber: String,
        phoneNumber: String,
        birthDate: String,
        pin: String
    ): Result<Long> {
        return runCatching {
            val normalizedEmail = normalizeEmail(email)
            require(userDao.getByEmail(normalizedEmail) == null) { "Ya existe un usuario con este correo." }
            userDao.insert(
                UserEntity(
                    fullName = fullName.trim(),
                    email = normalizedEmail,
                    documentNumber = documentNumber.trim(),
                    phoneNumber = phoneNumber.trim(),
                    birthDate = birthDate.trim(),
                    pin = pin.trim()
                )
            )
        }
    }

    suspend fun loginUser(email: String, pin: String): UserEntity? {
        ensureSeedData()
        return userDao.login(normalizeEmail(email), pin.trim())
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        ensureSeedData()
        return userDao.getById(userId)
    }

    suspend fun getNextAppointment(userId: Long): AppointmentSummary? {
        ensureSeedData()
        return appointmentDao.getNextSummaryForUser(userId)
    }

    suspend fun getAppointments(userId: Long): List<AppointmentSummary> {
        ensureSeedData()
        return appointmentDao.getAllSummariesForUser(userId)
    }

    suspend fun getSummaryByAppointmentId(appointmentId: Long): AppointmentSummary? {
        ensureSeedData()
        return appointmentDao.getSummaryById(appointmentId)
    }

    suspend fun getBookedTimesForDoctorAndDate(doctorId: Long, appointmentDate: String): List<String> {
        ensureSeedData()
        return appointmentDao.getBookedTimesForDoctorAndDate(doctorId, appointmentDate)
    }

    suspend fun cancelAppointment(appointmentId: Long) {
        ensureSeedData()
        appointmentDao.updateStatus(appointmentId, "CANCELADA")
    }

    suspend fun getSpecialties(): List<SpecialtyEntity> {
        ensureSeedData()
        return specialtyDao.getAll()
    }

    suspend fun getDoctorsBySpecialty(specialtyId: Long): List<DoctorEntity> {
        ensureSeedData()
        return doctorDao.getBySpecialty(specialtyId)
    }

    suspend fun getSpecialtyById(specialtyId: Long): SpecialtyEntity? {
        ensureSeedData()
        return specialtyDao.getById(specialtyId)
    }

    suspend fun getDoctorById(doctorId: Long): DoctorEntity? {
        ensureSeedData()
        return doctorDao.getById(doctorId)
    }

    suspend fun createAppointment(
        userId: Long,
        specialtyId: Long,
        doctorId: Long,
        appointmentDate: String,
        appointmentTime: String,
        room: String,
        reason: String
    ): Long {
        ensureSeedData()
        return appointmentDao.insert(
            AppointmentEntity(
                userId = userId,
                specialtyId = specialtyId,
                doctorId = doctorId,
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                room = room,
                status = "ACTIVA",
                reason = reason
            )
        )
    }

    suspend fun hasAppointmentAt(userId: Long, date: String, time: String): Boolean {
        ensureSeedData()
        return appointmentDao.countUserAppointmentsAt(userId, date, time) > 0
    }

    private suspend fun ensureSpecialtiesSeeded(): Map<String, Long> {
        val items = listOf(
            SpecialtyEntity(
                name = "Cardiología",
                description = "Cuidado integral del corazón y sistema circulatorio.",
                colorHex = "#C53030",
                iconRes = R.drawable.heart
            ),
            SpecialtyEntity(
                name = "Traumatología",
                description = "Atención de huesos, articulaciones y lesiones.",
                colorHex = "#1E429F",
                iconRes = R.drawable.bone
            ),
            SpecialtyEntity(
                name = "Oftalmología",
                description = "Diagnóstico y tratamiento de la salud visual.",
                colorHex = "#6B21A8",
                iconRes = R.drawable.eye
            ),
            SpecialtyEntity(
                name = "Medicina General",
                description = "Control general, seguimiento y primera atención.",
                colorHex = "#7BA289",
                iconRes = R.drawable.medicine
            ),
            SpecialtyEntity(
                name = "Neurología",
                description = "Atención del cerebro y sistema nervioso.",
                colorHex = "#9B591C",
                iconRes = R.drawable.brain
            ),
            SpecialtyEntity(
                name = "Geriatría",
                description = "Cuidado integral para adultos mayores.",
                colorHex = "#087452",
                iconRes = R.drawable.geriatria
            )
        )
        val existingNames = specialtyDao.getAll().map { it.name }.toSet()
        val missingItems = items.filter { it.name !in existingNames }
        if (missingItems.isNotEmpty()) specialtyDao.insertAll(missingItems)
        return specialtyDao.getAll().associate { it.name to it.id }
    }

    private suspend fun ensureDoctorsSeeded(specialtyIds: Map<String, Long>): Map<String, Long> {
        val items = listOf(
            DoctorEntity(
                fullName = "Dra. Ana Ríos",
                specialtyId = specialtyIds.getValue("Cardiología"),
                schedule = "Lun a Vie 08:00 - 12:00",
                consultationRoom = "Consultorio 301",
                phone = "1800-VITUS",
                photoRes = R.drawable.heart
            ),
            DoctorEntity(
                fullName = "Dr. Luis Torres",
                specialtyId = specialtyIds.getValue("Cardiología"),
                schedule = "Lun a Sáb 14:00 - 18:00",
                consultationRoom = "Consultorio 302",
                phone = "1800-VITUS",
                photoRes = R.drawable.heart
            ),
            DoctorEntity(
                fullName = "Dra. Sofía Paredes",
                specialtyId = specialtyIds.getValue("Traumatología"),
                schedule = "Mar a Vie 09:00 - 13:00",
                consultationRoom = "Consultorio 210",
                phone = "1800-VITUS",
                photoRes = R.drawable.bone
            ),
            DoctorEntity(
                fullName = "Dr. Marco Suárez",
                specialtyId = specialtyIds.getValue("Traumatología"),
                schedule = "Lun a Jue 15:00 - 19:00",
                consultationRoom = "Consultorio 211",
                phone = "1800-VITUS",
                photoRes = R.drawable.bone
            ),
            DoctorEntity(
                fullName = "Dra. Valeria Mena",
                specialtyId = specialtyIds.getValue("Oftalmología"),
                schedule = "Lun a Vie 10:00 - 14:00",
                consultationRoom = "Consultorio 118",
                phone = "1800-VITUS",
                photoRes = R.drawable.eye
            ),
            DoctorEntity(
                fullName = "Dr. Carlos Vega",
                specialtyId = specialtyIds.getValue("Medicina General"),
                schedule = "Todos los días 08:00 - 16:00",
                consultationRoom = "Consultorio 101",
                phone = "1800-VITUS",
                photoRes = R.drawable.medicine
            ),
            DoctorEntity(
                fullName = "Dra. Paula Ruiz",
                specialtyId = specialtyIds.getValue("Neurología"),
                schedule = "Lun a Vie 09:00 - 13:00",
                consultationRoom = "Consultorio 220",
                phone = "1800-VITUS",
                photoRes = R.drawable.brain
            ),
            DoctorEntity(
                fullName = "Dr. Mateo León",
                specialtyId = specialtyIds.getValue("Geriatría"),
                schedule = "Lun a Vie 08:00 - 12:00",
                consultationRoom = "Consultorio 120",
                phone = "1800-VITUS",
                photoRes = R.drawable.geriatria
            )
        )
        val existingNames = doctorDao.getAll().map { it.fullName }.toSet()
        val missingItems = items.filter { it.fullName !in existingNames }
        if (missingItems.isNotEmpty()) doctorDao.insertAll(missingItems)
        return doctorDao.getAll().associate { it.fullName to it.id }
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase(Locale.ROOT)
    }

    companion object {
        @Volatile
        private var INSTANCE: VitusRepository? = null

        fun getInstance(context: Context): VitusRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VitusRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
