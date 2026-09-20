package com.unistack.app.feature_support.domain

import org.json.JSONObject

/** Las dos páginas legales de la web. Se abren en el navegador del teléfono, no dentro de la app. */
object LegalPages {
    const val SITE = "https://web-unistack.netlify.app"
    const val TERMS = "$SITE/terminos.html"
    const val PRIVACY = "$SITE/privacidad.html"
}

/**
 * Las licencias cuyo texto va dentro del APK, en `assets/licencias/`.
 *
 * Van empaquetadas porque las dos las exigen: la OFL pide que su texto acompañe a las fuentes
 * allá donde se redistribuyan, y Apache 2.0 pide lo mismo para las bibliotecas. Un archivo en el
 * repo no viaja con la app; un asset sí.
 */
enum class TextoDeLicencia(val asset: String) {
    OFL("licencias/OFL-1.1.txt"),
    APACHE("licencias/Apache-2.0.txt");

    companion object {
        /** La licencia empaquetada que corresponde a un identificador SPDX, o nula si no la llevamos. */
        fun porSpdx(spdxId: String?): TextoDeLicencia? = when (spdxId) {
            "OFL-1.1" -> OFL
            "Apache-2.0" -> APACHE
            else -> null
        }
    }
}

/** Una de las familias de letra de Apariencia › Tipografía que va dentro del APK. */
data class FuenteEmpaquetada(
    val nombre: String,
    /** La línea de copyright de su `OFL.txt`, tal cual la publican sus autores. */
    val aviso: String,
    val url: String
)

/**
 * Las ocho fuentes de `res/font`, con el aviso que cada proyecto pone en su OFL.txt.
 *
 * El cuerpo de la licencia es el mismo para las ocho (comprobado el 20 sep 2026, byte a byte);
 * lo que cambia es este aviso, y es lo que la OFL obliga a conservar.
 */
object FuentesEmpaquetadas {
    val todas = listOf(
        FuenteEmpaquetada("Inter", "Copyright 2020 The Inter Project Authors", "https://github.com/rsms/inter"),
        FuenteEmpaquetada("Manrope", "Copyright 2018 The Manrope Project Authors", "https://github.com/googlefonts/manrope"),
        FuenteEmpaquetada("DM Sans", "Copyright 2014 The DM Sans Project Authors", "https://github.com/googlefonts/dm-fonts"),
        FuenteEmpaquetada("Outfit", "Copyright 2021 The Outfit Project Authors", "https://github.com/Outfitio/Outfit-Fonts"),
        FuenteEmpaquetada("Space Grotesk", "Copyright 2020 The Space Grotesk Project Authors", "https://github.com/floriankarsten/space-grotesk"),
        FuenteEmpaquetada("Nunito", "Copyright 2014 The Nunito Project Authors", "https://github.com/googlefonts/nunito"),
        FuenteEmpaquetada("Lora", "Copyright 2011 The Lora Project Authors, with Reserved Font Name «Lora»", "https://github.com/cyrealtype/Lora-Cyrillic"),
        FuenteEmpaquetada("JetBrains Mono", "Copyright 2020 The JetBrains Mono Project Authors", "https://github.com/JetBrains/JetBrainsMono")
    )
}

/**
 * Un texto de licencia tal como viene —con saltos de línea a 75 columnas— pero legible en un
 * teléfono: se unen las líneas de cada párrafo y se respetan los párrafos y las listas.
 */
fun String.comoParrafos(): String {
    val salida = StringBuilder()
    var lineaAnterior = ""
    lineSequence().forEach { cruda ->
        val linea = cruda.trimEnd()
        val enBlanco = linea.isBlank()
        val empiezaLista = Regex("""^\s*(\(?[a-z0-9]{1,2}\)|\d+\.|[-*])\s""").containsMatchIn(linea)
        when {
            enBlanco -> salida.append("\n\n")
            lineaAnterior.isBlank() || empiezaLista -> {
                if (empiezaLista && lineaAnterior.isNotBlank()) salida.append('\n')
                salida.append(linea.trim())
            }
            else -> salida.append(' ').append(linea.trim())
        }
        lineaAnterior = linea
    }
    return salida.toString().replace(Regex("\n{3,}"), "\n\n").trim()
}

/** La licencia de una biblioteca, tal como la trae el JSON del plugin. */
data class LicenciaDeBiblioteca(
    val nombre: String,
    val url: String?,
    val spdxId: String?,
    /** El texto entero, solo si el plugin lo trajo; casi nunca. Para lo demás está [TextoDeLicencia]. */
    val texto: String?
)

data class Biblioteca(
    val nombre: String,
    val version: String?,
    val autores: List<String>,
    val web: String?,
    val licencia: LicenciaDeBiblioteca?
)

/**
 * Lee `R.raw.aboutlibraries`, el JSON que el plugin de AboutLibraries genera al compilar.
 *
 * Se lee a mano con `org.json` en vez de usar la biblioteca del mismo plugin: esa biblioteca
 * viene compilada con Kotlin 2.3 y arrastraba la `stdlib` a esa versión, que el Dagger de la
 * app todavía no sabe leer. El plugin de Gradle no toca el APK; su JSON sí, y es todo lo que
 * hace falta.
 */
object BibliotecasDelJson {
    fun leer(json: String): List<Biblioteca> {
        val raiz = JSONObject(json)
        val licencias = raiz.optJSONObject("licenses") ?: JSONObject()
        val lista = raiz.optJSONArray("libraries") ?: return emptyList()
        return (0 until lista.length()).mapNotNull { i ->
            val lib = lista.optJSONObject(i) ?: return@mapNotNull null
            val nombre = lib.optString("name").ifBlank { lib.optString("uniqueId") }
            if (nombre.isBlank()) return@mapNotNull null
            val claveDeLicencia = lib.optJSONArray("licenses")?.optString(0)?.takeIf { it.isNotBlank() }
            val lic = claveDeLicencia?.let { licencias.optJSONObject(it) }?.let { l ->
                LicenciaDeBiblioteca(
                    nombre = l.optString("name").ifBlank { claveDeLicencia },
                    url = l.optString("url").takeIf { it.isNotBlank() },
                    spdxId = l.optString("spdxId").takeIf { it.isNotBlank() } ?: claveDeLicencia.takeIf { TextoDeLicencia.porSpdx(it) != null },
                    texto = l.optString("content").takeIf { it.isNotBlank() }
                )
            }
            val autores = lib.optJSONArray("developers")?.let { devs ->
                (0 until devs.length()).mapNotNull { j -> devs.optJSONObject(j)?.optString("name")?.takeIf { it.isNotBlank() } }
            }.orEmpty()
            Biblioteca(
                nombre = nombre,
                version = lib.optString("artifactVersion").takeIf { it.isNotBlank() },
                autores = autores,
                web = lib.optString("website").takeIf { it.isNotBlank() },
                licencia = lic
            )
        }.sortedBy { it.nombre.lowercase() }
    }
}
