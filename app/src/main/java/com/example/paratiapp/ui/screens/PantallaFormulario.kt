package com.example.paratiapp.ui.screens

// --- Imports (Lista Completa y Correcta) ---
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.paratiapp.R
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.RegaloViewModel

// --- Saver para TextFieldValue (sin cambios) ---
val textFieldValueSaver: Saver<TextFieldValue, *> = listSaver(
    save = { listOf(it.text, it.selection.start, it.selection.end) },
    restore = {
        TextFieldValue(
            text = it[0] as String,
            selection = TextRange(it[1] as Int, it[2] as Int)
        )
    }
)

// --- Función de ayuda para formatear números de teléfono (sin cambios) ---
fun formatearNumero(numero: String): String {
    val textoLimpio = numero.filter { it.isDigit() }
    return if (numero.trim().startsWith("+")) "+$textoLimpio" else "+34$textoLimpio"
}

// --- Composable Principal ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormulario(
    navController: NavController,
    onSiguienteClick: () -> Unit
) {
    // --- Lógica del ViewModel y Estados (sin cambios) ---
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(navController.graph.id)
    }
    val viewModel: RegaloViewModel = hiltViewModel(parentEntry)
    val nombre by viewModel.nombreDestinatario.collectAsState()
    val telefono by viewModel.telefonoDestinatario.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()
    val archivoUri by viewModel.archivoUriSeleccionado.collectAsState()
    var telefonoTfv by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(TextFieldValue(telefono, TextRange(telefono.length)))
    }
    LaunchedEffect(telefono) {
        if (telefonoTfv.text != telefono) {
            telefonoTfv = TextFieldValue(telefono, TextRange(telefono.length))
        }
    }
    var telefonoError by rememberSaveable { mutableStateOf<String?>(null) }
    fun validarTelefono(texto: String): Boolean {
        val numeroReal = when {
            texto.startsWith("+34") -> texto.substring(3)
            texto.startsWith("+") -> texto.substring(1)
            else -> texto
        }
        val soloDigitos = numeroReal.all { it.isDigit() }
        val longitudCorrecta = numeroReal.length >= 9
        val nuevoError = when {
            texto.isBlank() -> null
            !texto.startsWith("+") -> "Debe empezar con prefijo (+)"
            !soloDigitos -> "Solo puede contener dígitos (después del +)"
            !longitudCorrecta -> "Debe tener al menos 9 dígitos (sin prefijo)"
            else -> null
        }
        if (telefonoError != nuevoError) {
            telefonoError = nuevoError
        }
        return nuevoError == null
    }
    val isFormValid = nombre.isNotBlank() &&
            telefono.isNotBlank() &&
            mensaje.isNotBlank() &&
            archivoUri != null &&
            telefonoError == null &&
            validarTelefono(telefono)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.actualizarArchivoUri(uri)
        }
    }
    val fileName = archivoUri?.pathSegments?.lastOrNull() ?: "Ningún archivo seleccionado"
    var mostrarDialogoSeleccionNumero by remember { mutableStateOf(false) }
    var numerosDeContacto by remember { mutableStateOf<List<String>>(emptyList()) }
    var nombreDeContacto by remember { mutableStateOf("") }
    val context = LocalContext.current
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER), null, null, null)
            if (cursor?.moveToFirst() == true) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val contactId = cursor.getString(idIndex)
                nombreDeContacto = cursor.getString(nameIndex)
                if (cursor.getInt(hasPhoneIndex) > 0) {
                    val phoneNumbersList = mutableListOf<String>()
                    val phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contactId), null)
                    while (phoneCursor?.moveToNext() == true) {
                        val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phoneNumbersList.add(phoneCursor.getString(numberIndex))
                    }
                    phoneCursor?.close()
                    when {
                        phoneNumbersList.size == 1 -> {
                            viewModel.actualizarNombre(nombreDeContacto)
                            val numeroFormateado = formatearNumero(phoneNumbersList.first())
                            telefonoTfv = TextFieldValue(numeroFormateado, TextRange(numeroFormateado.length))
                            viewModel.actualizarTelefono(numeroFormateado)
                            validarTelefono(numeroFormateado)
                        }
                        phoneNumbersList.size > 1 -> {
                            numerosDeContacto = phoneNumbersList
                            mostrarDialogoSeleccionNumero = true
                        }
                    }
                }
            }
            cursor?.close()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // *** CAMBIO DE UI: Envolvemos los campos en una Card para mejorar la legibilidad ***
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                                val numeros = textoEntrada.substring(1).filter { it.isDigit() }
                                textoFormateado = "+$numeros"
                            } else {
                                val numeros = textoEntrada.filter { it.isDigit() }
                                textoFormateado = if (numeros.isNotEmpty()) "+34$numeros" else ""
                            }
                            telefonoTfv = TextFieldValue(textoFormateado, TextRange(textoFormateado.length))
                            viewModel.actualizarTelefono(textoFormateado)
                            validarTelefono(textoFormateado)
                        },
                        label = { Text("Número de WhatsApp") },
                        placeholder = { Text("+34 600 123 456") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                    contactPickerLauncher.launch(null)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
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

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seleccionar archivo")
                    }

                    Text(
                        text = "Archivo: $fileName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSiguienteClick,
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Siguiente")
            }
        }

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
    }
}

@Preview(showBackground = true, name = "Pantalla Formulario Preview")
@Composable
fun PantallaFormularioPreview() {
    ParaTiAppTheme {
        val context = LocalContext.current
        val previewNavController = remember(context) { NavController(context) }
        PantallaFormulario(
            navController = previewNavController,
            onSiguienteClick = {}
        )
    }
}