package com.unistack.app.feature_templates.domain

fun EssayTemplate.exportText(completedChecklistIds: List<String>): String {
    val checklist = AcademicTemplateLibrary.checklist.joinToString(separator = "\n") { item ->
        val mark = if (item.id in completedChecklistIds) "[x]" else "[ ]"
        "$mark ${item.title}: ${item.detail}"
    }
    val sections = sections.joinToString(separator = "\n\n") { section ->
        "${section.title}\n${section.prompt}"
    }
    val apa = AcademicTemplateLibrary.apaTips.joinToString(separator = "\n") { tip ->
        "- ${tip.title}: ${tip.description}"
    }
    val apaReferences = buildApaReferenceDraft("")

    return """
        $title
        $description

        Checklist
        $checklist

        Estructura
        $sections

        APA básico
        $apa

        Referencias APA
        $apaReferences
    """.trimIndent()
}

fun buildApaReferenceDraft(sourcesText: String): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    val sources = sourcesText
        .lineSequence()
        .map { line -> line.trim().trimStart('-', '*', ' ') }
        .filter { it.isNotBlank() }
        .take(6)
        .toList()

    if (sources.isEmpty()) {
        return if (isEn) {
            listOf(
                "Author, A. (Year). Book title. Publisher.",
                "Author, A. (Year). Article title. Journal, volume(issue), pages.",
                "Institution. (Year). Resource title. URL"
            ).joinToString(separator = "\n")
        } else {
            listOf(
                "Apellido, N. (Año). Título del libro. Editorial.",
                "Apellido, N. (Año). Título del artículo. Revista, volumen(número), páginas.",
                "Institución. (Año). Título del recurso. URL"
            ).joinToString(separator = "\n")
        }
    }

    val pendingMedium = if (isEn) "Medium or publisher pending." else "Medio o editorial pendiente."
    val pendingTitle = if (isEn) "Title pending." else "Título pendiente."
    val nd = if (isEn) "n.d." else "s. f."

    return sources.joinToString(separator = "\n") { source ->
        val parts = source.split("|").map { it.trim() }.filter { it.isNotBlank() }
        when {
            parts.size >= 4 -> "${parts[0].withoutTrailingDot()}. (${parts[1]}). ${parts[2].withoutTrailingDot()}. ${parts[3].withoutTrailingDot()}."
            parts.size == 3 -> "${parts[0].withoutTrailingDot()}. (${parts[1]}). ${parts[2].withoutTrailingDot()}. $pendingMedium"
            parts.size == 2 -> "${parts[0].withoutTrailingDot()}. ($nd). ${parts[1].withoutTrailingDot()}. $pendingMedium"
            else -> "${source.withoutTrailingDot()}. ($nd). $pendingTitle $pendingMedium"
        }
    }
}

private fun String.withoutTrailingDot(): String = trim().trimEnd('.')
