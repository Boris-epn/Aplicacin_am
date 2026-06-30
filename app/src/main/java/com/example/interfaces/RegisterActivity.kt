package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val btnRegistro = findViewById<Button>(R.id.btn_registro)

        btnRegistro.setOnClickListener {
            val nombre = etNombre.text.toString()
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("clave", nombre)
            startActivity(intent)
        }
    }
}