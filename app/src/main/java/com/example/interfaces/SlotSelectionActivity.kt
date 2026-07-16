package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

class SlotSelectionActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private val viewModel: SlotSelectionViewModel by viewModels {
        SlotSelectionViewModel.Factory(applicationContext, SessionManager.getUserId(this))
    }
    
    private var doctor: DoctorEntity? = null
    private var specialtyName = ""
    private var selectedSlot: SlotSelectionViewModel.Slot? = null
    private lateinit var slotAdapter: SlotAdapter

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
            
            viewModel.loadSlots(selectedDoctor)
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        slotAdapter = SlotAdapter { slot ->
            selectedSlot = slot
            findViewById<Button>(R.id.btn_confirm).text = "Confirmar: ${BookingUtils.formatIsoDateForDisplay(slot.date)} · ${slot.time}"
        }
        findViewById<RecyclerView>(R.id.rv_slots).apply {
            layoutManager = LinearLayoutManager(this@SlotSelectionActivity)
            adapter = slotAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SlotSelectionViewModel.SlotUiState.Loading -> { /* Mostrar progreso */ }
                    is SlotSelectionViewModel.SlotUiState.Empty -> {
                        Toast.makeText(this@SlotSelectionActivity, "No hay horarios disponibles", Toast.LENGTH_LONG).show()
                    }
                    is SlotSelectionViewModel.SlotUiState.Success -> {
                        slotAdapter.submitList(state.slots)
                    }
                }
            }
        }
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

    private fun proceedWithBooking(userId: Long, selectedDoctor: DoctorEntity, slot: SlotSelectionViewModel.Slot) {
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

    companion object {
        const val EXTRA_SPECIALTY_NAME = "specialty_name"
        const val EXTRA_DOCTOR_ID = "doctor_id"
    }
}
