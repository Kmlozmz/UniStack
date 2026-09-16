package com.unistack.app.feature_terms.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Lo que la app propone para el periodo siguiente, antes de preguntar nada.
 *
 * Todo sale de los periodos que ya existen: el nombre sigue la numeración del último, el inicio
 * mira cuándo empezaron los del mismo tramo en años anteriores y la duración copia la del
 * último. Son sugerencias con su motivo a la vista, no decisiones.
 */
data class PropuestaDePeriodo(
    val nombre: String,
    val tipo: AcademicTermType,
    val inicioSugerido: LocalDate,
    /**
     * El inicio que salía de los años anteriores, antes de moverlo a hoy si ya pasó.
     *
     * Es del que cuenta el aviso para empezar: contado desde [inicioSugerido], que avanza con los
     * días, el aviso volvería a sonar cada semana.
     */
    val inicioPrevisto: LocalDate = inicioSugerido,
    /** Cuándo empezaron los del mismo tramo, del más reciente al más antiguo. */
    val iniciosAnteriores: List<LocalDate>,
    val semanas: Int,
    val opcionesDeSemanas: List<Int>,
    val ultimo: AcademicTerm?
) {
    /** El fin previsto para una duración: el viernes de la última semana. */
    fun finPara(inicio: LocalDate, semanas: Int): LocalDate = inicio.plusDays(semanas * 7L - 3L)

    /** El día del aviso para empezar, según la opción elegida. */
    fun diaDelAviso(opcion: Int): LocalDate = inicioPrevisto.plusDays(OpcionesDelAviso.desplazamiento(opcion))
}

/** Las tres opciones del aviso para empezar el periodo, contadas desde el inicio sugerido. */
object OpcionesDelAviso {
    const val ANTES = 0
    const val EL_DIA = 1
    const val DESPUES = 2

    val todas = listOf(ANTES, EL_DIA, DESPUES)

    fun desplazamiento(opcion: Int): Long = when (opcion) {
        EL_DIA -> 0L
        DESPUES -> 7L
        else -> -5L
    }
}

object PropuestaDelSiguientePeriodo {
    private val nombreConTramo = Regex("""^\s*(\d{4})\s*-\s*(\d{1,2})\s*$""")
    private val nombreDeAnio = Regex("""^\s*(\d{4})\s*$""")

    fun build(terms: List<AcademicTerm>, hoy: LocalDate): PropuestaDePeriodo {
        val ultimo = terms.maxByOrNull { it.startEpochDay }
        val tipo = ultimo?.type ?: AcademicTermType.SEMESTER
        if (ultimo == null) {
            val inicio = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
            return PropuestaDePeriodo(
                nombre = AcademicTerm.suggestedName(tipo, inicio),
                tipo = tipo,
                inicioSugerido = inicio,
                iniciosAnteriores = emptyList(),
                semanas = tipo.weeks,
                opcionesDeSemanas = opciones(tipo.weeks),
                ultimo = null
            )
        }

        val finDelUltimo = ultimo.closedEpochDay?.let(LocalDate::ofEpochDay)
            ?.let { cierre -> ultimo.plannedEnd?.let { minOf(it, cierre) } ?: cierre }
            ?: ultimo.plannedEnd
            ?: ultimo.start.plusWeeks(tipo.weeks.toLong())

        val (anio, tramo) = siguienteTramo(ultimo, tipo)
        val mismosTramo = terms
            .filter { tramoDe(it, tipo) == tramo }
            .sortedByDescending { it.startEpochDay }
            .map { it.start }

        val previsto = inicioPara(mismosTramo.firstOrNull(), finDelUltimo)
        val inicio = if (previsto.isBefore(hoy)) hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY)) else previsto
        val semanas = ultimo.plannedEnd
            ?.let { ((ChronoUnit.DAYS.between(ultimo.start, it) + 3) / 7.0).roundToInt() }
            ?.takeIf { it in 4..60 }
            ?: tipo.weeks

        val nombre = when {
            anio != null && tipo == AcademicTermType.ANNUAL -> anio.toString()
            anio != null && tramo != null -> "$anio-$tramo"
            else -> AcademicTerm.suggestedName(tipo, inicio)
        }

        return PropuestaDePeriodo(
            nombre = nombre,
            tipo = tipo,
            inicioSugerido = inicio,
            inicioPrevisto = previsto,
            iniciosAnteriores = mismosTramo.take(3),
            semanas = semanas,
            opcionesDeSemanas = opciones(semanas),
            ultimo = ultimo
        )
    }

    private fun opciones(semanas: Int): List<Int> =
        listOf(semanas - 2, semanas, semanas + 2).filter { it >= 4 }

    /** Año y número del tramo que viene después del último periodo. */
    private fun siguienteTramo(ultimo: AcademicTerm, tipo: AcademicTermType): Pair<Int?, Int?> {
        nombreConTramo.matchEntire(ultimo.name)?.let { coincidencia ->
            val anio = coincidencia.groupValues[1].toInt()
            val numero = coincidencia.groupValues[2].toInt()
            return if (numero >= tipo.perYear) (anio + 1) to 1 else anio to (numero + 1)
        }
        nombreDeAnio.matchEntire(ultimo.name)?.let { coincidencia ->
            return (coincidencia.groupValues[1].toInt() + 1) to 1
        }
        val numero = tipo.blockFor(ultimo.start)
        return if (numero >= tipo.perYear) (ultimo.start.year + 1) to 1 else ultimo.start.year to (numero + 1)
    }

    private fun tramoDe(term: AcademicTerm, tipo: AcademicTermType): Int {
        nombreConTramo.matchEntire(term.name)?.let { return it.groupValues[2].toInt() }
        if (nombreDeAnio.matchEntire(term.name) != null) return 1
        return tipo.blockFor(term.start)
    }

    /**
     * El mismo día del año que el último periodo de ese tramo, llevado al lunes de su semana.
     *
     * Sin ninguno del mismo tramo, ocho semanas después de acabar el último: lo que suele durar
     * un receso. Si esa fecha ya pasó, quien llama la mueve al lunes siguiente.
     */
    private fun inicioPara(referencia: LocalDate?, finDelUltimo: LocalDate): LocalDate {
        return if (referencia != null) {
            (finDelUltimo.year..finDelUltimo.year + 2).asSequence()
                .map { anio ->
                    val mes = referencia.month
                    val dia = minOf(referencia.dayOfMonth, mes.length(java.time.Year.isLeap(anio.toLong())))
                    LocalDate.of(anio, mes, dia).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                }
                .firstOrNull { it.isAfter(finDelUltimo) }
                ?: finDelUltimo.plusWeeks(8).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        } else {
            finDelUltimo.plusWeeks(8).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
        }
    }
}

