package com.unistack.app.feature_user.domain

import java.time.LocalDate

import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage

import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesSort
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * El retrato que hay que pintar, venga de donde venga.
 *
 * La foto propia manda sobre la de la cuenta. Vivió un rato repartido: la pantalla de perfil
 * hacía el `?:` y el resto de la app seguía leyendo el de la cuenta a secas, así que cambiar el
 * retrato solo se notaba en el sitio donde se cambiaba. Con una sola propiedad no hay forma de
 * que a un lector se le olvide.
 */
val UserProfile.portraitUrl: String?
    get() = localPhotoUri ?: accountPhotoUrl

data class UserProfile(
    val userId: String,
    val preferredName: String,
    val accountProvider: AuthProvider = AuthProvider.LOCAL,
    val accountProviderUserId: String? = null,
    val accountEmail: String? = null,
    val accountPhotoUrl: String? = null,
    /**
     * El retrato que eligió quien usa la app, si eligió alguno.
     *
     * Va aparte de [accountPhotoUrl] y manda sobre él. Vincular Google trae la foto de esa
     * cuenta, que es un punto de partida razonable —mejor una foto tuya que una inicial—, pero
     * no puede ser la última palabra: hay quien tiene ahí una foto de hace seis años. Guardando
     * las dos, cambiar el retrato no borra la de la cuenta y desvincular no borra el tuyo.
     *
     * Es un `content://` con permiso de lectura persistido, no una copia del archivo: la app no
     * duplica imágenes que ya están en la galería.
     */
    val localPhotoUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastSyncAt: Long? = null,
    val careerOrProgram: String?,
    val studyArea: StudyArea?,
    /**
     * El nombre del área cuando [studyArea] es [StudyArea.OTHER].
     *
     * El área es un enum cerrado —hace falta para el catálogo de materias— así que «tu área no
     * está en la lista» no puede guardarse dentro de él. Esto es lo que guarda lo que sí
     * escribiste. [careerOrProgram] no necesita su equivalente: ya acepta texto libre para
     * cualquier carrera, esté o no en el catálogo.
     */
    val customStudyArea: String? = null,
    /**
     * Nombre de la institución tal y como lo escribió el usuario, sin normalizar.
     *
     * Se guarda en crudo a propósito: si algún día se añade un catálogo canónico, poder
     * mapear el texto original a una entidad es posible; recuperar lo que la persona
     * escribió después de haberlo sobrescrito, no. Es opcional en todos los niveles.
     */
    val institutionName: String? = null,
    /**
     * En qué semestre vas y cuántos dura tu programa, para el progreso del perfil.
     *
     * Los dos o ninguno: un semestre actual sin total no puede dibujar un camino, y un total
     * sin semestre actual no puede decir dónde estás parado. Nulos hasta que el usuario los
     * pone a mano —no se derivan del Histórico académico porque ese cuenta periodos ya creados
     * en la app, y esto es la carrera entera, incluida la parte de antes de instalarla.
     */
    val currentSemester: Int? = null,
    val totalSemesters: Int? = null,
    val gradingScale: GradingScale,
    val customGradeMax: Double = 100.0,
    val passingGrade: Double,
    val targetAverage: Double,
    /**
     * Cuántas faltas admite tu reglamento, o nulo si no lo has dicho.
     *
     * Es **uno para todo**, no uno por materia: el número sale del reglamento de la
     * universidad y no de cada asignatura. Estuvo un tiempo colgando de `Subject`, donde
     * obligaba a repetirlo materia por materia para decir lo mismo siete veces.
     *
     * Nulo no es «no hay tope», es «no lo sé», que es distinto: con nulo la app no puede
     * prometer «te quedan 4», así que enseña el porcentaje, que sí puede sostener.
     */
    val absenceLimit: Int? = null,
    val enabledModules: Set<AppModule>,
    val visualPreference: VisualPreference = VisualPreference.SYSTEM,
    val appearancePreferences: AppearancePreferences = AppearancePreferences(),
    val accessibilityPreferences: AccessibilityPreferences = AccessibilityPreferences(),
    val taskRemindersEnabled: Boolean = true,
    val academicWorkRemindersEnabled: Boolean = true,
    val overdueRemindersEnabled: Boolean = true,
    val gradeInsightRemindersEnabled: Boolean = true,
    val pendingGradeRemindersEnabled: Boolean = true,
    val reminderLeadHours: Int = 24,
    /*
     * El resumen de la manana se apaga y se pone a la hora que cada uno quiera.
     * Antes salia siempre a las 7:30 clavadas y solo se podia silenciar apagando todos
     * los demas avisos, porque iba atado a ellos: quien queria los de clase se comia el
     * resumen a la fuerza.
     */
    val dailyDigestEnabled: Boolean = true,
    val dailyDigestHour: Int = 7,
    val dailyDigestMinute: Int = 30,
    /** Avisar el dia en que acaba el periodo, para cerrarlo. */
    val termEndReminderEnabled: Boolean = true,
    /** Avisar si el periodo siguiente no se ha creado para cierto dia. */
    val nextTermReminderEnabled: Boolean = true,
    /** Ese dia, contado desde el inicio sugerido: ver `OpcionesDelAviso`. */
    val nextTermReminderOffset: Int = 0,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int? = null,
    val quietHoursEndHour: Int? = null,
    /**
     * Con que forma se dibuja el grafico de la tarjeta de Gastos.
     *
     * Vive aqui, junto al presupuesto y las categorias, y **no** en las preferencias de
     * apariencia: no cambia el aspecto de la app entera, solo el de una tarjeta. Por eso
     * tampoco se elige en Ajustes sino en la propia pantalla de Gastos, donde se ve el cambio
     * en el momento en vez de tener que ir a mirarlo a otro sitio.
     */
    val expenseChartStyle: ExpenseChartStyle = ExpenseChartStyle.BARS,
    val weeklyBudget: Int = 0,
    val monthlyBudget: Int = 0,
    val expenseAlertThresholdPercent: Int = 80,
    val enabledExpenseCategories: Set<ExpenseCategory> = ExpenseCategory.entries.toSet(),
    val notesLayout: NotesLayout = NotesLayout.CUADERNO,
    val notesSort: NotesSort = NotesSort.MODIFICADA,
    /**
     * Con que formato nacen las notas nuevas.
     *
     * Es solo el punto de partida: cada nota guarda el suyo y se puede cambiar desde el
     * propio editor. Un ajuste unico obligaria a quien escribe casi todo en Markdown a
     * cambiarlo en cada apunte suelto, y al reves.
     */
    val noteFormatDefault: NoteFormat = NoteFormat.PLAIN,
    val gradeScenarios: List<SavedGradeScenario> = emptyList(),
    val gradingCutScheme: GradingCutScheme = GradingCutScheme.default(),
    val setupCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class SavedGradeScenario(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val name: String,
    val targetAverage: Double,
    val neededGrade: Double?,
    val createdAt: Long
)

data class GradingCutScheme(
    val cuts: List<GradingCut> = defaultCuts()
) {
    val totalWeight: Double
        get() = cuts.sumOf { it.weight }

    val isValid: Boolean
        get() = cuts.isNotEmpty() &&
            cuts.all { it.weight > 0.0 } &&
            kotlin.math.abs(totalWeight - 1.0) <= 0.0001 &&
            datesAreSound

    /**
     * Si hay fechas puestas, y si tienen sentido.
     *
     * Los cortes son **contiguos por construccion**: se pide solo el dia en que acaba cada uno,
     * el siguiente empieza al dia posterior, y el ultimo acaba con el periodo. Por eso el
     * ultimo no lleva fecha, y por eso solapes y huecos no pueden existir: no hay forma de
     * escribirlos. Lo unico que queda por comprobar es que las fechas suban.
     */
    private val datesAreSound: Boolean
        get() {
            val ordenados = cuts.sortedBy { it.order }
            // El ultimo acaba con el periodo, asi que no puede llevar fecha propia.
            if (ordenados.lastOrNull()?.endEpochDay != null) return false
            val puestas = ordenados.dropLast(1).map { it.endEpochDay }
            // O todos los cortes intermedios tienen fecha, o ninguno: a medias no sirve.
            if (puestas.any { it == null } && puestas.any { it != null }) return false
            val fechas = puestas.filterNotNull()
            return fechas.zipWithNext().all { (a, b) -> b > a }
        }

    /** Si el esquema puede elegir corte a partir de una fecha. */
    val hasDates: Boolean
        get() = cuts.size == 1 || cuts.sortedBy { it.order }.dropLast(1).all { it.endEpochDay != null }

    /**
     * En que corte cae [date], o nulo si este esquema no tiene fechas.
     *
     * Nulo significa «pregunta a mano», no «no hay corte»: el selector manual sigue existiendo
     * y es lo que aparece cuando esto no puede responder.
     */
    fun cutForDate(date: LocalDate): GradingCut? {
        if (!hasDates) return null
        val ordenados = cuts.sortedBy { it.order }
        val dia = date.toEpochDay()
        // El primero cuyo cierre no ha llegado todavia. Si ninguno, es el ultimo: acaba con
        // el periodo y por tanto recoge todo lo que venga despues.
        return ordenados.firstOrNull { it.endEpochDay != null && dia <= it.endEpochDay } ?: ordenados.last()
    }

    /**
     * Del primer dia al ultimo de un corte, con los extremos que aporta el periodo.
     *
     * Solo se guarda el dia en que cierra cada uno, asi que el principio hay que deducirlo:
     * es el dia siguiente al cierre del anterior, y para el primero es el inicio del periodo.
     * El final del ultimo es el del periodo. Cualquiera de los dos extremos puede faltar
     * —sin periodo declarado, o sin fechas puestas— y entonces sale nulo en vez de inventado.
     */
    fun rangeFor(cutId: String, termStart: LocalDate?, termEnd: LocalDate?): Pair<LocalDate?, LocalDate?> {
        val ordenados = cuts.sortedBy { it.order }
        val indice = ordenados.indexOfFirst { it.id == cutId }
        if (indice < 0) return null to null
        val desde = if (indice == 0) {
            termStart
        } else {
            ordenados[indice - 1].endEpochDay?.let { LocalDate.ofEpochDay(it).plusDays(1) }
        }
        val hasta = if (indice == ordenados.lastIndex) {
            termEnd
        } else {
            ordenados[indice].endEpochDay?.let(LocalDate::ofEpochDay)
        }
        return desde to hasta
    }

    fun cutName(cutId: String?): String {
        return cuts.firstOrNull { it.id == cutId }?.name
            ?: cuts.firstOrNull()?.name
            ?: Corte.Singular
    }

    companion object {
        fun default(): GradingCutScheme = GradingCutScheme(cuts = defaultCuts())

        fun defaultCuts(): List<GradingCut> = listOf(
            GradingCut(id = "period-1", name = "${Corte.Singular} 1", weight = 0.30, order = 1),
            GradingCut(id = "period-2", name = "${Corte.Singular} 2", weight = 0.40, order = 2),
            GradingCut(id = "period-3", name = "${Corte.Singular} 3", weight = 0.30, order = 3)
        )
    }
}

data class GradingCut(
    val id: String,
    val name: String,
    val weight: Double,
    val order: Int,
    /**
     * El dia en que cierra este corte, o nulo.
     *
     * Nulo en el **ultimo** corte es lo normal: acaba cuando acaba el periodo. Nulo en los
     * demas significa que el usuario todavia no ha dado las fechas, y entonces el corte de
     * cada nota se elige a mano, como siempre.
     *
     * No hay fecha de inicio: la del primero es la del periodo y la de cada siguiente es el
     * dia posterior al cierre del anterior. Guardar los dos extremos permitiria escribir
     * solapes y huecos; guardar solo el corte los hace imposibles.
     */
    val endEpochDay: Long? = null
)

/**
 * Como se nombra un corte en la interfaz.
 *
 * Era un enum de dos valores —«Corte» y «Periodo»— que el usuario elegia al configurarse, y esa
 * eleccion existia por primaria y secundaria: en el colegio se dice periodo y en la universidad
 * corte. Sin esos niveles queda una sola forma de decirlo, asi que es una constante y no una
 * pregunta. Ademas libera la palabra «periodo», que pasa a significar el semestre.
 */
/**
 * La palabra con que la app llama a cada tramo de la nota. Era una constante en espanol
 * repartida por veintiocho sitios; ahora sale de los recursos, asi que en ingles es «cut».
 */
object Corte {
    val Singular: String get() = Textos.get(R.string.corte_singular)
    val Plural: String get() = Textos.get(R.string.corte_plural)
}

enum class GradingScale {
    ZERO_TO_FIVE,
    ZERO_TO_HUNDRED,
    CUSTOM
}

/**
 * La escala guardada con su nombre, también los de versiones anteriores.
 *
 * «ZERO_TO_ONE_HUNDRED» es como se llamó la de cien, y «ZERO_TO_TEN» y «LETTERS» se retiraron y
 * pasan a la personalizada. La leen las preferencias y la copia de seguridad; estaba escrita dos
 * veces, una en cada una.
 */
internal fun String.toGradingScaleOrNull(): GradingScale? = when (this) {
    "ZERO_TO_ONE_HUNDRED" -> GradingScale.ZERO_TO_HUNDRED
    "ZERO_TO_TEN", "LETTERS" -> GradingScale.CUSTOM
    else -> runCatching { GradingScale.valueOf(this) }.getOrNull()
}

enum class StudyArea {
    ENGINEERING_TECHNOLOGY,
    ECONOMICS_BUSINESS,
    LAW_POLITICS,
    HEALTH_SCIENCES,
    EDUCATION,
    ARTS_DESIGN,
    SOCIAL_SCIENCES,
    BASIC_SCIENCES,
    OTHER
}

enum class AppModule {
    GRADES,
    TASKS,
    EXPENSES,
    ACADEMIC_TEMPLATES
}

/**
 * Los módulos que esta compilación puede ofrecer para encender o apagar.
 *
 * Trabajos sale con su etiqueta «Pronto» y sin responder fuera de dev y alpha, así que su
 * interruptor prometía algo que no se podía cumplir: encenderlo no daba acceso a nada, y
 * apagarlo tampoco quitaba nada de en medio. Un interruptor que no cambia lo que ves es peor
 * que no tener interruptor — hace dudar de si la app está rota.
 *
 * Quien lo encendió en una alpha se lo queda: el módulo sigue en su perfil y sus recordatorios
 * siguen funcionando. Lo que desaparece es la fila, no el ajuste.
 */
fun offerableModules(): List<AppModule> {
    val unfinished = BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished
    return AppModule.entries.filter { unfinished || it != AppModule.ACADEMIC_TEMPLATES }
}

enum class VisualPreference {
    SYSTEM,
    LIGHT,
    DARK,
    OLED,
    CUSTOM
}

/**
 * Las dos maneras de mirar el gasto de la semana en la tarjeta de Gastos.
 *
 * No es un adorno: cada una responde una pregunta distinta. Las barras dicen **cuando**
 * gastaste, el anillo dice **en que**. Se elige en la propia pantalla, y por eso son dos y no
 * seis: una lista larga de estilos en una tarjeta es un menu, no una eleccion.
 */
enum class ExpenseChartStyle {
    /** Una barra por dia de la semana. Se puede tocar un dia para ver su total. */
    BARS,

    /** Un anillo repartido por categoria, con la forma de galleta de M3E detras. */
    RING
}
