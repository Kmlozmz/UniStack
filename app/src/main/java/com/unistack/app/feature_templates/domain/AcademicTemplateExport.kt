package com.unistack.app.feature_templates.domain

import com.unistack.app.R
import com.unistack.app.core.utils.Textos

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

        ${Textos.get(R.string.tpl_export_checklist)}
        $checklist

        ${Textos.get(R.string.tpl_export_structure)}
        $sections

        ${Textos.get(R.string.tpl_export_apa)}
        $apa

        ${Textos.get(R.string.tpl_export_references)}
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
            Textos.get(R.string.tpl_apa_sample_book),
            Textos.get(R.string.tpl_apa_sample_article),
            Textos.get(R.string.tpl_apa_sample_web)
        ).joinToString(separator = "\n")
    }

    val pendingMedium = Textos.get(R.string.works_medio_o_editorial_pendiente)
    val pendingTitle = Textos.get(R.string.works_titulo_pendiente)
    val nd = Textos.get(R.string.works_s_f)

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
