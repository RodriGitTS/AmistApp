package com.example.amistapp

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.amistapp.ui.theme.AmistAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {
    val viewModel = LoginViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmistAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Rutas.login) {
                    composable(Rutas.login) {
                        LoginScreen(navController = navController, loginViewModel = viewModel)
                    }
                    composable(Rutas.home) {
                        HomeScreen(navController = navController, logginViewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, loginViewModel: LoginViewModel) {
    val context = LocalContext.current

    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginSuccess by loginViewModel.loginSuccess.collectAsState()
    val errorMessage by loginViewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var isRegistering by remember { mutableStateOf(false) } // Para alternar entre Login y Registro


    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.result
            val idToken = account?.idToken
            if (idToken != null) {
                loginViewModel.loginWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Error obteniendo token de Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) //Agrega tu client_id de Firebase, lo encontrarás en google-services.json, oauth-client/clinet_id
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Registrar Cuenta" else "Iniciar Sesión",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isRegistering) {
                    loginViewModel.registerWithEmail(email.text, password.text)
                } else {
                    loginViewModel.loginWithEmail(email.text, password.text)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isRegistering) "Registrar" else "Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar Sesión con Google")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(
                text = if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate"
            )
        }

        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }


        //++++++++++ Navegación hacia la pantalla Home ++++++++++
        /*
        //-------------------- Opción A)
        En este caso, si loginSuccess sigue siendo true durante una recomposición, el código dentro del if se ejecutará nuevamente cada vez que se recomponga.
        Esto podría causar:

            Mostrar el Toast varias veces.
            Intentar navegar repetidamente a Rutas.home, lo cual puede producir errores en la navegación.
         */
//        if (loginSuccess) {
//            Toast.makeText(context, "Login correcto", Toast.LENGTH_SHORT).show()
//            navController.navigate(Rutas.home)
//        }

        //-------------------- Opción B)
        /*
        1. ¿Qué es LaunchedEffect?

            LaunchedEffect es una función composable que se utiliza para realizar tareas que requieren efectos secundarios que dependen de cambios en uno o más valores clave.
            Estos efectos secundarios solo se ejecutan cuando uno de los valores clave cambia o cuando el composable es recompositado.
            Funciona dentro del contexto de un CoroutineScope, lo que significa que puedes realizar operaciones suspendidas (como tareas asincrónicas o retrasos).

        2. Parámetro clave loginSuccess

            El parámetro loginSuccess es el valor clave que se observa.
            Cuando el valor de loginSuccess cambia (por ejemplo, de false a true), se ejecuta el bloque de código dentro de LaunchedEffect.
            Si loginSuccess no cambia, el bloque no se ejecutará nuevamente.
         */
        LaunchedEffect(loginSuccess) {
            if (loginSuccess) {
                Toast.makeText(context, "Login correcto", Toast.LENGTH_SHORT).show()
                navController.navigate(Rutas.home) {
                    popUpTo(Rutas.login) { inclusive = true } //Borra la pila de navegación
                }
            }
        }
        /*
        LaunchedEffect evita múltiples ejecuciones

        LaunchedEffect está diseñado para manejar este tipo de situaciones porque:

            Se ejecuta una sola vez por cada cambio del valor clave (en este caso, loginSuccess).
            Una vez que el efecto se ejecuta, no se vuelve a ejecutar hasta que loginSuccess cambie nuevamente.

        Esto garantiza que las acciones, como mostrar un Toast o navegar, ocurran solo cuando realmente cambien las condiciones, evitando comportamientos inesperados debido a las recomposiciones.
         */
    }
}
