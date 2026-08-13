package com.unistack.app.feature_schedule.presentation

/**
 * Qué filas dibuja la rejilla de la vista previa, y a qué altura cae cada minuto.
 *
 * La previa cabe en unas siete horas y antes las anclaba a la clase más temprana del día. Con
 * una clase a la 1:00 —un error de dedo, pero un dato válido— la ventana pasaba a ser 1:00-8:00
 * y el resto del día desaparecía. Y fijar el inicio a las 6:00 tampoco vale: quien tenga algo a
 * las 6:30 y otra cosa a las 15:30 seguiría sin ver la segunda, porque siete horas no cubren un
 * día repartido.
 *
 * Así que la rejilla no enseña un rango: enseña **las horas que tienen clase**, y pliega los
 * tramos vacíos en una línea de corte. Un día seguido se ve igual que antes; uno con un hueco de
 * seis horas cabe entero.
 *
 * El precio, y conviene saberlo: **el eje deja de ser lineal**. Dos bloques separados por un
 * corte no guardan entre sí una distancia proporcional al tiempo que los separa. Dentro de un
 * mismo tramo sí.
 */
internal sealed interface TimelineRow {
    /** Una hora del día, con su etiqueta y su altura completa. */
    data class Hour(val hour: Int) : TimelineRow

    /** Un tramo sin nada, plegado. [from] y [to] son inclusivos. */
    data class Break(val from: Int, val to: Int) : TimelineRow
}

/** Cuántas filas de hora se enseñan como mínimo, para que la previa no quede en una tira. */
private const val MIN_HOURS = 5

/** Un hueco se pliega a partir de esta cantidad de horas seguidas sin nada. */
private const val MIN_GAP_TO_COLLAPSE = 2

/**
 * @param ranges intervalos en minutos desde medianoche, uno por clase visible.
 * @param fallbackStartHour con qué hora empezar cuando no hay ninguna clase.
 */
internal fun buildTimelineRows(
    ranges: List<IntRange>,
    fallbackStartHour: Int = 6
): List<TimelineRow> {
    val used = sortedSetOf<Int>()
    ranges.forEach { range ->
        if (range.last <= range.first) return@forEach
        val firstHour = range.first / 60
        // El último minuto pertenece a la hora anterior: una clase que acaba a las 9:00 en
        // punto no ocupa la franja de las 9.
        val lastHour = (range.last - 1) / 60
        (firstHour..lastHour).forEach { hour -> used += hour.coerceIn(0, 23) }
    }

    if (used.isEmpty()) {
        val start = fallbackStartHour.coerceIn(0, 23 - MIN_HOURS + 1)
        return (start until start + MIN_HOURS).map(TimelineRow::Hour)
    }

    // Aire alrededor hasta llegar al mínimo, primero por abajo y luego por arriba.
    while (used.size < MIN_HOURS) {
        val after = used.last() + 1
        val before = used.first() - 1
        when {
            after <= 23 -> used += after
            before >= 0 -> used += before
            else -> break
        }
    }

    val rows = mutableListOf<TimelineRow>()
    var previous: Int? = null
    used.forEach { hour ->
        val gap = previous?.let { hour - it - 1 } ?: 0
        when {
            previous == null -> Unit
            gap >= MIN_GAP_TO_COLLAPSE -> rows += TimelineRow.Break(previous!! + 1, hour - 1)
            // Un hueco de una sola hora no se pliega: la línea de corte ocuparía casi lo mismo
            // que la fila que ahorra, y encima rompería el eje sin necesidad.
            else -> (previous!! + 1 until hour).forEach { rows += TimelineRow.Hour(it) }
        }
        rows += TimelineRow.Hour(hour)
        previous = hour
    }
    return rows
}

/**
 * A qué altura cae un minuto, en las mismas unidades que [hourHeight] y [breakHeight].
 *
 * Devuelve null si ese minuto cae en un tramo plegado, que para una clase no puede pasar: todas
 * las horas que toca están en la rejilla por construcción.
 */
internal fun offsetForMinute(
    rows: List<TimelineRow>,
    minute: Int,
    hourHeight: Float,
    breakHeight: Float
): Float? {
    var offset = 0f
    rows.forEach { row ->
        when (row) {
            is TimelineRow.Break -> offset += breakHeight
            is TimelineRow.Hour -> {
                if (minute in row.hour * 60 until (row.hour + 1) * 60) {
                    return offset + hourHeight * ((minute - row.hour * 60) / 60f)
                }
                // El final de una clase cae justo en el borde de la hora siguiente.
                if (minute == (row.hour + 1) * 60) return offset + hourHeight
                offset += hourHeight
            }
        }
    }
    return null
}

/** Alto total de la rejilla con estas filas. */
internal fun timelineHeight(
    rows: List<TimelineRow>,
    hourHeight: Float,
    breakHeight: Float
): Float = rows.sumOf { row ->
    when (row) {
        is TimelineRow.Hour -> hourHeight.toDouble()
        is TimelineRow.Break -> breakHeight.toDouble()
    }
}.toFloat()
