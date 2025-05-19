package com.example.paratiapp.ui.components

// ... (TODOS los imports CORRECTOS: BitmapFactory, FlowRow, ExperimentalLayoutApi, etc.) ...
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Importa FlowRow y ExperimentalLayoutApi aquí o arriba
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

private suspend fun esImagenValida(context: Context, assetPath: String, nombreArchivo: String): Boolean {
    val extensionValida = nombreArchivo.endsWith(".png", ignoreCase = true) ||
                          nombreArchivo.endsWith(".jpg", ignoreCase = true) ||
                          nombreArchivo.endsWith(".jpeg", ignoreCase = true) ||
                          nombreArchivo.endsWith(".webp", ignoreCase = true)
    if (!extensionValida) return false
    return withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open("$assetPath/$nombreArchivo")
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            options.outWidth != -1 && options.outHeight != -1
        } catch (e: IOException) { false }
        catch (e: Exception) { false }
        finally { try { inputStream?.close() } catch (e: IOException) { /* Ignorar */ } }
    }
}

@OptIn(ExperimentalLayoutApi::class) // Para FlowRow
@Composable
fun SelectorDeTexturas(
    assetPath: String,
    valorSeleccionado: String?, // <-- ¡AQUÍ ESTÁ EL PARÁMETRO!
    onTexturaSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var texturasValidas by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(assetPath) {
        // ... (Lógica de carga y filtro como la teníamos) ...
         Log.d("SelectorDeTexturas", "[$assetPath] Iniciando carga y filtro...")
        try {
            val todosLosArchivos = context.assets.list(assetPath)?.toList() ?: emptyList()
             Log.d("SelectorDeTexturas", "[$assetPath] Archivos encontrados: $todosLosArchivos")
            val imagenesFiltradas = mutableListOf<String>()
            todosLosArchivos.forEach { archivo ->
                if (esImagenValida(context, assetPath, archivo)) {
                    imagenesFiltradas.add(archivo)
                }
            }
             Log.d("SelectorDeTexturas", "[$assetPath] Imágenes válidas filtradas: $imagenesFiltradas")
            texturasValidas = imagenesFiltradas

            if (imagenesFiltradas.isNotEmpty() && (valorSeleccionado == null || valorSeleccionado.isBlank() || valorSeleccionado !in imagenesFiltradas)) {
                val primeraValida = imagenesFiltradas.first()
                 Log.d("SelectorDeTexturas", "[$assetPath] No había selección válida en ViewModel. Seleccionando por defecto: $primeraValida")
                onTexturaSeleccionada(primeraValida)
            } else if (imagenesFiltradas.isEmpty()) {
                 Log.d("SelectorDeTexturas", "[$assetPath] No se encontraron imágenes válidas.")
            }
        } catch (e: IOException) {
             Log.e("SelectorDeTexturas", "[$assetPath] Error al listar assets: ${e.message}")
            texturasValidas = emptyList()
        }
    }

    FlowRow( // <-- Usando FlowRow
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        texturasValidas.forEach { nombreArchivo ->
            val isSelected = nombreArchivo == valorSeleccionado // <-- Compara con el PARÁMETRO
            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        Log.d("SelectorDeTexturas", "[$assetPath] Clic en: $nombreArchivo")
                        onTexturaSeleccionada(nombreArchivo) // Llama al callback
                    }
                    .border(2.dp, borderColor, RoundedCornerShape(8.dp))
             ) {
                 Image(
                     painter = rememberAsyncImagePainter("file:///android_asset/$assetPath/$nombreArchivo"), // <-- QUÉ IMAGEN MOSTRAR (Coil)
                     contentDescription = nombreArchivo, // <-- DESCRIPCIÓN (para accesibilidad)
                     contentScale = ContentScale.Crop, // <-- CÓMO AJUSTAR LA IMAGEN
                     modifier = Modifier.fillMaxSize() // <-- Ocupar el Surface
                 )
             }
         }
     }
}