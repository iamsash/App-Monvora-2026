package com.example.appmovimientos.data.repository

import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.model.Category
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PropuestaAsistenteTest {

    private val hoy = LocalDate.of(2026, 11, 12)
    private val cuentas = listOf(
        Account(id = "cta-bcp", name = "BCP Ahorros", type = "Ahorros", bank = "BCP", balance = 100.0),
        Account(id = "cta-efe", name = "Efectivo", type = "Efectivo", balance = 20.0)
    )
    private val categorias = listOf(
        Category(id = "cat-comida", name = "Comida", type = "Gasto"),
        Category(id = "cat-sueldo", name = "Sueldo", type = "Ingreso")
    )

    private fun movimiento(
        tipo: String = "Gasto", monto: Double = 15.0, cuentaId: String = "cta-bcp",
        categoriaId: String? = "cat-comida", fecha: LocalDate = hoy
    ) = Propuesta.Movimiento(tipo, monto, "Menú", cuentaId, "BCP Ahorros", categoriaId, "Comida", fecha)

    // ---------- Lectura de la respuesta de la API ----------

    @Test
    fun `respuesta con accion de movimiento trae la propuesta`() {
        val r = AsistenteProtocolo.interpretar(
            200,
            """{"respuesta":"Confírmalo con Guardar","consultasRestantes":28,"accion":{"tipo":"movimiento","datos":
               {"tipo":"Ingreso","monto":50,"descripcion":"Partido","cuentaId":"cta-bcp","cuentaNombre":"BCP Ahorros",
                "categoriaId":null,"categoriaNombre":null,"fecha":"2026-11-10"}}}"""
        ) as ResultadoAsistente.Exito

        val p = r.propuesta as Propuesta.Movimiento
        assertEquals("Ingreso", p.tipo)
        assertEquals(50.0, p.monto, 0.0)
        assertNull(p.categoriaId)
        assertEquals(LocalDate.of(2026, 11, 10), p.fecha)
    }

    @Test
    fun `respuesta sin accion no trae propuesta`() {
        val r = AsistenteProtocolo.interpretar(200, """{"respuesta":"Hola","consultasRestantes":29,"accion":null}""")
        assertEquals(ResultadoAsistente.Exito("Hola", 29), r)
    }

    @Test
    fun `accion mal formada se ignora`() {
        assertNull(Propuesta.desdeJson(JSONObject("""{"tipo":"movimiento","datos":{"tipo":"Gasto","monto":5}}""")))
        assertNull(Propuesta.desdeJson(JSONObject("""{"tipo":"borrar","datos":{}}""")))
        assertNull(Propuesta.desdeJson(JSONObject("""{"tipo":"categoria","datos":{"nombre":"X","tipo":"Otro"}}""")))
    }

    @Test
    fun `accion de cuenta y categoria`() {
        val cuenta = Propuesta.desdeJson(JSONObject("""{"tipo":"cuenta","datos":{"nombre":"Viaje","tipo":"Ahorros","banco":"BBVA","saldoInicial":0}}"""))
        assertEquals(Propuesta.Cuenta("Viaje", "Ahorros", "BBVA", 0.0), cuenta)
        val cat = Propuesta.desdeJson(JSONObject("""{"tipo":"categoria","datos":{"nombre":"Mascotas","tipo":"Gasto"}}"""))
        assertEquals(Propuesta.Categoria("Mascotas", "Gasto"), cat)
    }

    // ---------- Conversión con las validaciones de la app ----------

    @Test
    fun `movimiento valido se convierte como lo haria el formulario`() {
        val (expense, cuenta) = ConversorPropuesta.aMovimiento(movimiento(monto = 12.5), cuentas, categorias, hoy)
        assertEquals("12.50", expense.amount)
        assertEquals("12/11/2026", expense.date)
        assertEquals("cat-comida", expense.categoryId)
        assertEquals("cta-bcp", expense.accountId)
        assertEquals("cta-bcp", cuenta.id)
    }

    @Test
    fun `movimiento a una cuenta que ya no existe no se guarda`() {
        val e = assertThrows(IllegalArgumentException::class.java) {
            ConversorPropuesta.aMovimiento(movimiento(cuentaId = "borrada"), cuentas, categorias, hoy)
        }
        assertTrue(e.message!!.contains("ya no existe"))
    }

    @Test
    fun `montos invalidos o fecha futura no se guardan`() {
        listOf(movimiento(monto = 0.0), movimiento(monto = -3.0), movimiento(monto = 10.999), movimiento(fecha = hoy.plusDays(1)))
            .forEach { p ->
                assertThrows(IllegalArgumentException::class.java) { ConversorPropuesta.aMovimiento(p, cuentas, categorias, hoy) }
            }
    }

    @Test
    fun `categoria de otro tipo o inexistente queda sin categoria`() {
        val (ingreso, _) = ConversorPropuesta.aMovimiento(movimiento(tipo = "Ingreso", categoriaId = "cat-comida"), cuentas, categorias, hoy)
        assertEquals("", ingreso.categoryId)
        val (gasto, _) = ConversorPropuesta.aMovimiento(movimiento(categoriaId = "no-existe"), cuentas, categorias, hoy)
        assertEquals("", gasto.categoryId)
    }

    @Test
    fun `cuenta en efectivo no guarda banco y no se repite el nombre`() {
        val efectivo = ConversorPropuesta.aCuenta(Propuesta.Cuenta("Billetera", "Efectivo", "BCP", 5.0), cuentas)
        assertEquals("", efectivo.bank)
        assertEquals(5.0, efectivo.balance, 0.0)

        assertThrows(IllegalArgumentException::class.java) {
            ConversorPropuesta.aCuenta(Propuesta.Cuenta("bcp ahorros", "Ahorros", "BCP", 0.0), cuentas)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ConversorPropuesta.aCuenta(Propuesta.Cuenta("Rara", "Cripto", "", 0.0), cuentas)
        }
    }

    @Test
    fun `categoria nueva toma el emoji del catalogo solo si corresponde`() {
        val futbol = ConversorPropuesta.aCategoria(Propuesta.Categoria("Fútbol", "Gasto"), categorias)
        assertEquals("deporte", futbol.icon)
        val rara = ConversorPropuesta.aCategoria(Propuesta.Categoria("Clases de guitarra", "Gasto"), categorias)
        assertEquals(CategoriasCatalogo.SIN_ICONO, rara.icon)
    }

    @Test
    fun `categoria repetida del mismo tipo no se guarda`() {
        assertThrows(IllegalArgumentException::class.java) {
            ConversorPropuesta.aCategoria(Propuesta.Categoria("COMIDA", "Gasto"), categorias)
        }
        // Mismo nombre pero de ingreso sí se permite
        ConversorPropuesta.aCategoria(Propuesta.Categoria("Comida", "Ingreso"), categorias)
    }
}
