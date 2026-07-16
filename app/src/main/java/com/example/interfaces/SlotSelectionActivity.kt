package com.example.interfaces

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.data.session.SessionManager
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SlotSelectionActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private var doctor: DoctorEntity? = null
    private var specialtyName = ""
    private var selectedSlot: Slot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_selection)

        specialtyName = intent.getStringExtra(EXTRA_SPECIALTY_NAME) ?: run { finish(); return }
        val doctorId = intent.getLongExtra(EXTRA_DOCTOR_ID, -1L)
        if (doctorId == -1L) {
            finish()
            return
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_confirm).setOnClickListener { createAppointment() }

        lifecycleScope.launch {
            doctor = withContext(Dispatchers.IO) { repository.getDoctorById(doctorId) }
            val selectedDoctor = doctor ?: run { finish(); return@launch }
            findViewById<TextView>(R.id.txt_doctor).text = selectedDoctor.fullName
            findViewById<TextView>(R.id.txt_specialty).text = if (specialtyName == "Medicina General") "Med. General" else specialtyName
            findViewById<TextView>(R.id.txt_initials).text = initialsFor(selectedDoctor.fullName)
            bindSlots()
        }
    }

    private fun bindSlots() {
        lifecycleScope.launch {
            val selectedDoctor = doctor ?: return@launch
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val firstDate = BookingUtils.toIsoDate(calendar)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val secondDate = BookingUtils.toIsoDate(calendar)

            val potentialSlots = listOf(
                Slot(firstDate, "08:30"), Slot(firstDate, "10:30"), Slot(firstDate, "11:30"), Slot(firstDate, "15:00"),
                Slot(secondDate, "09:00"), Slot(secondDate, "11:00"), Slot(secondDate, "14:00"), Slot(secondDate, "16:00")
            )

            // Filtrar por horario del doctor y disponibilidad
            val availableSlots = withContext(Dispatchers.IO) {
                val userId = SessionManager.getUserId(this@SlotSelectionActivity)
                potentialSlots.filter { slot ->
                    val isWithinSchedule = isTimeInSchedule(slot.time, selectedDoctor.schedule)
                    val isBooked = repository.getBookedTimesForDoctorAndDate(selectedDoctor.id, slot.date).contains(slot.time)
                    val userConflict = if (userId != -1L) repository.hasAppointmentAt(userId, slot.date, slot.time) else false
                    isWithinSchedule && !isBooked && !userConflict
                }.take(5) // Solo tomamos hasta 5 para los 5 cards que tenemos en el layout
            }

            val cardIds = listOf(R.id.card_slot_one, R.id.card_slot_two, R.id.card_slot_three, R.id.card_slot_four, R.id.card_slot_five)
            val dateIds = listOf(R.id.txt_date_one, R.id.txt_date_two, R.id.txt_date_three, R.id.txt_date_four, R.id.txt_date_five)
            val timeIds = listOf(R.id.txt_time_one, R.id.txt_time_two, R.id.txt_time_three, R.id.txt_time_four, R.id.txt_time_five)
            val statusIds = listOf(R.id.txt_status_one, R.id.txt_status_two, R.id.txt_status_three, R.id.txt_status_four, R.id.txt_status_five)

            if (availableSlots.isEmpty()) {
                Toast.makeText(this@SlotSelectionActivity, "No hay horarios disponibles próximamente", Toast.LENGTH_LONG).show()
                return@launch
            }

            cardIds.forEachIndexed { index, cardId ->
                val card = findViewById<CardView>(cardId)
                if (index < availableSlots.size) {
                    val slot = availableSlots[index]
                    card.visibility = View.VISIBLE
                    findViewById<TextView>(dateIds[index]).text = BookingUtils.formatIsoDateForDisplay(slot.date)
                    findViewById<TextView>(timeIds[index]).text = slot.time
                    findViewById<TextView>(statusIds[index]).text = ""
                    card.setOnClickListener {
                        selectSlot(slot, index, cardIds, dateIds, timeIds, statusIds, availableSlots.size)
                    }
                } else {
                    card.visibility = View.GONE
                }
            }
            selectSlot(availableSlots.first(), 0, cardIds, dateIds, timeIds, statusIds, availableSlots.size)
        }
    }

    private fun isTimeInSchedule(time: String, schedule: String): Boolean {
        return try {
            val times = Regex("(\\d{2}:\\d{2})").findAll(schedule).map { it.value }.toList()
            if (times.size < 2) return true

            val start = times.first().replace(":", "").toInt()
            val end = times.last().replace(":", "").toInt()
            val current = time.replace(":", "").toInt()

            current in start..end
        } catch (e: Exception) {
            true
        }
    }

    private fun selectSlot(
        slot: Slot,
        selectedIndex: Int,
        cardIds: List<Int>,
        dateIds: List<Int>,
        timeIds: List<Int>,
        statusIds: List<Int>,
        availableSize: Int
    ) {
        selectedSlot = slot
        cardIds.forEachIndexed { index, cardId ->
            if (index < availableSize) {
                val selected = index == selectedIndex
                findViewById<CardView>(cardId).setCardBackgroundColor(Color.parseColor(if (selected) "#173B63" else "#FFFFFF"))
                findViewById<TextView>(dateIds[index]).setTextColor(Color.parseColor(if (selected) "#FFFFFF" else "#17324F"))
                findViewById<TextView>(timeIds[index]).setTextColor(Color.parseColor(if (selected) "#FFFFFF" else "#6F7782"))
                findViewById<TextView>(statusIds[index]).apply {
                    text = if (selected) "✓" else ""
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor(if (selected) "#5E8F73" else "#FFFFFF"))
                }
            }
        }
        findViewById<Button>(R.id.btn_confirm).text = "Confirmar: ${BookingUtils.formatIsoDateForDisplay(slot.date)} · ${slot.time}"
    }

    private fun createAppointment() {
        val selectedDoctor = doctor ?: return
        val slot = selectedSlot ?: return
        val userId = SessionManager.getUserId(this)
        if (userId == -1L) {
            Toast.makeText(this, "No se pudo identificar tu sesión", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar Cita")
            .setMessage("¿Está seguro de agendar la cita para el ${BookingUtils.formatIsoDateForDisplay(slot.date)} a las ${slot.time} con ${selectedDoctor.fullName}?")
            .setPositiveButton("Sí") { _, _ ->
                proceedWithBooking(userId, selectedDoctor, slot)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun proceedWithBooking(userId: Long, selectedDoctor: DoctorEntity, slot: Slot) {
        lifecycleScope.launch {
            val specialty = withContext(Dispatchers.IO) {
                repository.getSpecialties().firstOrNull { it.name == specialtyName }
            } ?: return@launch

            val doctorOccupied = withContext(Dispatchers.IO) {
                repository.getBookedTimesForDoctorAndDate(selectedDoctor.id, slot.date).contains(slot.time)
            }
            if (doctorOccupied) {
                Toast.makeText(this@SlotSelectionActivity, "Ese horario ya fue ocupado por otro paciente", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val userConflict = withContext(Dispatchers.IO) {
                repository.hasAppointmentAt(userId, slot.date, slot.time)
            }
            if (userConflict) {
                Toast.makeText(this@SlotSelectionActivity, "Ya tienes otra cita agendada a esta misma hora", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val appointmentId = withContext(Dispatchers.IO) {
                repository.createAppointment(userId, specialty.id, selectedDoctor.id, slot.date, slot.time, selectedDoctor.consultationRoom, "Agendamiento desde la app")
            }
            startActivity(Intent(this@SlotSelectionActivity, ConfirmationActivity::class.java).apply {
                putExtra(ConfirmationActivity.EXTRA_APPOINTMENT_ID, appointmentId)
            })
            finish()
        }
    }

    private fun initialsFor(name: String): String = name.replace("Dra. ", "").replace("Dr. ", "")
        .split(" ").take(2).joinToString("") { it.first().toString() }

    private data class Slot(val date: String, val time: String)

    companion object {
        const val EXTRA_SPECIALTY_NAME = "specialty_name"
        const val EXTRA_DOCTOR_ID = "doctor_id"
    }
}
