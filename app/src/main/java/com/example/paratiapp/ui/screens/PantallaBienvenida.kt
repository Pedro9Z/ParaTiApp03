package com.example.paratiapp.ui.screens

// --- Imports ---
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paratiapp.R // Importante para R.drawable
import com.example.paratiapp.ui.theme.ParaTiAppTheme

@Composable
fun PantallaBienvenida(
    onEmpezarClick: () -> Unit // Callback para navegar
) {
    Surface( // Usamos Surface como contenedor principal
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Color de fondo base del tema
    ) {
        Box( // Box para superponer fondo y contenido
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen de Fondo
            Image(
                painter = painterResource(id = R.drawable.bg_bienvenida),
                contentDescription = "Fondo de bienvenida",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillHeight // Ajustar por alto
            )

            // Contenido Principal (Textos y Botón)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp), // Aumentar padding vertical
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Distribuye espacio
            ) {
                // Contenedor para Textos (para que no se peguen al botón si hay poco espacio)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎁 Crea un Regalo Inolvidable",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            // fontSize = 32.sp, // Puedes ajustar si es necesario
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary // Usar color primario del tema
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Transforma tus mensajes en una experiencia 3D única y personal",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground // Color de texto sobre fondo
                    )
                } // Fin Column Textos

                // Botón para Empezar
                Button(
                    onClick = onEmpezarClick, // Llama al callback de navegación
                    modifier = Modifier.fillMaxWidth() // Ocupa el ancho
                ) {
                    Text(
                        text = "✨ Empezar a Crear",
                        style = MaterialTheme.typography.labelLarge // Estilo para botones
                    )
                } // Fin Button
            } // Fin Column Contenido Principal
        } // Fin Box
    } // Fin Surface
}

// --- Preview ---
@Preview(showBackground = true, name = "Pantalla Bienvenida Preview")
@Composable
fun PantallaBienvenidaPreview() {
    ParaTiAppTheme {
        PantallaBienvenida(onEmpezarClick = {})
    }
}