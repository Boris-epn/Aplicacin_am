package com.example.interfaces

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.repository.VitusRepository
import com.example.interfaces.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val repository = VitusRepository.getInstance(applicationContext)
        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etCedula = findViewById<EditText>(R.id.et_cedula)
        val etCelular = findViewById<EditText>(R.id.et_celular)
        val etFecha = findViewById<EditText>(R.id.et_anio)
        val etPin = findViewById<EditText>(R.id.et_pin)
        val etPinConfirm = findViewById<EditText>(R.id.et_pin_confirm)
        val btnRegistro = findViewById<Button>(R.id.btn_registro)

        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (selectedCalendar.after(today)) {
                    Toast.makeText(this, "La fecha no puede ser futura", Toast.LENGTH_SHORT).show()
                    etFecha.setText("")
                } else {
                    val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedCalendar.time)
                    etFecha.setText(isoDate)
                }
            }, year, month, day)

            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }

        btnRegistro.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val cedula = etCedula.text.toString().trim()
            val celular = etCelular.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val pin = etPin.text.toString().trim()
            val pinConfirm = etPinConfirm.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || cedula.isEmpty() || celular.isEmpty() || fecha.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != pinConfirm) {
                Toast.makeText(this, "Los PIN no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin.length != 4) {
                Toast.makeText(this, "El PIN debe tener 4 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    repository.registerUser(
                        fullName = nombre,
                        email = correo,
                        documentNumber = cedula,
                        phoneNumber = celular,
                        birthDate = fecha,
                        pin = pin
                    )
                }

                result.fold(
                    onSuccess = { userId ->
                        SessionManager.save(this@RegisterActivity, userId, nombre, correo)
                        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("user_mail", correo)
                            .apply()

                        val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("user_name", nombre)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@RegisterActivity,
                            error.message ?: "No se pudo registrar el usuario",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
