package com.example.paratiapp.ui.screens

// --- Imports (Lista Completa y Correcta) ---
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri // Importar Uri si se usa en actualizarArchivoUri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable // <-- FIX 2: Añadir import
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver // Necesario para listSaver
import androidx.compose.runtime.saveable.listSaver // Necesario para el saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
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
    val archivoUri by viewModel.archivoUriSeleccionado.collectAsState() // <-- NUEVO: Observar el URI del archivo

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
            texto.startsWith("+") -> texto.substring(1) // Mantener para otros prefijos
            else -> texto
        }
        val soloDigitos = numeroReal.all { it.isDigit() }
        val longitudCorrecta = numeroReal.length >= 9
        val nuevoError = when {
            texto.isBlank() -> null // Si está vacío, no hay error de formato aún
            !texto.startsWith("+") -> "Debe empezar con prefijo (+)"
            !soloDigitos -> "Solo puede contener dígitos (después del +)"
            !longitudCorrecta -> "Debe tener al menos 9 dígitos (sin prefijo)"
            else -> null
        }
        if (telefonoError != nuevoError) {
            telefonoError = nuevoError
            Log.d("PantallaFormulario", "Estado de error teléfono: $telefonoError")
        }
        return nuevoError == null // Devolver el estado del error actual, no el guardado
    }


    // --- Estado para habilitar el botón "Siguiente" ---
    // La validación ahora depende del estado del ViewModel y del error local
    val isFormValid = nombre.isNotBlank() &&
            telefono.isNotBlank() &&
            mensaje.isNotBlank() &&
            archivoUri != null && // <-- HACER OBLIGATORIO EL ARCHIVO
            telefonoError == null &&
            validarTelefono(telefono)

    // --- ActivityResultLauncher para el selector de archivos ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Log.d("PantallaFormulario", "Archivo seleccionado con URI: $uri")
            viewModel.actualizarArchivoUri(uri)
        } else {
            Log.d("PantallaFormulario", "El usuario no seleccionó ningún archivo.")
        }
    }

    // Función para obtener un nombre de archivo legible desde una URI
    val fileName = archivoUri?.pathSegments?.lastOrNull() ?: "Ningún archivo seleccionado"

    // --- Definición de la función de ayuda (MOVIMOS ESTO AQUÍ ARRIBA) ---
    fun formatearNumero(numero: String): String {
        val textoLimpio = numero.filter { it.isDigit() }
        return if (numero.trim().startsWith("+")) "+$textoLimpio" else "+34$textoLimpio"
    }

    // --- ESTADO PARA EL DIÁLOGO DE SELECCIÓN DE NÚMERO ---
    var mostrarDialogoSeleccionNumero by remember { mutableStateOf(false) }
    var numerosDeContacto by remember { mutableStateOf<List<String>>(emptyList()) }
    var nombreDeContacto by remember { mutableStateOf("") }

    // --- LÓGICA PARA LA LIBRETA DE CONTACTOS ---
    val context = LocalContext.current

    // Launcher para el selector de contactos
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            // Tenemos la URI del contacto, ahora necesitamos consultar su número
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER), null, null, null)

            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                val contactId = cursor.getString(idIndex)
                nombreDeContacto = cursor.getString(nameIndex) // Guardamos el nombre

                if (cursor.getInt(hasPhoneIndex) > 0) {
                    // El contacto tiene al menos un número, ahora los buscamos todos
                    val phoneNumbersList = mutableListOf<String>()
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )
                    while (phoneCursor?.moveToNext() == true) {
                        val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phoneNumbersList.add(phoneCursor.getString(numberIndex))
                    }
                    phoneCursor?.close()

                    when {
                        phoneNumbersList.size == 1 -> {
                            // Si solo hay un número, lo usamos directamente
                            viewModel.actualizarNombre(nombreDeContacto)
                            val numeroFormateado = formatearNumero(phoneNumbersList.first())
                            telefonoTfv = TextFieldValue(numeroFormateado, TextRange(numeroFormateado.length))
                            viewModel.actualizarTelefono(numeroFormateado)
                            validarTelefono(numeroFormateado)
                        }
                        phoneNumbersList.size > 1 -> {
                            // Si hay varios, preparamos y mostramos el diálogo
                            numerosDeContacto = phoneNumbersList
                            mostrarDialogoSeleccionNumero = true
                        }
                        else -> {
                            Log.w("PantallaFormulario", "El contacto tiene la bandera HAS_PHONE_NUMBER pero no se encontraron números.")
                        }
                    }
                }
            }
            cursor?.close()
        }
    }

    // Launcher para la petición de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("PantallaFormulario", "Permiso de contactos CONCEDIDO.")
            contactPickerLauncher.launch(null)
        } else {
            Log.d("PantallaFormulario", "Permiso de contactos DENEGADO.")
        }
    }

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
                    val textoEntrada = newValue.text
                    var textoFormateado = ""

                    if (textoEntrada.startsWith("+")) {
                        // Si empieza con +, solo permitir dígitos después
                        val numeros = textoEntrada.substring(1).filter { it.isDigit() }
                        textoFormateado = "+$numeros"
                    } else {
                        // Si NO empieza con +, solo permitir dígitos
                        val numeros = textoEntrada.filter { it.isDigit() }
                        textoFormateado = if (numeros.isNotEmpty()) "+34$numeros" else ""
                    }

                    telefonoTfv = TextFieldValue(textoFormateado, TextRange(textoFormateado.length))
                    viewModel.actualizarTelefono(textoFormateado)
                    validarTelefono(textoFormateado) // Validar en cada cambio
                },
                label = { Text("Número de WhatsApp") },
                placeholder = { Text("+34 600 123 456") }, // Placeholder como ejemplo
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                                // Permiso ya concedido, lanzar selector
                                Log.d("PantallaFormulario", "Permiso ya concedido. Lanzando selector.")
                                contactPickerLauncher.launch(null)
                            }
                            else -> {
                                // Permiso no concedido, solicitarlo
                                Log.d("PantallaFormulario", "Permiso no concedido. Solicitando...")
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.AccountBox, contentDescription = "Seleccionar Contacto")
                    }
                },
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

            // --- SECCIÓN DE SELECCIÓN DE ARCHIVO (MODIFICADA) ---
            OutlinedButton(
                onClick = {
                    Log.d("PantallaFormulario", "Lanzando selector de archivos...")
                    // Lanzamos el selector para imágenes y videos
                    filePickerLauncher.launch("*/*") // Puedes ser más específico, ej: "image/*"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar archivo")
            }
            // Texto para mostrar el nombre del archivo seleccionado
            Text(
                text = "Archivo: $fileName",
                style = MaterialTheme.typography.bodySmall
            )

            // --- DIÁLOGO DE SELECCIÓN DE NÚMERO ---
            if (mostrarDialogoSeleccionNumero) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoSeleccionNumero = false },
                    title = { Text("Seleccionar un número") },
                    text = {
                        Column {
                            Text("El contacto '${nombreDeContacto}' tiene varios números. ¿Cuál quieres usar?")
                            Spacer(modifier = Modifier.height(8.dp))
                            numerosDeContacto.forEach { numero ->
                                Text(
                                    text = numero,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.actualizarNombre(nombreDeContacto)
                                            val numeroFormateado = formatearNumero(numero)
                                            telefonoTfv = TextFieldValue(numeroFormateado, TextRange(numeroFormateado.length))
                                            viewModel.actualizarTelefono(numeroFormateado)
                                            validarTelefono(numeroFormateado)
                                            mostrarDialogoSeleccionNumero = false
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { mostrarDialogoSeleccionNumero = false }) {
                            Text("Cancelar")
                        }
                    }
                )
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