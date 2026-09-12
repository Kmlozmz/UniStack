package com.unistack.app.feature_user.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El catálogo es lo que guarda, lo que lee y lo que pinta el movimiento.
 *
 * Por eso se prueba **el catálogo** y no cada gesto por su lado: un identificador repetido, un
 * `read` que mira un campo y un `write` que escribe otro, o un gesto que se queda fuera de la
 * lista son fallos que no dan error de compilación y que se notan meses después, cuando alguien
 * elige «Cascada» y al volver a la pantalla sigue puesta «Escalonada».
 */
class MotionCatalogTest {

    @org.junit.Before
    fun instalarTextos() {
        com.unistack.app.TextosDePrueba.instalar()
    }

    @Test
    fun `cada gesto tiene un identificador propio`() {
        val ids = MotionCatalog.gestures.map { it.id }
        assertEquals("hay identificadores repetidos: $ids", ids.size, ids.distinct().size)
    }

    @Test
    fun `los interruptores no chocan con los gestos`() {
        val gestos = MotionCatalog.gestures.map { it.id }.toSet()
        MotionCatalog.toggles.forEach { toggle ->
            assertTrue("«${toggle.id}» está en los dos sitios", toggle.id !in gestos)
        }
    }

    @Test
    fun `dentro de un gesto, cada variante tiene su identificador`() {
        MotionCatalog.gestures.forEach { gesto ->
            val ids = gesto.options.map { it.id }
            assertEquals("«${gesto.id}» repite variantes: $ids", ids.size, ids.distinct().size)
        }
    }

    @Test
    fun `ningun gesto se queda sin variantes`() {
        MotionCatalog.gestures.forEach { gesto ->
            assertTrue("«${gesto.id}» no tiene ninguna", gesto.options.size >= 2)
        }
    }

    /**
     * Lo que de verdad se rompe al copiar y pegar un gesto: leer de un campo y escribir en otro.
     *
     * Se escribe cada variante y se lee de vuelta. Si `write` apunta a un campo distinto del que
     * mira `read`, lo que vuelve es el valor por defecto y no el que se acaba de escribir.
     */
    @Test
    fun `escribir una variante y volver a leerla da la misma`() {
        MotionCatalog.gestures.forEach { gesto ->
            gesto.options.forEach { opcion ->
                val despues = gesto.write(MotionPreferences.defaults(), opcion)
                assertEquals(
                    "«${gesto.id}» no guarda «${opcion.id}»",
                    opcion.id,
                    gesto.read(despues).id
                )
            }
        }
    }

    @Test
    fun `escribir un gesto no toca a los demas`() {
        val base = MotionPreferences.defaults()
        MotionCatalog.gestures.forEach { gesto ->
            // Una variante que no sea la de por defecto: con la misma, el cambio no probaría nada.
            val otra = gesto.options.firstOrNull { it.id != gesto.read(base).id } ?: return@forEach
            val despues = gesto.write(base, otra)
            MotionCatalog.gestures.filter { it.id != gesto.id }.forEach { vecino ->
                assertEquals(
                    "cambiar «${gesto.id}» movió «${vecino.id}»",
                    vecino.read(base).id,
                    vecino.read(despues).id
                )
            }
        }
    }

    @Test
    fun `los interruptores encienden y apagan lo suyo`() {
        val base = MotionPreferences.defaults()
        MotionCatalog.toggles.forEach { toggle ->
            val volteado = toggle.write(base, !toggle.read(base))
            assertNotEquals("«${toggle.id}» no cambia", toggle.read(base), toggle.read(volteado))
        }
    }

    /**
     * El recuento, fijado a proposito.
     *
     * Es lo que dice el subtitulo de la pantalla de Movimiento. Si alguien quita un gesto sin
     * querer, aqui salta; si lo anade queriendo, cambia el numero y ya.
     *
     * Bajo de 102 a 101 al retirarse «Doble linea» del tachado, que se quito a proposito por
     * no distinguirse de la linea normal. De 78 a 72 al irse «Pasarse del presupuesto» entero
     * (cinco variantes: queda el aviso arriba, fijo) y «Trazo» de la asistencia. De 72 a 68
     * al irse «Marcar asistencia» entero: la rueda rebota, y siempre.
     */
    @Test
    fun `el catalogo cubre los siete gestos y las treinta y nueve variantes`() {
        assertEquals(7, MotionCatalog.gestures.size)
        assertEquals(0, MotionCatalog.toggles.size)
        assertEquals(39, MotionCatalog.variantCount)
    }

    @Test
    fun `los grupos salen en el orden del catalogo`() {
        val grupos = MotionCatalog.grouped().map { it.first }
        assertEquals(
            // Tres grupos: con siete gestos, «academico», «tareas» y «avisos» tenian uno cada
            // uno y eran cabeceras para nada. Los momentos —sello, celebracion, clase en
            // curso— van juntos.
            listOf(
                MotionCatalog.GROUP_BASE,
                MotionCatalog.GROUP_TRANSITIONS,
                MotionCatalog.GROUP_MOMENTS
            ),
            grupos
        )
    }
}
