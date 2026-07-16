package com.example.interfaces

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.data.session.SessionManager
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SlotSelectionActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private val viewModel: SlotSelectionViewModel by viewModels {
        SlotSelectionViewModel.Factory(applicationContext, SessionManager.getUserId(this))
    }

    private var doctor: DoctorEntity? = null
    private var specialtyName = ""
    private var selectedTime: LocalTime? = null
    private lateinit var slotAdapter: SlotAdapter

    private val dayCardIds = listOf(R.id.card_day_one, R.id.card_day_two, R.id.card_day_three, R.id.card_day_four, R.id.card_day_five)
    private val dayLabelIds = listOf(R.id.txt_day_label_one, R.id.txt_day_label_two, R.id.txt_day_label_three, R.id.txt_day_label_four, R.id.txt_day_label_five)
    private val dayNumberIds = listOf(R.id.txt_day_number_one, R.id.txt_day_number_two, R.id.txt_day_number_three, R.id.txt_day_number_four, R.id.txt_day_number_five)
    private val dayNameFormat = DateTimeFormatter.ofPattern("EEE", Locale("es", "EC"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slot_selection)

        specialtyName = intent.getStringExtra(EXTRA_SPECIALTY_NAME) ?: run { finish(); return }
        val doctorId = intent.getLongExtra(EXTRA_DOCTOR_ID, -1L)
        if (doctorId == -1L) {
            finish()
            return
        }

        setupRecyclerView()

        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_confirm).setOnClickListener { createAppointment() }

        lifecycleScope.launch {
            doctor = withContext(Dispatchers.IO) { repository.getDoctorById(doctorId) }
            val selectedDoctor = doctor ?: run { finish(); return@launch }

            findViewById<TextView>(R.id.txt_doctor).text = selectedDoctor.fullName
            findViewById<TextView>(R.id.txt_specialty).text = if (specialtyName == "Medicina General") "Med. General" else specialtyName
            findViewById<TextView>(R.id.txt_initials).text = initialsFor(selectedDoctor.fullName)

            viewModel.loadDayStrip(selectedDoctor)
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        slotAdapter = SlotAdapter { time ->
            selectedTime = time
            updateConfirmButtonText()
        }
        findViewById<RecyclerView>(R.id.rv_slots).apply {
            layoutManager = LinearLayoutManager(this@SlotSelectionActivity)
            adapter = slotAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.dayOptions.collect { days -> bindDayStrip(days) }
        }
        lifecycleScope.launch {
            viewModel.timeState.collect { state ->
                when (state) {
                    is SlotSelectionViewModel.TimeUiState.Loading -> { /* Mostrar progreso */ }
                    is SlotSelectionViewModel.TimeUiState.Empty -> {
                        selectedTime = null
                        slotAdapter.submitList(emptyList())
                        Toast.makeText(this@SlotSelectionActivity, "No hay horarios disponibles ese día", Toast.LENGTH_SHORT).show()
                    }
                    is SlotSelectionViewModel.TimeUiState.Success -> {
                        slotAdapter.submitList(state.times)
                    }
                }
                updateConfirmButtonText()
            }
        }
    }

    private fun bindDayStrip(days: List<SlotSelectionViewModel.DayOption>) {
        days.forEachIndexed { index, day ->
            val card = findViewById<CardView>(dayCardIds[index])
            val label = findViewById<TextView>(dayLabelIds[index])
            val number = findViewById<TextView>(dayNumberIds[index])

            label.text = dayNameFormat.format(day.date).replaceFirstChar { it.uppercase() }
            number.text = day.date.dayOfMonth.toString()

            when {
                day.selected -> {
                    card.setCardBackgroundColor(Color.parseColor("#173B63"))
                    label.setTextColor(Color.WHITE)
                    number.setTextColor(Color.WHITE)
                }
                day.enabled -> {
                    card.setCardBackgroundColor(Color.WHITE)
                    label.setTextColor(Color.parseColor("#6F7782"))
                    number.setTextColor(Color.parseColor("#17324F"))
                }
                else -> {
                    card.setCardBackgroundColor(Color.parseColor("#F0F1F3"))
                    label.setTextColor(Color.parseColor("#C2C7CC"))
                    number.setTextColor(Color.parseColor("#C2C7CC"))
                }
            }

            card.isClickable = day.enabled
            card.setOnClickListener { if (day.enabled) viewModel.selectDay(day.date) }
        }
    }

    private fun updateConfirmButtonText() {
        val date = viewModel.selectedDate
        val time = selectedTime
        findViewById<Button>(R.id.btn_confirm).text = if (date != null && time != null) {
            "Confirmar: ${BookingUtils.formatDateForDisplay(date)} · ${BookingUtils.formatTimeForDisplay(time)}"
        } else {
            "Confirmar Cita"
        }
    }

    private fun createAppointment() {
        val selectedDoctor = doctor ?: return
        val date = viewModel.selectedDate ?: return
        val time = selectedTime ?: return
        val userId = SessionManager.getUserId(this)
        if (userId == -1L) {
            Toast.makeText(this, "No se pudo identificar tu sesión", Toast.LENGTH_SHORT).show()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar Cita")
            .setMessage("¿Está seguro de agendar la cita para el ${BookingUtils.formatDateForDisplay(date)} a las ${BookingUtils.formatTimeForDisplay(time)} con ${selectedDoctor.fullName}?")
            .setPositiveButton("Sí") { _, _ ->
                proceedWithBooking(userId, selectedDoctor, date, time)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun proceedWithBooking(userId: Long, selectedDoctor: DoctorEntity, date: java.time.LocalDate, time: LocalTime) {
        lifecycleScope.launch {
            val specialty = withContext(Dispatchers.IO) {
                repository.getSpecialties().firstOrNull { it.name == specialtyName }
            } ?: return@launch

            val doctorOccupied = withContext(Dispatchers.IO) {
                repository.getBookedTimesForDoctorAndDate(selectedDoctor.id, date).contains(time)
            }
            if (doctorOccupied) {
                Toast.makeText(this@SlotSelectionActivity, "Ese horario ya fue ocupado por otro paciente", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val userConflict = withContext(Dispatchers.IO) {
                repository.hasAppointmentAt(userId, date, time)
            }
            if (userConflict) {
                Toast.makeText(this@SlotSelectionActivity, "Ya tienes otra cita agendada a esta misma hora", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val appointmentId = withContext(Dispatchers.IO) {
                repository.createAppointment(userId, specialty.id, selectedDoctor.id, date, time, selectedDoctor.consultationRoom, "Agendamiento desde la app")
            }
            startActivity(Intent(this@SlotSelectionActivity, ConfirmationActivity::class.java).apply {
                putExtra(ConfirmationActivity.EXTRA_APPOINTMENT_ID, appointmentId)
            })
            finish()
        }
    }

    private fun initialsFor(name: String): String = name.replace("Dra. ", "").replace("Dr. ", "")
        .split(" ").take(2).joinToString("") { it.first().toString() }

    companion object {
        const val EXTRA_SPECIALTY_NAME = "specialty_name"
        const val EXTRA_DOCTOR_ID = "doctor_id"
    }
}
