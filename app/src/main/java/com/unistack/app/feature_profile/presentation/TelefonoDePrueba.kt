@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.formaDeDistintivo
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.navigation.BottomNavItem
import com.unistack.app.core.navigation.iconFor
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.ButtonShapeStyle
import com.unistack.app.feature_user.domain.ButtonSizeStyle
import com.unistack.app.feature_user.domain.ChipStyle
import com.unistack.app.feature_user.domain.ProgressShape
import com.unistack.app.feature_user.domain.ShadowIntensity
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.TextFieldStyle

/**
 * **La vista previa en vivo**: la app entera dentro de un teléfono, como en el diseño aprobado.
 *
 * Esto sustituye a las muestras sueltas —una para la superficie, otra para los chips, otra para
 * los distintivos—. El problema de tenerlas repartidas es que cada ajuste enseñaba su trocito y
 * ninguno enseñaba **el conjunto**, que es lo que de verdad se elige: un fondo con filete fino
 * y esquinas rectas no se juzga en una tarjeta suelta, se juzga viendo si la pantalla entera
 * respira o se agobia.
 *
 * Lleva las mismas piezas que el diseño: la tarjeta grande de arriba con lo primero de mañana,
 * la tarjeta de materias con su interruptor, tres notas con su distintivo y su color, la barra
 * de progreso, los botones y el chip, el campo de búsqueda, las tres cifras, el botón de crear
 * y la barra de abajo. Todas se pintan con lo que haya elegido: cambia un ajuste y cambia el
 * teléfono, no una aproximación suya.
 *
 * Va arriba del todo en cada puerta de Apariencia y se queda a la vista mientras se toca lo de
 * abajo. Debajo lleva una línea que dice en palabras qué hay puesto ahora mismo, que es lo que
 * deja comprobar de un vistazo si el ajuste que se acaba de tocar llegó a algún sitio.
 */
@Composable
fun TelefonoDePrueba(modifier: Modifier = Modifier) {
    val esquema = MaterialTheme.colorScheme
    val apariencia = LocalAppearancePreferences.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "VISTA PREVIA EN VIVO",
            style = SectionLabelStyle,
            color = esquema.outline,
            modifier = Modifier.padding(start = 2.dp, bottom = 7.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                // El marco negro del teléfono: es lo que separa «la app» de «los ajustes».
                .shadow(10.dp, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            color = Color.Black
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = esquema.background
                ) {
                    Column {
                        BarraDeEstadoFalsa()
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroDePrueba()
                            TarjetaDeMateriasDePrueba()
                            CifrasDePrueba()
                            BotonDeCrearDePrueba()
                        }
                        BarraDeAbajoDePrueba()
                    }
                }
            }
        }
        Text(
            text = resumenDeAjustes(apariencia.surfaceStyle, apariencia.shadowIntensity),
            style = MaterialTheme.typography.bodySmall,
            color = esquema.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp)
        )
    }
}

/** La hora y los puntos de arriba: sin esto el marco no se lee como un teléfono. */
@Composable
private fun BarraDeEstadoFalsa() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val tenue = MaterialTheme.colorScheme.onSurfaceVariant
        Text("07:45", style = MaterialTheme.typography.labelSmall, color = tenue)
        Text("▪ ▪ ▪", style = MaterialTheme.typography.labelSmall, color = tenue)
    }
}

