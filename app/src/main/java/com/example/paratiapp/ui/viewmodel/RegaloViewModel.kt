package com.example.paratiapp.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paratiapp.data.Regalo
import com.example.paratiapp.data.RegaloRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EstadoGuardado {
    object Idle : EstadoGuardado()
    object Loading : EstadoGuardado()
    object UploadingFile : EstadoGuardado() // Nuevo estado para la subida
    data class Success(val id: String) : EstadoGuardado()
    data class Error(val mensaje: String) : EstadoGuardado()
}

@HiltViewModel
class RegaloViewModel @Inject constructor(
    private val regaloRepository: RegaloRepository
) : ViewModel() {

    private val _nombreDestinatario = MutableStateFlow("")
    val nombreDestinatario: StateFlow<String> = _nombreDestinatario.asStateFlow()

    private val _telefonoDestinatario = MutableStateFlow("")
    val telefonoDestinatario: StateFlow<String> = _telefonoDestinatario.asStateFlow()

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> = _mensaje.asStateFlow()

    private val _archivoUriSeleccionado = MutableStateFlow<Uri?>(null)
    val archivoUriSeleccionado: StateFlow<Uri?> = _archivoUriSeleccionado.asStateFlow()

    private val _texturaCajaSeleccionada = MutableStateFlow("")
    val texturaCajaSeleccionada: StateFlow<String> = _texturaCajaSeleccionada.asStateFlow()

    private val _texturaPapelSeleccionada = MutableStateFlow("")
    val texturaPapelSeleccionada: StateFlow<String> = _texturaPapelSeleccionada.asStateFlow()

    // --- ESTADOS DEL LAZO (ACTUALIZADOS) ---
    private val _formaLazoSeleccionada = MutableStateFlow<String?>(null)
    val formaLazoSeleccionada: StateFlow<String?> = _formaLazoSeleccionada.asStateFlow()

    // ELIMINADO: _texturaLazoSeleccionada y texturaLazoSeleccionada
    // Ya no usamos una textura de imagen separada para el lazo.

    private val _colorLazoSeleccionado = MutableStateFlow<String?>(null)
    val colorLazoSeleccionado: StateFlow<String?> = _colorLazoSeleccionado.asStateFlow()

    private val _acabadoLazoSeleccionado = MutableStateFlow("brillante") // Valor por defecto
    val acabadoLazoSeleccionado: StateFlow<String> = _acabadoLazoSeleccionado.asStateFlow()
    // --- FIN ESTADOS DEL LAZO ---

    private val _texturaTarjetaSeleccionada = MutableStateFlow("")
    val texturaTarjetaSeleccionada: StateFlow<String> = _texturaTarjetaSeleccionada.asStateFlow()

    // --- NUEVOS ESTADOS PARA TEXTURAS ALEATORIAS ---
    private val _texturaMesaAleatoria = MutableStateFlow("")
    val texturaMesaAleatoria: StateFlow<String> = _texturaMesaAleatoria.asStateFlow()

    private val _detalleTexturaPapelAleatoria = MutableStateFlow("")
    val detalleTexturaPapelAleatoria: StateFlow<String> = _detalleTexturaPapelAleatoria.asStateFlow()

    private val _detalleTexturaCintaAleatoria = MutableStateFlow("")
    val detalleTexturaCintaAleatoria: StateFlow<String> = _detalleTexturaCintaAleatoria.asStateFlow()

    private val _estadoGuardado = MutableStateFlow<EstadoGuardado>(EstadoGuardado.Idle)
    val estadoGuardado: StateFlow<EstadoGuardado> = _estadoGuardado.asStateFlow()

    fun actualizarNombre(nombre: String) {
        _nombreDestinatario.value = nombre
    }

    fun actualizarTelefono(telefono: String) {
        _telefonoDestinatario.value = telefono
    }

    fun actualizarMensaje(mensaje: String) {
        _mensaje.value = mensaje
    }

    fun actualizarArchivoUri(uri: Uri?) {
        _archivoUriSeleccionado.value = uri
    }

    fun actualizarTexturaCaja(textura: String) {
        _texturaCajaSeleccionada.value = textura
    }

    fun actualizarTexturaPapel(textura: String) {
        // Lógica para elegir detalle de papel aleatorio
        val detallesPapel = listOf("PaperText01.jpg", "PaperText02.jpg", "PaperText03.jpg", "PaperText04.jpg", "PaperText05.jpg")
        _detalleTexturaPapelAleatoria.value = detallesPapel.random()
        _texturaPapelSeleccionada.value = textura
        Log.d("RegaloViewModel", "Papel seleccionado: $textura, Detalle aleatorio: ${_detalleTexturaPapelAleatoria.value}")
    }

    fun actualizarFormaLazo(forma: String?) {
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO FormaLazo -> '$forma'")
        _formaLazoSeleccionada.value = forma
    }

    // ELIMINADA: fun actualizarTexturaLazo(textura: String?)
    // Ya que el lazo solo tendrá color y acabado.

    fun actualizarColorLazo(color: String?) {
        // Lógica para elegir detalle de cinta aleatorio
        val detallesCinta = listOf("CintaText01.jpg", "CintaText02.jpg", "CintaText03.jpg", "CintaText04.jpg", "CintaText05.jpg")
        _detalleTexturaCintaAleatoria.value = detallesCinta.random()
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO ColorLazo -> '$color'")
        _colorLazoSeleccionado.value = color
        Log.d("RegaloViewModel", "Color de lazo seleccionado. Detalle de cinta aleatorio: ${_detalleTexturaCintaAleatoria.value}")
        // Ya no necesitamos limpiar texturaLazo porque no existe.
    }

    fun actualizarAcabadoLazo(acabado: String) {
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO AcabadoLazo -> '$acabado'")
        _acabadoLazoSeleccionado.value = acabado
    }

    fun actualizarTexturaTarjeta(textura: String) {
        _texturaTarjetaSeleccionada.value = textura
    }

    // --- NUEVA FUNCIÓN PARA INICIALIZAR VALORES ALEATORIOS ---
    init {
        // Lógica para elegir textura de mesa aleatoria al crear el ViewModel
        val texturasMesa = listOf("MesaColor01.jpg", "MesaColor02.jpg", "MesaColor03.jpg", "MesaColor04.jpg")
        _texturaMesaAleatoria.value = texturasMesa.random()
        Log.d("RegaloViewModel", "ViewModel inicializado. Textura de mesa aleatoria: ${_texturaMesaAleatoria.value}")
        // Inicializar también las de detalle por si el usuario no cambia nada
        actualizarTexturaPapel(_texturaPapelSeleccionada.value)
        actualizarColorLazo(_colorLazoSeleccionado.value)
    }

    fun guardarRegalo() {
        viewModelScope.launch {
            _estadoGuardado.value = EstadoGuardado.Loading
            Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] Intentando guardar regalo...")
            try {
                val regaloParaGuardar = Regalo(
                    destinatarioNombre = _nombreDestinatario.value.trim(),
                    destinatarioWhatsApp = _telefonoDestinatario.value,
                    mensaje = _mensaje.value,
                    texturaCaja = _texturaCajaSeleccionada.value,
                    texturaPapel = _texturaPapelSeleccionada.value,
                    formaLazo = _formaLazoSeleccionada.value,
                    // texturaLazo ELIMINADO de aquí
                    colorLazo = _colorLazoSeleccionado.value,
                    acabadoLazo = _acabadoLazoSeleccionado.value, // AÑADIDO
                    texturaTarjeta = _texturaTarjetaSeleccionada.value
                )
                Log.d("RegaloViewModel_DEBUG", "Regalo a guardar: $regaloParaGuardar")

                val regaloId: String? = regaloRepository.guardarRegaloEnFirestore(regaloParaGuardar)

                if (regaloId != null) {
                    // Si hay un archivo para subir, lo hacemos ahora
                    val uri = _archivoUriSeleccionado.value
                    if (uri != null) {
                        _estadoGuardado.value = EstadoGuardado.UploadingFile
                        val downloadUrl = regaloRepository.subirArchivo(uri, regaloId)
                        if (downloadUrl != null) {
                            regaloRepository.actualizarUrlArchivo(regaloId, downloadUrl)
                            _estadoGuardado.value = EstadoGuardado.Success(regaloId)
                        } else {
                            _estadoGuardado.value = EstadoGuardado.Error("Error al subir el archivo.")
                        }
                    } else {
                        // No hay archivo, el proceso termina con éxito aquí
                        _estadoGuardado.value = EstadoGuardado.Success(regaloId)
                    }
                } else {
                    _estadoGuardado.value = EstadoGuardado.Error("Error al guardar en Firestore (ID nulo devuelto por Repo)")
                }
            } catch (e: Exception) {
                _estadoGuardado.value = EstadoGuardado.Error(e.message ?: "Error desconocido al guardar")
            }
        }
    }

    fun resetearEstadoGuardado() {
        _estadoGuardado.value = EstadoGuardado.Idle
    }
}