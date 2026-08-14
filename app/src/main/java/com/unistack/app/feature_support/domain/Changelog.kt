package com.unistack.app.feature_support.domain

import com.unistack.app.core.utils.BuildStage

/** Una versión del registro de cambios: su número, su fecha y lo que trajo. */
data class ChangelogSection(
    val version: String,
    val date: String?,
    val body: String
) {
    val stage: BuildStage get() = BuildStage.of(version)
}

private val HEADING = Regex("""^##\s*\[([^\]]+)\]\s*(?:[—-]\s*(.+))?$""")

/**
 * Las versiones publicadas que hay en el archivo, de la más nueva a la más vieja.
 *
 * «Sin publicar» se cae aquí: es la libreta de lo que se está haciendo, y no tiene nada que
 * hacer delante de alguien que instaló una versión concreta.
 */
fun parseChangelog(markdown: String): List<ChangelogSection> {
    val lines = markdown.lines()
    val starts = lines.indices.filter { HEADING.containsMatchIn(lines[it].trim()) }
    return starts.mapIndexedNotNull { order, start ->
        val match = HEADING.find(lines[start].trim()) ?: return@mapIndexedNotNull null
        val version = match.groupValues[1].trim()
        if (version.equals("Sin publicar", ignoreCase = true)) return@mapIndexedNotNull null
        val end = starts.getOrNull(order + 1) ?: lines.size
        val body = lines.subList(start + 1, end)
            .joinToString("\n")
            .trim()
            .removeSuffix("---")
            .trim()
        ChangelogSection(
            version = version,
            date = match.groupValues[2].trim().takeIf(String::isNotBlank),
            body = body
        )
    }
}

/**
 * Lo que le toca ver a quien instaló [versionName].
 *
 * Cada canal es un público distinto, así que cada uno tiene que leer su propia historia: quien
 * va por betas nunca instaló las alphas de en medio, y para quien tiene la definitiva un
 * «arreglado lo que rompió la beta 2» no significa nada —esa beta no la tuvo—.
 *
 * La regla es una sola: se recogen las versiones desde la instalada hacia atrás **hasta la
 * anterior del mismo canal**, sin incluirla. Así una definitiva reúne todo lo que pasó desde la
 * definitiva anterior —alphas y betas incluidas—, y una beta reúne lo suyo desde la beta
 * anterior. Sin esto, la 1.0.0 saldría con una lista de arreglos de fallos que su usuario nunca
 * llegó a sufrir.
 */
fun changelogFor(markdown: String, versionName: String): List<ChangelogSection> {
    val sections = parseChangelog(markdown)
    if (sections.isEmpty()) return emptyList()

    val installed = versionName.trim().removePrefix("v")
    val stage = BuildStage.of(installed)
    // Una compilación de trabajo no tiene sección propia: se le enseña desde lo último
    // publicado, que es lo que su APK lleva dentro.
    val start = sections.indexOfFirst { it.version == installed }.takeIf { it >= 0 } ?: 0

    val visible = mutableListOf<ChangelogSection>()
    for (index in start until sections.size) {
        val section = sections[index]
        if (index > start && section.isPreviousFor(stage)) break
        visible += section
    }
    return visible
}

/**
 * Si esta versión es el punto donde parar mirando hacia atrás desde [stage].
 *
 * Lo es la anterior del mismo canal, y también cualquier definitiva: las definitivas llegan a
 * los tres canales, así que quien esté en beta ya tuvo la última definitiva y no necesita que
 * se la vuelvan a contar.
 */
private fun ChangelogSection.isPreviousFor(stage: BuildStage): Boolean =
    this.stage == stage || this.stage == BuildStage.STABLE
