package com.unistack.app.feature_updates.domain

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
            ?.coerceIn(0, 19)
            ?: 0
        val stage = when {
            suffix == null -> 99
            suffix.startsWith("alpha") -> 10 + iteration
            suffix.startsWith("beta") -> 30 + iteration
            suffix.startsWith("rc") -> 60 + iteration
            else -> 0
        }.coerceIn(0, 99)
        return (
            parts.getOrElse(0) { 0 } * 1_000_000 +
                parts.getOrElse(1) { 0 } * 10_000 +
                parts.getOrElse(2) { 0 } * 100 +
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
}
