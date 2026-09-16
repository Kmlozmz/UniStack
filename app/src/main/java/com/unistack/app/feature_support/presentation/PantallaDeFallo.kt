package com.unistack.app.feature_support.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.mezclaCon
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.core.fallos.InformeDeFallo
import com.unistack.app.core.utils.Textos
import kotlinx.coroutines.flow.collectLatest

/**
 * Lo que ve el usuario cuando la app se cierra sola. Una pantalla, dos puertas.
 *
 * [enElActo] distingue de cuál de las dos viene, y es lo único que cambia: si el fallo se cazó
 * al vuelo, la pantalla habla en presente y abajo ofrece reiniciar; si se recogió al arrancar,
 * habla del cierre anterior y abajo ofrece seguir. El informe, los botones y el archivo son
 * los mismos, porque son el mismo fallo.
 *
 * **Los textos no adivinan qué estaba haciendo el usuario.** La app rara vez lo sabe con
 * certeza —el fallo puede venir de un aviso en segundo plano, de una corrutina o de tres
 * pantallas atrás— y quedar en ridículo inventándolo es peor que no decirlo. Lo concreto lo
 * pone quien lo vivió, en el campo de texto de la hoja de reporte.
 *
 * **Es la réplica aprobada (opción B, «las cuatro fichas»).** Cada medida de este archivo es un
 * píxel de su CSS, salvo la letra, y [AEscalaDeLaReplica] es quien los lleva al tamaño del
 * teléfono.
 */
