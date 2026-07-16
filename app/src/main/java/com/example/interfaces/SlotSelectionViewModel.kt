package com.example.interfaces

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class SlotSelectionViewModel(
    private val repository: VitusRepository,
    private val userId: Long
) : ViewModel() {

    private lateinit var doctor: DoctorEntity

    private val _dayOptions = MutableStateFlow<List<DayOption>>(emptyList())
    val dayOptions: StateFlow<List<DayOption>> = _dayOptions

    private val _timeState = MutableStateFlow<TimeUiState>(TimeUiState.Loading)
    val timeState: StateFlow<TimeUiState> = _timeState

    var selectedDate: LocalDate? = null
        private set

    /** Cinta de 5 días hábiles: cálculo de calendario puro, no toca la BD. */
    fun loadDayStrip(doctor: DoctorEntity) {
        this.doctor = doctor
        val days = generateBusinessDayStrip().map { date ->
            DayOption(date = date, enabled = doctor.workDays.contains(date.dayOfWeek), selected = false)
        }
        _dayOptions.value = days

        val firstEnabled = days.firstOrNull { it.enabled }
        if (firstEnabled != null) {
            selectDay(firstEnabled.date)
        } else {
            selectedDate = null
            _timeState.value = TimeUiState.Empty
        }
    }

    private fun generateBusinessDayStrip(): List<LocalDate> {
        val days = mutableListOf<LocalDate>()
        var cursor = LocalDate.now()
        while (days.size < 5) {
            if (cursor.dayOfWeek != DayOfWeek.SATURDAY && cursor.dayOfWeek != DayOfWeek.SUNDAY) {
                days.add(cursor)
            }
            cursor = cursor.plusDays(1)
        }
        return days
    }

    /** Al elegir un día habilitado, recién ahí se consulta la BD por las horas de ESE día. */
    fun selectDay(date: LocalDate) {
        selectedDate = date
        _dayOptions.value = _dayOptions.value.map { it.copy(selected = it.date == date) }
        loadTimesForSelectedDay(date)
    }

    private fun loadTimesForSelectedDay(date: LocalDate) {
        viewModelScope.launch {
            _timeState.value = TimeUiState.Loading
            val times = withContext(Dispatchers.IO) {
                candidateTimes(date).filter { time ->
                    val isBooked = repository.getBookedTimesForDoctorAndDate(doctor.id, date).contains(time)
                    val userConflict = if (userId != -1L) repository.hasAppointmentAt(userId, date, time) else false
                    !isBooked && !userConflict
                }
            }
            _timeState.value = if (times.isEmpty()) TimeUiState.Empty else TimeUiState.Success(times)
        }
    }

    private fun candidateTimes(date: LocalDate): List<LocalTime> {
        val today = LocalDate.now()
        val now = LocalTime.now()
        val times = mutableListOf<LocalTime>()
        var currentTime = doctor.startTime
        while (currentTime.isBefore(doctor.endTime)) {
            // Si son las 7:15, queremos turnos desde las 8:00.
            // Usamos un buffer de 45 mins para dar tiempo al usuario.
            val isFuture = date != today || !currentTime.isBefore(now.plusMinutes(45))
            if (isFuture) times.add(currentTime)
            currentTime = currentTime.plusMinutes(30)
        }
        return times
    }

    data class DayOption(val date: LocalDate, val enabled: Boolean, val selected: Boolean)

    sealed class TimeUiState {
        object Loading : TimeUiState()
        object Empty : TimeUiState()
        data class Success(val times: List<LocalTime>) : TimeUiState()
    }

    class Factory(private val context: Context, private val userId: Long) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SlotSelectionViewModel(VitusRepository.getInstance(context), userId) as T
        }
    }
}
