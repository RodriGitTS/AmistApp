package com.example.amistapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController, logginViewModel: LoginViewModel) {
    val user = logginViewModel.getCurrentUser()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bienvenido a Home", style = MaterialTheme.typography.titleLarge)

        user?.let {
            Text(
                text = "Usuario:, ${it.displayName ?: it.email ?: "Usuario"}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Correo: ${it.email ?: "No disponible"}",
                style = MaterialTheme.typography.bodyMedium
            )
        } ?:
            Text(
                text = "No se encontró información del usuario",
                style = MaterialTheme.typography.bodyMedium
            )


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                logginViewModel.signOut(context)
                navController.navigate(Rutas.login) {
                    popUpTo(Rutas.login) { inclusive = true }
                    /*
                    popUpTo(Rutas.login) { inclusive = true }:

                    popUpTo(): Esta función se utiliza para definir hasta qué punto de la pila de navegación se debe retroceder antes de realizar una nueva navegación.
                    En este caso, el argumento Rutas.login indica que se debe eliminar todo lo que está por encima de la pantalla de inicio de sesión (Rutas.login) en la pila de navegación.

                    inclusive = true: Al usar este parámetro con el valor true, se elimina también el destino de Rutas.login de la pila de navegación. Esto es importante para que, cuando se
                    navegue de nuevo a Rutas.login, se eliminen las pantallas anteriores y no puedan ser accesibles mediante el botón "Atrás". Si inclusive se pone a false, solo se eliminarían
                    las pantallas por encima de Rutas.login, pero no Rutas.login misma.
                     */
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}