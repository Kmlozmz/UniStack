package com.unistack.app

import com.unistack.app.core.utils.Textos
import java.io.File
import java.util.Locale

/**
 * [Textos] para las pruebas de JVM: lee los `strings.xml` del arbol de fuentes y resuelve por
 * el idioma de `Locale.getDefault()`.
 *
 * Sin esto, cualquier prueba que pase por dominio o ViewModel —que ahora piden sus textos a
 * recursos— caeria con «Textos sin proveedor». Y leyendo los XML de verdad, la prueba comprueba
 * de paso que la clave existe en los dos idiomas.
 *
 * El id numerico se traduce a nombre mirando `R.string` por reflexion: los ids son constantes
 * generadas al compilar y estan en la ruta de clases de las pruebas.
 */
object TextosDePrueba {
    private val nombres: Map<Int, String> by lazy {
        R.string::class.java.fields.associate { it.getInt(null) to it.name }
    }
    private val porIdioma = HashMap<String, Map<String, String>>()

    private fun tabla(idioma: String): Map<String, String> = porIdioma.getOrPut(idioma) {
        val carpeta = if (idioma == "en") "values-en" else "values"
        val raiz = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .map { File(it, "app/src/main/res") }
            .firstOrNull { it.isDirectory }
            ?: File(System.getProperty("user.dir"), "src/main/res")
        val xml = File(raiz, "$carpeta/strings.xml").readText()
        Regex("""<string name="([^"]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml)
            .associate { it.groupValues[1] to desescapar(it.groupValues[2]) }
    }

    private fun desescapar(s: String): String = s
        .replace("\\'", "'")
        .replace("\\\"", "\"")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("\\n", "\n")
        .trim('"')

    private val nombresDeListas: Map<Int, String> by lazy {
        R.array::class.java.fields.associate { it.getInt(null) to it.name }
    }

    private fun listas(idioma: String): Map<String, List<String>> {
        val carpeta = if (idioma == "en") "values-en" else "values"
        val raiz = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
            .map { File(it, "app/src/main/res") }
            .firstOrNull { it.isDirectory }
            ?: File(System.getProperty("user.dir"), "src/main/res")
        val xml = File(raiz, "$carpeta/strings.xml").readText()
        return Regex("""<string-array name="([^"]+)">(.*?)</string-array>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(xml)
            .associate { m ->
                m.groupValues[1] to Regex("<item>(.*?)</item>").findAll(m.groupValues[2]).map { desescapar(it.groupValues[1]) }.toList()
            }
    }

    fun instalar() {
        Textos.proveedorDeListas = { id ->
            val nombre = nombresDeListas[id] ?: error("no hay lista con id $id")
            listas(Locale.getDefault().language)[nombre] ?: listas("es")[nombre] ?: error("falta la lista «$nombre»")
        }
        Textos.proveedor = { id, args ->
            val nombre = nombres[id] ?: error("no hay recurso con id $id")
            val idioma = Locale.getDefault().language
            val plantilla = tabla(idioma)[nombre]
                ?: tabla("es")[nombre]
                ?: error("falta la cadena «$nombre» en strings.xml")
            if (args.isEmpty()) plantilla else String.format(Locale.getDefault(), plantilla, *args)
        }
    }
}
