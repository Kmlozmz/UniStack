package com.unistack.app.feature_user.domain

import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * Lo que hace que la app se pueda usar, y no solo se vea bien.
 *
 * Eran seis ajustes: idioma, contraste, reloj de 24 horas, tamaño de texto, movimiento y la
 * animación del hero. Ahora son diecisiete repartidos en cuatro grupos —idioma, ver, tocar y
 * controlar, voz y seguridad— porque los seis de antes cubrían la vista y poco más: nada
 * decía qué hacer si no distingues el verde del rojo, si el pulgar no llega arriba, o si
 * necesitas más de cinco segundos para deshacer un borrado.
 *
 * **El movimiento vive aquí y en Apariencia a la vez, y no es una duplicación.** El de
 * Apariencia elige entre veinticinco gestos; el de aquí los apaga todos de un toque. Gana el
 * más restrictivo de los dos.
 */
data class AccessibilityPreferences(
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val highContrastEnabled: Boolean = false,
    val use24HourTime: Boolean = true,
    val textScale: TextScalePreference = TextScalePreference.STANDARD,
    val motionPreference: MotionPreference = MotionPreference.FULL,
    val heroAnimationEnabled: Boolean = true,
    val dateFormat: DateFormatPreference = DateFormatPreference.DMY,
    val currency: CurrencyPreference = CurrencyPreference.COP,

    // ------------------------------------------------------------------ ver

    /** Cuánta separación hay entre el texto y su fondo. */
    val contrast: ContrastLevel = ContrastLevel.ESTANDAR,

    /**
     * Una paleta que no depende del par verde-rojo.
     *
     * La app dice «bien o mal» con esos dos colores en media docena de sitios, y son justo los
     * que se confunden con la deuteranopía. Cada paleta cambia el par entero, no solo el rojo.
     */
    val colorBlindPalette: ColorBlindPalette = ColorBlindPalette.NINGUNA,

    /**
     * Formas además del color junto a cada nota.
     *
     * Círculo para al día, triángulo para en riesgo, cuadrado para suspenso. Es lo que deja
     * leer el estado sin depender de ver el color, y no estorba a quien sí lo ve.
     */
    val shapesBesidesColor: Boolean = false,

    /** Sube el peso de todas las letras de la app. */
    val boldText: Boolean = false,

    /** Letra más abierta, pensada para la dislexia. */
    val readingFont: ReadingFont = ReadingFont.NORMAL,

    // ------------------------------------------------------------------ tocar y controlar

    /** Agranda el área que responde al dedo, sin cambiar cómo se ve. */
    val touchTargetSize: TouchTargetSize = TouchTargetSize.ESTANDAR,

    /** Quita el cristal y los desenfoques, que en pantallas lentas cuestan fotogramas. */
    val reduceTransparency: Boolean = false,

    /** Baja el contenido para alcanzarlo con el pulgar. */
    val oneHandedMode: Boolean = false,

    /**
     * Cuánto dura el aviso para deshacer un borrado.
     *
     * Cinco segundos es lo que trae Material y lo que basta a la mayoría; para quien lee
     * despacio o navega con un conmutador, cinco segundos es un botón que desaparece antes de
     * poder pulsarlo.
     */
    val undoDuration: UndoDuration = UndoDuration.CORTA,

    // ------------------------------------------------------------------ voz y seguridad

    /** TalkBack lee «tres coma cuatro sobre cinco» en vez de «3,4/5». */
    val spokenDescriptions: Boolean = false,

    /** Preguntar antes de borrar o de cerrar un periodo. */
    val confirmIrreversible: Boolean = true,

    /** Mantener la pantalla encendida mientras hay una nota abierta. */
    val keepScreenOn: Boolean = false
)

enum class AppLanguage {
    SYSTEM,
    SPANISH,
    ENGLISH
}

enum class ContrastLevel {
    ESTANDAR,
    ALTO,
    MAXIMO
}

/**
 * Las paletas que no dependen del par verde-rojo.
 *
 * [DEUTERANOPIA] cambia el verde por un azul y el rojo por un naranja, que es el par que mejor
 * se separa sin el canal verde. [TRITANOPIA] va al revés: conserva el rojo y mueve el verde
 * hacia el magenta, porque ahí lo que falta es el azul.
 */
enum class ColorBlindPalette {
    NINGUNA,
    DEUTERANOPIA,
    TRITANOPIA
}

enum class ReadingFont {
    NORMAL,
    DISLEXIA
}

enum class TouchTargetSize {
    ESTANDAR,
    GRANDE,
    MAXIMO
}

enum class UndoDuration(val segundos: Int) {
    CORTA(5),
    LARGA(10),
    MAXIMA(30)
}

/**
 * Formato de visualización de fechas numéricas, independiente del idioma.
 */
enum class DateFormatPreference(
    val pattern: String,
    val labelRes: Int,
    val previewDate: String
) {
    DMY("dd/MM/yyyy", R.string.date_format_dmy, "31/12/2026"),
    MDY("MM/dd/yyyy", R.string.date_format_mdy, "12/31/2026"),
    YMD("yyyy/MM/dd", R.string.date_format_ymd, "2026/12/31");

    val label: String get() = Textos.get(labelRes)
}

/**
 * Divisa principal de la app para presupuestos y gastos, independiente del idioma.
 */
enum class CurrencyPreference(
    val code: String,
    val symbol: String,
    val labelRes: Int,
    val preview: String
) {
    COP("COP", "$", R.string.currency_cop, "$ 50.000 COP"),
    USD("USD", "$", R.string.currency_usd, "$ 50 USD"),
    EUR("EUR", "€", R.string.currency_eur, "50 € EUR"),
    MXN("MXN", "$", R.string.currency_mxn, "$ 50 MXN"),
    ARS("ARS", "$", R.string.currency_ars, "$ 50.000 ARS"),
    CLP("CLP", "$", R.string.currency_clp, "$ 50.000 CLP"),
    PEN("PEN", "S/", R.string.currency_pen, "S/ 50 PEN");

    val label: String get() = Textos.get(labelRes)
}
