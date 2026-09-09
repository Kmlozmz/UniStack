@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_home.presentation

import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.feature_profile.presentation.AccountAvatar

internal data class DrawerPanelAction(
    val icon: ImageVector,
    val title: String,
    val accent: Color,
    val badge: String? = null,
    /** Nulo cuando la función todavía no existe: la fila se pinta apagada y no responde. */
    val onClick: (() -> Unit)? = null
)

@Composable
internal fun HomeNavigationPanel(
    displayName: String,
    educationLine: String,
    photoUrl: String?,
    onWorksClick: () -> Unit,
    onGpaClick: () -> Unit,
    onQuickNotesClick: () -> Unit,
    onResourcesClick: () -> Unit,
    onWhatsNewClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAiClick: () -> Unit,
    onLabsClick: () -> Unit
) {
    /*
     * El panel es la app, no una tarjeta encima de la app.
     *
     * Iba como una tarjeta flotante: fondo de tarjeta, borde propio, ocho dp de separación por
     * la derecha y las dos esquinas de ese lado redondeadas. Eso lo dejaba suspendido sobre
     * Inicio, sin pertenecer a nada —de ahí que se sintiera fuera de sitio—. Ahora usa el fondo
     * de la app, llega hasta el borde y solo redondea la esquina que asoma, que es la que
     * necesita forma para no cortar en seco.
     */
    val panelShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)

    /*
     * Lo que no está terminado solo se abre en dev y alpha.
     *
     * En una beta, las pantallas a medio hacer llegan apagadas y con su etiqueta: quien la usa
     * para su semestre no tiene por qué toparse con algo incompleto, y así las pruebas de
     * verdad se hacen donde toca. En dev y alpha se abre todo, que es para lo que están.
     *
     * Se marcan sin `onClick`: la fila se pinta apagada, sin responder al toque, en vez de
     * cerrar el panel como si la app hubiera fallado.
     */
    val unfinished = BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished

    /*
     * Un color por fila, no un color por grupo.
     *
     * Las cinco herramientas iban en morado y el resto repartía tonos de sección, así que media
     * lista era del mismo color y la otra media parecía apagada. Con la paleta entera —los
     * cuatro tonos de sección más el primario y el terciario— cada fila se reconoce por su
     * mancha antes de leer el nombre, que es para lo que sirve un icono en un menú.
     *
     * Dentro de cada grupo no se repite ninguno. Entre grupos sí, y a propósito: hay seis tonos
     * legibles y nueve filas, y forzar un séptimo saldría de la paleta o del gris.
     */
    val tools = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.Description,
            title = stringResource(R.string.home_nav_assignments),
            accent = LocalSectionColors.current.expenses,
            badge = if (unfinished) null else stringResource(R.string.home_nav_soon),
            onClick = onWorksClick.takeIf { unfinished }
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Calculate,
            // «GPA» es el promedio del sistema estadounidense, sobre cuatro puntos. Esta
            // calculadora trabaja con la escala que tengas puesta —cien, veinte, diez—, así
            // que el nombre prometía otra cosa, y encima en un idioma que no es el de la app.
            title = stringResource(R.string.home_nav_gpa_calculator),
            accent = MaterialTheme.colorScheme.primary,
            onClick = onGpaClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.EditNote,
            title = stringResource(R.string.home_nav_quick_notes),
            accent = LocalSectionColors.current.atRisk,
            onClick = onQuickNotesClick
        ),
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            title = stringResource(R.string.home_nav_resources),
            accent = LocalSectionColors.current.onTrack,
            onClick = onResourcesClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.AutoAwesome,
            title = stringResource(R.string.home_nav_ai),
            accent = MaterialTheme.colorScheme.tertiary,
            badge = if (unfinished) null else stringResource(R.string.home_nav_soon),
            onClick = onAiClick.takeIf { unfinished }
        )
    )

    /*
     * Qué es esto y qué trae.
     *
     * Perfil se cayó de aquí: vive en Ajustes, que es su sitio —ahí está el resto de lo
     * que se configura— y el panel se quedó con la regla que lo definió desde el
     * principio: solo lo que no tiene otra puerta. El retrato de la cabecera sigue diciendo
     * de quién es la app.
     *
     * Labs cierra el grupo, y UniStack AI cierra el de arriba: las dos apagadas y con su
     * etiqueta, como Trabajos. Estuvieron un rato saliendo solo en dev y alpha —la idea
     * era no ocupar fila con lo que no existe— y así lo que se pierde es el aviso de que
     * vienen. Una fila apagada que dice «Pronto» informa; una fila que no está, no.
     */
    val about = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.RocketLaunch,
            title = stringResource(R.string.home_nav_whats_new),
            accent = LocalSectionColors.current.schedule,
            onClick = onWhatsNewClick
        ),
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.Help,
            title = stringResource(R.string.home_nav_help_support),
            accent = LocalSectionColors.current.expenses,
            onClick = onHelpClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Info,
            title = stringResource(R.string.home_nav_about),
            accent = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onAboutClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Science,
            title = stringResource(R.string.home_nav_labs),
            accent = LocalSectionColors.current.atRisk,
            badge = if (unfinished) null else stringResource(R.string.home_nav_soon),
            onClick = onLabsClick.takeIf { unfinished }
        )
    )

    val sectionTools = stringResource(R.string.home_nav_section_tools)
    val sectionAccountApp = stringResource(R.string.home_nav_section_account_app)

    /*
     * Sin `ModalDrawerSheet`: la superficie es nuestra y llega a los dos bordes.
     *
     * La hoja de Material aplica `DrawerDefaults.windowInsets` por dentro, y en esta versión no
     * hay parámetro para quitarlos. El resultado era un cajón que empezaba por debajo de la
     * barra de estado: entre el reloj y el panel asomaba Inicio, y el panel se leía cortado por
     * arriba en vez de cubrir la pantalla.
     *
     * Envolver una `Surface` propia dentro de la hoja no arreglaba nada —el hueco lo dejaba la
     * hoja, no lo de dentro— y además la hoja ya no aportaba nada: su color iba en transparente
     * y su forma repetida. `drawerContent` acepta cualquier composable, así que va la superficie
     * a secas, de arriba abajo, y el contenido esquiva las barras por su cuenta más abajo.
     */
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            // Un poco más estrecho: el trozo de Inicio que queda a la vista es lo que recuerda
            // de dónde vienes, y con el 92% no quedaba nada que ver.
            .fillMaxWidth(0.86f)
            .widthIn(max = 340.dp),
        shape = panelShape,
        color = MaterialTheme.colorScheme.background,
        // Sin borde: un contorno alrededor lo volvía a convertir en una lámina pegada
        // encima. La sombra del propio cajón ya lo separa de lo que hay detrás.
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        // El fondo llega a los bordes; lo que esquiva las barras es lo que se lee.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 18.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                DrawerPanelHeader(
                    displayName = displayName,
                    educationLine = educationLine,
                    photoUrl = photoUrl
                )
            }
            item { DrawerPanelDivider() }
            drawerSection(sectionTools, tools)
            drawerSection(sectionAccountApp, about)
        }
        /*
         * La versión, al pie del cajón y no detrás de la última fila.
         *
         * Dentro de la lista se colocaba justo debajo de Labs, a media altura de la pantalla,
         * como una fila más a la que se le hubiera olvidado el nombre. Es un pie: va abajo,
         * separada de lo que se toca, y por encima de la barra de navegación.
         */
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 30.dp, top = 8.dp, bottom = 16.dp)
        )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.drawerSection(
    title: String,
    actions: List<DrawerPanelAction>
) {
    item { DrawerPanelSection(title) }
    actions.forEach { action ->
        item { DrawerPanelItem(action = action) }
    }
}

