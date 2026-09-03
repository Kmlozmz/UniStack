@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.AppTheme
import com.unistack.app.core.design.theme.AppThemes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.ThemePalette
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AccentIntensity

/**
 * El tema, entero: fondo, tarjetas, tinta y acento a la vez.
 *
 * Vive en su propia pantalla y no dentro de Apariencia porque veintiocho temas más el modo,
 * el acento y su intensidad no caben en una fila de un hub sin convertirla en un scroll
 * interminable.
 *
 * **Dos cosas que estuvieron rotas y se arreglan aquí:**
 *
 * 1. El modo —claro, oscuro, OLED— no hacía nada salvo en OLED. El tema pintaba una sola
 *    paleta encima, y como casi todos son oscuros, «Claro» quedaba tapado. Ahora cada tema
 *    tiene sus dos caras y el modo dice cuál.
 * 2. El carrusel volvía al principio cada vez que se entraba, así que con el tema veinte
 *    puesto había que arrastrar veinte tarjetas para verlo. Ahora abre en el que está elegido.
 *
 * Y una que se quita: **Monet ya no está**. Con veintiocho temas y un acento a medida, tomar
 * prestada la paleta del fondo de pantalla no añadía nada y sí quitaba —mientras estaba
 * encendido, los temas quedaban en pausa y media pantalla no pintaba.
 */
