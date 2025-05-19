package com.example.paratiapp // Asegúrate que el paquete sea el correcto

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <-- ¡La anotación MÁGICA para Hilt!
class ParaTiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Puedes añadir aquí código de inicialización que necesites
        // que se ejecute una sola vez cuando la app arranca.
        // Por ejemplo: inicializar librerías de logging, analytics, etc.
        println("✅ ParaTiApplication inicializada con Hilt.")
    }
}