@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.HomeSection

/**
 * Qué sale en Inicio, en qué orden, y qué cuenta la tarjeta grande.
 *
 * **Esta pantalla estaba mal hecha y se rehízo entera.** Tenía tres problemas a la vez:
 *
 * 1. **El asa de arrastre no funcionaba bien.** Vive dentro de una tarjeta, dentro de un
 *    `item` de una lista perezosa, y el gesto se peleaba con el scroll de la pantalla: se
 *    arrastraba y la lista se iba, o no se movía nada. Aquí el orden se cambia con **dos
 *    flechas**, que no compiten con ningún otro gesto, funcionan con el pulgar en cualquier
 *    parte de la fila y las lee TalkBack.
 * 2. **No se veía el resultado.** Se encendían y apagaban cuatro cosas a ciegas y había que
 *    salir a Inicio para saber cómo quedaba. Ahora la ventana de arriba enseña el inicio como
 *    va a quedar, con los bloques en el orden elegido.
 * 3. **Estaba desordenada:** el saludo mezclado con los bloques ordenables aunque no se puede
 *    mover, y los tres interruptores del hero como un grupo suelto sin decir de quién dependen.
 */
@Composable
fun HomeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val appearance = current.appearancePreferences
    val orden = appearance.homeSectionOrder

    LargeTitleScaffold(
        title = "Tu inicio",
        subtitle = "Qué bloques salen, en qué orden y qué cuentan",
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item {
            VentanaDeInicio(
                nombre = current.preferredName.takeIf { it.isNotBlank() } ?: "Estudiante",
                appearance = appearance
            )
        }

        item {
            Text("EL SALUDO", style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
        }
        item {
            // Fuera de la lista ordenable: el saludo **siempre encabeza**, y tenerlo dentro con
            // un asa que no movía nada era prometer algo que no se podía hacer.
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilaDeBloque(
                    titulo = "Saludo y tu nombre",
                    detalle = "«Buenas tardes» y cómo te llamas. Siempre va lo primero.",
                    marcado = appearance.showHomeGreeting,
                    onCambio = { valor -> viewModel.updateAppearance { it.copy(showHomeGreeting = valor) } }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BLOQUES, EN ORDEN",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${orden.count(appearance::showsSection)} de ${orden.size} encendidos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        itemsIndexedOrden(orden) { posicion, seccion ->
            BloqueOrdenable(
                posicion = posicion,
                total = orden.size,
                seccion = seccion,
                marcado = appearance.showsSection(seccion),
                onCambio = { valor ->
                    viewModel.updateAppearance { prefs ->
                        when (seccion) {
                            HomeSection.HERO -> prefs.copy(showHomeHero = valor)
                            HomeSection.AGENDA -> prefs.copy(showHomeAgenda = valor)
                            HomeSection.SNAPSHOT -> prefs.copy(showHomeSnapshot = valor)
                        }
                    }
                },
                onMover = { desde, hasta ->
                    viewModel.updateAppearance { prefs ->
                        prefs.copy(
                            homeSectionOrder = prefs.homeSectionOrder.toMutableList()
                                .apply { add(hasta, removeAt(desde)) }
                        )
                    }
                }
            )
        }

        if (appearance.showHomeHero) {
            item {
                Text(
                    "QUÉ CUENTA «LO SIGUIENTE»",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item {
                Text(
                    text = "La tarjeta grande enseña lo más urgente de lo que dejes encendido. " +
                        "Con los tres apagados se queda con las clases del día.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        FilaDeBloque(
                            titulo = "Notas",
                            detalle = "Materias en riesgo y promedios que bajan",
                            marcado = appearance.heroShowsGrades,
                            onCambio = { valor -> viewModel.updateAppearance { it.copy(heroShowsGrades = valor) } }
                        )
                        FilaDeBloque(
                            titulo = "Tareas",
                            detalle = "Entregas vencidas y las que vencen hoy",
                            marcado = appearance.heroShowsTasks,
                            onCambio = { valor -> viewModel.updateAppearance { it.copy(heroShowsTasks = valor) } }
                        )
                        FilaDeBloque(
                            titulo = "Gastos",
                            detalle = "Cuando te pasas del presupuesto del mes",
                            marcado = appearance.heroShowsExpenses,
                            onCambio = { valor -> viewModel.updateAppearance { it.copy(heroShowsExpenses = valor) } }
                        )
                    }
                }
            }
        }
    }
}

/** `itemsIndexed` sobre el orden, con clave estable para que las flechas no repinten de más. */
private inline fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedOrden(
    orden: List<HomeSection>,
    crossinline fila: @Composable (Int, HomeSection) -> Unit
) {
    orden.forEachIndexed { indice, seccion ->
        item(key = "bloque-${seccion.name}") { fila(indice, seccion) }
    }
}

/**
 * Un bloque con su posición, su interruptor y las dos flechas para moverlo.
 *
 * Las flechas y no un asa: el asa vivía dentro de una lista perezosa que ya arrastra en
 * vertical, así que el gesto se peleaba con el scroll —se arrastraba y la pantalla se iba—.
 * Dos botones no compiten con nada, se pulsan con el pulgar sin apuntar, y TalkBack los lee.
 *
 * La primera fila no lleva flecha de subir y la última no lleva la de bajar: una flecha que no
 * hace nada es peor que no tenerla.
 */
@Composable
private fun BloqueOrdenable(
    posicion: Int,
    total: Int,
    seccion: HomeSection,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
    onMover: (Int, Int) -> Unit
) {
    val atenuado by animateFloatAsState(if (marcado) 1f else 0.5f, label = "bloque")
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cleanClickable { onCambio(!marcado) }
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            // El número dice en qué puesto va, que es lo que las flechas cambian.
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${posicion + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f).alpha(atenuado)) {
                Text(
                    seccion.label(),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    seccion.detail(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FlechaDeOrden(
                    icono = Icons.Rounded.KeyboardArrowUp,
                    descripcion = "Subir ${seccion.label()}",
                    activa = posicion > 0,
                    onClick = { onMover(posicion, posicion - 1) }
                )
                FlechaDeOrden(
                    icono = Icons.Rounded.KeyboardArrowDown,
                    descripcion = "Bajar ${seccion.label()}",
                    activa = posicion < total - 1,
                    onClick = { onMover(posicion, posicion + 1) }
                )
            }
            UniSwitch(checked = marcado, onCheckedChange = onCambio)
        }
    }
}

