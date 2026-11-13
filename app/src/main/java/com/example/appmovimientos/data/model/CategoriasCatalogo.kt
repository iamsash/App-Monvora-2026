package com.example.appmovimientos.data.model

import java.text.Normalizer

/** Categoría predefinida: el emoji y el nombre van siempre juntos. */
data class CategoriaSugerida(
    val id: String,
    val nombre: String,
    val emoji: String,
    val tipo: String,
    val porDefecto: Boolean,
    /** Palabras que identifican esta categoría en nombres escritos por el usuario (sin tildes, en minúsculas). */
    val palabrasClave: List<String>
)

/**
 * Catálogo único de categorías con emoji. De aquí salen:
 * - las categorías por defecto que recibe un usuario nuevo,
 * - las sugeridas que se pueden agregar después,
 * - el emoji que se muestra para cualquier categoría (incluidas las antiguas).
 *
 * Regla: una categoría solo muestra el emoji de su entrada del catálogo. Si no corresponde
 * a ninguna, se muestra en blanco (sin emoji), para que nunca haya un emoji equivocado.
 */
object CategoriasCatalogo {

    const val GASTO = "Gasto"
    const val INGRESO = "Ingreso"

    /** Valor de `Category.icon` para las categorías personalizadas, que se muestran sin emoji. */
    const val SIN_ICONO = ""

    val todas: List<CategoriaSugerida> = listOf(
        // ---- Gastos ----
        CategoriaSugerida("comida", "Comida", "🍽️", GASTO, true, listOf("comida", "restaurante", "almuerzo", "cena", "desayuno", "menu", "food")),
        CategoriaSugerida("supermercado", "Supermercado", "🛒", GASTO, true, listOf("supermercado", "mercado", "bodega", "abarrotes", "compras")),
        CategoriaSugerida("transporte", "Transporte", "🚌", GASTO, true, listOf("transporte", "taxi", "combi", "bus", "pasaje", "pasajes", "movilidad", "uber", "transport")),
        CategoriaSugerida("combustible", "Combustible", "⛽", GASTO, false, listOf("combustible", "gasolina", "grifo")),
        CategoriaSugerida("vivienda", "Alquiler y vivienda", "🏠", GASTO, true, listOf("alquiler", "vivienda", "renta", "departamento")),
        CategoriaSugerida("servicios", "Servicios", "💡", GASTO, true, listOf("servicios", "luz", "agua", "internet", "telefono", "cable")),
        CategoriaSugerida("salud", "Salud", "💊", GASTO, true, listOf("salud", "farmacia", "medico", "medicina", "medicinas", "clinica", "health")),
        CategoriaSugerida("educacion", "Educación", "🎓", GASTO, true, listOf("educacion", "colegio", "universidad", "pension", "curso", "cursos", "utiles", "education")),
        CategoriaSugerida("entretenimiento", "Entretenimiento", "🎬", GASTO, true, listOf("entretenimiento", "cine", "salidas", "diversion", "juegos", "entertainment")),
        CategoriaSugerida("deporte", "Deporte", "⚽", GASTO, false, listOf("deporte", "deportes", "futbol", "fulbito", "gimnasio", "gym")),
        CategoriaSugerida("ropa", "Ropa", "👕", GASTO, true, listOf("ropa", "vestimenta", "zapatillas", "zapatos")),
        CategoriaSugerida("mascotas", "Mascotas", "🐾", GASTO, false, listOf("mascota", "mascotas", "perro", "gato", "veterinaria")),
        CategoriaSugerida("viajes", "Viajes", "✈️", GASTO, false, listOf("viaje", "viajes", "vacaciones", "hotel", "vuelo")),
        CategoriaSugerida("regalos", "Regalos", "🎁", GASTO, false, listOf("regalo", "regalos", "gift")),
        CategoriaSugerida("suscripciones", "Suscripciones", "📺", GASTO, false, listOf("suscripcion", "suscripciones", "netflix", "spotify", "streaming")),
        CategoriaSugerida("deudas", "Deudas y tarjeta", "💳", GASTO, false, listOf("deuda", "deudas", "tarjeta", "cuota", "cuotas")),
        CategoriaSugerida("cuidado_personal", "Cuidado personal", "💇", GASTO, false, listOf("cuidado", "peluqueria", "belleza")),
        CategoriaSugerida("hogar", "Hogar", "🔧", GASTO, false, listOf("hogar", "reparacion", "reparaciones", "muebles")),
        CategoriaSugerida("tecnologia", "Tecnología", "📱", GASTO, false, listOf("tecnologia", "computadora", "laptop", "celular")),
        CategoriaSugerida("otros_gastos", "Otros gastos", "📦", GASTO, true, listOf("otros", "otro", "varios")),
        // ---- Ingresos ----
        CategoriaSugerida("sueldo", "Sueldo", "💼", INGRESO, true, listOf("sueldo", "salario", "salary", "planilla")),
        CategoriaSugerida("trabajos_extra", "Trabajos extra", "💻", INGRESO, true, listOf("extra", "extras", "freelance", "cachuelo", "cachuelos")),
        CategoriaSugerida("ventas", "Ventas", "🏪", INGRESO, false, listOf("venta", "ventas", "negocio")),
        CategoriaSugerida("premios", "Premios y ganancias", "🏆", INGRESO, true, listOf("premio", "premios", "ganancia", "ganancias", "apuesta", "apuestas")),
        CategoriaSugerida("regalos_recibidos", "Regalos recibidos", "🎁", INGRESO, false, listOf("regalo", "regalos", "gift")),
        CategoriaSugerida("intereses", "Intereses y ahorros", "🏦", INGRESO, false, listOf("interes", "intereses", "ahorro", "ahorros")),
        CategoriaSugerida("reembolsos", "Reembolsos", "↩️", INGRESO, false, listOf("reembolso", "reembolsos", "devolucion")),
        CategoriaSugerida("prestamo", "Préstamo recibido", "🤝", INGRESO, false, listOf("prestamo", "prestamos")),
        CategoriaSugerida("otros_ingresos", "Otros ingresos", "📦", INGRESO, true, listOf("otros", "otro", "varios"))
    )

