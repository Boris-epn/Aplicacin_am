package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SpecialtySelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialty_selection)

        val userName = intent.getStringExtra(EXTRA_USER_NAME) ?: "Juana"
        findViewById<TextView>(R.id.txt_title).text = "¿Qué tipo de cita necesita, $userName?"
        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<android.view.View>(R.id.card_cardiology).setOnClickListener { openDoctors("Cardiología") }
        findViewById<android.view.View>(R.id.card_traumatology).setOnClickListener { openDoctors("Traumatología") }
        findViewById<android.view.View>(R.id.card_ophthalmology).setOnClickListener { openDoctors("Oftalmología") }
        findViewById<android.view.View>(R.id.card_general_medicine).setOnClickListener { openDoctors("Medicina General") }
        findViewById<android.view.View>(R.id.card_neurology).setOnClickListener { openDoctors("Neurología") }
        findViewById<android.view.View>(R.id.card_geriatrics).setOnClickListener { openDoctors("Geriatría") }
    }

    private fun openDoctors(specialtyName: String) {
        startActivity(Intent(this, DoctorSelectionActivity::class.java).apply {
            putExtra(EXTRA_SPECIALTY_NAME, specialtyName)
        })
    }

    companion object {
        const val EXTRA_SPECIALTY_NAME = "specialty_name"
        const val EXTRA_USER_NAME = "user_name"
    }
}
