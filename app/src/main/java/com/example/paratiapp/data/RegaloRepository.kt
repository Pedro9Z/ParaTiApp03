package com.example.paratiapp.data
import android.net.Uri

// Interfaz que define las operaciones para los datos de Regalo
interface RegaloRepository {

    /**
     * Guarda los datos de un regalo en Firestore.
     * @param datosRegalo El objeto Regalo a guardar (sin ID).
     * @return El ID del documento creado en Firestore si tiene éxito, o null si falla.
     */
    suspend fun guardarRegaloEnFirestore(datosRegalo: Regalo): String?
    suspend fun subirArchivo(uri: Uri, regaloId: String): String?

    suspend fun actualizarUrlArchivo(regaloId: String, url: String)
    // --- Aquí podríamos añadir más funciones en el futuro ---
    // suspend fun obtenerRegalo(id: String): Regalo?
    // suspend fun obtenerTodosLosRegalos(): List<Regalo>
    // etc.
}