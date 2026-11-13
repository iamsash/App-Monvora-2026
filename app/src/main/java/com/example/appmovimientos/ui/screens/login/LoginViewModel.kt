package com.example.appmovimientos.ui.screens.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.repository.AuthRepository
import com.example.appmovimientos.data.repository.CategoryRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(

    private val authRepository: AuthRepository = AuthRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    var correo: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(correo = value) }
        }

    var contrasena: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(contrasena = value) }
        }

    data class LoginUiState(
        val correo: String = "",
        val contrasena: String = "",
        val mostrarContrasena: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )


    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(newValue: String) {
        correo = newValue
    }

    fun onContrasenaChange(newValue: String) {
        contrasena = newValue
    }

    fun toggleMostrarContrasena() {
        _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    fun login(onNavigateMain: () -> Unit, onNavigateTerminos: () -> Unit) {
        val inputLimpio = correo.trim()
        if (inputLimpio.isBlank() || contrasena.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingresa correo o usuario y contraseña") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val emailToSign = if (inputLimpio.contains("@")) {
                    inputLimpio
                } else {
                    authRepository.getEmailByUsername(inputLimpio)
                        ?: throw IllegalArgumentException("El usuario ingresado no existe o no tiene correo asociado.")
                }
                authRepository.signInWithEmailAndPassword(emailToSign, contrasena)
                val uid = authRepository.getCurrentUserUid()
                if (uid != null) {
                    val hasAccepted = try {
                        authRepository.checkUserAcceptance(uid)
                    } catch (e: Exception) {
                        authRepository.signOut()
                        throw Exception("No se pudo verificar la aceptación de términos por error de conexión.")
                    }

                    _uiState.update { it.copy(isLoading = false) }
                    if (hasAccepted) {
                        onNavigateMain()
                    } else {
                        onNavigateTerminos()
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener usuario autenticado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al iniciar sesión") }
            }
        }
    }

    fun loginWithGoogle(context: Context, onNavigateMain: () -> Unit, onNavigateTerminos: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("741518921630-slb7lv7qicfd4etmdclckod0u0he52t5.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    authRepository.signInWithGoogleIdToken(googleIdToken)
                    val uid = authRepository.getCurrentUserUid()
                    if (uid != null) {
                        val hasAccepted = try {
                            authRepository.checkUserAcceptance(uid)
                        } catch (e: Exception) {
                            false
                        }

                        if (!hasAccepted) {
                            val profile = try {
                                authRepository.getUserProfile(uid)
                            } catch (e: Exception) {
                                emptyMap()
                            }
                            if (profile["correo"].isNullOrBlank()) {
                                val userData = mapOf(
                                    "nombre" to (googleIdTokenCredential.displayName ?: "Usuario Google"),
                                    "correo" to (googleIdTokenCredential.id ?: ""),
                                    "usuario" to (googleIdTokenCredential.id?.substringBefore("@") ?: "google_user")
                                )
                                authRepository.saveUserData(uid, userData)
                                // Usuario nuevo con Google: empieza con las categorías más comunes
                                try {
                                    categoryRepository.crearCategoriasPorDefecto()
                                } catch (e: Exception) {
                                    // No bloquea el inicio de sesión; puede agregarlas después
                                }
                            }
                        }

                        _uiState.update { it.copy(isLoading = false) }
                        if (hasAccepted) {
                            onNavigateMain()
                        } else {
                            onNavigateTerminos()
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Error al autenticar con Google") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Credencial de Google no válida") }
                }
            } catch (e: NoCredentialException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "NoCredentialException: ${e.message}") }
            } catch (e: GetCredentialCancellationException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Cancelado por el usuario") }
            } catch (e: GetCredentialException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "GetCredentialException [${e.type}]: ${e.message}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error [${e.javaClass.simpleName}]: ${e.message}") }
            }
        }
    }

    /**
     * Envía el enlace para restablecer la contraseña. Por privacidad, el
     * mensaje de éxito no revela si el correo está registrado o no.
     */
    fun recuperarContrasena(correo: String, onResultado: (exito: Boolean, mensaje: String) -> Unit) {
        val correoLimpio = correo.trim()
        if (correoLimpio.isBlank()) {
            onResultado(false, "Ingresa tu correo electrónico")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            onResultado(false, "El correo no es válido")
            return
        }
        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetEmail(correoLimpio)
                onResultado(true, MENSAJE_RECUPERACION_ENVIADA)
            } catch (e: FirebaseAuthInvalidUserException) {
                onResultado(true, MENSAJE_RECUPERACION_ENVIADA)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                onResultado(false, "El correo no es válido")
            } catch (e: FirebaseTooManyRequestsException) {
                onResultado(false, "Demasiados intentos. Espera unos minutos e inténtalo de nuevo.")
            } catch (e: FirebaseNetworkException) {
                onResultado(false, "Sin conexión. Revisa tu internet e inténtalo de nuevo.")
            } catch (e: Exception) {
                onResultado(false, "No se pudo enviar el correo. Inténtalo más tarde.")
            }
        }
    }

    companion object {
        const val MENSAJE_RECUPERACION_ENVIADA =
            "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña. Revisa también tu carpeta de spam."
    }
}
