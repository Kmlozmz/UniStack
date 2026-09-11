package com.unistack.app.feature_setup.presentation

import com.unistack.app.feature_user.domain.StudyArea
import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

const val OTHER_OPTION = "Otra"
const val OTHER_OPTION_EN = "Other"

fun isOtherOption(value: String?): Boolean =
    value == OTHER_OPTION || value == OTHER_OPTION_EN

fun otherOptionLabel(): String =
    if (Locale.getDefault().language == "en") OTHER_OPTION_EN else OTHER_OPTION

fun labelFor(area: StudyArea): String {
    val isEn = Locale.getDefault().language == "en"
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
    val isEn = Locale.getDefault().language == "en"
    val other = otherOptionLabel()
    return when (area) {
        StudyArea.ENGINEERING_TECHNOLOGY -> if (isEn) listOf(
            "Systems Engineering",
            "Industrial Engineering",
            "Civil Engineering",
            "Mechanical Engineering",
            "Electronic Engineering",
            "Environmental Engineering",
            "Software Engineering",
            other
        ) else listOf(
            "Ingeniería de Sistemas",
            "Ingeniería Industrial",
            "Ingeniería Civil",
            "Ingeniería Mecánica",
            "Ingeniería Electrónica",
            "Ingeniería Ambiental",
            "Ingeniería de Software",
            other
        )
        StudyArea.ECONOMICS_BUSINESS -> if (isEn) listOf(
            "Public Accounting",
            "Business Administration",
            "Economics",
            "Finance",
            "Marketing",
            "International Business",
            other
        ) else listOf(
            "Contaduría Pública",
            "Administración de Empresas",
            "Economía",
            "Finanzas",
            "Mercadeo",
            "Negocios Internacionales",
            other
        )
        StudyArea.LAW_POLITICS -> if (isEn) listOf("Law", "Political Science", "Criminology", other)
            else listOf("Derecho", "Ciencias Políticas", "Criminalística", other)
        StudyArea.HEALTH_SCIENCES -> if (isEn) listOf("Medicine", "Nursing", "Dentistry", "Physical Therapy", "Nutrition", "Bacteriology", other)
            else listOf("Medicina", "Enfermería", "Odontología", "Fisioterapia", "Nutrición", "Bacteriología", other)
        StudyArea.EDUCATION -> if (isEn) listOf(
            "Early Childhood Education",
            "Mathematics Education",
            "Languages Education",
            "Social Studies Education",
            "Physical Education",
            other
        ) else listOf(
            "Licenciatura en Educación Infantil",
            "Licenciatura en Matemáticas",
            "Licenciatura en Lenguas",
            "Licenciatura en Ciencias Sociales",
            "Licenciatura en Educación Física",
            other
        )
        StudyArea.ARTS_DESIGN -> if (isEn) listOf("Graphic Design", "Industrial Design", "Fashion Design", "Visual Arts", "Music", other)
            else listOf("Diseño Gráfico", "Diseño Industrial", "Diseño de Modas", "Artes Visuales", "Música", other)
        StudyArea.SOCIAL_SCIENCES -> if (isEn) listOf("Psychology", "Social Work", "Social Communication", "Sociology", "Anthropology", other)
            else listOf("Psicología", "Trabajo Social", "Comunicación Social", "Sociología", "Antropología", other)
        StudyArea.BASIC_SCIENCES -> if (isEn) listOf("Mathematics", "Physics", "Chemistry", "Biology", "Statistics", other)
            else listOf("Matemáticas", "Física", "Química", "Biología", "Estadística", other)
        StudyArea.OTHER -> listOf(other)
    }
}
