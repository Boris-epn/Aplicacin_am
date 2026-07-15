package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.local.model.AppointmentSummary
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.data.session.SessionManager
import com.example.interfaces.ui.booking.BookingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private var currentUserId: Long = -1L
    private var currentAppointment: AppointmentSummary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        currentUserId = intent.getLongExtra("user_id", SessionManager.getUserId(this))
        if (currentUserId == -1L) {
            goToLogin()
            return
        }

        val txtFecha = findViewById<TextView>(R.id.txt_fecha)
        val txtNombre = findViewById<TextView>(R.id.txt_nombre)
        val txtDoctora = findViewById<TextView>(R.id.txt_doctora)
        val txtEspecialidad = findViewById<TextView>(R.id.txt_especialidad)
        val txtHoy = findViewById<TextView>(R.id.txt_hoy)
        val btnCancelar = findViewById<Button>(R.id.btn_cancelarcita)
        val btnEspecialidades = findViewById<Button>(R.id.btn_especialidades)

        txtFecha.text = BookingUtils.todayDisplayDate()
        txtNombre.text = intent.getStringExtra("user_name")
            ?: SessionManager.getUserName(this)

        btnCancelar.setOnClickListener { confirmCancelNextAppointment() }
        btnEspecialidades.setOnClickListener { openSpecialtyList() }

        findViewById<LinearLayout>(R.id.ly_cardiologia).setOnClickListener { openDoctorFlow("Cardiología") }
        findViewById<LinearLayout>(R.id.ly_traumatologia).setOnClickListener { openDoctorFlow("Traumatología") }
        findViewById<LinearLayout>(R.id.ly_oftalmologia).setOnClickListener { openDoctorFlow("Oftalmología") }
        findViewById<LinearLayout>(R.id.ly_medicina).setOnClickListener { openDoctorFlow("Medicina General") }
        findViewById<android.widget.ImageButton>(R.id.imgbtn_corazon).setOnClickListener { openDoctorFlow("Cardiología") }
        findViewById<android.widget.ImageButton>(R.id.imageButton10).setOnClickListener { openDoctorFlow("Traumatología") }
        findViewById<android.widget.ImageButton>(R.id.imageButton11).setOnClickListener { openDoctorFlow("Oftalmología") }
        findViewById<android.widget.ImageButton>(R.id.imgbtn_medicinageneral).setOnClickListener { openDoctorFlow("Medicina General") }

        lifecycleScope.launch {
            refreshAppointmentUI(txtDoctora, txtEspecialidad, txtHoy, btnCancelar)
        }
    }

    private suspend fun refreshAppointmentUI(
        txtDoctora: TextView,
        txtEspecialidad: TextView,
        txtHoy: TextView,
        btnCancelar: Button
    ) {
        currentAppointment = withContext(Dispatchers.IO) {
            repository.getNextAppointment(currentUserId)
        }

        val appointment = currentAppointment
        if (appointment == null) {
            txtDoctora.text = "Sin citas próximas"
            txtEspecialidad.text = "Puedes agendar una nueva cita desde el flujo de especialidades."
            txtHoy.text = "Agenda"
            btnCancelar.isEnabled = false
            btnCancelar.alpha = 0.6f
            return
        }

        txtDoctora.text = appointment.doctorName
        txtEspecialidad.text = "${appointment.specialtyName} - ${BookingUtils.formatIsoDateForDisplay(appointment.appointment.appointmentDate)} ${appointment.appointment.appointmentTime}"
        val todayIso = BookingUtils.toIsoDate(Calendar.getInstance())
        txtHoy.text = if (appointment.appointment.appointmentDate == todayIso) {
            "Hoy"
        } else {
            BookingUtils.formatIsoDateForDisplay(appointment.appointment.appointmentDate)
        }
        btnCancelar.isEnabled = true
        btnCancelar.alpha = 1f
    }

    private fun confirmCancelNextAppointment() {
        val appointment = currentAppointment ?: run {
            Toast.makeText(this, "No tienes una cita activa para cancelar", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Cancelar cita")
            .setMessage("¿Seguro que deseas cancelar la cita con ${appointment.doctorName}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        repository.cancelAppointment(appointment.appointment.id)
                    }
                    Toast.makeText(this@HomeActivity, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    val txtDoctora = findViewById<TextView>(R.id.txt_doctora)
                    val txtEspecialidad = findViewById<TextView>(R.id.txt_especialidad)
                    val txtHoy = findViewById<TextView>(R.id.txt_hoy)
                    val btnCancelar = findViewById<Button>(R.id.btn_cancelarcita)
                    refreshAppointmentUI(txtDoctora, txtEspecialidad, txtHoy, btnCancelar)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun openSpecialtyList() {
        startActivity(Intent(this, SpecialtySelectionActivity::class.java).apply {
            putExtra(SpecialtySelectionActivity.EXTRA_USER_NAME, SessionManager.getUserName(this@HomeActivity))
        })
    }

    private fun openDoctorFlow(specialtyName: String) {
        startActivity(Intent(this, DoctorSelectionActivity::class.java).apply {
            putExtra(SpecialtySelectionActivity.EXTRA_SPECIALTY_NAME, specialtyName)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.li_perfil -> {
                Toast.makeText(this, "Perfil en desarrollo", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.li_cita -> {
                openSpecialtyList()
                true
            }
            R.id.li_salir -> {
                SessionManager.clear(this)
                goToLogin()
                true
            }
            android.R.id.home -> {
                goToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