/** La tarjeta grande de Inicio: lo primero de mañana, con su botón. */
@Composable
private fun HeroDePrueba() {
    val esquema = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = esquema.primary,
        contentColor = esquema.onPrimary
    ) {
        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("● LO PRIMERO DE MAÑANA", style = SectionLabelStyle)
            Text(
                "Estados Financieros a las 18:30",
                style = MaterialTheme.typography.titleMediumEmphasized,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Revisa aula y asistencia.",
                style = MaterialTheme.typography.bodySmall
            )
            BotonDePrueba(
                texto = "Ver horario",
                relleno = esquema.onPrimary,
                tinta = esquema.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/**
 * La tarjeta de materias: interruptor, tres notas, progreso, botones, chip y campo.
 *
 * Es la que más ajustes toca a la vez —superficie, esquinas, densidad, distintivos, colores por
 * sección, decimales, separadores, progreso, forma y tamaño de botón, chips y campos— y por eso
 * va entera y no partida: es donde se ve si todo junto queda bien.
 */
@Composable
private fun TarjetaDeMateriasDePrueba() {
    val esquema = MaterialTheme.colorScheme
    val secciones = LocalSectionColors.current
    val a = LocalAppearancePreferences.current
    val notas = listOf(
        Triple("estados", "Estados Financieros", 4.2),
        Triple("matefin", "Matemática Fin.", 3.4),
        Triple("costos", "Costos II", 2.8)
    )

    TarjetaDePrueba {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tus materias",
                style = MaterialTheme.typography.titleSmallEmphasized,
                modifier = Modifier.weight(1f)
            )
            UniSwitch(checked = true, onCheckedChange = {})
        }
        notas.forEachIndexed { indice, (id, nombre, valor) ->
            // El color dice cómo va: verde al día, ámbar justo, rojo en riesgo. Con «colores
            // por sección» apagado, los tres van del acento y el número es quien lo cuenta.
            val color = when {
                !a.sectionColorsEnabled -> esquema.primary
                valor >= 4.0 -> secciones.onTrack
                valor >= 3.0 -> secciones.expenses
                else -> secciones.atRisk
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(formaDeDistintivo(a.badgeShape, id))
                        .background(color)
                )
                Text(
                    nombre,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "%.${a.decimalPlaces}f".format(valor).replace('.', ','),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = color
                )
            }
            if (a.listDividers && indice < notas.lastIndex) {
                HorizontalDivider(color = esquema.outlineVariant)
            }
        }
        if (a.academicProgressShape == ProgressShape.WAVY) {
            LinearWavyProgressIndicator(progress = { 0.62f }, modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(progress = { 0.62f }, modifier = Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            BotonDePrueba("Guardar", esquema.primary, esquema.onPrimary)
            BotonDePrueba("Cancelar", Color.Transparent, esquema.primary, contorno = esquema.primary)
            ChipDePrueba("Filtro")
        }
        CampoDePrueba()
    }
}

/** Las tres cifras de Inicio: promedio, pendientes y gasto de la semana. */
@Composable
private fun CifrasDePrueba() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("PROMEDIO" to "3,5", "PENDIENTES" to "2", "SEMANA" to "$61k").forEach { (rotulo, valor) ->
            TarjetaDePrueba(modifier = Modifier.weight(1f), aire = 9.dp) {
                Text(rotulo, style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(valor, style = MaterialTheme.typography.titleMediumEmphasized)
            }
        }
    }
}

/** El botón de crear, en la esquina, con la forma y el tamaño elegidos. */
@Composable
private fun BotonDeCrearDePrueba() {
    val esquema = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Row(
            modifier = Modifier
                .clip(formaDeBoton())
                .background(esquema.primary)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = esquema.onPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Registrar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = esquema.onPrimary
            )
        }
    }
}

/**
 * La barra de abajo del teléfono de prueba, con las cinco pestañas de verdad.
 *
 * Usa [BottomNavItem] y su [iconFor], así que el estilo de icono —redondeado, lineal o
 * relleno— y el de rótulos se ven aquí exactamente como se van a ver abajo del todo.
 */
