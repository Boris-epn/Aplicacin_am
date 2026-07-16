package com.example.interfaces

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.data.session.SessionManager
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SlotSelectionViewModel(
    private val repository: VitusRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<SlotUiState>(SlotUiState.Loading)
    val uiState: StateFlow<SlotUiState> = _uiState

    fun loadSlots(doctor: DoctorEntity) {
        viewModelScope.launch {
            _uiState.value = SlotUiState.Loading
            val potentialSlots = generatePotentialSlots(doctor.schedule)
            
            val availableSlots = withContext(Dispatchers.IO) {
                potentialSlots.filter { slot ->
                    val isBooked = repository.getBookedTimesForDoctorAndDate(doctor.id, slot.date).contains(slot.time)
                    val userConflict = if (userId != -1L) repository.hasAppointmentAt(userId, slot.date, slot.time) else false
                    !isBooked && !userConflict
                }
            }

            if (availableSlots.isEmpty()) {
                _uiState.value = SlotUiState.Empty
            } else {
                _uiState.value = SlotUiState.Success(availableSlots)
            }
        }
    }

    private fun generatePotentialSlots(schedule: String): List<Slot> {
        val slots = mutableListOf<Slot>()
        val times = Regex("(\\d{1,2}:\\d{2})").findAll(schedule).map { it.value }.toList()
        if (times.size < 2) return emptyList()

        val start = LocalTime.parse(times.first(), DateTimeFormatter.ofPattern("HH:mm"))
        val end = LocalTime.parse(times.last(), DateTimeFormatter.ofPattern("HH:mm"))

        val today = LocalDate.now()
        val now = LocalTime.now()

        for (i in 0..2L) { // Hoy, Mañana, Pasado Mañana
            val targetDate = today.plusDays(i)
            val dateStr = targetDate.toString()

            var currentTime = start
            while (currentTime.isBefore(end)) {
                val timeStr = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                
                val isFuture = if (targetDate == today) {
                    // Si son las 7:15, queremos turnos desde las 8:00. 
                    // Usamos un buffer de 45 mins para dar tiempo al usuario.
                    !currentTime.isBefore(now.plusMinutes(45))
                } else {
                    true
                }

                if (isFuture) {
                    slots.add(Slot(dateStr, timeStr))
                }
                currentTime = currentTime.plusMinutes(30)
            }
        }
        return slots
    }

    sealed class SlotUiState {
        object Loading : SlotUiState()
        object Empty : SlotUiState()
        data class Success(val slots: List<Slot>) : SlotUiState()
    }

    data class Slot(val date: String, val time: String)

    class Factory(private val context: Context, private val userId: Long) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SlotSelectionViewModel(VitusRepository.getInstance(context), userId) as T
        }
    }
}
