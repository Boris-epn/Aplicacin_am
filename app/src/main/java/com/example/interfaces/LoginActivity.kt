package com.example.interfaces

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

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_login)

        val repository = VitusRepository.getInstance(applicationContext)
        val mail = findViewById<EditText>(R.id.txt_mail)
        val pass = findViewById<EditText>(R.id.txt_pass)
        val login = findViewById<Button>(R.id.btn_login)
        val signUp = findViewById<Button>(R.id.btn_signUp)

        mail.setText(getSharedPreferences("LoginPrefs", MODE_PRIVATE).getString("user_mail", ""))

        login.setOnClickListener {
            val userMail = mail.text.toString().trim()
            val userPin = pass.text.toString().trim()

            if (userMail.isEmpty() || userPin.isEmpty()) {
                Toast.makeText(this, "Completa correo y PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    repository.loginUser(userMail, userPin)
                }

                if (user == null) {
                    Toast.makeText(this@LoginActivity, "Correo o PIN incorrectos", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_mail", user.email)
                    .apply()

                SessionManager.save(this@LoginActivity, user.id, user.fullName, user.email)

                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("user_id", user.id)
                intent.putExtra("user_name", user.fullName)
                startActivity(intent)
                finish()
            }
        }

        signUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
