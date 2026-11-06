package com.example.appmovimientos.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class CuentaConMovimientosExceptionTest {

    @Test
    fun `defecto 5 - el mensaje indica cuantos movimientos impiden eliminar la cuenta`() {
        assertEquals(
            "No se puede eliminar la cuenta porque tiene 3 movimientos registrados.",
            CuentaConMovimientosException(3).message
        )
    }

    @Test
    fun `defecto 5 - el mensaje usa singular con un movimiento`() {
        assertEquals(
            "No se puede eliminar la cuenta porque tiene 1 movimiento registrado.",
            CuentaConMovimientosException(1).message
        )
    }
}
