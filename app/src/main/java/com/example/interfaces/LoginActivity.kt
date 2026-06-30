package com.example.interfaces

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_login)

        // Recupero valores ...
        val mail = findViewById<EditText>(R.id.txt_mail)
        val pass = findViewById<EditText>(R.id.txt_pass)
        val login = findViewById<Button>(R.id.btn_login)
        val signUp = findViewById<Button>(R.id.btn_signUp)

        // Evento para el botón ...
//        login.setOnClickListener {
//            Log.d("MainActivity", "Estoy dando click al botón")
//            val msg = mail.text.toString()
//            val tel = phone.text.toString()
//            val a = "correo $msg phone $tel"
//            Toast.makeText(this, a, Toast.LENGTH_LONG).show()
//
//        }
        // BOTON LOGIN
        login.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("clave", mail.text.toString())
            startActivity(intent)
        }

        // BOTON CREAR CUENTA
        signUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
