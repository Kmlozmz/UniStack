package com.unistack.app.feature_support.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_user.domain.MotionChoice
import com.unistack.app.feature_user.domain.MotionGesture
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

/**
 * Las palancas del banco de pruebas.
 *
 * **Existe porque probar una animación cuesta más que escribirla.** Para ver el aviso de
 * pasarse del presupuesto hay que ir a Gastos, abrir el presupuesto, poner una cifra, volver,
 * registrar gastos hasta cruzarla — y repetirlo entero cada vez que se cambia una duración.
 * Para ver el barrido de recuperación hay que dejar una materia por debajo del aprobado y
 * después subirla. Para ver el aviso de horarios cruzados hay que crear dos clases a la misma
 * hora a mano.
 *
 * Cada palanca de aquí deja la app en el estado exacto que hace falta para mirar una cosa, y
 * la deshace. Es la misma idea que las notas de ejemplo, extendida a lo demás.
 *
 * **Todo lo que crea lleva el prefijo `prueba-`**, así que se puede recoger sin tocar nada de
 * verdad: eso es lo que hace seguro tenerlo dentro de la app y no en una rama aparte.
 */
@HiltViewModel
class BancoDePruebasViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val gradesRepository: GradesRepository,
    private val expensesRepository: ExpensesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private companion object {
        /** Lo que distingue lo fabricado de lo real. Nada se borra sin esto delante. */
        const val MARCA = "prueba-"
    }

    // ------------------------------------------------------------------ horario

    /**
     * Una clase que está pasando **ahora mismo**, para ver «clase en curso».
     *
     * Empieza hace media hora y acaba dentro de hora y media: cualquier variante que dependa de
     * estar dentro del horario se dispara al instante, que es lo que no se podía provocar.
     */
    fun claseEnCursoAhora() {
        val materia = gradesRepository.subjects.value.firstOrNull() ?: return
        val ahora = LocalTime.now().let { it.hour * 60 + it.minute }
        val hoy = LocalDate.now()
        guardarClase(
            subjectId = materia.id,
            dia = hoy.dayOfWeek.value,
            desde = (ahora - 30).coerceAtLeast(0),
            hasta = (ahora + 90).coerceAtMost(24 * 60 - 1),
            sufijo = "curso"
        )
    }

    /**
     * Dos clases pisándose el mismo día, para ver el aviso de cruce.
     *
     * La segunda empieza a la mitad de la primera: es el solape más claro que hay, y el que
     * de verdad rompe las cuentas de horas de la semana.
     */
    fun cruceDeHorarios() {
        val materias = gradesRepository.subjects.value
        val primera = materias.firstOrNull() ?: return
        val segunda = materias.getOrNull(1) ?: primera
        val dia = LocalDate.now().dayOfWeek.value
        guardarClase(primera.id, dia, 10 * 60, 12 * 60, "cruceA")
        guardarClase(segunda.id, dia, 11 * 60, 13 * 60, "cruceB")
    }

    /**
     * Tres clases de días pasados, para que «Ponerse al día» tenga qué enseñar.
     *
     * Van a días anteriores porque una clase futura no pregunta nada: la lista de pendientes
     * se llena de lo que ya ocurrió sin marcar.
     */
    fun clasesSinMarcar() {
        val materia = gradesRepository.subjects.value.firstOrNull() ?: return
        (1..3).forEach { atras ->
            val dia = LocalDate.now().minusDays(atras.toLong())
            guardarClase(materia.id, dia.dayOfWeek.value, 8 * 60, 10 * 60, "pend$atras")
        }
    }

    private fun guardarClase(subjectId: String, dia: Int, desde: Int, hasta: Int, sufijo: String) {
        val ahora = System.currentTimeMillis()
        scheduleRepository.saveSession(
            ClassSession(
                id = "$MARCA$sufijo-${UUID.randomUUID()}",
                subjectId = subjectId,
                daysOfWeek = setOf(dia),
                startMinute = desde,
                endMinute = hasta,
                location = "Banco",
                reminderMinutes = 15,
                createdAt = ahora,
                updatedAt = ahora,
                recurrenceStartEpochDay = LocalDate.now().minusDays(30).toEpochDay()
            )
        )
    }

    // ------------------------------------------------------------------ gastos

    /**
     * Un presupuesto ya cruzado, para ver la alarma sin registrar gastos uno a uno.
     *
     * Se pone justo por debajo de lo gastado este mes: cruzarlo es el estado, y así la alarma
     * salta con el primer vistazo en vez de pedir cinco gastos de mentira.
     */
    fun presupuestoPasado() {
        val perfil = userRepository.userProfile.value ?: return
        val gastado = expensesRepository.expenses.value.sumOf { it.amount }
        val objetivo = (gastado * 0.8).toInt().coerceAtLeast(1000)
        userRepository.saveUserProfile(
            perfil.copy(monthlyBudget = objetivo, updatedAt = System.currentTimeMillis())
        )
    }

    /** Un presupuesto holgado: la misma fila sin alarma, para comparar. */
    fun presupuestoHolgado() {
        val perfil = userRepository.userProfile.value ?: return
        val gastado = expensesRepository.expenses.value.sumOf { it.amount }
        userRepository.saveUserProfile(
            perfil.copy(
                monthlyBudget = (gastado * 2).toInt().coerceAtLeast(50_000),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun sinPresupuesto() {
        val perfil = userRepository.userProfile.value ?: return
        userRepository.saveUserProfile(
            perfil.copy(monthlyBudget = 0, updatedAt = System.currentTimeMillis())
        )
    }

    /** Un gasto de mentira, para cruzar el presupuesto en vivo y ver el momento. */
    fun gastoDePrueba(cuanto: Int = 9_000) {
        val ahora = System.currentTimeMillis()
        expensesRepository.addExpense(
            Expense(
                id = "$MARCA${UUID.randomUUID()}",
                category = ExpenseCategory.OTHER,
                amount = cuanto,
                dateMillis = ahora,
                createdAt = ahora,
                updatedAt = ahora
            )
        )
    }

    // ------------------------------------------------------------------ académico

    /**
     * Deja la primera materia **por debajo del aprobado**, que es de donde se sale.
     *
     * Borra sus notas de prueba anteriores para que el promedio sea el que se busca y no la
     * mezcla de tres intentos: si no, cada pulsación deja la materia en un sitio distinto.
     */
    fun materiaEnRojo() {
        val materia = gradesRepository.subjects.value.firstOrNull() ?: return
        val perfil = userRepository.userProfile.value
        val maximo = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
        limpiarNotasDePrueba(materia.id)
        val corte = materia.cutScheme.cuts.firstOrNull() ?: return
        gradesRepository.addGrade(
            materia.id,
            notaDePrueba(corte.id, valor = maximo * 0.35, peso = 1.0)
        )
    }

    /** La sube por encima del aprobado: es el cruce que dispara el barrido. */
    fun materiaRecuperada() {
        val materia = gradesRepository.subjects.value.firstOrNull() ?: return
        val perfil = userRepository.userProfile.value
        val maximo = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
        val corte = materia.cutScheme.cuts.getOrNull(1) ?: materia.cutScheme.cuts.firstOrNull() ?: return
        gradesRepository.addGrade(
            materia.id,
            notaDePrueba(corte.id, valor = maximo * 0.95, peso = 1.0)
        )
    }

    /**
     * Un corte repartido al 100 %, listo para cerrarse.
     *
     * El botón de cerrar solo aparece cuando no queda nada por repartir, así que sin esto hay
     * que inventar tres notas con sus pesos cada vez que se quiere mirar el sello.
     */
    fun corteListoParaCerrar() {
        val materia = gradesRepository.subjects.value.firstOrNull() ?: return
        val perfil = userRepository.userProfile.value
        val maximo = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
        val corte = materia.cutScheme.cuts.firstOrNull() ?: return
        limpiarNotasDePrueba(materia.id)
        listOf(0.5 to 0.9, 0.3 to 0.8, 0.2 to 0.7).forEach { (peso, nota) ->
            gradesRepository.addGrade(
                materia.id,
                notaDePrueba(corte.id, valor = maximo * nota, peso = peso)
            )
        }
    }

    private fun notaDePrueba(cutId: String, valor: Double, peso: Double) = GradeItem(
        id = "$MARCA${UUID.randomUUID()}",
        name = "Prueba",
        value = valor,
        percentage = peso,
        cutId = cutId,
        recordedAt = System.currentTimeMillis()
    )

    private fun limpiarNotasDePrueba(subjectId: String) {
        val materia = gradesRepository.subjects.value.firstOrNull { it.id == subjectId } ?: return
        materia.grades.filter { it.id.startsWith(MARCA) }.forEach {
            gradesRepository.deleteGrade(subjectId, it.id)
        }
    }

    // ------------------------------------------------------------------ recoger

    /**
     * Se lleva todo lo fabricado y no toca nada más.
     *
     * Es lo que hace que esto pueda vivir dentro de la app: el prefijo `prueba-` separa lo de
     * mentira de lo de verdad sin depender de acordarse de qué se pulsó.
     */
    // ------------------------------------------------------------------ preferencias

    /** El perfil vivo: el panel lee de aqui para pintar que variante esta puesta. */
    val perfil = userRepository.userProfile

    /**
     * Cambia una variante de Movimiento sin salir de donde estas.
     *
     * Es lo mismo que hace la pantalla de Ajustes, y esta aqui para no tener que ir hasta
     * ella y volver por cada prueba: entre mirar un gesto y cambiarle la variante habia
     * cuatro pantallas de ida y cuatro de vuelta.
     */
    fun ponVariante(gesto: MotionGesture, opcion: MotionChoice) {
        val p = userRepository.userProfile.value ?: return
        val apariencia = p.appearancePreferences
        userRepository.saveUserProfile(
            p.copy(
                appearancePreferences = apariencia.copy(
                    motion = gesto.write(apariencia.motion, opcion)
                ),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /** Todo el movimiento a sus valores de fabrica, sin tocar el resto del perfil. */
    fun movimientoDeFabrica() {
        val p = userRepository.userProfile.value ?: return
        userRepository.saveUserProfile(
            p.copy(
                appearancePreferences = p.appearancePreferences.copy(
                    motion = MotionPreferences.defaults()
                ),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // ------------------------------------------------------------------ deshacer

    /**
     * Cuantas cosas de mentira hay ahora mismo, por sitio.
     *
     * El panel lo enseña para que no haya que fiarse de la memoria: si dice «Horario 2», es
     * que quedan dos clases fabricadas ahi.
     */
    fun cuantasDePrueba(): Triple<Int, Int, Int> = Triple(
        scheduleRepository.sessions.value.count { it.id.startsWith(MARCA) },
        gradesRepository.subjects.value.sumOf { m -> m.grades.count { it.id.startsWith(MARCA) } },
        expensesRepository.expenses.value.count { it.id.startsWith(MARCA) }
    )

    /** Deshace solo lo de Horario. */
    fun recogerHorario() {
        scheduleRepository.sessions.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { scheduleRepository.deleteSession(it.id) }
    }

    /** Deshace solo lo academico. */
    fun recogerAcademico() {
        gradesRepository.subjects.value.forEach { materia ->
            materia.grades.filter { it.id.startsWith(MARCA) }.forEach {
                gradesRepository.deleteGrade(materia.id, it.id)
            }
        }
    }

    /** Deshace solo lo de Gastos, presupuesto incluido. */
    fun recogerGastos() {
        expensesRepository.expenses.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { expensesRepository.deleteExpense(it.id) }
        sinPresupuesto()
    }

    fun recogerlo() {
        recogerHorario()
        recogerAcademico()
        recogerGastos()
    }
}
