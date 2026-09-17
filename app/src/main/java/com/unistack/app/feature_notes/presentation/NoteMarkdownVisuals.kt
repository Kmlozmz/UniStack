package com.unistack.app.feature_notes.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteStyle
import com.unistack.app.feature_notes.domain.NoteTextEdits

/**
 * Los colores con los que se pinta una nota.
 *
 * Se saca del tema una sola vez y se pasa hecho: el formateo ocurre en cada tecla, y leer
 * `MaterialTheme` desde dentro obligaria a que eso fuera un composable.
 */
data class NotePalette(
    val texto: Color,
    val suave: Color,
    val tenue: Color,
    val acento: Color,
    val fondoCodigo: Color
)

@Composable
@ReadOnlyComposable
fun rememberNotePalette(): NotePalette = NotePalette(
    texto = MaterialTheme.colorScheme.onSurface,
    suave = MaterialTheme.colorScheme.onSurfaceVariant,
    tenue = MaterialTheme.colorScheme.outlineVariant,
    acento = MaterialTheme.colorScheme.primary,
    fondoCodigo = MaterialTheme.colorScheme.surfaceContainerHighest
)

/**
 * El formato, mientras se escribe.
 *
 * No hay vista previa ni boton de «ver como queda»: el texto se va formateando en el sitio. En
 * Markdown las marcas siguen ahi pero apagadas, para poder escribirlas y borrarlas; en sencillo
 * desaparecen y en su lugar quedan los simbolos —la vineta, la casilla—, que es lo que convierte
 * el mismo texto guardado en dos maneras de trabajar.
 */
class NoteVisualTransformation(
    private val format: NoteFormat,
    private val palette: NotePalette
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val crudo = text.text
        if (crudo.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val markup = NoteMarkdown.parse(crudo)

        if (format == NoteFormat.MARKDOWN) {
            val builder = AnnotatedString.Builder(crudo)
            markup.spans.forEach { span ->
                builder.pintar(estiloDe(span.style, palette), span.start, span.end, crudo.length)
            }
            // Las marcas no se esconden, se apagan: hay que poder verlas para poder quitarlas.
            markup.markers.forEach { marca ->
                builder.pintar(SpanStyle(color = palette.tenue), marca.start, marca.end, crudo.length)
            }
            return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
        }

        val editado = NoteTextEdits.apply(crudo, NoteTextEdits.hidingEdits(markup))
        val visible = editado.text
        val builder = AnnotatedString.Builder(visible)

        markup.spans.forEach { span ->
            builder.pintar(
                estiloDe(span.style, palette),
                editado.toTransformed(span.start),
                editado.toTransformed(span.end),
                visible.length
            )
        }
        markup.markers.filter { it.replacement.isNotEmpty() }.forEach { marca ->
            val inicio = editado.toTransformed(marca.start)
            builder.pintar(
                SpanStyle(color = palette.acento),
                inicio,
                inicio + marca.replacement.length,
                visible.length
            )
        }

        return TransformedText(
            builder.toAnnotatedString(),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = editado.toTransformed(offset)
                override fun transformedToOriginal(offset: Int): Int = editado.toOriginal(offset)
            }
        )
    }

    private fun AnnotatedString.Builder.pintar(estilo: SpanStyle, start: Int, end: Int, limite: Int) {
        val desde = start.coerceIn(0, limite)
        val hasta = end.coerceIn(desde, limite)
        if (hasta > desde) addStyle(estilo, desde, hasta)
    }
}

/**
 * Como se ve cada cosa.
 *
 * Los titulos crecen poco a proposito: un apunte de clase con un titulo del doble de alto se
 * lee como una presentacion y no como una libreta. Y el codigo lleva fondo ademas de fuente
 * monoespaciada, porque en un parrafo la fuente sola casi no se distingue.
 */
fun estiloDe(style: NoteStyle, p: NotePalette): SpanStyle = when (style) {
    NoteStyle.TITULO1 -> SpanStyle(fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = p.texto)
    NoteStyle.TITULO2 -> SpanStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = p.texto)
    NoteStyle.TITULO3 -> SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = p.suave)
    NoteStyle.NEGRITA -> SpanStyle(fontWeight = FontWeight.Bold)
    NoteStyle.CURSIVA -> SpanStyle(fontStyle = FontStyle.Italic)
    NoteStyle.TACHADO -> SpanStyle(textDecoration = TextDecoration.LineThrough, color = p.suave)
    NoteStyle.CODIGO -> SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        background = p.fondoCodigo,
        color = p.acento
    )
    NoteStyle.BLOQUE_CODIGO -> SpanStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        background = p.fondoCodigo,
        color = p.suave
    )
    NoteStyle.CITA -> SpanStyle(fontStyle = FontStyle.Italic, color = p.suave)
    NoteStyle.VINETA -> SpanStyle(color = p.texto)
    NoteStyle.NUMERO -> SpanStyle(color = p.acento, fontWeight = FontWeight.Bold)
    NoteStyle.CASILLA -> SpanStyle(color = p.texto)
    NoteStyle.CASILLA_HECHA -> SpanStyle(textDecoration = TextDecoration.LineThrough, color = p.tenue)
    NoteStyle.LINEA -> SpanStyle(color = p.tenue)
    NoteStyle.ENLACE -> SpanStyle(color = p.acento, textDecoration = TextDecoration.Underline)
    NoteStyle.TABLA -> SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.5.sp, color = p.suave)
}
