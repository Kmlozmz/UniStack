package com.unistack.app.feature_setup.presentation

import com.unistack.app.feature_user.domain.StudyArea
import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/** La opcion «Otra» tal como se muestra ahora mismo; lo guardado puede venir del otro idioma, por eso [isOtherOption] mira los dos. */
val OTHER_OPTION: String get() = Textos.get(R.string.setup_other_option)
private val OTHER_OPTION_ALL = setOf("Otra", "Other")

fun isOtherOption(value: String?): Boolean =
    value == OTHER_OPTION || value in OTHER_OPTION_ALL

fun otherOptionLabel(): String = Textos.get(R.string.setup_other_option)

fun labelFor(area: StudyArea): String {
    return when (area) {
        StudyArea.ENGINEERING_TECHNOLOGY -> Textos.get(R.string.setup_ingenieria_y_tecnologia)
        StudyArea.ECONOMICS_BUSINESS -> Textos.get(R.string.setup_ciencias_economicas_y_administrativas)
        StudyArea.LAW_POLITICS -> Textos.get(R.string.setup_ciencias_juridicas)
        StudyArea.HEALTH_SCIENCES -> Textos.get(R.string.setup_ciencias_de_la_salud)
        StudyArea.EDUCATION -> Textos.get(R.string.setup_educacion)
        StudyArea.ARTS_DESIGN -> Textos.get(R.string.setup_artes_y_diseno)
        StudyArea.SOCIAL_SCIENCES -> Textos.get(R.string.setup_ciencias_sociales)
        StudyArea.BASIC_SCIENCES -> Textos.get(R.string.setup_ciencias_basicas)
        StudyArea.OTHER -> otherOptionLabel()
    }
}

fun programsFor(area: StudyArea): List<String> {
    val other = otherOptionLabel()
    val lista = when (area) {
        StudyArea.ENGINEERING_TECHNOLOGY -> R.array.setup_programs_engineering
        StudyArea.ECONOMICS_BUSINESS -> R.array.setup_programs_economics
        StudyArea.LAW_POLITICS -> R.array.setup_programs_law
        StudyArea.HEALTH_SCIENCES -> R.array.setup_programs_health
        StudyArea.EDUCATION -> R.array.setup_programs_education
        StudyArea.ARTS_DESIGN -> R.array.setup_programs_arts
        StudyArea.SOCIAL_SCIENCES -> R.array.setup_programs_social
        StudyArea.BASIC_SCIENCES -> R.array.setup_programs_basic
        StudyArea.OTHER -> return listOf(other)
    }
    return Textos.lista(lista) + other
}