/**
 * Cómo se reparten las semanas del periodo entre sus cortes.
 *
 * Solo se eligen las semanas de cada corte menos el último, que se queda con lo que sobra: así
 * suman siempre la duración del periodo y no hay forma de dejar un hueco.
 */
object RepartoDeCortes {

    fun iguales(semanas: Int, cortes: Int): List<Int> {
        if (cortes <= 1) return listOf(semanas)
        val base = semanas / cortes
        return List(cortes - 1) { base } + (semanas - base * (cortes - 1))
    }

    /** Los primeros algo más cortos y el último más largo, que es donde suele caer el final. */
    fun ultimoMasLargo(semanas: Int, cortes: Int): List<Int> {
        if (cortes <= 1) return listOf(semanas)
        val base = floor(semanas * 0.84 / cortes).toInt().coerceAtLeast(minimo(semanas, cortes))
        val ultimo = semanas - base * (cortes - 1)
        return if (ultimo < minimo(semanas, cortes)) iguales(semanas, cortes) else List(cortes - 1) { base } + ultimo
    }

    /** Suma [delta] semanas al corte [indice]; el último absorbe la diferencia. Nulo si no cabe. */
    fun ajustar(actual: List<Int>, semanas: Int, indice: Int, delta: Int): List<Int>? {
        if (indice !in 0 until actual.lastIndex) return null
        val nuevo = actual.toMutableList()
        nuevo[indice] += delta
        nuevo[nuevo.lastIndex] = semanas - nuevo.dropLast(1).sum()
        val min = minimo(semanas, actual.size)
        return nuevo.takeIf { lista -> lista.all { it >= min } }
    }

    /** Menos de dos semanas no es un corte, salvo que el periodo no dé para más. */
    fun minimo(semanas: Int, cortes: Int): Int = if (semanas >= cortes * 2) 2 else 1

    /**
     * El último día de cada corte menos el último, que acaba con el periodo.
     *
     * El domingo de su última semana: el lunes siguiente empieza el otro corte, así que no
     * quedan días de nadie.
     */
    fun cierres(inicio: LocalDate, semanasPorCorte: List<Int>): List<Long?> {
        var acumuladas = 0
        return semanasPorCorte.mapIndexed { indice, semanas ->
            acumuladas += semanas
            if (indice == semanasPorCorte.lastIndex) null else inicio.plusDays(acumuladas * 7L - 1L).toEpochDay()
        }
    }
}
