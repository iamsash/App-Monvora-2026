package com.example.appmovimientos.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.repository.AuthRepository
import com.example.appmovimientos.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroUsuarioViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    var nombre: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(nombre = value) }
        }

    var correo: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(correo = value) }
        }

    var usuario: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(usuario = value) }
        }

    var contrasena: String = ""
        set(value) {
            field = value
            _uiState.update { it.copy(contrasena = value) }
        }

    data class RegistroUiState(
        val nombre: String = "",
        val correo: String = "",
        val usuario: String = "",
        val contrasena: String = "",
        val mostrarContrasena: Boolean = false,
        val errorMessage: String? = null,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(newValue: String) {
        nombre = newValue
    }

    fun onCorreoChange(newValue: String) {
        correo = newValue
    }

    fun onUsuarioChange(newValue: String) {
        usuario = newValue
    }

    fun onContrasenaChange(newValue: String) {
        contrasena = newValue
    }

    fun toggleMostrarContrasena() {
        _uiState.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }
    }

    fun validarFormulario(): Boolean {
        val correoLimpio = uiState.value.correo.trim()
        return when {
            uiState.value.nombre.isBlank() || correoLimpio.isBlank() || uiState.value.usuario.isBlank() || uiState.value.contrasena.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Todos los campos son obligatorios.") }
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches() -> {
                _uiState.update { it.copy(errorMessage = "El correo no es válido.") }
                false
            }
            uiState.value.contrasena.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
                false
            }
            else -> {
                _uiState.update { it.copy(errorMessage = null) }
                true
            }
        }
    }

    fun registrarUsuario(onSuccess: () -> Unit) {
        if (!validarFormulario()) return

        val correoLimpio = uiState.value.correo.trim()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.createUserWithEmailAndPassword(correoLimpio, uiState.value.contrasena)
                val uid = authRepository.getCurrentUserUid()
                if (uid != null) {
                    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    val aceptacionMap = mapOf(
                        "aceptado" to true,
                        "versionTerminos" to "1.0",
                        "versionPrivacidad" to "1.0",
                        "fechaAceptacion" to fechaActual
                    )
                    val userData = mapOf(
                        "nombre" to uiState.value.nombre,
                        "correo" to correoLimpio,
                        "usuario" to uiState.value.usuario,
                        "aceptacionTerminos" to aceptacionMap
                    )
                    authRepository.saveUserData(uid, userData)
                    crearCategoriasIniciales()
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error al obtener UID del usuario") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    /** Un usuario nuevo empieza con las categorías más comunes. Si falla, puede agregarlas después. */
    private suspend fun crearCategoriasIniciales() {
        try {
            categoryRepository.crearCategoriasPorDefecto()
        } catch (e: Exception) {
            // No se bloquea el registro: en la pantalla Categorías aparecerá "Agregar categorías sugeridas".
        }
    }
}
