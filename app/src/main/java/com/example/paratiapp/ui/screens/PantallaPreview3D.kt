package com.example.paratiapp.ui.screens

// --- Imports ---
import android.content.Intent
import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface // Necesario para AndroidBridge
import android.webkit.WebChromeClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // Para postear al hilo principal
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope // Para lifecycleScope.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.webkit.WebViewAssetLoader
import com.example.paratiapp.R
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.RegaloViewModel
import androidx.compose.ui.unit.dp
import com.example.paratiapp.ui.viewmodel.EstadoGuardado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPreview3D(
    navController: NavController
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(navController.graph.id)
    }
    val viewModel: RegaloViewModel = hiltViewModel(parentEntry)
    Log.d("PantallaPreview3D", "[VM ID: ${viewModel.hashCode()}] Composable INICIADO.")

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current // Para AndroidBridge

    // --- Estados leídos del ViewModel ---
    val texturaCaja by viewModel.texturaCajaSeleccionada.collectAsState()
    val texturaPapel by viewModel.texturaPapelSeleccionada.collectAsState()
    val formaLazo by viewModel.formaLazoSeleccionada.collectAsState()
    val colorLazo by viewModel.colorLazoSeleccionado.collectAsState()
    val acabadoLazo by viewModel.acabadoLazoSeleccionado.collectAsState()
    val texturaTarjeta by viewModel.texturaTarjetaSeleccionada.collectAsState()
    // --- LEER NUEVOS ESTADOS ALEATORIOS ---
    val texturaMesa by viewModel.texturaMesaAleatoria.collectAsState()
    val detallePapel by viewModel.detalleTexturaPapelAleatoria.collectAsState()
    val detalleCinta by viewModel.detalleTexturaCintaAleatoria.collectAsState()
    val mensajeTarjeta by viewModel.mensaje.collectAsState() // Para mostrar en el AlertDialog
    val estadoGuardado by viewModel.estadoGuardado.collectAsState()

    // --- Estado local para simular la vista de tarjeta grande ---
    var mostrarTarjetaGrande by remember { mutableStateOf(false) }
    var animacionAperturaCompleta by remember { mutableStateOf(false) } // <-- NUEVO ESTADO
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(texturaCaja, texturaPapel, formaLazo, colorLazo, acabadoLazo, texturaTarjeta) {
        Log.d("PantallaPreview3D", "[VM ID: ${viewModel.hashCode()}] State updated: Caja='$texturaCaja', Papel='$texturaPapel' (Detalle: '$detallePapel'), FormaLazo='$formaLazo', ColorLazo='$colorLazo' (Detalle: '$detalleCinta'), AcabadoLazo='$acabadoLazo', Tarjeta='$texturaTarjeta', Mesa='$texturaMesa'")
    }

    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .build()
    }

    // --- JavascriptInterface ---
    class AndroidBridge(
        private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        private val onTarjetaTocadaCallback: () -> Unit,
        private val onAnimacionAperturaCompletaCallback: () -> Unit // <-- NUEVO CALLBACK
    ) {
        @JavascriptInterface
        fun onTarjeta3DTocada() {
            Log.d("AndroidBridge", "onTarjeta3DTocada llamada desde JS")
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { // Ejecutar en el hilo principal
                onTarjetaTocadaCallback()
            }
        }

        @JavascriptInterface
        fun onAnimacionAperturaCompleta() {
            Log.d("AndroidBridge", "onAnimacionAperturaCompleta llamada desde JS")
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                onAnimacionAperturaCompletaCallback()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vista Previa del Regalo") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Al volver, reseteamos el estado por si el usuario quiere cambiar algo
                        viewModel.resetearEstadoGuardado()
                        Log.d("PantallaPreview3D", "Botón Atrás pulsado.")
                        navController.navigateUp()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.guardarRegalo() },
                // Deshabilitar el botón si ya se está guardando/subiendo
                // Deshabilitar si no se ha abierto la caja O si ya se está guardando
                modifier = if (!animacionAperturaCompleta || estadoGuardado is EstadoGuardado.Loading || estadoGuardado is EstadoGuardado.UploadingFile) Modifier.padding(0.dp) else Modifier
            ) {
                // El botón estará visible pero no será clickeable si la condición no se cumple
                // Podríamos ocultarlo completamente si quisiéramos
                Icon(Icons.Default.Send, contentDescription = "Guardar y Generar Enlace")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_preview),
                contentDescription = "Fondo Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight
            )
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AndroidView(
                    factory = { ctx ->
                        Log.d("PantallaPreview3D Factory", "Creando WebView...")
                        WebView(ctx).apply {
                            webViewRef = this // Guardar referencia
                            settings.apply {
                                javaScriptEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mediaPlaybackRequiresUserGesture = false
                                databaseEnabled = true
                                domStorageEnabled = true
                                cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                                WebView.setWebContentsDebuggingEnabled(true)
                            }
                            clearCache(true)
                            clearFormData()
                            clearHistory()
                            clearSslPreferences()

                            // Añadir JavascriptInterface correctamente
                            addJavascriptInterface(
                                AndroidBridge(
                                    lifecycleOwner = lifecycleOwner,
                                    onTarjetaTocadaCallback = {
                                        Log.i("PantallaPreview3D", "Kotlin: La tarjeta 3D fue tocada! Mostrando tarjeta grande...")
                                        mostrarTarjetaGrande = true
                                    },
                                    onAnimacionAperturaCompletaCallback = {
                                        Log.i("PantallaPreview3D", "Animación de apertura completa. Habilitando botón de enviar.")
                                        animacionAperturaCompleta = true
                                    }
                                ), "AndroidBridge"
                            )

                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
                                }

                                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                                    Log.e("PantallaPreview3D_WebView", "HTTP Error: ${errorResponse?.statusCode} for ${request?.url}")
                                    super.onReceivedHttpError(view, request, errorResponse)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    Log.d("PantallaPreview3D_WebView", "onPageFinished: $url")

                                    if (url == "https://appassets.androidplatform.net/assets/html3d/viewer.html" && view != null) {
                                        Log.d("PantallaPreview3D", "[VM ID: ${viewModel.hashCode()}] onPageFinished - Iniciando intento de aplicar personalizaciones...")

                                        fun intentarAplicarPersonalizaciones(intento: Int = 1, maxIntentos: Int = 10, delayMs: Long = 350) {
                                            Log.d("PantallaPreview3D", "intentarAplicarPersonalizaciones (Intento $intento / $maxIntentos)")
                                            view.evaluateJavascript("javascript:window.modeloEstaListo") { result ->
                                                val modeloListo = result == "true"
                                                Log.d("PantallaPreview3D", "Resultado de window.modeloEstaListo: $modeloListo (raw: $result)")

                                                if (modeloListo) {
                                                    Log.i("PantallaPreview3D", "Modelo LISTO en JS. Aplicando personalizaciones...")

                                                    val nombreObjSobre = "Sobre"

                                                    texturaMesa.takeIf { it.isNotBlank() }?.let { tm ->
                                                        view.evaluateJavascript("javascript:aplicarTexturaMesa('$tm')", null)
                                                    }
                                                    texturaCaja.takeIf { it.isNotBlank() }?.let { tc ->
                                                        view.evaluateJavascript("javascript:aplicarTexturaCaja('$tc')", null)
                                                    }
                                                    texturaPapel.takeIf { it.isNotBlank() }?.let { tp ->
                                                        val dp = detallePapel.takeIf { it.isNotBlank() } ?: ""
                                                        view.evaluateJavascript("javascript:aplicarTexturaPapel('$tp', '$dp')", null)
                                                    }

                                                    val colorDefectoSobre = "#E8E8E8"
                                                    val roughnessDefectoSobre = 0.75
                                                    view.evaluateJavascript("javascript:applyColor('$nombreObjSobre', '$colorDefectoSobre', true, $roughnessDefectoSobre)", null)

                                                    texturaTarjeta.takeIf { it.isNotBlank() }?.let { tt ->
                                                        view.evaluateJavascript("javascript:aplicarTexturaTarjeta('$tt')", null)
                                                    }

                                                    val formaLazoActual = formaLazo
                                                    val colorLazoActual = colorLazo
                                                    val acabadoLazoActual = acabadoLazo

                                                    if (formaLazoActual != null && formaLazoActual.isNotBlank()) {
                                                        Log.d("PantallaPreview3D", "Aplicando forma de lazo: $formaLazoActual")
                                                        view.evaluateJavascript("javascript:cambiarFormaLazo('$formaLazoActual')") {
                                                            if (colorLazoActual != null && colorLazoActual.isNotBlank() && detalleCinta.isNotBlank()) {
                                                                Log.d("PantallaPreview3D", "Aplicando color '$colorLazoActual' y acabado '$acabadoLazoActual' al lazo '$formaLazoActual'")
                                                                val dc = detalleCinta.takeIf { it.isNotBlank() } ?: ""
                                                                view.evaluateJavascript("javascript:aplicarColorYAcabadoLazoYCintas('$formaLazoActual', '$colorLazoActual', '$acabadoLazoActual', '$dc')", null)
                                                            } else {
                                                                Log.w("PantallaPreview3D", "Lazo '$formaLazoActual' no tiene color seleccionado.")
                                                            }
                                                        }
                                                    } else {
                                                        Log.w("PantallaPreview3D", "No hay forma de lazo. Ocultando lazos.")
                                                        view.evaluateJavascript("javascript:cambiarFormaLazo('')", null)
                                                    }

                                                    Log.i("PantallaPreview3D", "Llamadas JS para aplicar personalizaciones REALIZADAS.")
                                                } else if (intento < maxIntentos) {
                                                    view.postDelayed({ intentarAplicarPersonalizaciones(intento + 1, maxIntentos, delayMs) }, delayMs)
                                                } else {
                                                    Log.e("PantallaPreview3D", "El modelo JS no estuvo listo después de $maxIntentos intentos.")
                                                }
                                            }
                                        }
                                        intentarAplicarPersonalizaciones()
                                    } else if (url != null && !url.startsWith("https://appassets")) {
                                        Log.w("PantallaPreview3D_WebView", "onPageFinished para URL inesperada: $url")
                                    }
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                    cm?.let { Log.println(logPriority(it.messageLevel()), "PantallaPreview3D_JSConsole", "[${it.lineNumber()}] ${it.message()}") }
                                    return true
                                }

                                fun logPriority(level: ConsoleMessage.MessageLevel?): Int = when (level) {
                                    ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
                                    ConsoleMessage.MessageLevel.WARNING -> Log.WARN
                                    else -> Log.INFO
                                }
                            }

                            val htmlUrl = "https://appassets.androidplatform.net/assets/html3d/viewer.html"
                            Log.d("PantallaPreview3D Factory", "Loading URL in WebView: $htmlUrl")
                            loadUrl(htmlUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { _ -> Log.d("PantallaPreview3D Update", "[VM ID: ${viewModel.hashCode()}] Update lambda ejecutado.") }
                )
            }

            // --- Simulación de Vista de Tarjeta Grande ---
            if (mostrarTarjetaGrande) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarTarjetaGrande = false
                        webViewRef?.evaluateJavascript("javascript:ocultarTarjetaYSobre();", null)
                        webViewRef?.evaluateJavascript("javascript:actualizarEstado3DDesdeKotlin('TARJETA_LEIDA');", null)
                        Log.i("PantallaPreview3D", "Tarjeta grande cerrada (dismiss). Sobre/Tarjeta ocultos. Estado JS -> TARJETA_LEIDA.")
                    },
                    title = { Text("Mensaje de la Tarjeta") },
                    text = { Text(mensajeTarjeta.ifBlank { "No hay mensaje en la tarjeta." }) },
                    confirmButton = {
                        Button(onClick = {
                            mostrarTarjetaGrande = false
                            webViewRef?.evaluateJavascript("javascript:ocultarTarjetaYSobre();", null)
                            webViewRef?.evaluateJavascript("javascript:actualizarEstado3DDesdeKotlin('TARJETA_LEIDA');", null)
                            Log.i("PantallaPreview3D", "Tarjeta grande cerrada (botón). Sobre/Tarjeta ocultos. Estado JS -> TARJETA_LEIDA.")
                        }) { Text("Cerrar") }
                    }
                )
            }
            // --- Fin Simulación ---

            // --- DIÁLOGO DE ESTADO DE GUARDADO ---
            when (val estado = estadoGuardado) {
                is EstadoGuardado.Loading -> {
                    AlertDialog(
                        onDismissRequest = { /* No se puede cerrar */ },
                        title = { Text("Guardando Regalo...") },
                        text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator() } },
                        confirmButton = {}
                    )
                }
                is EstadoGuardado.UploadingFile -> {
                    AlertDialog(
                        onDismissRequest = { /* No se puede cerrar */ },
                        title = { Text("Subiendo Archivo...") },
                        text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator() } },
                        confirmButton = {}
                    )
                }
                is EstadoGuardado.Success -> {
                    val regaloUrl = "https://parati.app/gift?id=${estado.id}" // URL de ejemplo
                    AlertDialog(
                        onDismissRequest = { viewModel.resetearEstadoGuardado() },
                        title = { Text("¡Regalo Creado!") },
                        text = {
                            Column {
                                Text("Tu regalo está listo. Comparte este enlace con el destinatario:")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(regaloUrl, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "¡Te he enviado un regalo virtual! Ábrelo aquí: $regaloUrl")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                                viewModel.resetearEstadoGuardado()
                            }) {
                                Text("Compartir")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.resetearEstadoGuardado() }) {
                                Text("Cerrar")
                            }
                        }
                    )
                }
                is EstadoGuardado.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.resetearEstadoGuardado() },
                        title = { Text("Error") },
                        text = { Text(estado.mensaje) },
                        confirmButton = { Button(onClick = { viewModel.resetearEstadoGuardado() }) { Text("Aceptar") } }
                    )
                }
                is EstadoGuardado.Idle -> { /* No mostrar nada */ }
            }
        }
    }
}

@Preview(showBackground = true, name = "Pantalla Preview 3D Placeholder")
@Composable
fun PantallaPreview3DPreview() {
    ParaTiAppTheme {
        val context = LocalContext.current
        val previewNavController = remember(context) { NavController(context) }
        PantallaPreview3D(navController = previewNavController)
    }
}