@Composable
fun ThemeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val appearance = current.appearancePreferences
    val oscuro = LocalIsDarkTheme.current

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsHeader(
                title = "Tema y color",
                subtitle = "${AppThemes.catalog.size} temas, cada uno en claro y en oscuro",
                onBackClick = onBackClick
            )
        }

        item {
            Text("MODO", style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
        }
        item {
            UniSegmentedControl(
                // El modo personalizado no entra: no hay forma de configurarlo desde ninguna
                // pantalla, así que como quinto segmento sería un botón que no lleva a nada.
                selected = current.visualPreference.orSystem(),
                options = VisiblePreferences.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = viewModel::updateVisualPreference,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = current.visualPreference.themeDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text(
                "TEMA",
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        item {
            /*
             * Un solo carrusel con los veintiocho, abierto en el que está puesto.
             *
             * `initialItem` es lo que faltaba: sin él, volver a esta pantalla con Monokai
             * elegido dejaba el carrusel en UniStack y había que arrastrar veinticinco
             * tarjetas para ver cuál estaba marcado.
             *
             * Los tamaños de las pequeñas van más juntos —56 a 72dp en vez del rango ancho de
             * por defecto—: cuanto más estrecho es el margen en el que una tarjeta se encoge,
             * menos «pesado» se siente el arrastre, porque el cambio de tamaño ocurre en menos
             * recorrido y la lista acompaña al dedo en vez de resistirse.
             */
            val carousel = rememberCarouselState(
                initialItem = AppThemes.indexOf(appearance.themeId)
            ) { AppThemes.catalog.size }
            HorizontalMultiBrowseCarousel(
                state = carousel,
                preferredItemWidth = 150.dp,
                itemSpacing = 10.dp,
                minSmallItemWidth = 56.dp,
                maxSmallItemWidth = 72.dp,
                modifier = Modifier.fillMaxWidth().height(140.dp)
            ) { indice ->
                val tema = AppThemes.catalog[indice]
                ThemeCard(
                    tema = tema,
                    oscuro = oscuro,
                    elegido = tema.id == appearance.themeId,
                    modifier = Modifier.maskClip(RoundedCornerShape(18.dp)),
                    onClick = {
                        /*
                         * Cambiar de tema trae la paleta **entera**, acento incluido.
                         *
                         * Con el acento a medida puesto, cambiar de tema dejaba el color viejo
                         * encima del tema nuevo: se elegía Dracula y seguía el morado
                         * anterior, así que el tema no se veía nunca como es.
                         */
                        viewModel.updateAppearance {
                            it.copy(themeId = tema.id, customAccentColor = null)
                        }
                    }
                )
            }
        }
        item {
            val tema = AppThemes.byId(appearance.themeId)
            Text(
                text = buildString {
                    append(tema.name)
                    if (tema.family.isNotEmpty()) append(" · ").append(tema.family)
                    append(" · cada tema decide fondo, tarjetas y tinta a la vez, no solo el acento.")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Text(
                "ACENTO",
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        item {
            // El rótulo por sí solo no decía qué eran los doce puntos. Esta línea sí, y va
            // antes y no después: se lee para qué sirven *antes* de tocar uno.
            Text(
                text = "El color de los botones, los rótulos y lo marcado. Va por encima del tema; " +
                    "el primero es el que trae el tema puesto.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            AccentRow(
                delTema = AppThemes.byId(appearance.themeId).palette(oscuro).accent,
                elegido = appearance.customAccentColor?.let(::Color),
                onElegir = { color ->
                    // Se guarda como ARGB entero, que es lo que lee `Color(Int)` al arrancar.
                    // Empaquetar el `value` de Compose daría otro número y el color volvería
                    // distinto del que se tocó.
                    viewModel.updateAppearance { it.copy(customAccentColor = color?.toArgb()) }
                }
            )
        }

        item {
            Text(
                "INTENSIDAD",
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        item {
            UniSegmentedControl(
                selected = appearance.accentIntensity,
                options = AccentIntensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(accentIntensity = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = "Suave lo aclara, vivo lo satura. Mira los botones de arriba mientras eliges.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Los doce acentos, más el que trae el tema.
 *
 * El primero no es un color fijo: es «el del tema», y se marca cuando no hay ninguno a medida
 * elegido. Sin él no había forma de volver atrás después de tocar un punto.
 */
@Composable
private fun AccentRow(
    delTema: Color,
    elegido: Color?,
    onElegir: (Color?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            listOf(null) + Acentos.take(5),
            Acentos.drop(5)
        ).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { color ->
                    val real = color ?: delTema
                    val marcado = if (color == null) elegido == null else elegido == color
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(real)
                            .border(
                                width = if (marcado) 2.5.dp else 0.dp,
                                color = if (marcado) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .cleanClickable { onElegir(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (marcado) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (real.luminancia() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                repeat(6 - fila.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private fun Color.luminancia(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

/** Once tonos que funcionan sobre fondo claro y oscuro sin cambiar de valor. */
private val Acentos = listOf(
    Color(0xFF7F77DD), Color(0xFF3F8FE0), Color(0xFF2FB4C9), Color(0xFF4FBFA6), Color(0xFF5FC96E),
    Color(0xFFE0A63C), Color(0xFFE8693A), Color(0xFFE2564F), Color(0xFFE062A8), Color(0xFFC08BE0),
    Color(0xFF8C93A8)
)

/**
 * La tarjeta de un tema, pintada como un trozo de app y no como tres barras.
 *
 * Enseña lo que de verdad cambia: el fondo, una tarjeta encima con su tinta, y el acento en
 * una pastilla. Con barras sueltas, dos temas del mismo tono se veían iguales aunque su
 * contraste entre fondo y tarjeta fuera muy distinto, que es lo que se nota al usarlos.
 *
 * La cara que se pinta es la del modo puesto: enseñar el Dracula oscuro mientras la app está
 * en claro sería mentir sobre lo que va a pasar al tocarlo.
 */
@Composable
private fun ThemeCard(
    tema: AppTheme,
    oscuro: Boolean,
    elegido: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p: ThemePalette = tema.palette(oscuro)
    val anillo = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.background)
            /*
             * El anillo de elegido va con **el acento de la app**, no con el del tema.
             *
             * Con el del tema, cada tarjeta se marcaba de un color distinto y no se leia como
             * «este es el elegido» sino como un adorno mas del tema: con el acento verde puesto
             * y Macchiato elegido, el anillo salia morado y no se entendia por que.
             * «Elegido» es lo mismo en toda la app, y en toda la app se marca con el acento.
             */
            .border(
                width = if (elegido) 3.dp else 0.dp,
                color = if (elegido) anillo else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .cleanClickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            // La tarjeta de dentro, con su tinta: es el contraste que se juzga al elegir.
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = p.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(p.ink)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(p.ink.copy(alpha = 0.45f))
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(15.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(p.accent)
                )
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(p.accent.copy(alpha = 0.5f))
                )
                if (elegido) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Elegido",
                        tint = anillo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column {
                Text(
                    text = tema.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = p.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tema.family.ifEmpty { "UniStack" },
                    style = MaterialTheme.typography.labelSmall,
                    color = p.ink.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

internal fun AccentIntensity.label() = when (this) {
    AccentIntensity.SOFT -> "Suave"
    AccentIntensity.BALANCED -> "Medio"
    AccentIntensity.VIBRANT -> "Vivo"
}
