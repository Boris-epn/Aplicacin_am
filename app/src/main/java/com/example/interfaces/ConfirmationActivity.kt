package com.example.interfaces

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ConfirmationActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private var summary: com.example.interfaces.data.local.model.AppointmentSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, -1L)
        if (appointmentId == -1L) { finish(); return }

        findViewById<TextView>(R.id.btn_home).setOnClickListener { goHome() }
        findViewById<TextView>(R.id.btn_calendar).setOnClickListener { addToCalendar() }
        lifecycleScope.launch {
            summary = withContext(Dispatchers.IO) { repository.getSummaryByAppointmentId(appointmentId) }
            val appointment = summary ?: return@launch
            findViewById<TextView>(R.id.txt_specialty).text = if (appointment.specialtyName == "Medicina General") "Med. General" else appointment.specialtyName
            findViewById<TextView>(R.id.txt_doctor).text = appointment.doctorName
            findViewById<TextView>(R.id.txt_date).text = BookingUtils.formatIsoDateForDisplay(appointment.appointment.appointmentDate)
            findViewById<TextView>(R.id.txt_time).text = appointment.appointment.appointmentTime
            findViewById<TextView>(R.id.txt_room).text = appointment.appointment.room
        }
    }

    private fun addToCalendar() {
        val appointment = summary ?: return
        val start = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("${appointment.appointment.appointmentDate} ${appointment.appointment.appointmentTime}") ?: time
        }
        try {
            startActivity(Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                putExtra(CalendarContract.Events.TITLE, "Cita ${appointment.specialtyName}")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start.timeInMillis + 30 * 60 * 1000)
                putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.appointment.room)
            })
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No hay una app de calendario disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    companion object { const val EXTRA_APPOINTMENT_ID = "appointment_id" }
}