/**
 * Quién eres, y nada más.
 *
 * Antes ponía «Centro UniStack» encima de tu nombre —un nombre para el propio panel, que nadie
 * necesita— y un chip con la cuenta de materias que llevaba a la pestaña Académico, a un toque
 * de distancia por abajo. Queda el retrato, el nombre y en qué estás estudiando.
 */
@Composable
private fun DrawerPanelHeader(
    displayName: String,
    educationLine: String,
    photoUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 6.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountAvatar(
            photoUrl = photoUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            initial = displayName.firstOrNull()?.uppercase()
        )
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLargeEmphasized,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (educationLine.isNotBlank()) {
                // Dos líneas, no una: el panel no tiene el alto contado -- lo que apretaba
                // «Universidad del Atlántico» era el ancho fijo del cajón, no falta de sitio
                // debajo. Con dos líneas cabe entera casi siempre, sin cortar a media palabra.
                Text(
                    text = educationLine,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DrawerPanelDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun DrawerPanelSection(text: String) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 30.dp, top = 18.dp, bottom = 6.dp)
    )
}

/**
 * Una fila del cajón: media pastilla, icono y nombre.
 *
 * Iban como ocho tarjetas con borde, icono dentro de su cuadro de color, título, subtítulo y
 * flecha. Ocho bloques idénticos de 58 dp, uno detrás de otro, se leen como una pared y no como
 * un menú. Esta es la fila de cajón que describe Material 3: pastilla de 56, el icono suelto en
 * el color de su grupo y el nombre.
 *
 * Sin subtítulo: con ocho nombres claros la segunda línea solo añade ruido —«Recursos» no gana
 * nada con «Biblioteca y enlaces útiles» debajo—. Sin flecha: todas llevan a algún sitio, así
 * que marcarlo ocho veces no informa de nada.
 *
 * Ninguna sale marcada como activa, y no es un olvido: el panel solo se abre desde Inicio, así
 * que nunca estás en uno de sus destinos cuando lo abres.
 */
@Composable
private fun DrawerPanelItem(action: DrawerPanelAction) {
    val enabled = action.onClick != null
    Surface(
        onClick = action.onClick ?: {},
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            // El icono va sobre su propia mancha de color: suelto sobre el fondo del panel
            // se leía apagado, y con nueve filas seguidas el color apenas se notaba.
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enabled) action.accent.copy(alpha = 0.16f) else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = if (enabled) action.accent else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(21.dp)
                )
            }
            Text(
                text = action.title,
                modifier = Modifier.weight(1f),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                },
                style = MaterialTheme.typography.bodyLargeEmphasized,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            action.badge?.let { DrawerBadge(text = it) }
        }
    }
}

@Composable
private fun DrawerBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            maxLines = 1
        )
    }
}