    private val porId: Map<String, CategoriaSugerida> = todas.associateBy { it.id }

    fun porId(id: String?): CategoriaSugerida? = id?.let { porId[it] }

    /** Categorías que recibe automáticamente un usuario nuevo. */
    val porDefecto: List<CategoriaSugerida> get() = todas.filter { it.porDefecto }

    fun deTipo(tipo: String): List<CategoriaSugerida> = todas.filter { it.tipo == tipo }

    /**
     * Entrada del catálogo a la que corresponde una categoría guardada:
     * 1. por su id (categorías creadas desde el catálogo),
     * 2. o, para categorías antiguas o personalizadas, si su nombre coincide con una palabra clave del mismo tipo.
     * null si no corresponde a ninguna (se muestra sin emoji).
     */
    fun coincidencia(categoria: Category): CategoriaSugerida? =
        porId(categoria.icon)?.takeIf { it.tipo == categoria.type }
            ?: coincidenciaPorNombre(categoria.name, categoria.type)

    fun coincidenciaPorNombre(nombre: String, tipo: String): CategoriaSugerida? {
        val palabras = normalizar(nombre).split(' ').filter { it.isNotEmpty() }.toSet()
        if (palabras.isEmpty()) return null
        val candidatas = deTipo(tipo)
        // Primero el nombre exacto ("Otros gastos"), luego cualquier palabra clave ("FUTBOL" → Deporte)
        return candidatas.firstOrNull { normalizar(it.nombre) == normalizar(nombre) }
            ?: candidatas.firstOrNull { sugerida -> sugerida.palabrasClave.any { it in palabras } }
    }

    /** Emoji a mostrar para una categoría, o null si se muestra en blanco. */
    fun emojiPara(categoria: Category?): String? = categoria?.let { coincidencia(it)?.emoji }

    /** Emoji a partir del nombre (para pantallas que solo tienen el nombre, como Reportes). */
    fun emojiParaNombre(nombre: String, tipo: String = GASTO): String? = coincidenciaPorNombre(nombre, tipo)?.emoji

    /** Sugeridas que el usuario todavía no tiene (para no crear duplicados). */
    fun faltantes(existentes: List<Category>, entre: List<CategoriaSugerida> = todas): List<CategoriaSugerida> {
        val yaTiene = existentes.mapNotNull { coincidencia(it)?.id }.toSet()
        return entre.filter { it.id !in yaTiene }
    }

    /** Convierte una sugerida en la categoría que se guarda en Firebase. */
    fun aCategoria(sugerida: CategoriaSugerida, nombre: String = sugerida.nombre): Category =
        Category(name = nombre, type = sugerida.tipo, icon = sugerida.id, color = colorPara(sugerida.tipo))

    fun colorPara(tipo: String): String = if (tipo == INGRESO) "#4CAF50" else "#FF5722"

    /** Minúsculas, sin tildes y sin signos, para comparar nombres. */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
