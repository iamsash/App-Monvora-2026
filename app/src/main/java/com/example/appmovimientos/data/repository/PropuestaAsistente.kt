package com.example.appmovimientos.data.repository

import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.utils.DateUtils
import com.example.appmovimientos.utils.MoneyUtils
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.time.LocalDate

/**
 * Registro que el asistente dejó preparado. NO está guardado: la app lo muestra
 * en una tarjeta y solo se guarda si el usuario pulsa "Guardar".
 */
sealed class Propuesta {
    data class Movimiento(
        val tipo: String,
        val monto: Double,
        val descripcion: String,
        val cuentaId: String,
        val cuentaNombre: String,
        val categoriaId: String?,
        val categoriaNombre: String?,
        val fecha: LocalDate
    ) : Propuesta()

    data class Cuenta(
        val nombre: String,
        val tipo: String,
        val banco: String,
        val saldoInicial: Double
    ) : Propuesta()

    data class Categoria(val nombre: String, val tipo: String) : Propuesta()

    companion object {
        /** Lee el campo "accion" de la API. Si algo no cuadra, se ignora (null). */
        fun desdeJson(accion: JSONObject?): Propuesta? {
            if (accion == null) return null
            val d = accion.optJSONObject("datos") ?: return null
            return try {
                when (accion.optString("tipo")) {
                    "movimiento" -> Movimiento(
                        tipo = tipoValido(d.getString("tipo")),
                        monto = d.getDouble("monto"),
                        descripcion = d.getString("descripcion"),
                        cuentaId = d.getString("cuentaId").also { require(it.isNotBlank()) },
                        cuentaNombre = d.optString("cuentaNombre"),
                        categoriaId = d.textoOpcional("categoriaId"),
                        categoriaNombre = d.textoOpcional("categoriaNombre"),
                        fecha = LocalDate.parse(d.getString("fecha"))
                    )
                    "cuenta" -> Cuenta(
                        nombre = d.getString("nombre"),
                        tipo = d.getString("tipo"),
                        banco = d.optString("banco"),
                        saldoInicial = d.getDouble("saldoInicial")
                    )
                    "categoria" -> Categoria(d.getString("nombre"), tipoValido(d.getString("tipo")))
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun tipoValido(tipo: String) = tipo.also {
            require(it == CategoriasCatalogo.GASTO || it == CategoriasCatalogo.INGRESO)
        }

        private fun JSONObject.textoOpcional(clave: String): String? =
            if (isNull(clave)) null else optString(clave).takeIf { it.isNotBlank() }
    }
}

/**
 * Convierte una propuesta en lo que se guarda en Firebase, aplicando las mismas
 * validaciones que los formularios de la app (no se confía en lo que dijo la IA).
 */
object ConversorPropuesta {
    private val TIPOS_CUENTA = setOf("Banco", "Efectivo", "Ahorros", "Tarjeta de Crédito")

    fun aMovimiento(p: Propuesta.Movimiento, cuentas: List<Account>, categorias: List<Category>, hoy: LocalDate): Pair<Expense, Account> {
        val cuenta = cuentas.firstOrNull { it.id == p.cuentaId }
            ?: throw IllegalArgumentException("La cuenta ${p.cuentaNombre} ya no existe.")
        val monto = MoneyUtils.validarMontoMovimiento(MoneyUtils.normalizar(p.monto))
            ?.takeIf { it == p.monto }
            ?: throw IllegalArgumentException("El monto no es válido (mayor que 0, máximo 2 decimales).")
        require(p.descripcion.isNotBlank()) { "Falta la descripción." }
        require(!p.fecha.isAfter(hoy)) { "La fecha no puede ser futura." }
        // Si la categoría ya no existe o no es del mismo tipo, se guarda sin categoría
        val categoriaId = categorias.firstOrNull { it.id == p.categoriaId && it.type == p.tipo }?.id ?: ""

        val expense = Expense(
            amount = MoneyUtils.normalizar(monto),
            description = p.descripcion.trim().take(100),
            categoryId = categoriaId,
            accountId = cuenta.id,
            date = DateUtils.formatParaGuardar(p.fecha),
            type = p.tipo
        )
        return expense to cuenta
    }

    fun aCuenta(p: Propuesta.Cuenta, existentes: List<Account>): Account {
        val nombre = p.nombre.trim()
        require(nombre.isNotBlank()) { "Falta el nombre de la cuenta." }
        require(p.tipo in TIPOS_CUENTA) { "Tipo de cuenta no válido." }
        require(existentes.none { CategoriasCatalogo.normalizar(it.name) == CategoriasCatalogo.normalizar(nombre) }) {
            "Ya tienes una cuenta llamada $nombre."
        }
        val saldo = MoneyUtils.validarSaldo(MoneyUtils.normalizar(p.saldoInicial))
            ?: throw IllegalArgumentException("El saldo inicial no es válido.")
        val banco = if (p.tipo == "Efectivo") "" else p.banco.ifBlank { "Otro" }
        return Account(name = nombre, type = p.tipo, bank = banco, balance = saldo)
    }

    fun aCategoria(p: Propuesta.Categoria, existentes: List<Category>): Category {
        val nombre = p.nombre.trim()
        require(nombre.isNotBlank()) { "Falta el nombre de la categoría." }
        require(existentes.none { it.type == p.tipo && CategoriasCatalogo.normalizar(it.name) == CategoriasCatalogo.normalizar(nombre) }) {
            "Ya tienes la categoría $nombre."
        }
        // Mismo criterio que al crear a mano: emoji solo si corresponde a una del catálogo
        val icono = CategoriasCatalogo.coincidenciaPorNombre(nombre, p.tipo)?.id ?: CategoriasCatalogo.SIN_ICONO
        return Category(name = nombre, type = p.tipo, icon = icono, color = CategoriasCatalogo.colorPara(p.tipo))
    }
}

/** Guarda una propuesta confirmada por el usuario. */
fun interface GuardadorPropuestas {
    suspend fun guardar(propuesta: Propuesta)
}

/** Guarda usando los repositorios de siempre (los mismos que los formularios). */
class GuardadorFirebase(
    private val movimientos: MovementRepository = MovementRepository(),
    private val cuentas: AccountRepository = AccountRepository(),
    private val categorias: CategoryRepository = CategoryRepository()
) : GuardadorPropuestas {
    override suspend fun guardar(propuesta: Propuesta) {
        when (propuesta) {
            is Propuesta.Movimiento -> {
                val (expense, cuenta) = ConversorPropuesta.aMovimiento(
                    propuesta,
                    cuentas.getAccountsFlow().first(),
                    categorias.getCategoriesFlow().first(),
                    DateUtils.hoy()
                )
                movimientos.createMovement(expense, cuenta)
            }
            is Propuesta.Cuenta ->
                cuentas.createAccount(ConversorPropuesta.aCuenta(propuesta, cuentas.getAccountsFlow().first()))
            is Propuesta.Categoria ->
                categorias.createCategory(ConversorPropuesta.aCategoria(propuesta, categorias.getCategoriesFlow().first()))
        }
    }
}
