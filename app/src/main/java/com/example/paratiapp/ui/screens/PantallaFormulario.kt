package com.example.paratiapp.ui.screens

// --- Imports (Lista Completa y Correcta) ---
import android.net.Uri // Importar Uri si se usa en actualizarArchivoUri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver // Necesario para listSaver
import androidx.compose.runtime.saveable.listSaver // Necesario para el saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Para obtener VM con scope
import androidx.navigation.NavBackStackEntry // Para obtener el scope del NavGraph
import androidx.navigation.NavController // Para recibirlo como parámetro
import com.example.paratiapp.R
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.RegaloViewModel

// --- Saver para TextFieldValue ---
val textFieldValueSaver: Saver<TextFieldValue, *> = listSaver(
    save = { listOf(it.text, it.selection.start, it.selection.end) },
    restore = {
        TextFieldValue(
            text = it[0] as String,
            selection = TextRange(it[1] as Int, it[2] as Int)
        )
    }
)

// --- Composable Principal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    navController: NavController, // <-- AÑADIDO: Recibe NavController
    onSiguienteClick: () -> Unit // Callback para navegar
    // Quitado viewModel = hiltViewModel() de la firma
) {
    // --- Obtener ViewModel con Scope del NavGraph ---
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(navController.graph.id)
    }
    val viewModel: RegaloViewModel = hiltViewModel(parentEntry)
    // --- FIN Obtener ViewModel ---

    Log.d("PantallaFormulario", "[VM ID: ${viewModel.hashCode()}] Composable INICIADO.") // Log de ID

    // --- Estados leídos del ViewModel ---
    val nombre by viewModel.nombreDestinatario.collectAsState()
    val telefono by viewModel.telefonoDestinatario.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    // --- Estado local para el TextField del teléfono ---
    var telefonoTfv by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue(telefono, TextRange(telefono.length)))
    }
    LaunchedEffect(telefono) {
        if (telefonoTfv.text != telefono) {
            Log.d("PantallaFormulario", "Actualizando telefonoTfv desde ViewModel: '$telefono'")
            telefonoTfv = TextFieldValue(telefono, TextRange(telefono.length))
        }
    }

    // --- Estado local para el error de validación del teléfono ---
    var telefonoError by rememberSaveable { mutableStateOf<String?>(null) }

    // --- Función de Validación ---
    fun validarTelefono(texto: String): Boolean {
        val numeroReal = when {
            texto.startsWith("+34") -> texto.substring(3)
            texto.startsWith("+") -> texto.substring(1)
            else -> texto
        }
        val soloDigitos = numeroReal.all { it.isDigit() }
        val longitudCorrecta = numeroReal.length >= 9
        val nuevoError = when {
            // Modificación: No mostrar error si está vacío Y el usuario aún no ha interactuado significativamente
            // Para simplificar, validamos siempre que no esté vacío
            texto.isBlank() -> null // Si está vacío, no hay error de formato aún
            !texto.startsWith("+") -> "Debe empezar con prefijo (+34)" // Mejor validar prefijo
            !soloDigitos -> "El número solo puede contener dígitos (después del +)"
            !longitudCorrecta -> "El número debe tener al menos 9 dígitos (sin prefijo)"
            else -> null
        }
        if (telefonoError != nuevoError) {
            telefonoError = nuevoError
            Log.d("PantallaFormulario", "Estado de error teléfono: $telefonoError")
        }
        return telefonoError == null
    }


    // --- Estado para habilitar el botón "Siguiente" ---
    // La validación ahora depende del estado del ViewModel y del error local
    val isFormValid = nombre.isNotBlank() &&
            telefono.isNotBlank() &&
            mensaje.isNotBlank() &&
            telefonoError == null && // Importante: check local de error
            validarTelefono(telefono) // Re-validar el estado del VM por si acaso

    // --- UI ---
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_formulario),
            contentDescription = "Fondo Formulario",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillHeight
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { viewModel.actualizarNombre(it) },
                label = { Text("Nombre del destinatario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = telefonoTfv,
                onValueChange = { newValue ->
                    // Permitir borrar completamente o empezar con +
                    if (newValue.text.isEmpty() || newValue.text == "+") {
                        telefonoTfv = newValue
                        viewModel.actualizarTelefono(newValue.text) // Actualizar VM aunque esté "mal"
                        validarTelefono(newValue.text) // Validar para limpiar error si procede
                    } else {
                        val textoFiltrado = "+" + newValue.text.filter { it.isDigit() } // Forzar + y solo dígitos
                        if (textoFiltrado != telefonoTfv.text) {
                            if (validarTelefono(textoFiltrado)) { // Validar ANTES de actualizar VM
                                telefonoTfv = TextFieldValue(textoFiltrado, TextRange(textoFiltrado.length))
                                viewModel.actualizarTelefono(textoFiltrado)
                            } else {
                                // Si no es válido pero el usuario sigue escribiendo, actualizamos Tfv local
                                // pero NO el viewModel principal hasta que sea válido? O sí?
                                // Por simplicidad, actualizamos Tfv local, y la validación mostrará error.
                                // El botón "Siguiente" usará el estado del VM que SÍ es válido.
                                // -> Vamos a actualizar el Tfv local para que el usuario vea lo que escribe
                                //    y el error se muestre/oculte según la validación
                                telefonoTfv = TextFieldValue(textoFiltrado, TextRange(textoFiltrado.length))
                                // Podríamos NO actualizar el VM aquí si quisiéramos que solo guarde valores válidos
                                // viewModel.actualizarTelefono(textoFiltrado) // <- Comentado opcionalmente
                            }
                        } else {
                            telefonoTfv = newValue // Solo cambió cursor
                        }
                    }
                },
                label = { Text("Número de WhatsApp (+34)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = telefonoError != null,
                supportingText = { if (telefonoError != null) Text(telefonoError!!) }
            )

            OutlinedTextField(
                value = mensaje,
                onValueChange = { viewModel.actualizarMensaje(it) },
                label = { Text("Mensaje personalizado") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                minLines = 3
            )

            OutlinedButton(
                onClick = { Log.d("PantallaFormulario", "TODO: Implementar selección de archivo") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar archivo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSiguienteClick,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Siguiente")
            }
        } // Fin Column
    } // Fin Box
}

// --- Preview ---
@Preview(showBackground = true, name = "Pantalla Formulario Preview")
@Composable
fun PantallaFormularioPreview() {
    ParaTiAppTheme {
        // CORRECTO: Crear NavController básico usando LocalContext para la Preview
        val context = LocalContext.current
        val previewNavController = remember(context) { // Usar remember aquí es SEGURO
            NavController(context)
        }

        PantallaFormulario(
            navController = previewNavController, // Pasar el NavController dummy
            onSiguienteClick = {}
            // El hiltViewModel(parentEntry) dentro de PantallaFormulario
            // NO se ejecutará en modo Preview.
        )
    }
}