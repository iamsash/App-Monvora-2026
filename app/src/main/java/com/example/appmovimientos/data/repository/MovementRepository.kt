package com.example.appmovimientos.data.repository

import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.utils.MoneyUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MovementRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private fun getUid(): String? = auth.currentUser?.uid

    fun getMovimientosFlow(): Flow<List<Expense>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = database.child("users").child(uid).child("movimientos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Expense>()
                for (child in snapshot.children) {
                    val expense = child.getValue(Expense::class.java)
                    expense?.let { lista.add(it.copy(id = child.key ?: "")) }
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

    fun getCategoriasFlow(): Flow<List<Category>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = database.child("users").child(uid).child("categorias")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Category>()
                for (child in snapshot.children) {
                    val category = child.getValue(Category::class.java)
                    category?.let { lista.add(it.copy(id = child.key ?: "")) }
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

    fun getCuentasFlow(): Flow<List<Account>> = callbackFlow {
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

    /**
     * Guarda el movimiento y actualiza el saldo de la cuenta en una sola
     * escritura atómica: o se aplican ambos cambios o ninguno.
     *
     * El saldo se modifica con ServerValue.increment, así el servidor suma
     * o resta sobre el valor actual aunque haya otra operación en paralelo
     * (antes se calculaba con un saldo que podía estar desactualizado).
     */
    suspend fun createMovement(expense: Expense, account: Account) {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        val monto = MoneyUtils.parseMonto(expense.amount)
            ?: throw IllegalArgumentException("Monto inválido")
        val userRef = database.child("users").child(uid)
        val movId = userRef.child("movimientos").push().key
            ?: throw IllegalStateException("No se pudo generar el ID del movimiento")
        val movimiento = expense.copy(id = movId)

        val cambios = hashMapOf<String, Any?>(
            "movimientos/$movId" to movimiento.toMap(),
            "cuentas/${account.id}/balance" to ServerValue.increment(MoneyUtils.deltaSaldo(expense.type, monto))
        )
        userRef.updateChildren(cambios).await()
    }

    /**
     * Elimina el movimiento y revierte su efecto en el saldo en una sola
     * escritura atómica. Si la cuenta ya no existe, solo elimina el movimiento.
     */
    suspend fun deleteMovement(expense: Expense, account: Account?) {
        val uid = getUid() ?: throw IllegalStateException("Usuario no autenticado")
        if (expense.id.isBlank()) throw IllegalArgumentException("ID de movimiento inválido")
        val userRef = database.child("users").child(uid)

        val cambios = hashMapOf<String, Any?>("movimientos/${expense.id}" to null)
        if (account != null) {
            val monto = MoneyUtils.montoDe(expense)
            cambios["cuentas/${account.id}/balance"] =
                ServerValue.increment(MoneyUtils.deltaSaldo(expense.type, monto, revertir = true))
        }
        userRef.updateChildren(cambios).await()
    }

    private fun Expense.toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "amount" to amount,
        "description" to description,
        "categoryId" to categoryId,
        "accountId" to accountId,
        "date" to date,
        "type" to type
    )
}
