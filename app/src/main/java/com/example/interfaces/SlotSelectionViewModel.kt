package com.example.interfaces

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

class SlotSelectionViewModel(
    private val repository: VitusRepository,
    private val userId: Long
) : ViewModel() {

    private lateinit var doctor: DoctorEntity

    private val _dayOptions = MutableStateFlow<List<DayOption>>(emptyList())
    val dayOptions: StateFlow<List<DayOption>> = _dayOptions

    private val _timeState = MutableStateFlow<TimeUiState>(TimeUiState.Loading)
    val timeState: StateFlow<TimeUiState> = _timeState

    private val _weekState = MutableStateFlow(WeekState("", canGoPrev = false, canGoNext = false))
    val weekState: StateFlow<WeekState> = _weekState

    private var weekOffset = 0

    var selectedDate: LocalDate? = null
        private set

    fun loadDayStrip(doctor: DoctorEntity) {
        this.doctor = doctor
        renderWeek()
    }

    fun previousWeek() {
        if (weekOffset == 0) return
        weekOffset--
        renderWeek()
    }

    fun nextWeek() {
        if (weekOffset == MAX_WEEK_OFFSET) return
        weekOffset++
        renderWeek()
    }

    /** Cinta de lunes a viernes de la semana actual: cálculo de calendario puro, no toca la BD. */
    private fun renderWeek() {
        if (!::doctor.isInitialized) return
        val today = LocalDate.now()
        val dates = generateBusinessDayStrip()
        val days = dates.map { date ->
            DayOption(
                date = date,
                // Un día pasado sigue siendo laborable, pero ya no se puede agendar en él.
                enabled = doctor.workDays.contains(date.dayOfWeek) && !date.isBefore(today),
                selected = false
            )
        }
        _dayOptions.value = days
        _weekState.value = WeekState(
            label = BookingUtils.formatWeekLabel(dates.first(), dates.last()),
            canGoPrev = weekOffset > 0,
            canGoNext = weekOffset < MAX_WEEK_OFFSET
        )

        val firstEnabled = days.firstOrNull { it.enabled }
        if (firstEnabled != null) {
            selectDay(firstEnabled.date)
        } else {
            selectedDate = null
            _timeState.value = TimeUiState.Empty
        }
    }

    private fun generateBusinessDayStrip(): List<LocalDate> {
        val monday = referenceMonday().plusWeeks(weekOffset.toLong())
        return (0L..4L).map { monday.plusDays(it) }
    }

    /**
     * Lunes de la semana 0. En sábado o domingo salta al lunes siguiente: de lo contrario la
     * semana en curso aparecería entera en gris, sin ningún día agendable.
     */
    private fun referenceMonday(): LocalDate {
        val today = LocalDate.now()
        val anchor = if (today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY) {
            today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        } else {
            today
        }
        return anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
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

    data class WeekState(val label: String, val canGoPrev: Boolean, val canGoNext: Boolean)

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

    companion object {
        /** Cuántas semanas hacia adelante se puede agendar. */
        private const val MAX_WEEK_OFFSET = 4
    }
}
