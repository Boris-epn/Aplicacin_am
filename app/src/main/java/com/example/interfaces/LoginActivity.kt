package com.example.interfaces

import android.content.Context
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

        // SharedPreferences
        val sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Recupero valores ...
        val mail = findViewById<EditText>(R.id.txt_mail)
        val pass = findViewById<EditText>(R.id.txt_pass)
        val login = findViewById<Button>(R.id.btn_login)
        val signUp = findViewById<Button>(R.id.btn_signUp)

        // Cargar usuario guardado
        val savedMail = sharedPreferences.getString("user_mail", "")
        mail.setText(savedMail)

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
            val userMail = mail.text.toString()

            // Guardar en SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("user_mail", userMail)
            editor.apply()

            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("clave", userMail)
            startActivity(intent)
        }

        // BOTON CREAR CUENTA
        signUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
