@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_notes.domain.NoteFormat

private data class AyudaFila(val marca: String, val ejemplo: String)
private data class AyudaGrupo(val titulo: String, val filas: List<AyudaFila>)

/*
 * Lo que sabe hacer cada manera de escribir, entero.
 *
 * Fue condicion suya: «cada una debe mostrar todo lo que puede hacer y como». Por eso no es una
 * lista de nombres sino dos columnas —lo que se escribe a la izquierda, como queda a la
 * derecha—, y la de la derecha se pinta con el mismo motor que el editor, no con capturas ni
 * con texto imitando el resultado. Si algun dia el formateo cambia, esta pantalla cambia sola.
 */
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
            AyudaFila("[el aula](https://…)", "[el aula](https://ejemplo)")
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
            AyudaFila("```\ncódigo\n```", "```\ncodigo()\n```"),
            AyudaFila("---", "---"),
            AyudaFila("| a | b |", "| Corte | Peso |")
        )
    )
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
    )
)

/**
 * La ayuda del formato, abierta desde la interrogación del editor.
 *
 * Enseña lo del modo que esté puesto y no las dos cosas a la vez: quien escribe en sencillo no
 * necesita saber que existe una almohadilla, y quien escribe en Markdown no necesita que le
 * expliquen un botón que no tiene delante.
 */
@Composable
fun NoteFormatHelpSheet(
    format: NoteFormat,
    onDismiss: () -> Unit
) {
    val palette = rememberNotePalette()
    val grupos = if (format == NoteFormat.MARKDOWN) GRUPOS_MARKDOWN else GRUPOS_SENCILLO

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "cabecera") {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        if (format == NoteFormat.MARKDOWN) "Markdown de apuntes" else "Escritura sencilla",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (format == NoteFormat.MARKDOWN) {
                            "Escribes las marcas y el texto se va formateando solo. A la izquierda " +
                                "lo que se escribe; a la derecha, cómo queda."
                        } else {
                            "Sin marcas a la vista. Selecciona un trozo de texto y aparece la barra " +
                                "con estos botones."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            grupos.forEach { grupo ->
                item(key = "g-" + grupo.titulo) {
                    Text(
                        grupo.titulo,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                item(key = "c-" + grupo.titulo) {
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            grupo.filas.forEach { fila ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = fila.marca,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = if (format == NoteFormat.MARKDOWN) {
                                            FontFamily.Monospace
                                        } else {
                                            FontFamily.Default
                                        },
                                        fontWeight = if (format == NoteFormat.MARKDOWN) {
                                            FontWeight.Normal
                                        } else {
                                            FontWeight.Bold
                                        }
                                    )
                                    Text(
                                        // El resultado se pinta con el motor del editor y con las
                                        // marcas escondidas, que es exactamente lo que se ve.
                                        text = noteAnnotated(fila.ejemplo, NoteFormat.PLAIN, palette),
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "pie") {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            "Las dos guardan lo mismo",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Cambiar de una a otra no convierte nada ni pierde nada: es la misma " +
                                "nota con las marcas a la vista o escondidas. Lo único: en sencillo " +
                                "no hay botón para títulos, tablas ni bloques de código, así que si " +
                                "los escribiste en Markdown se siguen viendo pero no se pueden " +
                                "quitar desde ahí.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
