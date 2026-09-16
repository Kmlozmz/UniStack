package com.unistack.app.core.fallos

import android.content.Context
import android.os.Build
import com.unistack.app.BuildConfig
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Lo que la app sabe de un cierre inesperado, ya limpio de datos del usuario.
 *
 * **Se guarda estructurado, no como texto.** El `.txt` que se envía o se guarda se arma en
 * [comoTexto] cuando hace falta, y no antes: el informe se captura dentro de un proceso que
 * se está muriendo, y ahí no se puede contar con que los recursos de idioma respondan. Los
 * números se recogen en el momento del fallo; las palabras se ponen después, en la pantalla,
 * donde `Textos` sí funciona y además sale en el idioma que el usuario eligió.
 *
 * **Lo que se quita no se cuenta a ojo.** [correosQuitados] y [textosQuitados] son los
 * reemplazos que hizo [limpiar], no una estimación: la pantalla los enseña tal cual porque
 * decirle al usuario «se quitaron tres cosas» y que no sea verdad es peor que no decir nada.
 */
data class InformeDeFallo(
    val version: String,
    val codigoDeVersion: Long,
    val android: String,
    val dispositivo: String,
    val idioma: String,
    /** Cuándo ocurrió, en milisegundos desde la época. */
    val fecha: Long,
    /** El nombre corto de la excepción: `IllegalStateException`. */
    val tipo: String,
    /** La traza completa, ya limpia, con las líneas recortadas marcadas. */
    val traza: String,
    val correosQuitados: Int,
    val textosQuitados: Int
) {

    val huboRecorte: Boolean get() = correosQuitados > 0 || textosQuitados > 0

    /** El archivo que se comparte o se guarda. */
    fun comoTexto(): String = buildString {
        appendLine("UniStack $version ($codigoDeVersion)")
        appendLine("$android · $dispositivo · $idioma")
        appendLine(formatoDeFecha.format(fecha))
        appendLine()
        appendLine(traza)
    }

    fun comoJson(): String = JSONObject().apply {
        put(CAMPO_VERSION, version)
        put(CAMPO_CODIGO, codigoDeVersion)
        put(CAMPO_ANDROID, android)
        put(CAMPO_DISPOSITIVO, dispositivo)
        put(CAMPO_IDIOMA, idioma)
        put(CAMPO_FECHA, fecha)
        put(CAMPO_TIPO, tipo)
        put(CAMPO_TRAZA, traza)
        put(CAMPO_CORREOS, correosQuitados)
        put(CAMPO_TEXTOS, textosQuitados)
    }.toString()

    companion object {

        private const val CAMPO_VERSION = "version"
        private const val CAMPO_CODIGO = "codigo"
        private const val CAMPO_ANDROID = "android"
        private const val CAMPO_DISPOSITIVO = "dispositivo"
        private const val CAMPO_IDIOMA = "idioma"
        private const val CAMPO_FECHA = "fecha"
        private const val CAMPO_TIPO = "tipo"
        private const val CAMPO_TRAZA = "traza"
        private const val CAMPO_CORREOS = "correos"
        private const val CAMPO_TEXTOS = "textos"

        /** Con la zona horaria del teléfono: la hora que el usuario vio en su reloj. */
        private val formatoDeFecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US)

        /**
         * Cuántos marcos de pila se guardan por excepción.
         *
         * Veinte llegan de sobra al código de la app: lo de encima es el andamiaje de Compose
         * y de Android, que se repite igual en todos los fallos y sólo engorda el archivo.
         */
        private const val MARCOS = 20

        private val CORREO = Regex("""[\w.+%-]+@[\w-]+\.[\w.]{2,}""")

        /**
         * Cualquier cosa entrecomillada, con comillas rectas o angulares.
         *
         * Ahí es donde acaban los datos del usuario sin que nadie los ponga a propósito: el
         * mensaje de una excepción cita el valor que la provocó, y ese valor es el nombre de
         * su materia o el título de su tarea. Se exige un mínimo de dos caracteres para no
         * borrar comillas vacías ni las que envuelven una sola letra.
         */
        private val ENTRECOMILLADO = Regex(""""[^"\n]{2,}"|«[^»\n]{2,}»""")

        /*
         * Corchetes y no comillas angulares, y esto no es estética.
         *
         * Con `«correo»` de marca, la segunda pasada —la del entrecomillado, que caza
         * `«...»` porque es como escribe la app— se comía la marca que acababa de poner la
         * primera: el correo salía como «quitado» y además **contaba dos veces**, una como
         * correo y otra como texto. El número que la pantalla le enseña al usuario se iba
         * inflando solo. Los corchetes no los caza ninguna de las dos.
         */
        private const val MARCA_CORREO = "[correo quitado]"
        private const val MARCA_TEXTO = "[texto quitado]"

        fun de(context: Context, error: Throwable): InformeDeFallo {
            val crudo = trazaDe(error)
            val (limpia, correos, textos) = limpiar(crudo)
            return InformeDeFallo(
                version = BuildConfig.VERSION_NAME,
                codigoDeVersion = BuildConfig.VERSION_CODE.toLong(),
                android = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                dispositivo = "${Build.MANUFACTURER} ${Build.MODEL}",
                idioma = context.resources.configuration.locales[0]?.toLanguageTag().orEmpty(),
                fecha = System.currentTimeMillis(),
                tipo = error.javaClass.simpleName.ifBlank { error.javaClass.name },
                traza = limpia,
                correosQuitados = correos,
                textosQuitados = textos
            )
        }

        fun deJson(texto: String): InformeDeFallo? = runCatching {
            val json = JSONObject(texto)
            InformeDeFallo(
                version = json.getString(CAMPO_VERSION),
                codigoDeVersion = json.getLong(CAMPO_CODIGO),
                android = json.getString(CAMPO_ANDROID),
                dispositivo = json.getString(CAMPO_DISPOSITIVO),
                idioma = json.optString(CAMPO_IDIOMA),
                fecha = json.getLong(CAMPO_FECHA),
                tipo = json.getString(CAMPO_TIPO),
                traza = json.getString(CAMPO_TRAZA),
                correosQuitados = json.optInt(CAMPO_CORREOS),
                textosQuitados = json.optInt(CAMPO_TEXTOS)
            )
        }.getOrNull()

        /**
         * La traza recortada: la excepción, sus marcos y la cadena de causas.
         *
         * Se recorre a mano en vez de usar [Throwable.printStackTrace] porque ese vuelca la
         * pila entera —doscientas líneas largas en cualquier fallo dentro de Compose— y el
         * archivo acababa siendo ilegible justo para quien tiene que leerlo.
         */
        private fun trazaDe(error: Throwable): String = buildString {
            var actual: Throwable? = error
            var vuelta = 0
            val vistos = mutableSetOf<Throwable>()
            while (actual != null && vistos.add(actual) && vuelta < 4) {
                if (vuelta > 0) append("Causado por: ")
                appendLine("${actual.javaClass.name}: ${actual.message.orEmpty()}")
                val marcos = actual.stackTrace
                marcos.take(MARCOS).forEach { appendLine("  en $it") }
                if (marcos.size > MARCOS) appendLine("  ... ${marcos.size - MARCOS} líneas más")
                actual = actual.cause
                vuelta++
            }
        }.trimEnd()

        /**
         * Quita del texto lo que pueda ser del usuario y dice cuánto quitó.
         *
         * El orden importa: los correos primero, porque un correo dentro de unas comillas se
         * perdería en el reemplazo del entrecomillado y dejaría de contarse como lo que es.
         */
        fun limpiar(texto: String): Triple<String, Int, Int> {
            var correos = 0
            val sinCorreos = CORREO.replace(texto) { correos++; MARCA_CORREO }
            var textos = 0
            val sinNada = ENTRECOMILLADO.replace(sinCorreos) { textos++; MARCA_TEXTO }
            return Triple(sinNada, correos, textos)
        }

        /** El mismo formato que el archivo, para enseñarlo en pantalla. */
        fun fechaLegible(cuando: Long): String = formatoDeFecha.format(cuando)
    }
}

/**
 * El fallo de mentira del banco de pruebas.
 *
 * Lleva datos del usuario metidos en el mensaje **a propósito**: un nombre de materia, un
 * título de tarea y un correo. Así, al probarlo, se ve que la franja de «esto se quitó» dice
 * un número de verdad y no una frase de adorno. Es además la forma exacta en que estos datos
 * se cuelan en un fallo real: nadie los escribe, los cita el mensaje de la excepción.
 */
fun falloDeMuestra(): Throwable = IllegalStateException(
    "no existe la materia \"Álgebra Lineal — Grupo 3\" para la tarea " +
        "\"Taller 4: espacios vectoriales\" (usuario@correo.edu.co)"
)

/** El informe de [falloDeMuestra], para dejarlo esperando sin romper nada. */
fun informeDeMuestra(context: Context): InformeDeFallo =
    InformeDeFallo.de(context = context, error = falloDeMuestra())
