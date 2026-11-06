package com.example.appmovimientos.ui.screens.cuentas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CuentasViewModel(
    private val accountRepository: AccountRepository = AccountRepository()
) : ViewModel() {

    data class CuentasUiState(
        val cuentas: List<Account> = emptyList(),
        val isLoading: Boolean = true,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(CuentasUiState())
    val uiState: StateFlow<CuentasUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                accountRepository.getAccountsFlow().collect { cuentas ->
                    _uiState.update { it.copy(cuentas = cuentas, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun deleteAccount(account: Account, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                accountRepository.deleteAccount(account.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar cuenta")
            }
        }
    }

    fun createAccount(
        nombre: String,
        tipo: String,
        banco: String,
        saldoInicial: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        when {
            nombre.isBlank() -> {
                onError("Por favor ingresa un nombre para la cuenta")
            }
            saldoInicial.isBlank() -> {
                onError("Por favor ingresa el saldo inicial")
            }
            else -> {
                val bancoFinal = if (tipo == "Efectivo") "" else if (banco.isBlank()) "BCP" else banco
                val saldo = saldoInicial.toDoubleOrNull() ?: 0.0
                val account = Account(
                    name = nombre,
                    type = tipo,
                    bank = bancoFinal,
                    balance = saldo
                )
                viewModelScope.launch {
                    try {
                        accountRepository.createAccount(account)
                        onSuccess()
                    } catch (e: Exception) {
                        onError("Error al crear la cuenta: ${e.message}")
                    }
                }
            }
        }
    }

    fun updateAccount(
        account: Account,
        nuevoSaldoStr: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val saldo = nuevoSaldoStr.toDoubleOrNull()
        if (saldo == null) {
            onError("Por favor ingresa un monto válido")
            return
        }
        viewModelScope.launch {
            try {
                val updated = account.copy(balance = saldo)
                accountRepository.updateAccount(updated)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar la cuenta")
            }
        }
    }
}
