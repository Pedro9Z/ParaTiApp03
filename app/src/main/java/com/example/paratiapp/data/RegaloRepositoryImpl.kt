package com.example.paratiapp.data // Asegúrate que el package es el correcto

// --- Imports Necesarios ---
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.ktx.firestore // KTX para sintaxis más limpia
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await // Para usar await en Tasks de Firebase
import javax.inject.Inject
import javax.inject.Singleton

// Implementación concreta del Repositorio usando Firestore
@Singleton // Asegura que solo haya una instancia (inyectada por Hilt)
class RegaloRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : RegaloRepository {

    // Obtener la instancia de Firestore
    private val db: FirebaseFirestore = Firebase.firestore

    override suspend fun guardarRegaloEnFirestore(datosRegalo: Regalo): String? {
        Log.d("RegaloRepository", "Intentando guardar regalo: $datosRegalo")
        return try {
            // Crear el mapa de datos para Firestore
            // Incluir todos los campos ACTUALES de la data class Regalo
            val regaloData = hashMapOf(
                "destinatarioNombre" to datosRegalo.destinatarioNombre,
                "destinatarioWhatsApp" to datosRegalo.destinatarioWhatsApp,
                "mensaje" to datosRegalo.mensaje,
                "archivoUrl" to datosRegalo.archivoUrl,         // Puede ser null
                "texturaCaja" to datosRegalo.texturaCaja,
                "texturaPapel" to datosRegalo.texturaPapel,
                "formaLazo" to datosRegalo.formaLazo,           // AÑADIDO - Puede ser null
                // "texturaLazo" to datosRegalo.texturaLazo,    // ELIMINADO - Ya no existe en Regalo.kt
                "colorLazo" to datosRegalo.colorLazo,           // Puede ser null
                "acabadoLazo" to datosRegalo.acabadoLazo,       // AÑADIDO - String, default "brillante"
                "texturaTarjeta" to datosRegalo.texturaTarjeta,
                // "timestampCreacion" to FieldValue.serverTimestamp() // Opción 1: Usar FieldValue.serverTimestamp() aquí
                // y el campo en data class sería Date? = null (sin @ServerTimestamp)
                "timestamp" to FieldValue.serverTimestamp() // Opción 2: Si tu campo en Regalo se llama 'timestamp'
                // y quieres que Firestore lo genere. Si usas @ServerTimestamp
                // en el data class, este campo aquí podría ser redundante o
                // podrías pasar datosRegalo.timestamp (que sería null inicialmente).
                // Para MÁXIMA SEGURIDAD de que Firestore genere el timestamp,
                // es mejor usar FieldValue.serverTimestamp() aquí y que el campo
                // en el data class sea Date? = null (sin la anotación @ServerTimestamp)
                // O, si usas @ServerTimestamp, simplemente pasa datosRegalo.timestamp (que será null)
                // y Firestore lo sobrescribirá.
                // Vamos a asumir que quieres que Firestore lo genere y tu campo se llama 'timestamp'.
            )

            // Si algún valor es null y NO quieres guardarlo como null en Firestore,
            // puedes filtrar el mapa (opcional):
            // val nonNullRegaloData = regaloData.filterValues { it != null }
            // .add(nonNullRegaloData)

            // Añadir el documento a la colección "regalos"
            val documentReference = db.collection("regalos")
                .add(regaloData) // Usar regaloData (o nonNullRegaloData si filtras)
                .await() // Esperar a que la operación termine

            Log.d("RegaloRepository", "Regalo guardado con ÉXITO. ID: ${documentReference.id}")
            documentReference.id // Devolver el ID si todo fue bien

        } catch (e: Exception) {
            // Manejar cualquier excepción durante la escritura
            Log.e("RegaloRepository", "ERROR al guardar el regalo en Firestore", e)
            null // Devolver null para indicar fallo
        }
    }

    override suspend fun subirArchivo(uri: Uri, regaloId: String): String? {
        return try {
            val fileName = uri.lastPathSegment ?: "archivo_regalo"
            val storageRef = storage.reference.child("uploads/$regaloId/$fileName")
            storageRef.putFile(uri).await() // Sube el archivo
            val downloadUrl = storageRef.downloadUrl.await().toString() // Obtiene la URL de descarga
            Log.d("RegaloRepositoryImpl", "Archivo subido. URL: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("RegaloRepositoryImpl", "Error al subir archivo: ", e)
            null
        }
    }

    override suspend fun actualizarUrlArchivo(regaloId: String, url: String) {
        firestore.collection("regalos").document(regaloId)
            .update("archivoUrl", url)
            .await()
    }
}