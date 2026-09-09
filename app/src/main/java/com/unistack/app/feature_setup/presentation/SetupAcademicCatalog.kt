package com.unistack.app.feature_setup.presentation

import com.unistack.app.feature_user.domain.StudyArea
import java.util.Locale

const val OTHER_OPTION = "Otra"
const val OTHER_OPTION_EN = "Other"

fun isOtherOption(value: String?): Boolean =
    value == OTHER_OPTION || value == OTHER_OPTION_EN

fun otherOptionLabel(): String =
    if (Locale.getDefault().language == "en") OTHER_OPTION_EN else OTHER_OPTION

fun labelFor(area: StudyArea): String {
    val isEn = Locale.getDefault().language == "en"
    return when (area) {
        StudyArea.ENGINEERING_TECHNOLOGY -> if (isEn) "Engineering & Technology" else "Ingeniería y tecnología"
        StudyArea.ECONOMICS_BUSINESS -> if (isEn) "Economics & Business Sciences" else "Ciencias económicas y administrativas"
        StudyArea.LAW_POLITICS -> if (isEn) "Legal & Political Sciences" else "Ciencias jurídicas"
        StudyArea.HEALTH_SCIENCES -> if (isEn) "Health Sciences" else "Ciencias de la salud"
        StudyArea.EDUCATION -> if (isEn) "Education" else "Educación"
        StudyArea.ARTS_DESIGN -> if (isEn) "Arts & Design" else "Artes y diseño"
        StudyArea.SOCIAL_SCIENCES -> if (isEn) "Social Sciences" else "Ciencias sociales"
        StudyArea.BASIC_SCIENCES -> if (isEn) "Basic Sciences" else "Ciencias básicas"
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
