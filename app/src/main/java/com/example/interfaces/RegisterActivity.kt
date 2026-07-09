package com.example.interfaces

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Habilitar flecha de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val etFecha = findViewById<EditText>(R.id.et_anio)
        val etPin = findViewById<EditText>(R.id.et_pin)
        val etPinConfirm = findViewById<EditText>(R.id.et_pin_confirm)
        val btnRegistro = findViewById<Button>(R.id.btn_registro)

        etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (selectedDate.after(today)) {
                    Toast.makeText(this, "La fecha de nacimiento no puede ser futura", Toast.LENGTH_SHORT).show()
                    etFecha.setText("")
                } else {
                    var age = today.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR)
                    
                    if (today.get(Calendar.MONTH) < selectedDate.get(Calendar.MONTH) || 
                        (today.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) && 
                         today.get(Calendar.DAY_OF_MONTH) < selectedDate.get(Calendar.DAY_OF_MONTH))) {
                        age--
                    }

                    if (age < 18) {
                        Toast.makeText(this, "Debes tener al menos 18 años para registrarte", Toast.LENGTH_SHORT).show()
                        etFecha.setText("")
                    } else {
                        etFecha.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                    }
                }
            }, year, month, day)
            
            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }

        btnRegistro.setOnClickListener {
            val nombre = etNombre.text.toString()
            val fecha = etFecha.text.toString()
            val pin = etPin.text.toString()
            val pinConfirm = etPinConfirm.text.toString()

            if (nombre.isEmpty() || fecha.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != pinConfirm) {
                Toast.makeText(this, "Los PIN no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("clave", nombre)
            startActivity(intent)
        }
    }
}