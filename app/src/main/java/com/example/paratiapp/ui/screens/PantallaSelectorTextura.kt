package com.example.paratiapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border // Importar border
import androidx.compose.foundation.BorderStroke // <--- AÑADE ESTA LÍNEA
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Necesario para colores de botón
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.paratiapp.R
import com.example.paratiapp.ui.components.SelectorDeTexturas // Para Caja, Papel, Tarjeta
import com.example.paratiapp.ui.theme.ParaTiAppTheme
import com.example.paratiapp.ui.viewmodel.RegaloViewModel


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
    Log.d("PantallaSelectorTextura", "[VM ID: ${viewModel.hashCode()}] Composable INICIADO.")

    val texturaCajaSeleccionada by viewModel.texturaCajaSeleccionada.collectAsState()
    val texturaPapelSeleccionada by viewModel.texturaPapelSeleccionada.collectAsState()

    val formaLazoSeleccionada by viewModel.formaLazoSeleccionada.collectAsState()
    val colorLazoSeleccionado by viewModel.colorLazoSeleccionado.collectAsState()
    val acabadoLazoSeleccionado by viewModel.acabadoLazoSeleccionado.collectAsState()

    val texturaTarjetaSeleccionada by viewModel.texturaTarjetaSeleccionada.collectAsState()

    LaunchedEffect(
        texturaCajaSeleccionada, texturaPapelSeleccionada, formaLazoSeleccionada,
        colorLazoSeleccionado, acabadoLazoSeleccionado, texturaTarjetaSeleccionada
    ) {
        Log.d(
            "PantallaSelectorTextura",
            "[VM ID: ${viewModel.hashCode()}] Estado: Caja='$texturaCajaSeleccionada', Papel='$texturaPapelSeleccionada', FormaLazo='$formaLazoSeleccionada', ColorLazo='$colorLazoSeleccionado', AcabadoLazo='$acabadoLazoSeleccionado', Tarjeta='$texturaTarjetaSeleccionada'"
        )
    }

    val NOMBRE_OBJETO_LAZO_ESTRELLA = "Lazoestrella"
    val NOMBRE_OBJETO_LAZO_FLOR = "Lazoflor"
    val opcionesFormaLazo = listOf(
        "Lazo Estrella" to NOMBRE_OBJETO_LAZO_ESTRELLA,
        "Lazo Flor" to NOMBRE_OBJETO_LAZO_FLOR
    )
    val coloresDisponiblesLazo = listOf(
        "Rojo" to "#FF0000", "Azul" to "#0000FF", "Verde" to "#00FF00",
        "Amarillo" to "#FFFF00", "Blanco" to "#FFFFFF", "Negro" to "#000000"
    ) // Añadido Negro
    val opcionesAcabadoLazo = listOf("Brillante", "Mate")

    val lazoConfiguradoCorrectamente = formaLazoSeleccionada != null &&
            colorLazoSeleccionado != null &&
            acabadoLazoSeleccionado.isNotBlank() // AcabadoLazo es String, no nulo

    val puedeContinuar = texturaCajaSeleccionada.isNotBlank() &&
            texturaPapelSeleccionada.isNotBlank() &&
            lazoConfiguradoCorrectamente &&
            texturaTarjetaSeleccionada.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personaliza tu Regalo") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("PantallaSelectorTextura", "Botón Atrás pulsado.")
                        navController.navigateUp()
                    }) {
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
                verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio general entre grupos
            ) {
                // --- Selector Caja ---
                Text("Elige la textura de la caja", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                SelectorDeTexturas(assetPath = "texturas/packages", valorSeleccionado = texturaCajaSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaCaja, modifier = Modifier.fillMaxWidth())

                // --- Selector Papel ---
                Text("Elige el papel de regalo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                SelectorDeTexturas(assetPath = "texturas/paper", valorSeleccionado = texturaPapelSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaPapel, modifier = Modifier.fillMaxWidth())

                // --- Selector FORMA del Lazo ---
                Text("Elige la forma del lazo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    opcionesFormaLazo.forEachIndexed { index, (label, valorObjeto) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = opcionesFormaLazo.size),
                            onClick = { viewModel.actualizarFormaLazo(valorObjeto) },
                            selected = formaLazoSeleccionada == valorObjeto
                        ) { Text(label) }
                    }
                }

                // --- Selector COLOR del Lazo ---
                Text("Elige el color del lazo y cintas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Column(Modifier.fillMaxWidth()) {
                    coloresDisponiblesLazo.chunked(3).forEach { rowColors ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowColors.forEach { (nombreColor, valorHex) ->
                                OutlinedButton(
                                    onClick = { viewModel.actualizarColorLazo(valorHex) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (colorLazoSeleccionado == valorHex) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    ),
                                    border = BorderStroke(1.dp, if (colorLazoSeleccionado == valorHex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                ) { Text(nombreColor) }
                            }
                            // Rellenar con Espaciadores si la fila no tiene 3 botones
                            repeat(3 - rowColors.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(4.dp)) // Espacio entre filas de colores
                    }
                }

                // --- Selector ACABADO del Lazo ---
                Text("Acabado del Lazo y Cintas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    opcionesAcabadoLazo.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = opcionesAcabadoLazo.size),
                            onClick = { viewModel.actualizarAcabadoLazo(label.lowercase()) },
                            selected = acabadoLazoSeleccionado == label.lowercase()
                        ) { Text(label) }
                    }
                }

                // --- Selector Tarjeta ---
                Text("Elige la tarjeta", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                SelectorDeTexturas(assetPath = "texturas/cards", valorSeleccionado = texturaTarjetaSeleccionada, onTexturaSeleccionada = viewModel::actualizarTexturaTarjeta, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

                // --- Botón Siguiente ---
                Button(
                    onClick = onSiguiente,
                    enabled = puedeContinuar,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
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
        val context = LocalContext.current
        val previewNavController = remember(context) { NavController(context) }
        PantallaSelectorTextura(navController = previewNavController, onSiguiente = {})
    }
}