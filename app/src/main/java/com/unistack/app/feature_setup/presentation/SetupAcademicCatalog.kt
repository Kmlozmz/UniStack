package com.unistack.app.feature_setup.presentation

import com.unistack.app.feature_user.domain.StudyArea

const val OTHER_OPTION = "Otra"

fun labelFor(area: StudyArea): String = when (area) {
    StudyArea.ENGINEERING_TECHNOLOGY -> "Ingeniería y tecnología"
    StudyArea.ECONOMICS_BUSINESS -> "Ciencias económicas y administrativas"
    StudyArea.LAW_POLITICS -> "Ciencias jurídicas"
    StudyArea.HEALTH_SCIENCES -> "Ciencias de la salud"
    StudyArea.EDUCATION -> "Educación"
    StudyArea.ARTS_DESIGN -> "Artes y diseño"
    StudyArea.SOCIAL_SCIENCES -> "Ciencias sociales"
    StudyArea.BASIC_SCIENCES -> "Ciencias básicas"
    StudyArea.OTHER -> OTHER_OPTION
}

fun programsFor(area: StudyArea): List<String> = when (area) {
    StudyArea.ENGINEERING_TECHNOLOGY -> listOf(
        "Ingeniería de Sistemas",
        "Ingeniería Industrial",
        "Ingeniería Civil",
        "Ingeniería Mecánica",
        "Ingeniería Electrónica",
        "Ingeniería Ambiental",
        "Ingeniería de Software",
        OTHER_OPTION
    )
    StudyArea.ECONOMICS_BUSINESS -> listOf(
        "Contaduría Pública",
        "Administración de Empresas",
        "Economía",
        "Finanzas",
        "Mercadeo",
        "Negocios Internacionales",
        OTHER_OPTION
    )
    StudyArea.LAW_POLITICS -> listOf("Derecho", "Ciencias Políticas", "Criminalística", OTHER_OPTION)
    StudyArea.HEALTH_SCIENCES -> listOf("Medicina", "Enfermería", "Odontología", "Fisioterapia", "Nutrición", "Bacteriología", OTHER_OPTION)
    StudyArea.EDUCATION -> listOf(
        "Licenciatura en Educación Infantil",
        "Licenciatura en Matemáticas",
        "Licenciatura en Lenguas",
        "Licenciatura en Ciencias Sociales",
        "Licenciatura en Educación Física",
        OTHER_OPTION
    )
    StudyArea.ARTS_DESIGN -> listOf("Diseño Gráfico", "Diseño Industrial", "Diseño de Modas", "Artes Visuales", "Música", OTHER_OPTION)
    StudyArea.SOCIAL_SCIENCES -> listOf("Psicología", "Trabajo Social", "Comunicación Social", "Sociología", "Antropología", OTHER_OPTION)
    StudyArea.BASIC_SCIENCES -> listOf("Matemáticas", "Física", "Química", "Biología", "Estadística", OTHER_OPTION)
    StudyArea.OTHER -> listOf(OTHER_OPTION)
}
