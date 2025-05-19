package com.example.paratiapp.data // Esta es la ÚNICA declaración de package y está al principio

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Data class que representa la información de un regalo
data class Regalo(
    val destinatarioNombre: String = "",
    val destinatarioWhatsApp: String = "",
    val mensaje: String = "",
    val archivoUrl: String? = null,      // URL de Cloud Storage (opcional)

    val texturaCaja: String = "",        // Asumimos que siempre tendrá una textura (no nullable)
    val texturaPapel: String = "",       // Asumimos que siempre tendrá una textura (no nullable)

    // --- Campos del Lazo y Cintas ---
    val formaLazo: String? = null,       // Nombre del objeto 3D del lazo (ej: "Lazoestrella") o null
    // val texturaLazo: String? = null,    // ELIMINADO - Ya no usamos textura de imagen para el lazo
    val colorLazo: String? = null,         // Color hexadecimal para el lazo Y las cintas
    val acabadoLazo: String = "brillante", // "brillante" o "mate". Valor por defecto es "brillante".
    // --- Fin Campos del Lazo ---

    val texturaTarjeta: String = "",     // Asumimos que siempre tendrá una textura (no nullable)

    @ServerTimestamp
    val timestamp: Date? = null
)