@Composable
fun PantallaDeFallo(
    informe: InformeDeFallo,
    enElActo: Boolean,
    /** Qué se ha hecho ya con el archivo, o nulo si todavía nada. */
    estadoDelArchivo: String?,
    onContar: () -> Unit,
    onGuardar: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AEscalaDeLaReplica(ancho = maxWidth, alto = maxHeight) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Esta pantalla la abre una Activity suya, sin andamiaje de ninguna clase:
                    // si no se aparta ella de las barras del sistema, no lo hace nadie.
                    .statusBarsPadding()
            ) {
                /*
                 * El aviso en el centro y las acciones abajo del todo, como en la réplica.
                 *
                 * Plegada, la tarjeta cabe entera: la escala lo garantiza. Al desplegar el texto
                 * completo ya no, y entonces `weight(fill = false)` le da el alto que sobra y se
                 * desplaza por dentro, sin empujar los botones fuera de la pantalla.
                 */
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp, Alignment.CenterVertically)
                ) {
                    CabeceraDelFallo(informe = informe, enElActo = enElActo)
                    TarjetaDelInforme(
                        informe = informe,
                        estadoDelArchivo = estadoDelArchivo,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                /*
                 * Dos en fila y una debajo.
                 *
                 * Los dos de arriba son hermanos —las dos formas de quedarse con el archivo— y el
                 * de abajo es el contrario: irse sin hacer nada. La fila los agrupa como lo que
                 * son. Las etiquetas son cortas a propósito: a media anchura, con el icono
                 * delante, «Contar qué pasó» no cabía. Lo largo se dice en la hoja que abre.
                 *
                 * Abajo quedan los 16 px de la réplica hasta el borde, salvo que la barra del
                 * sistema pida más: `union` se queda con el mayor. Con gestos la píldora cabe
                 * dentro de ese margen; con los tres botones, manda la barra.
                 */
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.navigationBars.union(WindowInsets(bottom = 16.dp))
                        )
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        BotonDelFallo(
                            texto = Textos.get(R.string.fallo_btn_contar),
                            onClick = onContar,
                            tipo = TipoDeBoton.Relleno,
                            icono = Icons.AutoMirrored.Filled.Send,
                            modifier = Modifier.weight(1f)
                        )
                        BotonDelFallo(
                            texto = Textos.get(R.string.fallo_btn_guardar),
                            onClick = onGuardar,
                            tipo = TipoDeBoton.Tonal,
                            icono = Icons.Rounded.SaveAlt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    BotonDelFallo(
                        texto = if (enElActo) {
                            Textos.get(R.string.fallo_btn_reiniciar)
                        } else {
                            Textos.get(R.string.fallo_btn_seguir)
                        },
                        onClick = onSalir,
                        tipo = TipoDeBoton.Contorno,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** El ancho de la pantalla de la réplica, sin el marco del teléfono: 290 menos 9 por lado. */
private const val ANCHO_DE_LA_REPLICA = 272f

/**
 * Lo que necesita la pantalla por debajo de la barra de estado con la tarjeta plegada entera:
 * cabecera, tarjeta y botones con la letra de ahora, y unos píxeles de margen para teléfonos
 * cuya letra parte los renglones en otro sitio.
 */
private const val ALTO_BAJO_LA_BARRA = 550f

/** Lo que queda entre el último botón y el borde de abajo. */
private const val MARGEN_INFERIOR = 16f

/**
 * La pantalla a la escala de la réplica: dentro de esto, un dp mide lo que un píxel suyo.
 *
 * **Copiar sus números como dp fue el primer error.** La réplica se dibujó en un teléfono de
 * 272 px de ancho y el de verdad ronda los 410 dp: todo salía pequeño y nadando en hueco.
 * **Multiplicarlos a mano fue el segundo**, porque un factor fijo sólo acierta en un ancho, y
 * el que se usó partía del marco del teléfono (290) y no de su pantalla (272).
 *
 * Cambiar la densidad de este árbol resuelve las dos cosas: los tamaños, los rellenos y la letra
 * guardan la proporción del diseño en cualquier teléfono, y los números del código se leen igual
 * que los del CSS. Lo que no es del diseño no se toca: las barras del sistema se miden en píxeles
 * y siguen donde están, y el tamaño de letra que elija el usuario sigue multiplicando encima.
 *
 * **Manda la dimensión que antes se acabe**, y el alto se cuenta sin las barras del sistema,
 * que cambian de un teléfono a otro: plegada, la tarjeta tiene que verse entera, con el nombre
 * del archivo y su margen, sin desplazar nada. Sólo el texto desplegado puede no caber. La barra
 * de gestos cabe en el margen de abajo; la de tres botones no, y lo que sobresale también se
 * descuenta. Nunca baja de uno: en horizontal no cabe, y ahí lo que toca es desplazarse, no
 * encoger la letra.
 *
 * **La letra va un 15 % por debajo de la réplica** (16 sep): escalada con la pantalla quedaba
 * grande en el teléfono. Así cae en los tamaños de siempre de la app: 16 sp los botones, 14 el
 * cuerpo y 11 los rótulos.
 */
@Composable
private fun AEscalaDeLaReplica(ancho: Dp, alto: Dp, content: @Composable () -> Unit) {
    val densidad = LocalDensity.current
    val barraDeEstado = with(densidad) { WindowInsets.statusBars.getTop(this).toDp() }
    val barraDeAbajo = with(densidad) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val util = alto - barraDeEstado
    val escala = maxOf(
        1f,
        minOf(
            ancho.value / ANCHO_DE_LA_REPLICA,
            util.value / ALTO_BAJO_LA_BARRA,
            (util - barraDeAbajo).value / (ALTO_BAJO_LA_BARRA - MARGEN_INFERIOR)
        )
    )
    /*
     * Los mínimos táctiles se miden con la densidad de aquí dentro, así que se dividen por la
     * escala: el botón mide lo que dice la réplica y el dedo conserva sus 48 dp de verdad. Sin
     * esto, Material inflaba los botones a 48 px de réplica y Compose ensanchaba la zona de
     * toque del nombre del archivo hasta tapar el hueco que hay sobre los botones.
     */
    val configuracion = LocalViewConfiguration.current
    val configuracionEscalada = remember(configuracion, escala) {
        object : ViewConfiguration by configuracion {
            override val minimumTouchTargetSize: DpSize
                get() = configuracion.minimumTouchTargetSize / escala
        }
    }
    CompositionLocalProvider(
        LocalDensity provides Density(densidad.density * escala, densidad.fontScale),
        LocalMinimumInteractiveComponentSize provides
            LocalMinimumInteractiveComponentSize.current / escala,
        LocalViewConfiguration provides configuracionEscalada,
        content = content
    )
}

@Composable
private fun CabeceraDelFallo(informe: InformeDeFallo, enElActo: Boolean) {
    val colores = coloresDelFallo()
    val tinta = MaterialTheme.colorScheme.onErrorContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colores.cabecera)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colores.sello),
            contentAlignment = Alignment.Center
        ) {
            // Un signo y no un icono: en la réplica es una letra, y el icono de Material es
            // una barra redondeada con su punto que se lee más fino.
            Text(
                text = "!",
                color = colores.tintaDelSello,
                style = letra(tamano = 25.sp, peso = FontWeight.ExtraBold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (enElActo) {
                Textos.get(R.string.fallo_titulo_en_el_acto)
            } else {
                Textos.get(R.string.fallo_titulo_al_volver)
            },
            color = tinta,
            textAlign = TextAlign.Center,
            style = letra(tamano = 15.5.sp, peso = FontWeight.ExtraBold, espaciado = (-0.01).em)
        )
        Text(
            text = InformeDeFallo.fechaLegible(informe.fecha),
            color = tinta.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            style = letra(tamano = 9.5.sp),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * El informe en fichas, no en tabla.
 *
 * Cada dato va en su propio bloque, con el rótulo encima y el valor debajo. **Sin líneas
 * divisorias**, que eran lo que hacía que la tarjeta se leyera como una pantalla de ajustes:
 * la separación la dan los bloques, no unas rayas.
 *
 * El error ocupa la fila entera y va primero porque es lo único que cambia de un fallo a otro.
 * Fecha y versión se reparten la siguiente, que son cortos; el teléfono vuelve a ancho completo
 * porque no cabe en media.
 *
 * Sin `UniCard`: con la superficie «con filete», que es la de por defecto, le pone borde, y la
 * réplica no lleva ninguno.
 */
@Composable
private fun TarjetaDelInforme(
    informe: InformeDeFallo,
    estadoDelArchivo: String?,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    val colores = coloresDelFallo()
    val esquema = MaterialTheme.colorScheme
    val desplazamiento = rememberScrollState()
    val fecha = InformeDeFallo.fechaLegible(informe.fecha)
    val giro by animateFloatAsState(
        targetValue = if (abierto) 180f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 200),
        label = "giroDelDesplegable"
    )

    /*
     * Al desplegar, la tarjeta baja sola hasta el texto.
     *
     * Ya va al alto máximo, así que el texto completo aparece por debajo de lo que se ve: sin
     * esto, tocar el nombre del archivo sólo giraba la flecha. Se sigue al final mientras la
     * tarjeta crece, y si el usuario sube a mano no se le pelea, porque el final ya no cambia.
     */
    LaunchedEffect(abierto) {
        if (abierto) {
            snapshotFlow { desplazamiento.maxValue }.collectLatest { final ->
                desplazamiento.animateScrollTo(final)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colores.tarjeta)
            .verticalScroll(desplazamiento)
            .padding(15.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Ficha(
                etiqueta = Textos.get(R.string.fallo_dato_error),
                valor = informe.tipo,
                esError = true,
                modifier = Modifier.fillMaxWidth()
            )
            // Las dos del mismo alto, como las celdas de una rejilla: la versión ocupa tres
            // renglones y la fecha dos.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Ficha(
                    etiqueta = Textos.get(R.string.fallo_dato_fecha),
                    valor = fecha.replace(" ", "\n"),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Ficha(
                    etiqueta = Textos.get(R.string.fallo_dato_version),
                    // Con un unidor invisible tras cada punto: Android no respeta que un punto y
                    // una cifra van juntos, y si la versión no cabía dejaba «1.6.2-alpha.» arriba
                    // y un «8» solo debajo. Así, si corta, corta en el guion.
                    valor = informe.version.replace(".", ".\u2060") +
                        "\n(" + informe.codigoDeVersion + ")",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            Ficha(
                etiqueta = Textos.get(R.string.fallo_dato_telefono),
                valor = informe.dispositivo + " · " + informe.android,
                modifier = Modifier.fillMaxWidth()
            )
        }

        /*
         * La nota de qué lleva el archivo, como nota al pie y no como franja de color.
         *
         * Era un bloque verde saturado a todo lo ancho, justo debajo de una cabecera roja
         * saturada. Lo que dice no es una alarma, así que no tiene por qué vestirse de una.
         */
        Row(
            modifier = Modifier.padding(top = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_fallo_escudo),
                contentDescription = null,
                tint = colores.escudo,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(13.dp)
            )
            Text(
                text = Textos.get(R.string.fallo_no_se_envia_solo),
                color = esquema.onSurfaceVariant,
                style = letra(tamano = 8.5.sp, renglon = 1.45f)
            )
        }

        /*
         * El nombre del archivo es lo que despliega: es justo lo que uno toca cuando quiere ver
         * qué se va a mandar.
         *
         * En reposo va **solo**, una línea y su flecha, como en la réplica. El segundo renglón
         * aparece únicamente cuando ya se ha hecho algo con el archivo —enviado o guardado—
         * porque hasta entonces no hay nada que contar.
         */
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .cleanClickable(shape = RoundedCornerShape(6.dp)) { abierto = !abierto },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreDelArchivo(informe),
                    color = esquema.primary,
                    style = letra(tamano = 10.sp, peso = FontWeight.SemiBold)
                )
                if (estadoDelArchivo != null) {
                    Text(
                        text = estadoDelArchivo,
                        color = esquema.onSurfaceVariant,
                        style = letra(tamano = 8.5.sp, renglon = 1.45f)
                    )
                }
            }
            Icon(
                painter = painterResource(R.drawable.ic_fallo_desplegar),
                contentDescription = null,
                tint = esquema.primary,
                modifier = Modifier
                    .size(15.dp)
                    .rotate(giro)
            )
        }

        AnimatedVisibility(visible = abierto) {
            Column(
                modifier = Modifier
                    .padding(top = 9.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colores.ficha)
                    .padding(10.dp)
            ) {
                Text(
                    text = resumenDeLoQuitado(informe),
                    color = esquema.onSurfaceVariant,
                    style = letra(tamano = 8.5.sp, renglon = 1.45f),
                    modifier = Modifier.padding(bottom = 7.dp)
                )
                Text(
                    text = informe.comoTexto(),
                    color = esquema.onSurfaceVariant,
                    style = letra(tamano = 7.5.sp, renglon = 1.65f, mono = true),
                    modifier = Modifier
                        .heightIn(max = 150.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

/** Un dato en su bloque: el rótulo en versales encima, el valor en monoespaciado debajo. */
@Composable
private fun Ficha(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    esError: Boolean = false
) {
    val esquema = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(coloresDelFallo().ficha)
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(
            text = etiqueta.uppercase(),
            color = esquema.onSurfaceVariant,
            style = letra(tamano = 7.5.sp, espaciado = 0.1.em, mono = true)
        )
        Text(
            text = valor,
            color = if (esError) esquema.error else esquema.onSurface,
            style = letra(tamano = if (esError) 10.5.sp else 9.sp, renglon = 1.4f, mono = true),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private enum class TipoDeBoton { Relleno, Tonal, Contorno }

/**
 * Los botones de la réplica: píldora, 12 px arriba y abajo y la etiqueta a 12,5 en negrita, con
 * el icono pegado a ella y los dos centrados juntos.
 *
 * **No es `UniStackButton`**, y no por gusto. Ese mide 56 dp y escribe a 16 sp porque es la
 * acción anclada de un formulario; forzarle el alto de la réplica le recortaba la letra, y su
 * icono va al borde con la etiqueta centrada en lo que queda, que es justo lo que la réplica no
 * hace. Por dentro siguen siendo los de Material, con su cambio de forma al pulsar.
 *
 * El secundario no es el tonal de Material —que tiñe de acento fondo y letra— sino la ficha
 * con tinta blanca, y el de contorno escribe en blanco y no en gris.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BotonDelFallo(
    texto: String,
    onClick: () -> Unit,
    tipo: TipoDeBoton,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val colores = coloresDelFallo()
    val esquema = MaterialTheme.colorScheme
    val formas = ButtonDefaults.shapes(
        shape = RoundedCornerShape(percent = 50),
        pressedShape = ButtonDefaults.pressedShape
    )
    val relleno = PaddingValues(horizontal = 10.dp, vertical = 12.dp)
    val contenido: @Composable RowScope.() -> Unit = {
        if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
            text = texto,
            style = letra(tamano = 11.sp, renglon = 1.36f, peso = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    when (tipo) {
        TipoDeBoton.Relleno -> Button(
            onClick = onClick,
            shapes = formas,
            modifier = modifier.heightIn(min = 39.dp),
            contentPadding = relleno,
            content = contenido
        )

        TipoDeBoton.Tonal -> Button(
            onClick = onClick,
            shapes = formas,
            modifier = modifier.heightIn(min = 39.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colores.ficha,
                contentColor = esquema.onSurface
            ),
            contentPadding = relleno,
            content = contenido
        )

        // Dos píxeles más que los otros: en la réplica el borde suma por fuera, y en Material
        // se pinta por dentro.
        TipoDeBoton.Contorno -> OutlinedButton(
            onClick = onClick,
            shapes = formas,
            modifier = modifier.heightIn(min = 41.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = esquema.onSurface),
            border = BorderStroke(1.dp, colores.filete),
            contentPadding = relleno,
            content = contenido
        )
    }
}

/**
 * Un tipo de letra medido como lo mide el navegador.
 *
 * Cada texto de la réplica hereda un renglón de 1,55 veces su tamaño con el aire repartido
 * arriba y abajo, y sin espaciado entre letras. Los estilos de Material traen el suyo —el
 * cuerpo, 24 sp de renglón y medio punto entre letras—, así que partir de ellos descuadraba
 * todas las alturas. Aquí se parte de cero; de la tipografía sólo se toma la familia, para que
 * la letra elegida en Apariencia se siga respetando.
 */
@Composable
private fun letra(
    tamano: TextUnit,
    renglon: Float = 1.55f,
    peso: FontWeight = FontWeight.Normal,
    espaciado: TextUnit = 0.sp,
    mono: Boolean = false
): TextStyle = TextStyle(
    fontFamily = if (mono) {
        FontFamily.Monospace
    } else {
        MaterialTheme.typography.bodyLarge.fontFamily ?: FontFamily.SansSerif
    },
    fontSize = tamano,
    fontWeight = peso,
    lineHeight = renglon.em,
    letterSpacing = espaciado,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

/** Los colores de la pantalla, ya resueltos para el tema y el modo de ahora. */
private data class ColoresDelFallo(
    val cabecera: Color,
    val sello: Color,
    val tintaDelSello: Color,
    val tarjeta: Color,
    val ficha: Color,
    val filete: Color,
    val escudo: Color
)

/*
 * design-tokens-ok-begin: los tres colores de la réplica aprobada que no tienen rol en el esquema
 *
 * En oscuro la réplica se aparta de Material: la cabecera es un rojo teja más apagado que el
 * contenedor de error, y el sello lleva ese contenedor de fondo con el signo en blanco. En claro
 * coincide con los roles de siempre y no hace falta nada propio. El verde del escudo es el suyo,
 * más suave que el de «al día», que pegado a la cabecera roja volvía a gritar.
 */
private val CabeceraEnOscuro = Color(0xFF8C2F28)
private val EscudoEnOscuro = Color(0xFF5FC98F)
private val EscudoEnClaro = Color(0xFF1F8B58)
private val SignoDelSello = Color(0xFFFFFFFF)
// design-tokens-ok-end

/**
 * Los colores de la réplica, sacados del tema siempre que el tema los tiene.
 *
 * La tarjeta es la superficie de tarjeta del tema tal cual. La ficha y el filete salen de mezclas:
 * con el tema de UniStack dan los #20242F y #262B38 de la réplica en oscuro, y sus #ECEAF7 y
 * #E4E2EE en claro, donde la ficha no es gris sino fondo teñido de acento. Con cualquier otro tema
 * guardan la misma relación con su fondo, en vez de pegar grises de UniStack sobre un Dracula.
 */
@Composable
private fun coloresDelFallo(): ColoresDelFallo {
    val esquema = MaterialTheme.colorScheme
    val oscuro = LocalIsDarkTheme.current
    return ColoresDelFallo(
        cabecera = if (oscuro) CabeceraEnOscuro else esquema.errorContainer,
        sello = if (oscuro) esquema.errorContainer else esquema.error,
        tintaDelSello = SignoDelSello,
        tarjeta = esquema.surfaceVariant,
        ficha = if (oscuro) {
            esquema.surfaceVariant.mezclaCon(esquema.onSurface, 0.037f)
        } else {
            esquema.background.mezclaCon(esquema.primary, 0.057f)
        },
        filete = if (oscuro) {
            esquema.surfaceVariant.mezclaCon(esquema.onSurface, 0.07f)
        } else {
            esquema.background.mezclaCon(esquema.onSurface, 0.07f)
        },
        escudo = if (oscuro) EscudoEnOscuro else EscudoEnClaro
    )
}

/**
 * Qué se quitó del informe, dicho con los números de verdad.
 *
 * Nada de «se protegieron tus datos» en abstracto: o se quitó algo y se dice cuánto, o no
 * había nada que quitar y también se dice. Es la única forma de que la frase signifique algo
 * la segunda vez que se lee.
 */
private fun resumenDeLoQuitado(informe: InformeDeFallo): String {
    if (!informe.huboRecorte) return Textos.get(R.string.fallo_limpio_nada)
    val piezas = buildList {
        when {
            informe.correosQuitados == 1 -> add(Textos.get(R.string.fallo_limpio_correo_uno))
            informe.correosQuitados > 1 ->
                add(Textos.get(R.string.fallo_limpio_correo_varios, informe.correosQuitados))
        }
        when {
            informe.textosQuitados == 1 -> add(Textos.get(R.string.fallo_limpio_texto_uno))
            informe.textosQuitados > 1 ->
                add(Textos.get(R.string.fallo_limpio_texto_varios, informe.textosQuitados))
        }
    }
    val total = informe.correosQuitados + informe.textosQuitados
    val lista = piezas.joinToString(" · ")
    return if (total == 1) {
        Textos.get(R.string.fallo_limpio_uno, lista)
    } else {
        Textos.get(R.string.fallo_limpio_varios, total, lista)
    }
}

/** `unistack-fallo-2026-09-15.txt`: la fecha va delante para que ordene sola. */
fun nombreDelArchivo(informe: InformeDeFallo): String {
    val fecha = InformeDeFallo.fechaLegible(informe.fecha)
        .take(10)
        .split("/")
        .reversed()
        .joinToString("-")
    return "unistack-fallo-$fecha.txt"
}
