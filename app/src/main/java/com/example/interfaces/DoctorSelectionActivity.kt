package com.example.interfaces

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.interfaces.data.local.entity.DoctorEntity
import com.example.interfaces.data.repository.VitusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorSelectionActivity : AppCompatActivity() {
    private val repository by lazy { VitusRepository.getInstance(applicationContext) }
    private var selectedDoctor: DoctorEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_selection)

        val specialtyName = intent.getStringExtra(SpecialtySelectionActivity.EXTRA_SPECIALTY_NAME) ?: run {
            finish()
            return
        }

        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.txt_specialty).text = if (specialtyName == "Medicina General") "Med. General" else specialtyName
        findViewById<Button>(R.id.btn_continue).setOnClickListener { continueToSlots(specialtyName) }

        lifecycleScope.launch {
            val specialty = withContext(Dispatchers.IO) {
                repository.getSpecialties().firstOrNull { it.name == specialtyName }
            }
            val doctors = if (specialty == null) emptyList() else withContext(Dispatchers.IO) {
                repository.getDoctorsBySpecialty(specialty.id)
            }
            showDoctors(doctors)
        }
    }

    private fun showDoctors(doctors: List<DoctorEntity>) {
        val cards = listOf(R.id.card_doctor_one, R.id.card_doctor_two, R.id.card_doctor_three)
        val names = listOf(R.id.txt_doctor_one, R.id.txt_doctor_two, R.id.txt_doctor_three)
        val schedules = listOf(R.id.txt_schedule_one, R.id.txt_schedule_two, R.id.txt_schedule_three)
        val initials = listOf(R.id.txt_initials_one, R.id.txt_initials_two, R.id.txt_initials_three)

        cards.forEachIndexed { index, cardId ->
            val card = findViewById<CardView>(cardId)
            val doctor = doctors.getOrNull(index)
            card.visibility = if (doctor == null) View.GONE else View.VISIBLE
            if (doctor != null) {
                findViewById<TextView>(names[index]).text = doctor.fullName
                findViewById<TextView>(schedules[index]).text = doctor.schedule
                findViewById<TextView>(initials[index]).text = initialsFor(doctor.fullName)
                card.setOnClickListener { selectDoctor(doctor, index) }
            }
        }

        doctors.firstOrNull()?.let { selectDoctor(it, 0) }
    }

    private fun selectDoctor(doctor: DoctorEntity, selectedIndex: Int) {
        selectedDoctor = doctor
        val cards = listOf(R.id.card_doctor_one, R.id.card_doctor_two, R.id.card_doctor_three)
        val checks = listOf(R.id.txt_check_one, R.id.txt_check_two, R.id.txt_check_three)
        cards.forEachIndexed { index, cardId ->
            findViewById<CardView>(cardId).setCardBackgroundColor(
                Color.parseColor(if (index == selectedIndex) "#EAF4EE" else "#FCFBF7")
            )
            findViewById<TextView>(checks[index]).apply {
                visibility = if (index == selectedIndex) View.VISIBLE else View.INVISIBLE
                setBackgroundColor(Color.parseColor("#5E8F73"))
            }
        }
    }

    private fun continueToSlots(specialtyName: String) {
        val doctor = selectedDoctor ?: return
        startActivity(Intent(this, SlotSelectionActivity::class.java).apply {
            putExtra(SlotSelectionActivity.EXTRA_SPECIALTY_NAME, specialtyName)
            putExtra(SlotSelectionActivity.EXTRA_DOCTOR_ID, doctor.id)
        })
    }

    private fun initialsFor(name: String): String {
        return name.replace("Dra. ", "").replace("Dr. ", "")
            .split(" ").take(2).joinToString("") { it.first().toString() }
    }
}
