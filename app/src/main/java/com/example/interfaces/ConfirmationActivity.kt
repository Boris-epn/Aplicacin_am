package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfirmationActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private var summary: com.example.interfaces.data.local.model.AppointmentSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, -1L)
        if (appointmentId == -1L) { finish(); return }

        findViewById<TextView>(R.id.btn_home).setOnClickListener { goHome() }
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

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    companion object { const val EXTRA_APPOINTMENT_ID = "appointment_id" }
}
