package com.example.appmovimientos.data.repository

import com.example.appmovimientos.data.model.Account
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AccountRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private fun getUid(): String? = auth.currentUser?.uid

    fun getAccountsFlow(): Flow<List<Account>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = database.child("users").child(uid).child("cuentas")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Account>()
                for (child in snapshot.children) {
                    val account = child.getValue(Account::class.java)
                    account?.let { lista.add(it.copy(id = child.key ?: "")) }
                }
                trySend(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun createAccount(account: Account): String {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        val accountId = database.child("users").child(uid).child("cuentas").push().key!!
        val newAccount = account.copy(id = accountId)
        database.child("users").child(uid).child("cuentas").child(accountId).setValue(newAccount).await()
        return accountId
    }

    /** Cantidad de movimientos registrados en la cuenta. */
    suspend fun countMovements(accountId: String): Int {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        // Para que esta consulta sea eficiente, las reglas de Firebase deben
        // declarar ".indexOn": ["accountId"] en users/$uid/movimientos.
        val snapshot = database.child("users").child(uid).child("movimientos")
            .orderByChild("accountId").equalTo(accountId)
            .get().await()
        return snapshot.childrenCount.toInt()
    }

    /**
     * Elimina la cuenta solo si no tiene movimientos. Las cuentas con
     * historial se conservan para mantener la trazabilidad de los datos.
     *
     * @throws CuentaConMovimientosException si la cuenta tiene movimientos.
     */
    suspend fun deleteAccount(accountId: String) {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        if (accountId.isBlank()) throw IllegalArgumentException("ID de cuenta inválido")
        val cantidad = countMovements(accountId)
        if (cantidad > 0) throw CuentaConMovimientosException(cantidad)
        database.child("users").child(uid).child("cuentas").child(accountId).removeValue().await()
    }

    suspend fun updateAccount(account: Account) {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        if (account.id.isBlank()) throw IllegalArgumentException("ID de cuenta inválido")
        database.child("users").child(uid).child("cuentas").child(account.id).setValue(account).await()
    }
}

class CuentaConMovimientosException(val cantidad: Int) : Exception(
    if (cantidad == 1) {
        "No se puede eliminar la cuenta porque tiene 1 movimiento registrado."
    } else {
        "No se puede eliminar la cuenta porque tiene $cantidad movimientos registrados."
    }
)
