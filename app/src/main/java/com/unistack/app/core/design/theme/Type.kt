package com.unistack.app.core.design.theme

import androidx.compose.material3.Typography
import android.os.Build
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.text.font.FontFamily
import com.unistack.app.feature_user.domain.TypographyStyle
import com.unistack.app.feature_user.domain.LineHeightStyle
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

val UniStackTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        displayMedium = displayMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        displaySmall = displaySmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        bodySmall = bodySmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelMedium = labelMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelSmall = labelSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal)
    )
}

/**
 * Tipografía de la app aplicando solo la familia elegida.
 *
 * El tamaño NO se escala aquí: la preferencia de "texto grande" se aplica sobre el
 * `fontScale` de la densidad en [UniStackTheme], para que también alcance a los tamaños
 * declarados sueltos en las pantallas. Escalar en ambos sitios lo aplicaría dos veces.
 */
/**
 * La tipografia de la app: familia e interlineado.
 *
 * Eran dos familias —la de la app y la del sistema— y ninguna forma de tocar el aire entre
 * renglones. Ahora son cuatro y tres alturas, que es lo que pedia la pantalla de Tipografia.
 *
 * El interlineado se multiplica sobre el que ya trae cada estilo en vez de fijarse a un numero:
 * un titular y un parrafo no quieren el mismo aire, y darles el mismo lo estropea a los dos.
 */
fun appearanceTypography(
    estilo: TypographyStyle,
    negrita: Boolean = false
): Typography {
    val family = when (estilo) {
        TypographyStyle.SANS -> FontFamily.SansSerif
        TypographyStyle.SYSTEM -> FontFamily.Default
        TypographyStyle.SERIF -> FontFamily.Serif
        TypographyStyle.MONO -> FontFamily.Monospace
        /*
         * Las dos que se piden por nombre de dispositivo.
         *
         * `DeviceFontFamilyName` es de API 31, y la app llega hasta la 26. Debajo de eso —y en
         * cualquier telefono que no tenga esa familia instalada— cae en la `sans-serif` normal:
         * la letra no es la elegida pero la app se lee, que es lo que importa.
         */
        TypographyStyle.ESTRECHA -> familiaDelSistema("sans-serif-condensed")
        TypographyStyle.REDONDEADA -> familiaDelSistema("sans-serif-rounded", "casual")
    }
    fun TextStyle.ajustada(): TextStyle {
        return copy(
            fontFamily = family,
            /*
             * La negrita sube un escalon, no lo pone todo en «bold».
             *
             * Con todo al maximo se pierde la jerarquia: un titular y un pie de tabla acaban
             * pesando lo mismo, y lo que se gana en tinta se pierde en poder distinguirlos.
             * Un escalon deja el titular por encima del cuerpo y el cuerpo mas legible.
             */
            fontWeight = if (!negrita) fontWeight else when (fontWeight) {
                null, FontWeight.Light, FontWeight.Normal -> FontWeight.Medium
                FontWeight.Medium -> FontWeight.SemiBold
                FontWeight.SemiBold -> FontWeight.Bold
                else -> FontWeight.ExtraBold
            }
        )
    }
    return UniStackTypography.run {
        copy(
            displayLarge = displayLarge.ajustada(),
            displayMedium = displayMedium.ajustada(),
            displaySmall = displaySmall.ajustada(),
            headlineLarge = headlineLarge.ajustada(),
            headlineMedium = headlineMedium.ajustada(),
            headlineSmall = headlineSmall.ajustada(),
            titleLarge = titleLarge.ajustada(),
            titleMedium = titleMedium.ajustada(),
            titleSmall = titleSmall.ajustada(),
            bodyLarge = bodyLarge.ajustada(),
            bodyMedium = bodyMedium.ajustada(),
            bodySmall = bodySmall.ajustada(),
            labelLarge = labelLarge.ajustada(),
            labelMedium = labelMedium.ajustada(),
            labelSmall = labelSmall.ajustada()
        )
    }
}

/**
 * Una familia instalada en el telefono, o la `sans-serif` de siempre si no esta.
 *
 * @param nombres se prueban en orden. Android no dice si una familia existe, asi que el
 *   respaldo va dentro del propio [FontFamily]: si la primera no resuelve, usa la siguiente.
 */
private fun familiaDelSistema(vararg nombres: String): FontFamily {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return FontFamily.SansSerif
    return FontFamily(nombres.map { Font(DeviceFontFamilyName(it)) })
}

/**
 * El aire entre renglones, aplicado sobre una tipografia ya montada.
 *
 * **Va al final a proposito.** Estuvo dentro de `appearanceTypography`, y justo despues
 * `expressiveTypography` reescribia el `lineHeight` de los diecisiete estilos con su valor fijo
 * en sp: el ajuste se elegia y no movia un renglon. Aplicandolo por encima de todo, no queda
 * nada detras que lo pise.
 *
 * Los estilos de Material vienen todos con su `lineHeight` puesto, pero se comprueba igual: uno
 * sin especificar multiplicado da `NaN.sp`, y con eso el texto no llega a medirse.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun Typography.conInterlineado(estilo: LineHeightStyle): Typography {
    if (estilo == LineHeightStyle.NORMAL) return this
    val factor = if (estilo == LineHeightStyle.COMPACTO) 0.86f else 1.22f
    fun TextStyle.conAire(): TextStyle =
        if (lineHeight.isSpecified) copy(lineHeight = lineHeight * factor) else this
    return copy(
        displayLarge = displayLarge.conAire(),
        displayLargeEmphasized = displayLargeEmphasized.conAire(),
        displayMedium = displayMedium.conAire(),
        displayMediumEmphasized = displayMediumEmphasized.conAire(),
        displaySmall = displaySmall.conAire(),
        displaySmallEmphasized = displaySmallEmphasized.conAire(),
        headlineLarge = headlineLarge.conAire(),
        headlineLargeEmphasized = headlineLargeEmphasized.conAire(),
        headlineMedium = headlineMedium.conAire(),
        headlineMediumEmphasized = headlineMediumEmphasized.conAire(),
        headlineSmall = headlineSmall.conAire(),
        headlineSmallEmphasized = headlineSmallEmphasized.conAire(),
        titleLarge = titleLarge.conAire(),
        titleLargeEmphasized = titleLargeEmphasized.conAire(),
        titleMedium = titleMedium.conAire(),
        titleMediumEmphasized = titleMediumEmphasized.conAire(),
        titleSmall = titleSmall.conAire(),
        titleSmallEmphasized = titleSmallEmphasized.conAire(),
        bodyLarge = bodyLarge.conAire(),
        bodyLargeEmphasized = bodyLargeEmphasized.conAire(),
        bodyMedium = bodyMedium.conAire(),
        bodyMediumEmphasized = bodyMediumEmphasized.conAire(),
        bodySmall = bodySmall.conAire(),
        bodySmallEmphasized = bodySmallEmphasized.conAire(),
        labelLarge = labelLarge.conAire(),
        labelLargeEmphasized = labelLargeEmphasized.conAire(),
        labelMedium = labelMedium.conAire(),
        labelMediumEmphasized = labelMediumEmphasized.conAire(),
        labelSmall = labelSmall.conAire(),
        labelSmallEmphasized = labelSmallEmphasized.conAire()
    )
}
