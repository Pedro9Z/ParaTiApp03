package com.example.paratiapp.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send // *** CAMBIO 1: Import del icono nuevo ***
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.webkit.WebViewAssetLoader
import com.example.paratiapp.R
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.EstadoGuardado
import com.example.paratiapp.ui.viewmodel.RegaloViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    val lifecycleOwner = LocalLifecycleOwner.current

    val estadoGuardado by viewModel.estadoGuardado.collectAsState()
    val mensajeTarjeta by viewModel.mensaje.collectAsState()

    var mostrarTarjetaGrande by remember { mutableStateOf(false) }
    var animacionAperturaCompleta by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
            .build()
    }

    // *** CAMBIO 2: Se añade @Suppress("unused") para silenciar las advertencias ***
    @Suppress("unused")
    class AndroidBridge(
        private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        private val onTarjetaTocadaCallback: () -> Unit,
        private val onAnimacionAperturaCompletaCallback: () -> Unit,
        private val webView: WebView?
    ) {
        @JavascriptInterface
        fun onTarjeta3DTocada() {
            Log.d("AndroidBridge", "onTarjeta3DTocada llamada desde JS")
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { onTarjetaTocadaCallback() }
        }

        @JavascriptInterface
        fun onAnimacionAperturaCompleta() {
            Log.d("AndroidBridge", "onAnimacionAperturaCompleta llamada desde JS")
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { onAnimacionAperturaCompletaCallback() }
        }

        @JavascriptInterface
        fun getGiftData() {
            Log.d("AndroidBridge", "getGiftData() llamada desde JS. Preparando y enviando JSON.")
            val datosJsonString = JSONObject().apply {
                put("texturaCaja", viewModel.texturaCajaSeleccionada.value)
                put("texturaPapel", viewModel.texturaPapelSeleccionada.value)
                put("formaLazo", viewModel.formaLazoSeleccionada.value)
                put("colorLazo", viewModel.colorLazoSeleccionado.value)
                put("acabadoLazo", viewModel.acabadoLazoSeleccionado.value)
                put("texturaTarjeta", viewModel.texturaTarjetaSeleccionada.value)
                put("mensaje", viewModel.mensaje.value)
                put("texturaMesa", viewModel.texturaMesaAleatoria.value)
                put("detallePapel", viewModel.detalleTexturaPapelAleatoria.value)
                put("detalleCinta", viewModel.detalleTexturaCintaAleatoria.value)
            }.toString()

            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                val script = "javascript:window.recibirYAplicarDatos($datosJsonString);"
                Log.d("AndroidBridge", "Ejecutando script: $script")
                webView?.evaluateJavascript(script, null)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vista Previa del Regalo") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetearEstadoGuardado()
                        navController.navigateUp()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
                }
            )
        },
        floatingActionButton = {
            if (animacionAperturaCompleta) {
                FloatingActionButton(
                    onClick = {
                        if (estadoGuardado is EstadoGuardado.Idle) {
                            viewModel.guardarRegalo()
                        }
                    },
                ) {
                    // *** CAMBIO 1: Usar el icono AutoMirrored ***
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Guardar y Generar Enlace")
                }
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
                        WebView(ctx).apply {
                            webViewRef = this
                            settings.apply {
                                javaScriptEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                                mediaPlaybackRequiresUserGesture = false
                                domStorageEnabled = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                                WebView.setWebContentsDebuggingEnabled(true)
                            }
                            clearCache(true)
                            clearFormData()
                            clearHistory()
                            clearSslPreferences()

                            addJavascriptInterface(
                                AndroidBridge(
                                    lifecycleOwner,
                                    onTarjetaTocadaCallback = { mostrarTarjetaGrande = true },
                                    onAnimacionAperturaCompletaCallback = { animacionAperturaCompleta = true },
                                    webView = this
                                ), "AndroidBridge"
                            )
                            webViewClient = object : WebViewClient() {
                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                    cm?.let { Log.println(logPriority(it.messageLevel()), "JSConsole", "[${it.lineNumber()}] ${it.message()}") }
                                    return true
                                }
                                private fun logPriority(level: ConsoleMessage.MessageLevel?) = when (level) {
                                    ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
                                    ConsoleMessage.MessageLevel.WARNING -> Log.WARN
                                    else -> Log.INFO
                                }
                            }
                            loadUrl("https://appassets.androidplatform.net/assets/html3d/viewer.html")
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (mostrarTarjetaGrande) {
                AlertDialog(
                    onDismissRequest = {
                        mostrarTarjetaGrande = false
                        webViewRef?.evaluateJavascript("javascript:ocultarTarjetaYSobre();", null)
                        webViewRef?.evaluateJavascript("javascript:actualizarEstado3DDesdeKotlin('TARJETA_LEIDA');", null)
                    },
                    title = { Text("Mensaje de la Tarjeta") },
                    text = { Text(mensajeTarjeta.ifBlank { "No hay mensaje." }) },
                    confirmButton = {
                        Button(onClick = {
                            mostrarTarjetaGrande = false
                            webViewRef?.evaluateJavascript("javascript:ocultarTarjetaYSobre();", null)
                            webViewRef?.evaluateJavascript("javascript:actualizarEstado3DDesdeKotlin('TARJETA_LEIDA');", null)
                        }) { Text("Cerrar") }
                    }
                )
            }

            when (val estado = estadoGuardado) {
                is EstadoGuardado.Loading, is EstadoGuardado.UploadingFile -> {
                    val titulo = if (estado is EstadoGuardado.Loading) "Guardando Regalo..." else "Subiendo Archivo..."
                    AlertDialog(onDismissRequest = {}, title = { Text(titulo) }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator() } }, confirmButton = {})
                }
                is EstadoGuardado.Success -> {
                    val regaloUrl = "https://parati-v02.web.app/receiver.html?id=${estado.id}"
                    AlertDialog(
                        onDismissRequest = { viewModel.resetearEstadoGuardado() },
                        title = { Text("¡Regalo Creado!") },
                        text = {
                            Column {
                                Text("Tu regalo está listo. Comparte este enlace:")
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
                                context.startActivity(Intent.createChooser(sendIntent, null))
                                viewModel.resetearEstadoGuardado()
                            }) { Text("Compartir") }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.resetearEstadoGuardado() }) { Text("Cerrar") }
                        }
                    )
                }
                is EstadoGuardado.Error -> {
                    AlertDialog(onDismissRequest = { viewModel.resetearEstadoGuardado() }, title = { Text("Error") }, text = { Text(estado.mensaje) }, confirmButton = { Button(onClick = { viewModel.resetearEstadoGuardado() }) { Text("Aceptar") } })
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
        val previewNavController = rememberNavController() // Simplificado
        PantallaPreview3D(navController = previewNavController)
    }
}