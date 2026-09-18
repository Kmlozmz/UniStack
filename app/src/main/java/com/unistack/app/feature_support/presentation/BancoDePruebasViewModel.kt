package com.unistack.app.feature_support.presentation

import androidx.lifecycle.ViewModel
import android.content.Context
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.core.notifications.NotificationHistoryItem
import com.unistack.app.core.notifications.NotificationHistoryStore
import java.time.ZoneId
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_tasks.data.TaskAttachmentSamples
import com.unistack.app.feature_tasks.data.TaskAttachmentStore
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskSubtask
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
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
    private val tasksRepository: TasksRepository,
    private val userRepository: UserRepository,
    private val taskAttachmentStore: TaskAttachmentStore
) : ViewModel() {
    private companion object {
        /** Lo que distingue lo fabricado de lo real. Nada se borra sin esto delante. */
        const val MARCA = "prueba-"
    }


    // ------------------------------------------------------------------ simulación completa

    /**
     * Siembra un semestre universitario completo y realista en un solo toque:
     * - 5 materias activas con notas y cortes (Cálculo, Física, POO, Álgebra, Bases de Datos).
     * - Horario semanal de Lunes a Viernes con aulas y docentes, más una clase para hoy.
     * - Tareas académicas con subtareas, fechas de entrega variadas y estados.
     * - Presupuesto mensual de $180.000 COP y gastos reales en la semana.
     *
     * Todo lleva la marca `MARCA` para poder eliminarse limpiamente con [recogerlo].
     */
    fun sembrarAppCompleta() {
        recogerlo()

        val perfil = userRepository.userProfile.value
        val maximo = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0

        if (perfil != null && (perfil.preferredName.isBlank() || perfil.preferredName.startsWith("Student"))) {
            userRepository.updatePreferredName("Alex")
        }

        val hoy = LocalDate.now()
        val ahora = System.currentTimeMillis()

        // 1. Materias universitarias
        val matCalc = "${MARCA}calc"
        val matFis = "${MARCA}fis"
        val matPoo = "${MARCA}poo"
        val matAlg = "${MARCA}alg"
        val matBd = "${MARCA}bd"

        val materias = listOf(
            Subject(
                id = matCalc,
                name = "Cálculo Vectorial",
                targetAverage = maximo * 0.76,
                grades = emptyList(),
                visualType = SubjectVisualType.PURPLE,
                activeCutId = "period-2"
            ),
            Subject(
                id = matFis,
                name = "Física Mecánica",
                targetAverage = maximo * 0.70,
                grades = emptyList(),
                visualType = SubjectVisualType.CORAL,
                activeCutId = "period-2"
            ),
            Subject(
                id = matPoo,
                name = "Programación Orientada a Objetos",
                targetAverage = maximo * 0.80,
                grades = emptyList(),
                visualType = SubjectVisualType.GREEN,
                activeCutId = "period-2"
            ),
            Subject(
                id = matAlg,
                name = "Álgebra Lineal",
                targetAverage = maximo * 0.72,
                grades = emptyList(),
                visualType = SubjectVisualType.YELLOW,
                activeCutId = "period-2"
            ),
            Subject(
                id = matBd,
                name = "Bases de Datos",
                targetAverage = maximo * 0.80,
                grades = emptyList(),
                visualType = SubjectVisualType.BLUE,
                activeCutId = "period-2"
            )
        )

        materias.forEach { gradesRepository.addSubject(it) }

        // 2. Calificaciones distribuidas
        gradesRepository.addGrade(matCalc, GradeItem(id = "${MARCA}c1-g1", name = "Taller derivadas parciales", value = maximo * 0.84, percentage = 0.40, type = GradeType.WORKSHOP, cutId = "period-1", recordedAt = ahora - 25 * 86400000L))
        gradesRepository.addGrade(matCalc, GradeItem(id = "${MARCA}c1-g2", name = "Parcial 1", value = maximo * 0.76, percentage = 0.60, type = GradeType.EXAM, cutId = "period-1", recordedAt = ahora - 20 * 86400000L))
        gradesRepository.addGrade(matCalc, GradeItem(id = "${MARCA}c1-g3", name = "Quiz integrales dobles", value = maximo * 0.90, percentage = 0.30, type = GradeType.QUIZ, cutId = "period-2", recordedAt = ahora - 5 * 86400000L))
        gradesRepository.addGrade(matCalc, GradeItem(id = "${MARCA}c1-g4", name = "Parcial 2", value = maximo * 0.72, percentage = 0.70, type = GradeType.EXAM, cutId = "period-2", recordedAt = ahora - 1 * 86400000L))

        gradesRepository.addGrade(matFis, GradeItem(id = "${MARCA}fis-g1", name = "Laboratorio de cinemática", value = maximo * 0.80, percentage = 0.50, type = GradeType.PRACTICE, cutId = "period-1", recordedAt = ahora - 24 * 86400000L))
        gradesRepository.addGrade(matFis, GradeItem(id = "${MARCA}fis-g2", name = "Parcial 1 - Cinemática", value = maximo * 0.64, percentage = 0.50, type = GradeType.EXAM, cutId = "period-1", recordedAt = ahora - 18 * 86400000L))
        gradesRepository.addGrade(matFis, GradeItem(id = "${MARCA}fis-g3", name = "Taller leyes de Newton", value = maximo * 0.78, percentage = 1.00, type = GradeType.WORKSHOP, cutId = "period-2", recordedAt = ahora - 3 * 86400000L))

        gradesRepository.addGrade(matPoo, GradeItem(id = "${MARCA}poo-g1", name = "Proyecto: Clases y herencia", value = maximo * 0.96, percentage = 0.60, type = GradeType.PROJECT, cutId = "period-1", recordedAt = ahora - 22 * 86400000L))
        gradesRepository.addGrade(matPoo, GradeItem(id = "${MARCA}poo-g2", name = "Quiz principios SOLID", value = maximo * 0.84, percentage = 0.40, type = GradeType.QUIZ, cutId = "period-1", recordedAt = ahora - 16 * 86400000L))
        gradesRepository.addGrade(matPoo, GradeItem(id = "${MARCA}poo-g3", name = "Laboratorio polimorfismo", value = maximo * 0.90, percentage = 0.50, type = GradeType.PRACTICE, cutId = "period-2", recordedAt = ahora - 6 * 86400000L))
        gradesRepository.addGrade(matPoo, GradeItem(id = "${MARCA}poo-g4", name = "Parcial 2 - Java y Kotlin", value = maximo * 0.80, percentage = 0.50, type = GradeType.EXAM, cutId = "period-2", recordedAt = ahora - 2 * 86400000L))

        gradesRepository.addGrade(matAlg, GradeItem(id = "${MARCA}alg-g1", name = "Matrices y determinantes", value = maximo * 0.70, percentage = 0.50, type = GradeType.WORKSHOP, cutId = "period-1", recordedAt = ahora - 23 * 86400000L))
        gradesRepository.addGrade(matAlg, GradeItem(id = "${MARCA}alg-g2", name = "Control 1 - Sistemas lineales", value = maximo * 0.76, percentage = 0.50, type = GradeType.QUIZ, cutId = "period-1", recordedAt = ahora - 17 * 86400000L))
        gradesRepository.addGrade(matAlg, GradeItem(id = "${MARCA}alg-g3", name = "Taller espacios vectoriales", value = maximo * 0.82, percentage = 1.00, type = GradeType.WORKSHOP, cutId = "period-2", recordedAt = ahora - 4 * 86400000L))

        gradesRepository.addGrade(matBd, GradeItem(id = "${MARCA}bd-g1", name = "Modelo Entidad-Relación", value = maximo * 0.88, percentage = 0.50, type = GradeType.WORKSHOP, cutId = "period-1", recordedAt = ahora - 21 * 86400000L))
        gradesRepository.addGrade(matBd, GradeItem(id = "${MARCA}bd-g2", name = "SQL DDL y DML básico", value = maximo * 0.92, percentage = 0.50, type = GradeType.PRACTICE, cutId = "period-1", recordedAt = ahora - 15 * 86400000L))
        gradesRepository.addGrade(matBd, GradeItem(id = "${MARCA}bd-g3", name = "Normalización y restricciones", value = maximo * 0.80, percentage = 1.00, type = GradeType.WORKSHOP, cutId = "period-2", recordedAt = ahora - 2 * 86400000L))

        // 3. Horario semanal
        val epochDayInicio = hoy.minusDays(30).toEpochDay()
        fun sesion(subId: String, dias: Set<Int>, inicioH: Int, inicioM: Int, finH: Int, finM: Int, lugar: String, prof: String, key: String) {
            scheduleRepository.saveSession(
                ClassSession(
                    id = "$MARCA$key",
                    subjectId = subId,
                    daysOfWeek = dias,
                    startMinute = inicioH * 60 + inicioM,
                    endMinute = finH * 60 + finM,
                    location = "$lugar•$prof",
                    reminderMinutes = 15,
                    createdAt = ahora,
                    updatedAt = ahora,
                    recurrenceStartEpochDay = epochDayInicio
                )
            )
        }

        sesion(matCalc, setOf(1, 3), 7, 0, 9, 0, "Aula 204", "Prof. Carlos Mendoza", "ses-calc")
        sesion(matFis, setOf(1, 4), 9, 0, 11, 0, "Edificio B-102", "Prof. Elena Gómez", "ses-fis")
        sesion(matPoo, setOf(2, 4), 8, 0, 10, 0, "Lab 3", "Prof. Roberto Silva", "ses-poo")
        sesion(matAlg, setOf(2, 4), 10, 0, 12, 0, "Aula 301", "Prof. Martha Ruiz", "ses-alg")
        sesion(matBd, setOf(3, 5), 11, 0, 13, 0, "Lab 1", "Prof. Andrés Parra", "ses-bd")

        // Sesión para hoy según la hora del dispositivo para que Inicio muestre la clase
        val horaActual = LocalTime.now()
        val diaHoy = hoy.dayOfWeek.value
        val minutoActual = horaActual.hour * 60 + horaActual.minute
        val claseHoyInicio = (minutoActual + 20).coerceAtMost(22 * 60)
        val claseHoyFin = (claseHoyInicio + 110).coerceAtMost(23 * 60 + 50)
        scheduleRepository.saveSession(
            ClassSession(
                id = "${MARCA}ses-hoy",
                subjectId = matPoo,
                daysOfWeek = setOf(diaHoy),
                startMinute = claseHoyInicio,
                endMinute = claseHoyFin,
                location = "Lab 3•Prof. Roberto Silva",
                reminderMinutes = 15,
                createdAt = ahora,
                updatedAt = ahora,
                recurrenceStartEpochDay = epochDayInicio
            )
        )

        // 4. Tareas académicas
        fun tarea(id: String, titulo: String, desc: String, subId: String, tipo: TaskType, dias: Long, dif: TaskDifficulty, compl: Boolean, subtareas: List<String>, estadoNota: TaskGradingStatus = TaskGradingStatus.UNDECIDED) {
            val taskUid = "$MARCA$id"
            tasksRepository.addTask(
                StudentTask(
                    id = taskUid,
                    title = titulo,
                    description = desc,
                    subjectId = subId,
                    type = tipo,
                    dueDateMillis = TaskDateUtils.toMillis(hoy.plusDays(dias), LocalTime.of(23, 59)),
                    difficulty = dif,
                    estimatedMinutes = 90,
                    completed = compl,
                    createdAt = ahora - 2 * 86400000L,
                    updatedAt = ahora,
                    gradingStatus = estadoNota,
                    subtasks = subtareas.mapIndexed { idx, sub ->
                        TaskSubtask(id = "${taskUid}-s$idx", taskId = taskUid, title = sub, isCompleted = compl || idx == 0, position = idx)
                    }
                )
            )
        }

        tarea(
            "t-poo",
            "Proyecto Final: Sistema de Gestión",
            "Implementar arquitectura MVC, patrones DAO y repository, y persistencia local.",
            matPoo,
            TaskType.PROJECT,
            5,
            TaskDifficulty.HARD,
            false,
            listOf("Diseño del diagrama de clases UML", "Implementación de lógica de negocio y DAO", "Pruebas unitarias de repositorios", "Interfaz de usuario"),
            TaskGradingStatus.AWAITING_GRADE
        )
        tarea(
            "t-calc",
            "Taller 3: Integrales triples y coordenadas esféricas",
            "Ejercicios del capítulo 14. Entrega en PDF con procedimiento completo a mano.",
            matCalc,
            TaskType.WORKSHOP,
            1,
            TaskDifficulty.MEDIUM,
            false,
            listOf("Ejercicios 1 a 10 (coordenadas cilíndricas)", "Ejercicios 11 a 20 (coordenadas esféricas)", "Escanear y compilar en PDF")
        )
        tarea(
            "t-fis",
            "Informe de Laboratorio: Péndulo y conservación de energía",
            "Formato IEEE. Incluir tablas de datos de Tracker y cálculo de errores experimentales.",
            matFis,
            TaskType.RESEARCH,
            3,
            TaskDifficulty.MEDIUM,
            false,
            listOf("Análisis de video en Tracker", "Cálculo de incertidumbre y gráficas", "Conclusiones y referencias IEEE")
        )
        tarea(
            "t-bd",
            "Taller SQL: Consultas avanzadas y subqueries",
            "Guía de laboratorio con JOINs complejos, GROUP BY, HAVING y subconsultas correlacionadas.",
            matBd,
            TaskType.PRACTICE,
            8,
            TaskDifficulty.EASY,
            false,
            listOf("Consultas 1 a 8 en PostgreSQL", "Optimización con índices")
        )
        tarea(
            "t-alg",
            "Taller de Matrices y Determinantes",
            "Ejercicios prácticos de eliminación Gaussiana y cálculo de determinantes nxn.",
            matAlg,
            TaskType.WORKSHOP,
            -1,
            TaskDifficulty.EASY,
            true,
            listOf("Ejercicios resueltos", "Revisión con calculadora matricial")
        )

        // 5. Presupuesto y Gastos de la semana
        val perfilActual = userRepository.userProfile.value
        if (perfilActual != null) {
            userRepository.saveUserProfile(
                perfilActual.copy(monthlyBudget = 180_000, updatedAt = ahora)
            )
        }

        val gastosMuestra = listOf(
            Triple(ExpenseCategory.FOOD, 14_500, 0),
            Triple(ExpenseCategory.TRANSPORT, 3_200, 0),
            Triple(ExpenseCategory.COPIES, 5_800, 1),
            Triple(ExpenseCategory.FOOD, 18_000, 2),
            Triple(ExpenseCategory.OUTINGS, 4_500, 3),
            Triple(ExpenseCategory.TRANSPORT, 3_200, 4),
            Triple(ExpenseCategory.MATERIALS, 12_000, 5)
        )

        gastosMuestra.forEachIndexed { idx, (cat, monto, diasAtras) ->
            val fechaGasto = TaskDateUtils.toMillis(hoy.minusDays(diasAtras.toLong()), LocalTime.of(12, 30))
            expensesRepository.addExpense(
                Expense(
                    id = "${MARCA}exp-$idx",
                    category = cat,
                    amount = monto,
                    dateMillis = fechaGasto,
                    createdAt = ahora - diasAtras * 86400000L,
                    updatedAt = ahora
                )
            )
        }
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

    // ------------------------------------------------------------------ tareas

    /**
     * Siembra las 7 tareas exactas del artifact aprobado con subtareas y estados variados,
     * más las 4 materias asociadas (Sociología, Cálculo III, Programación, Física II).
     */
    fun sembrarTareasDelArtifact() {
        val matSocId = "${MARCA}mat-soc"
        val matCalcId = "${MARCA}mat-calc"
        val matProId = "${MARCA}mat-pro"
        val matFisId = "${MARCA}mat-fis"

        val existingSubjectIds = gradesRepository.subjects.value.map { it.id }.toSet()
        if (matSocId !in existingSubjectIds) {
            gradesRepository.addSubject(
                Subject(
                    id = matSocId,
                    name = "Sociología",
                    targetAverage = 3.8,
                    grades = emptyList(),
                    visualType = SubjectVisualType.YELLOW
                )
            )
        }
        if (matCalcId !in existingSubjectIds) {
            gradesRepository.addSubject(
                Subject(
                    id = matCalcId,
                    name = "Cálculo III",
                    targetAverage = 3.5,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE
                )
            )
        }
        if (matProId !in existingSubjectIds) {
            gradesRepository.addSubject(
                Subject(
                    id = matProId,
                    name = "Programación",
                    targetAverage = 4.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.GREEN
                )
            )
        }
        if (matFisId !in existingSubjectIds) {
            gradesRepository.addSubject(
                Subject(
                    id = matFisId,
                    name = "Física II",
                    targetAverage = 3.5,
                    grades = emptyList(),
                    visualType = SubjectVisualType.PURPLE
                )
            )
        }

        // Limpiar tareas de prueba anteriores para idempotencia (y los archivos que dejaron).
        tasksRepository.attachments.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { taskAttachmentStore.delete(it.storedName) }
        tasksRepository.tasks.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { tasksRepository.deleteTask(it.id) }

        val today = LocalDate.now()
        val nowMillis = System.currentTimeMillis()

        // 1. Ayer 23:59 - Ensayo: la ciudad como texto
        val t1Id = "${MARCA}task-1"
        tasksRepository.addTask(
            StudentTask(
                id = t1Id,
                title = "Ensayo: la ciudad como texto",
                description = "3 páginas. Formato APA 7. Analizar espacio público y segregación en Medellín.",
                subjectId = matSocId,
                type = TaskType.ESSAY,
                dueDateMillis = TaskDateUtils.toMillis(today.minusDays(1), LocalTime.of(23, 59)),
                difficulty = TaskDifficulty.MEDIUM,
                estimatedMinutes = 120,
                completed = false,
                createdAt = nowMillis - 3 * 86400000L,
                updatedAt = nowMillis - 86400000L,
                subtasks = listOf(
                    TaskSubtask(id = "${t1Id}-sub-1", taskId = t1Id, title = "Elegir caso de estudio (Comuna 13 vs El Poblado)", isCompleted = true, position = 0),
                    TaskSubtask(id = "${t1Id}-sub-2", taskId = t1Id, title = "Fichar 3 lecturas teóricas (Harvey, Lefebvre)", isCompleted = true, position = 1),
                    TaskSubtask(id = "${t1Id}-sub-3", taskId = t1Id, title = "Borrador de introducción y tesis", isCompleted = false, position = 2),
                    TaskSubtask(id = "${t1Id}-sub-4", taskId = t1Id, title = "Conclusiones y referencias APA", isCompleted = false, position = 3)
                )
            )
        )

        // 2. Hoy 23:59 - Taller 3
        val t2Id = "${MARCA}task-2"
        tasksRepository.addTask(
            StudentTask(
                id = t2Id,
                title = "Taller 3",
                description = "Ejercicios 12 al 28 de la guía de integrales dobles sobre regiones generales.",
                subjectId = matCalcId,
                type = TaskType.WORKSHOP,
                dueDateMillis = TaskDateUtils.toMillis(today, LocalTime.of(23, 59)),
                difficulty = TaskDifficulty.MEDIUM,
                estimatedMinutes = 45,
                completed = false,
                createdAt = nowMillis - 2 * 86400000L,
                updatedAt = nowMillis,
                subtasks = listOf(
                    TaskSubtask(id = "${t2Id}-sub-1", taskId = t2Id, title = "Ejercicios 12-18 (regiones tipo I y II)", isCompleted = true, position = 0),
                    TaskSubtask(id = "${t2Id}-sub-2", taskId = t2Id, title = "Ejercicios 19-28 (cambio de orden de integración)", isCompleted = false, position = 1)
                )
            )
        )

        // 3. Hoy 18:00 - Lectura cap. 4
        val t3Id = "${MARCA}task-3"
        tasksRepository.addTask(
            StudentTask(
                id = t3Id,
                title = "Lectura cap. 4",
                description = "Lectura preparatoria para la clase de mañana: árboles binarios y recorridos DFS/BFS.",
                subjectId = matProId,
                type = TaskType.READING,
                dueDateMillis = TaskDateUtils.toMillis(today, LocalTime.of(18, 0)),
                difficulty = TaskDifficulty.EASY,
                estimatedMinutes = 30,
                completed = false,
                createdAt = nowMillis - 86400000L,
                updatedAt = nowMillis,
                gradingStatus = TaskGradingStatus.NOT_GRADED,
                subtasks = emptyList()
            )
        )

        // 4. En 5 días 10:00 - Parcial 2
        val t4Id = "${MARCA}task-4"
        tasksRepository.addTask(
            StudentTask(
                id = t4Id,
                title = "Parcial 2",
                description = "Temas: Ley de Gauss, potencial eléctrico y capacitancia. Traer calculadora no programable.",
                subjectId = matFisId,
                type = TaskType.EXAM,
                dueDateMillis = TaskDateUtils.toMillis(today.plusDays(5), LocalTime.of(10, 0)),
                difficulty = TaskDifficulty.HARD,
                estimatedMinutes = 180,
                completed = false,
                createdAt = nowMillis - 4 * 86400000L,
                updatedAt = nowMillis,
                subtasks = listOf(
                    TaskSubtask(id = "${t4Id}-sub-1", taskId = t4Id, title = "Repasar teoría Ley de Gauss y flujo", isCompleted = true, position = 0),
                    TaskSubtask(id = "${t4Id}-sub-2", taskId = t4Id, title = "Taller preparatorio ejercicios 1 a 15", isCompleted = false, position = 1),
                    TaskSubtask(id = "${t4Id}-sub-3", taskId = t4Id, title = "Formulario resumen (una ficha)", isCompleted = false, position = 2),
                    TaskSubtask(id = "${t4Id}-sub-4", taskId = t4Id, title = "Simulacro con parcial del semestre pasado", isCompleted = false, position = 3),
                    TaskSubtask(id = "${t4Id}-sub-5", taskId = t4Id, title = "Dormir bien la noche anterior", isCompleted = false, position = 4)
                )
            )
        )

        // 5. En 10 días 23:59 - Avance 2 del proyecto
        val t5Id = "${MARCA}task-5"
        tasksRepository.addTask(
            StudentTask(
                id = t5Id,
                title = "Avance 2 del proyecto",
                description = "Entrega del backend funcional con endpoints REST, migraciones de base de datos y tests unitarios.",
                subjectId = matProId,
                type = TaskType.PROJECT,
                dueDateMillis = TaskDateUtils.toMillis(today.plusDays(10), LocalTime.of(23, 59)),
                difficulty = TaskDifficulty.HARD,
                estimatedMinutes = 240,
                completed = false,
                createdAt = nowMillis - 5 * 86400000L,
                updatedAt = nowMillis,
                subtasks = listOf(
                    TaskSubtask(id = "${t5Id}-sub-1", taskId = t5Id, title = "Diseño de la base de datos y migraciones", isCompleted = true, position = 0),
                    TaskSubtask(id = "${t5Id}-sub-2", taskId = t5Id, title = "Endpoints CRUD principales", isCompleted = false, position = 1),
                    TaskSubtask(id = "${t5Id}-sub-3", taskId = t5Id, title = "Tests unitarios y documentación OpenAPI", isCompleted = false, position = 2)
                )
            )
        )

        // 6. Hace 2 días 15:00 - Quiz 3 (Completada, espera nota)
        val t6Id = "${MARCA}task-6"
        tasksRepository.addTask(
            StudentTask(
                id = t6Id,
                title = "Quiz 3",
                description = "Quiz corto sobre derivadas direccionales y vector gradiente.",
                subjectId = matCalcId,
                type = TaskType.TEST,
                dueDateMillis = TaskDateUtils.toMillis(today.minusDays(2), LocalTime.of(15, 0)),
                difficulty = TaskDifficulty.EASY,
                estimatedMinutes = 20,
                completed = true,
                completedAt = nowMillis - 86400000L,
                createdAt = nowMillis - 6 * 86400000L,
                updatedAt = nowMillis - 86400000L,
                gradingStatus = TaskGradingStatus.AWAITING_GRADE,
                subtasks = emptyList()
            )
        )

        // 7. Hace 8 días 23:59 - Avance 1 del proyecto (Completada, calificada)
        val t7Id = "${MARCA}task-7"
        tasksRepository.addTask(
            StudentTask(
                id = t7Id,
                title = "Avance 1 del proyecto",
                description = "Propuesta de arquitectura y mockup de interfaz.",
                subjectId = matProId,
                type = TaskType.PROJECT,
                dueDateMillis = TaskDateUtils.toMillis(today.minusDays(8), LocalTime.of(23, 59)),
                difficulty = TaskDifficulty.HARD,
                estimatedMinutes = 200,
                completed = true,
                completedAt = nowMillis - 7 * 86400000L,
                createdAt = nowMillis - 14 * 86400000L,
                updatedAt = nowMillis - 7 * 86400000L,
                gradingStatus = TaskGradingStatus.GRADED,
                subtasks = listOf(
                    TaskSubtask(id = "${t7Id}-sub-1", taskId = t7Id, title = "Documento de arquitectura", isCompleted = true, position = 0),
                    TaskSubtask(id = "${t7Id}-sub-2", taskId = t7Id, title = "Mockup interactivo en Figma", isCompleted = true, position = 1)
                )
            )
        )

        // Adjuntos de ejemplo: una foto de apunte, un archivo y un audio, repartidos en tres
        // tareas distintas para ver la tira, el carrusel y la vista a pantalla completa.
        TaskAttachmentSamples.pizarra(taskAttachmentStore, t2Id, variante = 0)
            ?.let(tasksRepository::addAttachment)
        TaskAttachmentSamples.pizarra(taskAttachmentStore, t4Id, variante = 1)
            ?.let(tasksRepository::addAttachment)
        TaskAttachmentSamples.documento(
            store = taskAttachmentStore,
            taskId = t5Id,
            nombreVisible = "requisitos-backend.txt",
            contenido = """
                Avance 2 — requisitos
                ======================
                - Endpoints REST documentados en OpenAPI.
                - Migraciones versionadas, sin editar una ya aplicada.
                - Tests unitarios sobre los casos borde de cada endpoint.
            """
        )?.let(tasksRepository::addAttachment)
        TaskAttachmentSamples.grabacion(taskAttachmentStore, t1Id)
            ?.let(tasksRepository::addAttachment)
    }

    // ------------------------------------------------------------------ notificaciones

    /**
     * Las quince de la réplica interactiva, con sus horas y sus días.
     *
     * Son las mismas que se usaron para decidir el rediseño: tres con destino —las únicas que
     * ofrecen «Abrir»— y el resto sin él, repartidas entre hoy, ayer y el lunes para que se vean
     * los cortes por día y las cuentas de los filtros.
     */
    fun sembrarNotificacionesDelArtifact(context: Context) {
        val hoy = LocalDate.now()
        fun cuando(dia: LocalDate, hora: Int, minuto: Int): Long =
            dia.atTime(hora, minuto).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        data class Semilla(
            val titulo: String,
            val cuerpo: String,
            val dia: LocalDate,
            val hora: Int,
            val minuto: Int,
            val ruta: String? = null
        )

        val ayer = hoy.minusDays(1)
        val lunes = hoy.minusDays(3)
        val semillas = listOf(
            Semilla("Taller 3", "Vence hoy 23:59. Cálculo III.", hoy, 9, 26, AppRoutes.Tasks),
            Semilla("Lectura cap. 4", "Vence hoy 18:00. Programación.", hoy, 9, 26),
            Semilla("¡Buenos días!", "Tienes un pendiente vencido. Priorízalo antes de seguir.", hoy, 9, 25),
            Semilla("¿Fuiste a Sociología?", "La clase terminó hace 20 minutos y no la has marcado.", hoy, 10, 50, AppRoutes.Calendar),
            Semilla("Ensayo: la ciudad como texto", "Vence mañana 23:59. Sociología.", hoy, 8, 0),
            Semilla("ADMINISTRACIÓN FINANCIERA empieza en 15 minutos", "Aula 302. Recuerda la calculadora.", ayer, 18, 45),
            Semilla("Corte 1 cerrado", "Sociología quedó en 4,2. Ya cuenta para el promedio.", ayer, 14, 10, AppRoutes.Academic),
            Semilla("Quiz 3 espera nota", "Lo marcaste hace dos días y la nota aún no llega.", ayer, 11, 2),
            Semilla("Parcial 2", "Vence el domingo 10:00. Física II.", ayer, 9, 30),
            Semilla("¿Fuiste a Cálculo III?", "La clase terminó hace 20 minutos y no la has marcado.", ayer, 8, 20),
            Semilla("Resumen de la semana", "5 tareas hechas, 2 vencidas. Vas mejor que la semana pasada.", lunes, 20, 0),
            Semilla("Avance 1 del proyecto", "Vence el miércoles 23:59. Programación.", lunes, 16, 40),
            Semilla("Llevas 3 faltas en Sociología", "El tope que pusiste es 4. Una más y pierdes por fallas.", lunes, 12, 15),
            Semilla("PROGRAMACIÓN empieza en 15 minutos", "Laboratorio 2.", lunes, 9, 45),
            Semilla("UniStack instalada", "Mira qué cambió en Novedades.", lunes, 8, 5)
        )

        NotificationHistoryStore.seedForTesting(
            context = context,
            items = semillas.mapIndexed { indice, s ->
                NotificationHistoryItem(
                    id = NotificationHistoryStore.TEST_ID_BASE + indice,
                    requestCode = NotificationHistoryStore.TEST_ID_BASE + indice,
                    title = s.titulo,
                    body = s.cuerpo,
                    timestampMillis = cuando(s.dia, s.hora, s.minuto),
                    scheduledAtMillis = null,
                    delivered = true,
                    read = false,
                    targetRoute = s.ruta
                )
            }
        )
    }

    fun recogerNotificaciones(context: Context) {
        NotificationHistoryStore.removeSeeded(context)
    }

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

    data class ResumenDePruebas(
        val horario: Int,
        val academico: Int,
        val gastos: Int,
        val tareas: Int
    )

    /**
     * Cuantas cosas de mentira hay ahora mismo, por sitio.
     *
     * El panel lo enseña para que no haya que fiarse de la memoria: si dice «Horario 2», es
     * que quedan dos clases fabricadas ahi.
     */
    fun cuantasDePrueba(): ResumenDePruebas = ResumenDePruebas(
        horario = scheduleRepository.sessions.value.count { it.id.startsWith(MARCA) },
        academico = gradesRepository.subjects.value.sumOf { m -> m.grades.count { it.id.startsWith(MARCA) } },
        gastos = expensesRepository.expenses.value.count { it.id.startsWith(MARCA) },
        tareas = tasksRepository.tasks.value.count { it.id.startsWith(MARCA) }
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

    /** Deshace solo lo de Tareas: sus adjuntos (fila y archivo), materias y tareas fabricadas. */
    fun recogerTareas() {
        tasksRepository.attachments.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { taskAttachmentStore.delete(it.storedName) }
        tasksRepository.tasks.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { tasksRepository.deleteTask(it.id) }
        gradesRepository.subjects.value
            .filter { it.id.startsWith(MARCA) }
            .forEach { gradesRepository.deleteSubject(it.id) }
    }

    fun recogerlo() {
        com.unistack.app.feature_terms.presentation.HistoricoDeMuestra.salir()
        recogerHorario()
        recogerAcademico()
        recogerGastos()
        recogerTareas()
    }
}
