package com.example.appmovimientos.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.data.repository.AccountRepository
import com.example.appmovimientos.data.repository.AuthRepository
import com.example.appmovimientos.data.repository.CategoryRepository
import com.example.appmovimientos.data.repository.MovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.appmovimientos.utils.MoneyUtils
import com.example.appmovimientos.utils.MovementFilters

class MainViewModel(
    private val accountRepository: AccountRepository = AccountRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val movementRepository: MovementRepository = MovementRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    data class MainUiState(
        val cuentas: List<Account> = emptyList(),
        val categorias: Map<String, Category> = emptyMap(),
        val movimientos: List<Expense> = emptyList(),
        val balanceTotal: Double = 0.0,
        val gastoTotal: Double = 0.0,
        val ingresoTotal: Double = 0.0,
        val gastosMensuales: Map<String, Double> = emptyMap(),
        val categoriasMasGastadas: List<Pair<String, Double>> = emptyList(),
        val periodoSeleccionado: String = "Mes Actual",
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                combine(
                    accountRepository.getAccountsFlow(),
                    categoryRepository.getCategoriesFlow(),
                    movementRepository.getMovimientosFlow()
                ) { cuentas, categoriasList, movimientosList ->
                    val balanceTotal = cuentas.sumOf { it.balance }

                    var gasto = 0.0
                    var ingreso = 0.0
                    val gastosPorCategoria = mutableMapOf<String, Double>()

                    for (mov in movimientosList) {
                        val monto = MoneyUtils.montoDe(mov)
                        if (mov.type == "Gasto") {
                            gasto += monto
                            gastosPorCategoria[mov.categoryId] = (gastosPorCategoria[mov.categoryId] ?: 0.0) + monto
                        } else {
                            ingreso += monto
                        }
                    }
                    val gastosPorMes = MovementFilters.gastosPorMes(movimientosList)

                    val categoriasMap = categoriasList.associateBy { it.id }
                    val topCategorias = gastosPorCategoria.toList()
                        .sortedByDescending { it.second }
                        .take(5)

                    Triple(
                        Triple(cuentas, categoriasMap, MovementFilters.ordenarPorFechaDesc(movimientosList)),
                        Triple(balanceTotal, gasto, ingreso),
                        Pair(gastosPorMes, topCategorias)
                    )
                }.collect { result ->
                    val (baseData, totals, stats) = result
                    val (cuentas, categorias, movimientos) = baseData
                    val (balanceTotal, gastoTotal, ingresoTotal) = totals
                    val (gastosMensuales, categoriasMasGastadas) = stats

                    _uiState.update {
                        it.copy(
                            cuentas = cuentas,
                            categorias = categorias,
                            movimientos = movimientos,
                            balanceTotal = balanceTotal,
                            gastoTotal = gastoTotal,
                            ingresoTotal = ingresoTotal,
                            gastosMensuales = gastosMensuales,
                            categoriasMasGastadas = categoriasMasGastadas,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun setPeriodo(periodo: String) {
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
    }

    fun deleteMovement(expense: Expense, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val cuenta = _uiState.value.cuentas.find { it.id == expense.accountId }
                movementRepository.deleteMovement(expense, cuenta)
                onSuccess()
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        authRepository.signOut()
        onSuccess()
    }

    /** Movimientos del periodo elegido, ya ordenados del más reciente al más antiguo. */
    fun getFilteredMovimientos(): List<Expense> {
        val state = _uiState.value
        return MovementFilters.filtrarPorPeriodo(state.movimientos, state.periodoSeleccionado)
    }
}
