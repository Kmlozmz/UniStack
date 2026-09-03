package com.unistack.app.feature_expenses.domain

import java.time.LocalDate
import java.time.YearMonth

/**
 * Las cuentas de las tres maneras de leer tus gastos.
 *
 * Cada una responde **una** pregunta distinta, y por eso son tres y no un tablero:
 *
 * - [compararSemanas]: ¿voy peor que antes?
 * - [ritmoDelMes]: ¿voy a llegar a fin de mes?
 * - [calendarioDelMes]: ¿qué pasó el día que gasté tanto?
 *
 * Vive en `domain` y sin nada de Android a la vista para poder probarlas con fechas fijas: una
 * proyección que se calcula sola el día 11 no se puede juzgar mirando la pantalla en otro día.
 */
object ExpenseInsights {

    // ---------------------------------------------------------------- comparar

    /** Lo gastado en una categoría ahora y en el periodo anterior. */
    data class CategoriaComparada(
        val category: ExpenseCategory,
        val actual: Int,
        val anterior: Int
    ) {
        /** Cuánto ha cambiado, en tanto por ciento. Nulo si antes no había nada con qué comparar. */
        val cambioPorcentual: Int?
            get() = if (anterior <= 0) null else ((actual - anterior) * 100.0 / anterior).toInt()

        val diferencia: Int get() = actual - anterior
    }

    data class ComparacionSemanas(
        val actual: Int,
        val anterior: Int,
        val categorias: List<CategoriaComparada>
    ) {
        val diferencia: Int get() = actual - anterior

        val cambioPorcentual: Int?
            get() = if (anterior <= 0) null else ((actual - anterior) * 100.0 / anterior).toInt()

        /**
         * La categoría que más explica el cambio.
         *
         * Es la que hace que la pantalla pueda cerrar con una frase en vez de con una tabla:
         * saber que gastaste 32.800 menos no sirve de nada si no dices de dónde salen.
         */
        val categoriaQueMasCambia: CategoriaComparada?
            get() = categorias.maxByOrNull { kotlin.math.abs(it.diferencia) }?.takeIf { it.diferencia != 0 }
    }

    fun compararSemanas(
        expenses: List<Expense>,
        today: LocalDate = LocalDate.now()
    ): ComparacionSemanas {
        val inicioActual = ExpenseDateUtils.startOfWeek(today)
        val inicioAnterior = inicioActual.minusDays(7)

        fun deLaSemana(desde: LocalDate) = expenses.filter {
            ExpenseDateUtils.fromMillis(it.dateMillis) in desde..desde.plusDays(6)
        }

        val actual = deLaSemana(inicioActual)
        val anterior = deLaSemana(inicioAnterior)

        // Solo las categorías que aparecen en alguna de las dos semanas: enseñar las seis
        // siempre llenaría la pantalla de ceros que no cambian nada.
        val categorias = ExpenseCategory.entries
            .map { categoria ->
                CategoriaComparada(
                    category = categoria,
                    actual = actual.filter { it.category == categoria }.sumOf { it.amount },
                    anterior = anterior.filter { it.category == categoria }.sumOf { it.amount }
                )
            }
            .filter { it.actual > 0 || it.anterior > 0 }
            .sortedByDescending { maxOf(it.actual, it.anterior) }

        return ComparacionSemanas(
            actual = actual.sumOf { it.amount },
            anterior = anterior.sumOf { it.amount },
            categorias = categorias
        )
    }

    // ---------------------------------------------------------------- ritmo

    data class RitmoDelMes(
        val gastado: Int,
        val presupuesto: Int,
        val diaDelMes: Int,
        val diasDelMes: Int,
        /** Lo acumulado día a día hasta hoy, para dibujar la línea. */
        val acumuladoPorDia: List<Int>,
        val proyeccion: Int,
        /**
         * Cuánto puedes gastar cada día que queda sin pasarte del presupuesto.
         *
         * Nulo si no hay presupuesto puesto -- sin él no hay nada de lo que no pasarse -- y cero
         * si ya te lo gastaste entero.
         */
        val diarioRestante: Int?
    ) {
        val diasRestantes: Int get() = diasDelMes - diaDelMes

        /** Si a este ritmo se acaba el mes por encima de lo presupuestado. */
        val seVaAPasar: Boolean get() = presupuesto > 0 && proyeccion > presupuesto
    }

    fun ritmoDelMes(
        expenses: List<Expense>,
        presupuestoMensual: Int,
        today: LocalDate = LocalDate.now()
    ): RitmoDelMes {
        val mes = YearMonth.from(today)
        val diasDelMes = mes.lengthOfMonth()
        val delMes = expenses.filter {
            val fecha = ExpenseDateUtils.fromMillis(it.dateMillis)
            YearMonth.from(fecha) == mes && !fecha.isAfter(today)
        }

        var suma = 0
        val acumulado = (1..today.dayOfMonth).map { dia ->
            suma += delMes
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis).dayOfMonth == dia }
                .sumOf { it.amount }
            suma
        }

        val gastado = suma
        // La proyeccion es una regla de tres sobre los dias que han pasado. Es tosca a
        // proposito: cualquier cosa mas fina el dia 2 del mes daria una sensacion de
        // precision que el dato no tiene.
        val proyeccion = if (today.dayOfMonth == 0) 0 else gastado * diasDelMes / today.dayOfMonth
        val restante = presupuestoMensual - gastado
        val diasQueQuedan = diasDelMes - today.dayOfMonth

        return RitmoDelMes(
            gastado = gastado,
            presupuesto = presupuestoMensual,
            diaDelMes = today.dayOfMonth,
            diasDelMes = diasDelMes,
            acumuladoPorDia = acumulado,
            proyeccion = proyeccion,
            diarioRestante = when {
                presupuestoMensual <= 0 -> null
                restante <= 0 -> 0
                diasQueQuedan <= 0 -> restante
                else -> restante / diasQueQuedan
            }
        )
    }

    // ---------------------------------------------------------------- calendario

    data class DiaDelMes(
        val fecha: LocalDate,
        val total: Int,
        val gastos: List<Expense>
    )

    data class CalendarioDelMes(
        val mes: YearMonth,
        val dias: List<DiaDelMes>,
        /**
         * Cuántas casillas vacías van antes del día 1.
         *
         * La rejilla empieza en lunes, así que un mes que arranca en jueves necesita tres
         * huecos delante o los días caen bajo el nombre equivocado.
         */
        val huecosIniciales: Int
    ) {
        val total: Int get() = dias.sumOf { it.total }

        val diaMasCaro: DiaDelMes? get() = dias.filter { it.total > 0 }.maxByOrNull { it.total }
    }

    fun calendarioDelMes(
        expenses: List<Expense>,
        mes: YearMonth = YearMonth.now()
    ): CalendarioDelMes {
        val delMes = expenses.filter { YearMonth.from(ExpenseDateUtils.fromMillis(it.dateMillis)) == mes }
        val dias = (1..mes.lengthOfMonth()).map { numero ->
            val fecha = mes.atDay(numero)
            val delDia = delMes
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == fecha }
                .sortedByDescending { it.amount }
            DiaDelMes(fecha = fecha, total = delDia.sumOf { it.amount }, gastos = delDia)
        }
        return CalendarioDelMes(
            mes = mes,
            dias = dias,
            // DayOfWeek va de 1 (lunes) a 7 (domingo), y la rejilla tambien empieza en lunes.
            huecosIniciales = mes.atDay(1).dayOfWeek.value - 1
        )
    }
}
