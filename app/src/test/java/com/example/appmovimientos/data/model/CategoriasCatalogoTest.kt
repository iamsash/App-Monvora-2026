package com.example.appmovimientos.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoriasCatalogoTest {

    private fun cat(nombre: String, tipo: String = "Gasto", icon: String = "ShoppingCart") =
        Category(id = nombre, name = nombre, type = tipo, icon = icon)

    @Test
    fun `por defecto hay 10 de gasto y 4 de ingreso`() {
        val defecto = CategoriasCatalogo.porDefecto
        assertEquals(10, defecto.count { it.tipo == "Gasto" })
        assertEquals(4, defecto.count { it.tipo == "Ingreso" })
    }

    @Test
    fun `los ids del catalogo son unicos`() {
        val ids = CategoriasCatalogo.todas.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `categorias creadas desde el catalogo usan su emoji`() {
        assertEquals("⚽", CategoriasCatalogo.emojiPara(cat("Fútbol", icon = "deporte")))
        assertEquals("💼", CategoriasCatalogo.emojiPara(cat("Sueldo", tipo = "Ingreso", icon = "sueldo")))
    }

    @Test
    fun `categorias antiguas toman el emoji si el nombre coincide`() {
        assertEquals("⚽", CategoriasCatalogo.emojiPara(cat("FUTBOL")))
        assertEquals("🚌", CategoriasCatalogo.emojiPara(cat("TRANSPORTE", icon = "Transport")))
        assertEquals("🎓", CategoriasCatalogo.emojiPara(cat("Pensión universidad")))
    }

    @Test
    fun `nombres desconocidos quedan en blanco aunque tengan un icono antiguo`() {
        assertNull(CategoriasCatalogo.emojiPara(cat("Clases de guitarra", icon = "Work")))
        assertNull(CategoriasCatalogo.emojiPara(cat("XYZ", icon = "Food")))
    }

    @Test
    fun `personalizadas sin icono quedan en blanco`() {
        assertNull(CategoriasCatalogo.emojiPara(cat("Clases de guitarra", icon = CategoriasCatalogo.SIN_ICONO)))
    }

    @Test
    fun `el tipo importa - un regalo recibido no usa la categoria de gasto`() {
        assertEquals("regalos_recibidos", CategoriasCatalogo.coincidencia(cat("Regalo de cumpleaños", tipo = "Ingreso"))?.id)
        assertEquals("regalos", CategoriasCatalogo.coincidencia(cat("Regalo de cumpleaños"))?.id)
    }

    @Test
    fun `un id del catalogo con el tipo cambiado no muestra un emoji equivocado`() {
        // "deporte" es de gasto: si la categoría se cambió a ingreso, se busca por nombre
        assertNull(CategoriasCatalogo.emojiPara(cat("Clases", tipo = "Ingreso", icon = "deporte")))
    }

    @Test
    fun `faltantes omite las que el usuario ya tiene`() {
        val existentes = listOf(cat("FUTBOL"), cat("Comida", icon = "comida"), cat("Sueldo", tipo = "Ingreso"))
        val faltantes = CategoriasCatalogo.faltantes(existentes, CategoriasCatalogo.porDefecto).map { it.id }

        assertTrue("comida" !in faltantes)
        assertTrue("sueldo" !in faltantes)
        assertTrue("transporte" in faltantes)
        assertEquals(12, faltantes.size) // 14 por defecto - Comida - Sueldo
    }

    @Test
    fun `a categoria guarda el id del catalogo como icono`() {
        val c = CategoriasCatalogo.aCategoria(CategoriasCatalogo.porId("transporte")!!)
        assertEquals("Transporte", c.name)
        assertEquals("Gasto", c.type)
        assertEquals("transporte", c.icon)
    }

    @Test
    fun `normalizar quita tildes y mayusculas`() {
        assertEquals("educacion y pension", CategoriasCatalogo.normalizar("  EDUCACIÓN y Pensión! "))
    }
}