@Composable
private fun BarraDeAbajoDePrueba() {
    val esquema = MaterialTheme.colorScheme
    val a = LocalAppearancePreferences.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(esquema.surfaceContainer)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.items.forEachIndexed { indice, item ->
            val activo = indice == 0
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(20.dp)
                        .clip(CircleShape)
                        // La pastilla del activo, con el mismo color que la barra real.
                        .background(if (activo) esquema.secondaryContainer else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.iconFor(activo, a.iconStyle),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (activo) esquema.onSecondaryContainer else esquema.onSurfaceVariant
                    )
                }
                if (a.bottomBarStyle == BottomBarStyle.LABELED) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (activo) esquema.primary else esquema.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ piezas

/**
 * Una tarjeta con la superficie elegida.
 *
 * No usa `UniCard` a propósito: dentro del teléfono hace falta una versión más pequeña —el
 * relleno de la app entera dentro de trescientos píxeles quedaría hueco— pero la decisión de
 * qué hace cada superficie sale del mismo sitio, así que las dos se mueven juntas.
 */
@Composable
private fun TarjetaDePrueba(
    modifier: Modifier = Modifier,
    aire: androidx.compose.ui.unit.Dp = 11.dp,
    contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val esquema = MaterialTheme.colorScheme
    val a = LocalAppearancePreferences.current
    val forma = MaterialTheme.shapes.medium
    val sombra = when {
        a.surfaceStyle != SurfaceStyle.ELEVATED -> 0.dp
        a.shadowIntensity == ShadowIntensity.SUAVE -> 2.dp
        a.shadowIntensity == ShadowIntensity.FUERTE -> 10.dp
        else -> 5.dp
    }
    val grosor = when {
        a.surfaceStyle == SurfaceStyle.OUTLINED -> when (a.outlineWeight) {
            com.unistack.app.feature_user.domain.OutlineWeight.FINO -> 1.dp
            com.unistack.app.feature_user.domain.OutlineWeight.MEDIO -> 1.5.dp
            else -> 2.5.dp
        }
        a.surfaceStyle == SurfaceStyle.TRANSLUCENT -> 1.dp
        else -> 0.dp
    }
    Column(
        modifier = modifier
            .then(if (sombra > 0.dp) Modifier.shadow(sombra, forma) else Modifier)
            .clip(forma)
            .background(
                if (a.surfaceStyle == SurfaceStyle.TRANSLUCENT) {
                    esquema.surfaceContainer.copy(alpha = 0.55f)
                } else {
                    esquema.surfaceContainer
                }
            )
            .then(
                if (grosor > 0.dp) Modifier.border(grosor, esquema.outlineVariant, forma) else Modifier
            )
            .padding(aire),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = contenido
    )
}

@Composable
private fun BotonDePrueba(
    texto: String,
    relleno: Color,
    tinta: Color,
    modifier: Modifier = Modifier,
    contorno: Color? = null
) {
    val a = LocalAppearancePreferences.current
    val alto = when (a.buttonSize) {
        ButtonSizeStyle.PEQUENO -> 5.dp
        ButtonSizeStyle.MEDIO -> 8.dp
        ButtonSizeStyle.GRANDE -> 11.dp
    }
    Box(
        modifier = modifier
            .clip(formaDeBoton())
            .background(relleno)
            .then(if (contorno != null) Modifier.border(1.5.dp, contorno, formaDeBoton()) else Modifier)
            .padding(horizontal = alto + 6.dp, vertical = alto)
    ) {
        Text(texto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = tinta)
    }
}

@Composable
private fun formaDeBoton() = when (LocalAppearancePreferences.current.buttonShape) {
    ButtonShapeStyle.RECTO -> RoundedCornerShape(5.dp)
    ButtonShapeStyle.MEDIO -> RoundedCornerShape(12.dp)
    ButtonShapeStyle.PASTILLA -> RoundedCornerShape(percent = 50)
}

@Composable
private fun ChipDePrueba(texto: String) {
    val esquema = MaterialTheme.colorScheme
    val estilo = LocalAppearancePreferences.current.chipStyle
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .then(
                when (estilo) {
                    ChipStyle.RELLENO -> Modifier.background(esquema.primary.copy(alpha = 0.20f))
                    ChipStyle.FILETE -> Modifier.border(1.dp, esquema.outlineVariant, RoundedCornerShape(percent = 50))
                    ChipStyle.TEXTO -> Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (estilo != ChipStyle.TEXTO) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = esquema.primary
            )
        }
        Text(texto, style = MaterialTheme.typography.labelMedium, color = esquema.primary)
    }
}

@Composable
private fun CampoDePrueba() {
    val esquema = MaterialTheme.colorScheme
    val estilo = LocalAppearancePreferences.current.textFieldStyle
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when (estilo) {
                        TextFieldStyle.RELLENO -> Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(esquema.surfaceContainerHighest)
                        TextFieldStyle.FILETE -> Modifier
                            .clip(MaterialTheme.shapes.small)
                            .border(1.5.dp, esquema.outline, MaterialTheme.shapes.small)
                        TextFieldStyle.SUBRAYADO -> Modifier
                    }
                )
                .padding(horizontal = 11.dp, vertical = 9.dp)
        ) {
            Text("Buscar…", style = MaterialTheme.typography.bodySmall, color = esquema.onSurfaceVariant)
        }
        if (estilo == TextFieldStyle.SUBRAYADO) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(esquema.outline)
            )
        }
    }
}

/**
 * La línea que dice en palabras qué hay puesto.
 *
 * Es lo que deja comprobar que un ajuste llegó a algún sitio sin tener que fiarse de la vista:
 * si dice «con filete» y la tarjeta no tiene filete, el ajuste está roto y se sabe al momento.
 */
private fun resumenDeAjustes(superficie: SurfaceStyle, sombra: ShadowIntensity): String = buildString {
    append("Superficie ")
    append(
        when (superficie) {
            SurfaceStyle.FLAT -> "plana"
            SurfaceStyle.OUTLINED -> "con filete"
            SurfaceStyle.ELEVATED -> "con sombra " + when (sombra) {
                ShadowIntensity.SUAVE -> "suave"
                ShadowIntensity.MEDIA -> "media"
                ShadowIntensity.FUERTE -> "fuerte"
            }
            SurfaceStyle.TRANSLUCENT -> "de cristal"
        }
    )
    append(". Todo lo que toques abajo se ve aquí arriba al momento.")
}
