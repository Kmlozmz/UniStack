@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_notes.domain.NoteFormat

private data class AyudaFila(val marca: String, val ejemplo: String)
private data class AyudaGrupo(val titulo: String, val filas: List<AyudaFila>)

/*
 * Lo que sabe hacer cada manera de escribir, entero.
 *
 * Fue condicion suya: «cada una debe mostrar todo lo que puede hacer y como». Por eso no es una
 * lista de nombres sino dos columnas —lo que se escribe a la izquierda, como queda a la
 * derecha—, y la de la derecha se pinta con el mismo motor que el editor, no con capturas ni con
 * texto imitando el resultado. Si algun dia el formateo cambia, esta pantalla cambia sola.
 */
private val VINCULAR = AyudaGrupo(
    "Vincular una materia",
    listOf(
        AyudaFila("@calculo", "Cálculo II"),
        AyudaFila("@fis", "Física I")
    )
)

private val GRUPOS_MARKDOWN = listOf(
    AyudaGrupo(
        "Títulos",
        listOf(
            AyudaFila("# Parcial 2", "# Parcial 2"),
            AyudaFila("## Temas que entran", "## Temas que entran"),
            AyudaFila("### Capítulo 4", "### Capítulo 4")
        )
    ),
    AyudaGrupo(
        "Dentro del texto",
        listOf(
            AyudaFila("**importante**", "**importante**"),
            AyudaFila("*matiz*", "*matiz*"),
            AyudaFila("~~ya no va~~", "~~ya no va~~"),
            AyudaFila("`x = 2`", "`x = 2`"),
            AyudaFila("[el aula](enlace)", "[el aula](https://ejemplo)")
        )
    ),
    AyudaGrupo(
        "Listas",
        listOf(
            AyudaFila("- traer calculadora", "- traer calculadora"),
            AyudaFila("1. leer el capítulo", "1. leer el capítulo"),
            AyudaFila("- [ ] taller 3", "- [ ] taller 3"),
            AyudaFila("- [x] taller 2", "- [x] taller 2")
        )
    ),
    AyudaGrupo(
        "Bloques",
        listOf(
            AyudaFila("> lo que dijo el profe", "> lo que dijo el profe"),
            AyudaFila("```", "```\ncodigo()\n```"),
            AyudaFila("---", "---"),
            AyudaFila("| a | b |", "| Corte | Peso |")
        )
    ),
    VINCULAR
)

private val GRUPOS_SENCILLO = listOf(
    AyudaGrupo(
        "Selecciona y toca",
        listOf(
            AyudaFila("Negrita", "**importante**"),
            AyudaFila("Cursiva", "*matiz*"),
            AyudaFila("Tachado", "~~ya no va~~")
        )
    ),
    AyudaGrupo(
        "Listas, desde la misma barra",
        listOf(
            AyudaFila("Lista", "- traer calculadora"),
            AyudaFila("Lista numerada", "1. leer el capítulo"),
            AyudaFila("Casilla", "- [ ] taller 3")
        )
    ),
    VINCULAR
)

/**
 * La ayuda del formato, abierta desde la interrogación del editor.
 *
 * Enseña lo del modo que esté puesto y no las dos cosas a la vez: quien escribe en sencillo no
 * necesita saber que existe una almohadilla, y quien escribe en Markdown no necesita que le
 * expliquen un botón que no tiene delante.
 *
 * Era una pila de tarjetas redondeadas, una por grupo, con las filas dentro: tres bordes entre
 * la pregunta y la respuesta. Ahora los grupos se separan con su rótulo y una línea, que es lo
 * que hace falta para saber dónde empieza cada uno, y la hoja se lee de un tirón.
 */
@Composable
fun NoteFormatHelpSheet(
    format: NoteFormat,
    onDismiss: () -> Unit
) {
    val palette = rememberNotePalette()
    val grupos = if (format == NoteFormat.MARKDOWN) GRUPOS_MARKDOWN else GRUPOS_SENCILLO
    val esMarkdown = format == NoteFormat.MARKDOWN

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            item(key = "cabecera") {
                Column(
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        if (esMarkdown) "Markdown de apuntes" else "Escritura sencilla",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (esMarkdown) {
                            "Escribes las marcas y el texto se formatea solo, mientras escribes."
                        } else {
                            "Sin marcas a la vista. Selecciona un trozo y aparece la barra."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item(key = "columnas") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColumnLabel(if (esMarkdown) "Escribes" else "Tocas", Modifier.weight(1f))
                    ColumnLabel("Queda", Modifier.weight(1f))
                }
            }

            grupos.forEachIndexed { indice, grupo ->
                item(key = "g-" + grupo.titulo) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (indice > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(horizontal = 22.dp)
                            )
                        }
                        Text(
                            grupo.titulo,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = TextUnit(0.08f, TextUnitType.Em),
                            modifier = Modifier.padding(
                                start = 22.dp,
                                end = 22.dp,
                                top = if (indice > 0) 20.dp else 8.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                }
                items(grupo.filas.size) { fila ->
                    AyudaLinea(
                        fila = grupo.filas[fila],
                        // La columna izquierda va en monoespaciada cuando es algo que se
                        // teclea tal cual; en sencillo son nombres de botones, y esos no.
                        monospace = esMarkdown || grupo.filas[fila].marca.startsWith("@"),
                        palette = palette
                    )
                }
            }

            item(key = "pie") {
                Column(
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier.padding(start = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                "Las dos guardan lo mismo",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Cambiar de una a otra no convierte nada ni pierde nada: es la " +
                                    "misma nota con las marcas a la vista o escondidas. Lo único: " +
                                    "en sencillo no hay botón para títulos, tablas ni bloques de " +
                                    "código, así que si los escribiste en Markdown se siguen " +
                                    "viendo pero no se pueden quitar desde ahí.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = TextUnit(0.12f, TextUnitType.Em)
    )
}

/**
 * Una línea de la ayuda: lo que se escribe y cómo queda.
 *
 * La derecha se pinta con el motor del editor y con las marcas escondidas, que es exactamente
 * lo que se ve al escribirlo. Nada de aquí está escrito a mano imitando el resultado.
 */
@Composable
private fun AyudaLinea(
    fila: AyudaFila,
    monospace: Boolean,
    palette: NotePalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 7.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = fila.marca,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (monospace) FontWeight.Normal else FontWeight.Bold
        )
        Text(
            text = noteAnnotated(fila.ejemplo, NoteFormat.PLAIN, palette),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
