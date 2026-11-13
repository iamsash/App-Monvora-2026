package com.example.appmovimientos.utils

import com.example.appmovimientos.data.model.Expense
import java.time.LocalDate
import java.time.YearMonth

/**
 * Filtros y ordenamientos de movimientos compartidos por el dashboard
 * y los reportes, para que ambos muestren siempre los mismos resultados.
 */
object MovementFilters {

    const val MES_ACTUAL = "Mes Actual"
    const val ULTIMOS_3_MESES = "Últimos 3 Meses"
    const val ULTIMO_ANIO = "Último Año"
    const val TODO = "Todo"

    /**
     * Filtra por periodo usando la fecha real de cada movimiento.
     * - Mes Actual: movimientos del mes calendario en curso.
     * - Últimos 3 Meses: el mes en curso y los dos anteriores.
     * - Último Año: movimientos del año calendario en curso.
     * - Todo (o cualquier otro valor): sin filtro.
     * Los movimientos con fecha inválida solo aparecen en "Todo".
     */
    fun filtrarPorPeriodo(
        movimientos: List<Expense>,
        periodo: String,
        hoy: LocalDate = DateUtils.hoy()
    ): List<Expense> {
        val mesActual = YearMonth.from(hoy)
        return when (periodo) {
            MES_ACTUAL -> movimientos.filter { mesDe(it) == mesActual }
            ULTIMOS_3_MESES -> {
                val desde = mesActual.minusMonths(2)
                movimientos.filter { mov ->
                    val mes = mesDe(mov)
                    mes != null && !mes.isBefore(desde) && !mes.isAfter(mesActual)
                }
            }
            ULTIMO_ANIO -> movimientos.filter { DateUtils.parse(it.date)?.year == hoy.year }
            else -> movimientos
        }
    }

    /** Más recientes primero; los de fecha inválida quedan al final. */
    fun ordenarPorFechaDesc(movimientos: List<Expense>): List<Expense> =
        movimientos.sortedWith(compareByDescending<Expense> { DateUtils.parse(it.date) })

    /** Movimientos de un año concreto. */
    fun delAnio(movimientos: List<Expense>, anio: Int): List<Expense> =
        movimientos.filter { DateUtils.parse(it.date)?.year == anio }

    /**
     * Suma de gastos por mes en orden cronológico. La clave es "M/yyyy"
     * (por ejemplo "1/2025") para mantener las etiquetas del gráfico.
     */
    fun gastosPorMes(movimientos: List<Expense>): LinkedHashMap<String, Double> {
        val totales = sortedMapOf<YearMonth, Double>()
        for (mov in movimientos) {
            if (mov.type != "Gasto") continue
            val mes = mesDe(mov) ?: continue
            totales[mes] = (totales[mes] ?: 0.0) + MoneyUtils.montoDe(mov)
        }
        val resultado = LinkedHashMap<String, Double>()
        for ((mes, total) in totales) {
            resultado["${mes.monthValue}/${mes.year}"] = total
        }
        return resultado
    }

    private fun mesDe(mov: Expense): YearMonth? = DateUtils.parse(mov.date)?.let { YearMonth.from(it) }
}
