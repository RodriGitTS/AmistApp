package com.example.amistapp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow

class LoginViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "Fernando"

    //Variables para los estados...
    val isLoading = MutableStateFlow(false)
    val loginSuccess = MutableStateFlow(false)
    val loginGoogleSuccess = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun loginWithEmail(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        loginSuccess.value = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    loginSuccess.value = true
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun registerWithEmail(email: String, password: String) {
        isLoading.value = true
        errorMessage.value = null
        loginSuccess.value = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    loginSuccess.value = true
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }

    fun loginWithGoogle(idToken: String) {
        isLoading.value = true
        errorMessage.value = null
        loginSuccess.value = false

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    loginSuccess.value = true
                    loginGoogleSuccess.value = true
                } else {
                    errorMessage.value = task.exception?.message ?: "Error desconocido"
                }
            }
    }


    fun signOut(context: Context) {
        Log.d(TAG, "signOut() llamado ${loginGoogleSuccess.value}")
        if (loginGoogleSuccess.value) {
            //El usuario inició sesión con Google
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )

            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    Log.d(TAG, "Acceso revocado correctamente")
                    auth.signOut()
                    Log.d(TAG, "Sesión cerrada correctamente")
                } else {
                    Log.e(TAG, "Error al revocar el acceso")
                }
            }
        } else {
            //El usuario no inició sesión con Google (email/contraseña u otro proveedor)
            auth.signOut()
            Log.d(TAG, "Sesión cerrada para usuario no Google")
        }

        //Actualizar el estado de las variables de UI
        loginGoogleSuccess.value = false
        loginSuccess.value = false
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

}