package com.example.appmovimientos.ui.screens.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.data.repository.MovementRepository
import com.example.appmovimientos.utils.MoneyUtils
import com.example.appmovimientos.utils.MovementFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovimientosViewModel(
    private val movementRepository: MovementRepository = MovementRepository()
) : ViewModel() {

    data class MovimientosUiState(
        val movimientos: List<Expense> = emptyList(),
        val categorias: List<Category> = emptyList(),
        val cuentas: List<Account> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(MovimientosUiState())
    val uiState: StateFlow<MovimientosUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                movementRepository.getMovimientosFlow().collect { movimientos ->
                    _uiState.update { it.copy(movimientos = MovementFilters.ordenarPorFechaDesc(movimientos), isLoading = false) }
                }
            }
            launch {
                movementRepository.getCategoriasFlow().collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias) }
                }
            }
            launch {
                movementRepository.getCuentasFlow().collect { cuentas ->
                    _uiState.update { it.copy(cuentas = cuentas) }
                }
            }
        }
    }

    fun createMovement(
        monto: String,
        descripcion: String,
        fecha: String,
        tipo: String,
        categoria: Category?,
        cuenta: Account?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (monto.isBlank() || descripcion.isBlank() || fecha == "Selecciona fecha" || cuenta == null) {
            onError("Por favor completa todos los campos")
            return
        }

        val montoValidado = MoneyUtils.validarMontoMovimiento(monto)
        if (montoValidado == null) {
            onError("Ingresa un monto válido mayor que 0 (máximo 2 decimales)")
            return
        }

        val expense = Expense(
            amount = MoneyUtils.normalizar(montoValidado),
            description = descripcion,
            categoryId = categoria?.id ?: "",
            accountId = cuenta.id,
            date = fecha,
            type = tipo
        )

        viewModelScope.launch {
            try {
                movementRepository.createMovement(expense, cuenta)
                onSuccess()
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun deleteMovement(
        expense: Expense,
        cuentasMap: Map<String, Account>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cuenta = cuentasMap[expense.accountId]
                movementRepository.deleteMovement(expense, cuenta)
                onSuccess()
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }
}
