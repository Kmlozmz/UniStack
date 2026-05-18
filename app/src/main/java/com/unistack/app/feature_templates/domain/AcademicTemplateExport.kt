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
    val sources = sourcesText
        .lineSequence()
        .map { line -> line.trim().trimStart('-', '*', ' ') }
        .filter { it.isNotBlank() }
        .take(6)
        .toList()

    if (sources.isEmpty()) {
        return listOf(
            "Apellido, N. (Año). Título del libro. Editorial.",
            "Apellido, N. (Año). Título del artículo. Revista, volumen(número), páginas.",
            "Institución. (Año). Título del recurso. URL"
        ).joinToString(separator = "\n")
    }

    return sources.joinToString(separator = "\n") { source ->
        val parts = source.split("|").map { it.trim() }.filter { it.isNotBlank() }
        when {
            parts.size >= 4 -> "${parts[0].withoutTrailingDot()}. (${parts[1]}). ${parts[2].withoutTrailingDot()}. ${parts[3].withoutTrailingDot()}."
            parts.size == 3 -> "${parts[0].withoutTrailingDot()}. (${parts[1]}). ${parts[2].withoutTrailingDot()}. Medio o editorial pendiente."
            parts.size == 2 -> "${parts[0].withoutTrailingDot()}. (s. f.). ${parts[1].withoutTrailingDot()}. Medio o editorial pendiente."
            else -> "${source.withoutTrailingDot()}. (s. f.). Título pendiente. Medio o editorial pendiente."
        }
    }
}

private fun String.withoutTrailingDot(): String = trim().trimEnd('.')