@Composable
private fun FlechaDeOrden(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    activa: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = activa,
        modifier = Modifier.size(30.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Icon(icono, contentDescription = descripcion, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun FilaDeBloque(
    titulo: String,
    detalle: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable { onCambio(!marcado) }
            .padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmallEmphasized)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        UniSwitch(checked = marcado, onCheckedChange = onCambio)
    }
}

/**
 * El inicio como va a quedar, dentro de una ventana.
 *
 * Es lo que faltaba: se encendían y apagaban cuatro bloques a ciegas y para saber el resultado
 * había que salir de ajustes. Los bloques se pintan **en el orden elegido**, así que subir uno
 * con las flechas se ve aquí mismo.
 */
@Composable
private fun VentanaDeInicio(nombre: String, appearance: AppearancePreferences) {
    val esquema = MaterialTheme.colorScheme
    val secciones = LocalSectionColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(esquema.background)
            .border(1.dp, esquema.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(esquema.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(esquema.error))
            Text("INICIO", style = SectionLabelStyle, color = esquema.onSurfaceVariant)
        }
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            if (appearance.showHomeGreeting) {
                Column {
                    Text(greetingForNow().uppercase(), style = SectionLabelStyle, color = esquema.primary)
                    Text(
                        nombre,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // En el orden elegido: es lo que hacen las flechas de abajo.
            appearance.homeSectionOrder.forEach { seccion ->
                if (!appearance.showsSection(seccion)) return@forEach
                when (seccion) {
                    HomeSection.HERO -> BloqueHero(appearance)
                    HomeSection.AGENDA -> BloqueAgenda()
                    HomeSection.SNAPSHOT -> BloqueCifras(secciones.expenses)
                }
            }
            if (!appearance.showHomeGreeting && appearance.homeSectionOrder.none(appearance::showsSection)) {
                Text(
                    "Inicio se queda solo con el logo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = esquema.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun BloqueHero(appearance: AppearancePreferences) {
    // El texto sale de lo que esté encendido: es la forma de ver que los tres interruptores
    // de abajo mandan de verdad en lo que cuenta la tarjeta.
    val linea = when {
        appearance.heroShowsTasks -> "Entrega de Cálculo III vence hoy"
        appearance.heroShowsGrades -> "Física II va en 2,8: en riesgo"
        appearance.heroShowsExpenses -> "Llevas $412.000 de $400.000"
        else -> "Cálculo III a las 10:00"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("LO SIGUIENTE", style = SectionLabelStyle)
            Text(
                linea,
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BloqueAgenda() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("HOY", style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
            listOf("10:00 · Cálculo III", "14:00 · Física II").forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ColumnScope.BloqueCifras(rojo: androidx.compose.ui.graphics.Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("4,25" to "Promedio", "3" to "Pendientes", "$412k" to "Gastado").forEachIndexed { indice, (cifra, rotulo) ->
            Surface(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        cifra,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = if (indice == 2) rojo else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
