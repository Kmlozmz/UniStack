@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniChoiceRow
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.AppTheme
import com.unistack.app.core.design.theme.AppThemes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AccentIntensity
import com.unistack.app.feature_user.domain.AccentStyle

/**
 * El tema, entero: fondo, tarjetas, tinta y acento a la vez.
 *
 * Vive en su propia pantalla y no dentro de Apariencia porque veintiocho temas mas el acento
 * mas la intensidad no caben en una fila de un hub sin convertirla en un scroll interminable.
 *
 * **Aqui se arregla algo que llevaba tiempo roto:** `accentStyle` guardaba cinco colores de los
 * que solo pintaba «del fondo», y `accentIntensity` no pintaba ninguno. Se elegian, se
 * guardaban y viajaban en la copia de seguridad sin cambiar un pixel.
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
    val dinamico = appearance.accentStyle == AccentStyle.DYNAMIC

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
                subtitle = "${AppThemes.catalog.size} temas, y el acento por encima",
                onBackClick = onBackClick
            )
        }

        /*
         * El modo entra aqui, y no se queda en el hub de Apariencia.
         *
         * Claro y oscuro deciden la mitad de lo que decide un tema —de que color es el fondo—,
         * asi que tenerlos en dos pantallas distintas obligaba a ir y volver para juzgar el
         * resultado: se elegia Catppuccin Latte arriba y habia que salir a poner «Claro» abajo
         * para verlo como es. Juntos, se elige y se ve.
         */
        item {
            Text(
                text = "MODO",
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            UniSegmentedControl(
                // El modo personalizado no entra: no hay forma de configurarlo desde ninguna
                // pantalla, asi que como quinto segmento seria un boton que no lleva a nada.
                // Quien lo tuviera guardado ve «Sistema» marcado y puede salir de ahi.
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
                text = "DEL FONDO DE PANTALLA",
                modifier = Modifier.padding(top = 6.dp),
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            UniChoiceRow(
                selected = dinamico,
                options = listOf(
                    UniSegmentedOption(value = false, label = "Un tema"),
                    UniSegmentedOption(value = true, label = "Del fondo")
                ),
                onSelected = { usarDinamico ->
                    viewModel.updateAppearance {
                        it.copy(accentStyle = if (usarDinamico) AccentStyle.DYNAMIC else AccentStyle.VIOLET)
                    }
                }
            )
        }
        item {
            Text(
                text = if (dinamico) {
                    "La app toma los colores de tu fondo de pantalla. Los temas quedan en pausa mientras esté encendido."
                } else {
                    "Cada tema decide fondo, tarjetas y tinta a la vez, no solo el acento."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!dinamico) {
            item {
                Text(
                    text = "TEMAS",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                /*
                 * Un solo carrusel con los veintiocho.
                 *
                 * Estuvieron partidos por familia, con un carrusel por cada una: Dracula,
                 * Nord y Monokai tienen un tema, asi que eran nueve bandas de las que seis
                 * llevaban una sola tarjeta y ninguna se podia comparar con la de al lado.
                 * La familia se lee en la propia tarjeta, que es donde hace falta.
                 */
                val carousel = rememberCarouselState { AppThemes.catalog.size }
                HorizontalMultiBrowseCarousel(
                    state = carousel,
                    preferredItemWidth = 132.dp,
                    itemSpacing = 8.dp,
                    modifier = Modifier.fillMaxWidth().height(126.dp)
                ) { indice ->
                    val tema = AppThemes.catalog[indice]
                    ThemeCard(
                        tema = tema,
                        elegido = tema.id == appearance.themeId,
                        modifier = Modifier.maskClip(MaterialTheme.shapes.large),
                        onClick = {
                            /*
                             * Cambiar de tema trae la paleta **entera**, acento incluido.
                             *
                             * Con el acento a medida puesto, cambiar de tema dejaba el color
                             * viejo encima del tema nuevo: se elegia Dracula y seguia el
                             * morado anterior, asi que el tema no se veia nunca como es. Si
                             * despues se quiere otro acento, se elige debajo.
                             */
                            viewModel.updateAppearance {
                                it.copy(themeId = tema.id, customAccentColor = null)
                            }
                        }
                    )
                }
            }

            item {
                Text(
                    text = "ACENTO",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                AccentRow(
                    seleccionado = appearance.customAccentColor,
                    delTema = AppThemes.byId(appearance.themeId).accent,
                    onElegir = { color ->
                        viewModel.updateAppearance { it.copy(customAccentColor = color) }
                    }
                )
            }
            item {
                Text(
                    text = "INTENSIDAD",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                UniChoiceRow(
                    selected = appearance.accentIntensity,
                    options = listOf(
                        UniSegmentedOption(value = AccentIntensity.SOFT, label = "Suave"),
                        UniSegmentedOption(value = AccentIntensity.BALANCED, label = "Medio"),
                        UniSegmentedOption(value = AccentIntensity.VIBRANT, label = "Vivo")
                    ),
                    onSelected = { intensidad ->
                        viewModel.updateAppearance { it.copy(accentIntensity = intensidad) }
                    }
                )
            }
        }
    }
}

/** Una miniatura de la app con los colores de ese tema, que es lo que se viene a comparar. */
@Composable
private fun ThemeCard(
    tema: AppTheme,
    elegido: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.large,
        color = tema.background,
        border = if (elegido) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(tema.accent)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(tema.surface)
                )
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(tema.accent.copy(alpha = 0.55f))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(tema.surface)
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tema.name,
                        color = tema.ink,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tema.family.ifEmpty { "UniStack" },
                        color = tema.ink.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                if (elegido) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Elegido",
                        tint = tema.accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/** Doce acentos, mas «el del tema» para volver al que trae puesto. */
@Composable
private fun AccentRow(
    seleccionado: Int?,
    delTema: Color,
    onElegir: (Int?) -> Unit
) {
    val acentos = listOf(
        0xFF7F77DD, 0xFF3F8FE0, 0xFF4FBFA6, 0xFF5FC96E, 0xFFE0A63C, 0xFFE8693A,
        0xFFE062A8, 0xFF8C93A8, 0xFFC08BE0, 0xFFE2564F, 0xFF2FB4C9, 0xFF9B8CF0
    ).map { it.toInt() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AccentDot(color = delTema, elegido = seleccionado == null) { onElegir(null) }
            acentos.take(5).forEach { valor ->
                AccentDot(color = Color(valor), elegido = seleccionado == valor) { onElegir(valor) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            acentos.drop(5).take(6).forEach { valor ->
                AccentDot(color = Color(valor), elegido = seleccionado == valor) { onElegir(valor) }
            }
        }
        Text(
            text = if (seleccionado == null) {
                "Usando el acento propio del tema."
            } else {
                "Acento a medida, encima del tema elegido."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.AccentDot(
    color: Color,
    elegido: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .cleanClickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        if (elegido) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Elegido",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
