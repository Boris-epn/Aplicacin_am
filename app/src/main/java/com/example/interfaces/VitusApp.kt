package com.example.interfaces

import android.app.Application
import com.example.interfaces.data.repository.VitusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VitusApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            VitusRepository.getInstance(this@VitusApp).ensureSeedData()
        }
    }
}
