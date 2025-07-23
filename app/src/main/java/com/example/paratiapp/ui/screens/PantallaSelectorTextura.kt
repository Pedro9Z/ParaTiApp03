package com.example.paratiapp.ui.screens

// Los imports Log y LocalContext se eliminaron porque no se usaban directamente en esta versión.
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.paratiapp.R
import com.example.paratiapp.ui.components.SelectorDeTexturas
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.RegaloViewModel

// Composable de ayuda para no repetir el código de la Card
@Composable
private fun ContenedorSeccion(titulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaSelectorTextura(
    navController: NavController,
    onSiguiente: () -> Unit
) {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(navController.graph.id)
    }
    val viewModel: RegaloViewModel = hiltViewModel(parentEntry)

    val texturaCajaSeleccionada by viewModel.texturaCajaSeleccionada.collectAsState()
    val texturaPapelSeleccionada by viewModel.texturaPapelSeleccionada.collectAsState()
    val formaLazoSeleccionada by viewModel.formaLazoSeleccionada.collectAsState()
    val colorLazoSeleccionado by viewModel.colorLazoSeleccionado.collectAsState()
    val acabadoLazoSeleccionado by viewModel.acabadoLazoSeleccionado.collectAsState()
    val texturaTarjetaSeleccionada by viewModel.texturaTarjetaSeleccionada.collectAsState()

    val nombreObjetoLazoEstrella = "Lazoestrella"
    val nombreObjetoLazoFlor = "Lazoflor"
    val nombreObjetoLazoNormal = "Lazonormal"

    val opcionesFormaLazo = listOf(
        "Normal" to nombreObjetoLazoNormal,
        "Estrella" to nombreObjetoLazoEstrella,
        "Flor" to nombreObjetoLazoFlor
    )
    val coloresDisponiblesLazo = listOf(
        "Rojo" to "#FF0000", "Azul" to "#0000FF", "Verde" to "#00FF00",
        "Amarillo" to "#FFFF00", "Blanco" to "#FFFFFF", "Negro" to "#000000"
    )
    val opcionesAcabadoLazo = listOf("Brillante", "Mate")

    val lazoConfiguradoCorrectamente = formaLazoSeleccionada != null &&
            colorLazoSeleccionado != null &&
            acabadoLazoSeleccionado.isNotBlank()

    val puedeContinuar = texturaCajaSeleccionada.isNotBlank() &&
            texturaPapelSeleccionada.isNotBlank() &&
            lazoConfiguradoCorrectamente &&
            texturaTarjetaSeleccionada.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personaliza tu Regalo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.bg_seleccion),
                contentDescription = "Fondo Selección",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))

                ContenedorSeccion(titulo = "Elige la textura de la caja") {
                    SelectorDeTexturas(assetPath = "texturas/packages", valorSeleccionado = texturaCajaSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaCaja, modifier = Modifier.fillMaxWidth())
                }
                ContenedorSeccion(titulo = "Elige el papel de regalo") {
                    SelectorDeTexturas(assetPath = "texturas/paper", valorSeleccionado = texturaPapelSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaPapel, modifier = Modifier.fillMaxWidth())
                }
                ContenedorSeccion(titulo = "Elige la forma del lazo") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        opcionesFormaLazo.forEachIndexed { index, (label, valorObjeto) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = opcionesFormaLazo.size),
                                onClick = { viewModel.actualizarFormaLazo(valorObjeto) },
                                selected = formaLazoSeleccionada == valorObjeto
                            ) { Text(label) }
                        }
                    }
                }
                ContenedorSeccion(titulo = "Elige el color del lazo y cintas") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        coloresDisponiblesLazo.forEach { (nombreColor, valorHex) ->
                            OutlinedButton(
                                onClick = { viewModel.actualizarColorLazo(valorHex) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (colorLazoSeleccionado == valorHex) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (colorLazoSeleccionado == valorHex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                            ) { Text(nombreColor) }
                        }
                    }
                }
                ContenedorSeccion(titulo = "Acabado del Lazo y Cintas") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        opcionesAcabadoLazo.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = opcionesAcabadoLazo.size),
                                onClick = { viewModel.actualizarAcabadoLazo(label.lowercase()) },
                                selected = acabadoLazoSeleccionado == label.lowercase()
                            ) { Text(label) }
                        }
                    }
                }
                ContenedorSeccion(titulo = "Elige la tarjeta") {
                    SelectorDeTexturas(assetPath = "texturas/cards", valorSeleccionado = texturaTarjetaSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaTarjeta, modifier = Modifier.fillMaxWidth())
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onSiguiente,
                    enabled = puedeContinuar,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text("Siguiente")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Pantalla Selección Texturas")
@Composable
fun PantallaSelectorTexturaPreview() {
    ParaTiAppTheme {
        // *** CORRECCIÓN: Se quita LocalContext porque ya no es necesario aquí ***
        val previewNavController = rememberNavController()
        PantallaSelectorTextura(navController = previewNavController, onSiguiente = {})
    }
}