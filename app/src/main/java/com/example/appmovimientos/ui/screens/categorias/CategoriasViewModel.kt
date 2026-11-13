package com.example.appmovimientos.ui.screens.categorias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.model.CategoriaSugerida
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriasViewModel(
    private val categoryRepository: CategoryRepository = CategoryRepository()
) : ViewModel() {

    data class CategoriasUiState(
        val categorias: List<Category> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(CategoriasUiState())
    val uiState: StateFlow<CategoriasUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                categoryRepository.getCategoriesFlow().collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun deleteCategory(category: Category, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(category.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar categoría")
            }
        }
    }

    fun createCategory(
        nombre: String,
        tipo: String,
        icono: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (nombre.isBlank()) {
            onError("Por favor ingresa un nombre para la categoría")
            return
        }
        if (existeNombre(nombre, tipo)) {
            onError("Ya tienes una categoría de $tipo llamada \"${nombre.trim()}\"")
            return
        }

        val category = Category(
            name = nombre.trim(),
            type = tipo,
            icon = icono,
            color = CategoriasCatalogo.colorPara(tipo)
        )

        viewModelScope.launch {
            try {
                categoryRepository.createCategory(category)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al crear la categoría: ${e.message}")
            }
        }
    }

    fun updateCategory(
        category: Category,
        nombre: String,
        tipo: String,
        icono: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (nombre.isBlank()) {
            onError("El nombre es obligatorio")
            return
        }
        if (existeNombre(nombre, tipo, excepto = category.id)) {
            onError("Ya tienes una categoría de $tipo llamada \"${nombre.trim()}\"")
            return
        }

        val categoriaActualizada = category.copy(
            name = nombre.trim(),
            type = tipo,
            icon = icono,
            color = CategoriasCatalogo.colorPara(tipo)
        )

        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(categoriaActualizada)
                onSuccess()
            } catch (e: Exception) {
                onError("Error al actualizar: ${e.message}")
            }
        }
    }

    /** Categorías por defecto que el usuario todavía no tiene (para el aviso "Agregar sugeridas"). */
    fun sugeridasFaltantes(): List<CategoriaSugerida> =
        CategoriasCatalogo.faltantes(_uiState.value.categorias, CategoriasCatalogo.porDefecto)

    /** Agrega de una vez las categorías por defecto que falten, sin duplicar las que ya tiene. */
    fun agregarSugeridas(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        val faltantes = sugeridasFaltantes()
        if (faltantes.isEmpty()) {
            onSuccess(0)
            return
        }
        viewModelScope.launch {
            try {
                categoryRepository.createCategories(faltantes.map { CategoriasCatalogo.aCategoria(it) })
                onSuccess(faltantes.size)
            } catch (e: Exception) {
                onError("No se pudieron agregar las categorías: ${e.message}")
            }
        }
    }

    private fun existeNombre(nombre: String, tipo: String, excepto: String? = null): Boolean {
        val buscado = CategoriasCatalogo.normalizar(nombre)
        return _uiState.value.categorias.any {
            it.id != excepto && it.type == tipo && CategoriasCatalogo.normalizar(it.name) == buscado
        }
    }
}
