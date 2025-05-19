package com.example.paratiapp

// --- Imports Necesarios ---
import android.os.Bundle
import android.util.Log // Importar Log para depuración
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // Importar remember
import androidx.compose.ui.tooling.preview.Preview
// Quita la importación de hiltViewModel de aquí, se usa dentro de las pantallas
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Importar NavController (ya estaba implícito)
import androidx.navigation.NavHostController // Importar NavHostController
import androidx.navigation.NavBackStackEntry // Importar si se usa getBackStackEntry aquí (no necesario ahora)
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// import androidx.navigation.NavGraph.Companion.findStartDestination // No es necesario aquí
import com.example.paratiapp.ui.screens.PantallaBienvenida
import com.example.paratiapp.ui.screens.PantallaFormulario
import com.example.paratiapp.ui.screens.PantallaPreview3D
import com.example.paratiapp.ui.screens.PantallaSelectorTextura
import com.example.paratiapp.ui.theme.ParaTiAppTheme
// Quita la importación del ViewModel de aquí, las pantallas lo obtienen
// import com.example.paratiapp.ui.viewmodel.RegaloViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.Text

// --- Clase MainActivity ---
@AndroidEntryPoint // Anotación Hilt necesaria
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ParaTiAppTheme {
                AppNavigation() // Llama al gestor de navegación
            }
        }
    }
}

// --- Composable de Navegación ---
@Composable
fun AppNavigation() {
    // El NavController se crea aquí y se pasa al NavHost y a las pantallas que lo necesiten
    val navController: NavHostController = rememberNavController() // Especificar tipo

    Log.d("AppNavigation", "NavController Instancia: ${navController.hashCode()}") // Log para ver ID del NavController

    NavHost(
        navController = navController,
        startDestination = "bienvenida" // Empieza en Bienvenida
    ) {
        // --- Rutas y Pantallas ---
        composable("bienvenida") {
            // PantallaBienvenida no necesita el ViewModel compartido ni NavController
            PantallaBienvenida(
                onEmpezarClick = { navController.navigate("formulario") }
            )
        }

        // --- Definición ÚNICA y CORRECTA para "formulario" ---
        composable("formulario") {
            Log.d("AppNavigation", "Cargando composable 'formulario'")
            // Pasamos el NavController para que la pantalla pueda obtener el ViewModel con scope correcto
            PantallaFormulario(
                navController = navController, // <-- PASAR NavController
                onSiguienteClick = { navController.navigate("selectorTexturas") }
            )
        }

        // --- Definición CORRECTA para "selectorTexturas" ---
        composable("selectorTexturas") {
            Log.d("AppNavigation", "Cargando composable 'selectorTexturas'")
            // Pasamos el NavController para navegación interna y para obtener el VM scope
            PantallaSelectorTextura(
                navController = navController, // <-- PASAR NavController
                onSiguiente = {
                    navController.navigate("preview3D")
                    Log.i("AppNavigation", "Navegando desde Selector Texturas a Preview...") // Usar Log.i o Log.d
                }
            )
        }

        // --- Definición CORRECTA para "preview3D" ---
        composable("preview3D") {
            Log.d("AppNavigation", "Cargando composable 'preview3D'")
            // Pasamos el NavController para navegación interna y para obtener el VM scope
            PantallaPreview3D(
                navController = navController // <-- PASAR NavController
            )
        }

        // composable("resumenEnviar") { ... } // Futura pantalla
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ParaTiAppTheme {
        // La preview no puede ejecutar la navegación real
        Text("Preview de MainActivity (No navega)")
    }
}