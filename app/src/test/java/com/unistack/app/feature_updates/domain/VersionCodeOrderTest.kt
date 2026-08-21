package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El `versionCode` que genera `app/build.gradle.kts` decide qué APK se puede instalar encima de
 * cuál, y la fórmula vive allí, fuera del alcance de los tests. Esta copia fija el orden que
 * tiene que producir, para que un cambio en la de Gradle que rompa la escalera se note aquí.
 *
 * Si cambia una, cambia la otra.
 */
class VersionCodeOrderTest {

    private fun versionCodeFor(versionName: String): Int {
        val cleaned = versionName.trim().removePrefix("v").removePrefix("V")
        val separator = cleaned.indexOfFirst { it == '-' || it == '+' }
        val numeric = if (separator >= 0) cleaned.take(separator) else cleaned
        val suffix = if (separator >= 0) cleaned.substring(separator + 1).lowercase() else null
        val parts = numeric.split('.').map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        val iteration = suffix
            ?.dropWhile { !it.isDigit() }
            ?.takeWhile(Char::isDigit)
            ?.toIntOrNull()
            ?: 0
        val major = parts.getOrElse(0) { 0 }
        val minor = parts.getOrElse(1) { 0 }
        val patch = parts.getOrElse(2) { 0 }
        val numbered = suffix != null &&
            (suffix.startsWith("alpha") || suffix.startsWith("beta") || suffix.startsWith("rc"))
        require(!numbered || iteration <= 99) { "«$versionName»: la iteración pasa de 99." }
        require(minor <= 99 && patch <= 99) { "«$versionName»: menor y parche llegan hasta 99." }
        require(major <= 200) { "«$versionName»: el mayor llega hasta 200." }
        val stage = when {
            suffix == null -> 999
            suffix.startsWith("alpha") -> 100 + iteration
            suffix.startsWith("beta") -> 300 + iteration
            suffix.startsWith("rc") -> 600 + iteration
            else -> 0
        }
        return (
            major * 10_000_000 +
                minor * 100_000 +
                patch * 1_000 +
                stage
            ).coerceAtLeast(1)
    }

    @Test
    fun `el orden de instalacion sigue al de las versiones`() {
        val escalera = listOf(
            "0.0.0-dev.26081310",
            "1.0.0-alpha.1",
            "1.0.0-alpha.2",
            "1.0.0-beta.1",
            "1.0.0-rc.1",
            "1.0.0",
            "1.0.1",
            "1.1.0",
            "2.0.0"
        )

        escalera.zipWithNext { anterior, siguiente ->
            assertTrue(
                "$siguiente debería poder instalarse sobre $anterior",
                versionCodeFor(siguiente) > versionCodeFor(anterior)
            )
        }
    }

    @Test
    fun `dos compilaciones locales comparten numero y se reinstalan entre si`() {
        // Reinstalar el mismo versionCode está permitido; lo que Android rechaza es bajar.
        assertTrue(versionCodeFor("0.0.0-dev.26081310") == versionCodeFor("0.0.0-dev.26081411"))
    }

    @Test
    fun `una alpha no entra sobre la version definitiva`() {
        // Y está bien que no entre: es un paso atrás y se hace desinstalando a conciencia.
        assertTrue(versionCodeFor("1.0.0-alpha.1") < versionCodeFor("1.0.0"))
    }

    @Test
    fun `pasada la novena iteracion cada una sigue teniendo su numero`() {
        // Con el reparto viejo, de la alpha.19 en adelante todas daban 1030029: doce
        // compilaciones seguidas que el sistema no podía distinguir.
        val alphas = (1..99).map { versionCodeFor("1.3.0-alpha.$it") }
        assertEquals("cada alpha tiene su número", alphas.size, alphas.distinct().size)
        assertTrue(versionCodeFor("1.3.0-alpha.31") > versionCodeFor("1.3.0-alpha.19"))
    }

    @Test
    fun `pasarse de la centena rompe la compilacion en vez de repetir numero`() {
        assertThrows(IllegalArgumentException::class.java) {
            versionCodeFor("1.3.0-alpha.100")
        }
    }

    @Test
    fun `el reparto nuevo deja instalar encima de lo compilado con el viejo`() {
        // Regla de la mudanza: ninguna versión puede bajar de número al cambiar la fórmula,
        // o el móvil que ya tenga la anterior instalada rechazaría la siguiente.
        fun viejo(nombre: String): Int {
            val cleaned = nombre.trim().removePrefix("v")
            val separator = cleaned.indexOfFirst { it == '-' || it == '+' }
            val numeric = if (separator >= 0) cleaned.take(separator) else cleaned
            val suffix = if (separator >= 0) cleaned.substring(separator + 1).lowercase() else null
            val parts = numeric.split('.').map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
            val iteration = suffix?.dropWhile { !it.isDigit() }?.takeWhile(Char::isDigit)
                ?.toIntOrNull()?.coerceIn(0, 19) ?: 0
            val stage = when {
                suffix == null -> 99
                suffix.startsWith("alpha") -> 10 + iteration
                suffix.startsWith("beta") -> 30 + iteration
                suffix.startsWith("rc") -> 60 + iteration
                else -> 0
            }.coerceIn(0, 99)
            return (parts.getOrElse(0) { 0 } * 1_000_000 + parts.getOrElse(1) { 0 } * 10_000 +
                parts.getOrElse(2) { 0 } * 100 + stage).coerceAtLeast(1)
        }
        listOf(
            "1.3.0-alpha.31", "1.3.0-alpha.32", "1.2.0-beta.1", "1.3.0-beta.1", "1.3.0", "2.0.0"
        ).forEach { nombre ->
            assertTrue(
                "$nombre tiene que subir de número, no bajar",
                versionCodeFor(nombre) > viejo(nombre)
            )
        }
    }
}
