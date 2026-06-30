package com.example.interfaces

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        //recibo valores
        val recibo = intent.getStringExtra("clave")
        //muestro valores
        val nombre = findViewById<TextView>(R.id.txt_nombre)
        nombre.text = recibo
        //val mitexto = findViewById<TextView>(R.id.lbl_title2)
        //mitexto.text = recibo
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.li_perfil -> {
                Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show()
            }
            R.id.li_cita -> {
                Toast.makeText(this, "Cita", Toast.LENGTH_SHORT).show()
            }
            R.id.li_salir -> {
                Toast.makeText(this, "Salir", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)

    }
}