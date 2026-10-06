package com.unistack.app.feature_updates.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.R

/**
 * Las notas de una versión, escritas en Markdown y leídas como tales.
 *
 * Salen de `CHANGELOG.md`, así que llegan con encabezados `###`, viñetas y negritas `**`. Antes
 * se pintaban tal cual y con un guion delante de cada línea, de modo que en pantalla se leía
 * «- ### Corregido» y los asteriscos a la vista. Y las líneas de una misma frase, al venir
 * partidas en el archivo, salían como viñetas sueltas a medio empezar.
 *
 * No usa ninguna biblioteca de Markdown: lo que hace falta es esto y nada más —encabezados,
 * viñetas, negrita y párrafos—, y las notas las escribimos nosotros mismos.
 */
@Composable
fun ReleaseNotes(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val isEnglish = stringResource(R.string.release_notes_lang_code) == "en"
    val localizedMarkdown = remember(markdown, isEnglish) { filterReleaseNotesByLanguage(markdown, isEnglish) }
    val blocks = remember(localizedMarkdown) { parseReleaseNotes(localizedMarkdown) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is NotesBlock.Heading -> Text(
                    block.text,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )

                is NotesBlock.Bullet -> Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        block.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is NotesBlock.Paragraph -> Text(
                    block.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NotesBlock.Divider -> Spacer(Modifier.height(2.dp))
            }
        }
    }
}

internal sealed interface NotesBlock {
    data class Heading(val text: String) : NotesBlock
    data class Bullet(val text: AnnotatedString) : NotesBlock
    data class Paragraph(val text: AnnotatedString) : NotesBlock
    data object Divider : NotesBlock
}

/**
 * Si las notas contienen secciones por idioma (ej. encabezado `### English` o `## English`),
 * devuelve únicamente el bloque correspondiente al idioma activo.
 * Si no hay diferenciación de idioma, devuelve el texto íntegro.
 */
internal fun filterReleaseNotesByLanguage(markdown: String, isEnglish: Boolean): String {
    val spanishHeadingRegex = Regex("""(?mi)^#{2,3}\s+(?:Español|Spanish|En español)\s*$""")
    val englishHeadingRegex = Regex("""(?mi)^#{2,3}\s+(?:English|Inglés|In english)\s*$""")

    val spanishMatch = spanishHeadingRegex.find(markdown)
    val englishMatch = englishHeadingRegex.find(markdown)

    if (spanishMatch != null) {
        val beforeSpanish = markdown.substring(0, spanishMatch.range.first).trim().removeSuffix("---").trim()
        val afterSpanish = markdown.substring(spanishMatch.range.last + 1).trim()

        return if (isEnglish) {
            if (englishMatch != null && englishMatch.range.first < spanishMatch.range.first) {
                beforeSpanish.substring(englishMatch.range.last + 1).trim()
            } else {
                beforeSpanish
            }
        } else {
            afterSpanish
        }
    }

    if (englishMatch != null) {
        val beforeEnglish = markdown.substring(0, englishMatch.range.first).trim().removeSuffix("---").trim()
        val afterEnglish = markdown.substring(englishMatch.range.last + 1).trim()

        return if (isEnglish) {
            afterEnglish
        } else {
            beforeEnglish
        }
    }

    return markdown
}

/**
 * Une las líneas de un mismo párrafo o viñeta antes de darles formato.
 *
 * En el archivo las frases se parten a los 96 caracteres para poder leerlas al editarlas. En
 * pantalla eso no significa nada: una viñeta es una viñeta hasta que llega una línea en blanco
 * o empieza otra.
 */
internal fun parseReleaseNotes(markdown: String): List<NotesBlock> {
    val blocks = mutableListOf<NotesBlock>()
    val pending = StringBuilder()
    var pendingIsBullet = false

    fun flush() {
        val text = pending.toString().trim()
        pending.setLength(0)
        if (text.isEmpty()) return
        blocks += if (pendingIsBullet) {
            NotesBlock.Bullet(markdownInline(text))
        } else {
            NotesBlock.Paragraph(markdownInline(text))
        }
    }

    markdown.lines().forEach { rawLine ->
        val line = rawLine.trim()
        when {
            line.isEmpty() -> flush()

            // Las rayas separadoras del archivo no pintan nada aquí.
            line.all { it == '-' } && line.length >= 3 -> {
                flush()
                blocks += NotesBlock.Divider
            }

            line.startsWith("#") -> {
                flush()
                blocks += NotesBlock.Heading(line.trimStart('#').trim())
            }

            line.startsWith("- ") || line.startsWith("* ") -> {
                flush()
                pendingIsBullet = true
                pending.append(line.drop(2).trim())
            }

            else -> {
                if (pending.isEmpty()) pendingIsBullet = false
                if (pending.isNotEmpty()) pending.append(' ')
                pending.append(line)
            }
        }
    }
    flush()
    return blocks
}

/** Negritas `**así**`. Lo demás se deja tal cual, que es lo que hay en las notas. */
internal fun markdownInline(text: String): AnnotatedString = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        val open = text.indexOf("**", index)
        if (open < 0) {
            append(text.substring(index))
            return@buildAnnotatedString
        }
        val close = text.indexOf("**", open + 2)
        if (close < 0) {
            append(text.substring(index))
            return@buildAnnotatedString
        }
        append(text.substring(index, open))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(open + 2, close))
        }
        index = close + 2
    }
}
