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
        _texturaPapelSeleccionada.value = textura
    }

    fun actualizarFormaLazo(forma: String?) {
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO FormaLazo -> '$forma'")
        _formaLazoSeleccionada.value = forma
    }

    // ELIMINADA: fun actualizarTexturaLazo(textura: String?)
    // Ya que el lazo solo tendrá color y acabado.

    fun actualizarColorLazo(color: String?) {
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO ColorLazo -> '$color'")
        _colorLazoSeleccionado.value = color
        // Ya no necesitamos limpiar texturaLazo porque no existe.
    }

    fun actualizarAcabadoLazo(acabado: String) {
        Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] ACTUALIZANDO AcabadoLazo -> '$acabado'")
        _acabadoLazoSeleccionado.value = acabado
    }

    fun actualizarTexturaTarjeta(textura: String) {
        _texturaTarjetaSeleccionada.value = textura
    }

    fun guardarRegalo() {
        val archivoUrlTemporal = _archivoUriSeleccionado.value?.toString() // Placeholder

        viewModelScope.launch {
            _estadoGuardado.value = EstadoGuardado.Loading
            Log.d("RegaloViewModel_DEBUG", "[VM ID: ${this.hashCode()}] Intentando guardar regalo...")
            try {
                val regaloParaGuardar = Regalo(
                    destinatarioNombre = _nombreDestinatario.value,
                    destinatarioWhatsApp = _telefonoDestinatario.value,
                    mensaje = _mensaje.value,
                    archivoUrl = archivoUrlTemporal,
                    texturaCaja = _texturaCajaSeleccionada.value,
                    texturaPapel = _texturaPapelSeleccionada.value,
                    formaLazo = _formaLazoSeleccionada.value,
                    // texturaLazo ELIMINADO de aquí
                    colorLazo = _colorLazoSeleccionado.value,
                    acabadoLazo = _acabadoLazoSeleccionado.value, // AÑADIDO
                    texturaTarjeta = _texturaTarjetaSeleccionada.value
                )
                Log.d("RegaloViewModel_DEBUG", "Regalo a guardar: $regaloParaGuardar")

                val idDevuelto: String? = regaloRepository.guardarRegaloEnFirestore(regaloParaGuardar)

                if (idDevuelto != null) {
                    _estadoGuardado.value = EstadoGuardado.Success(idDevuelto)
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