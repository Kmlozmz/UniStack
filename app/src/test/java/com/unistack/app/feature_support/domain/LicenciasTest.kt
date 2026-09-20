package com.unistack.app.feature_support.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric por `org.json`, que en la JVM pelada viene sin cuerpo.
@RunWith(RobolectricTestRunner::class)
class LicenciasTest {

    @Test
    fun leeElJsonDelPluginConSuLicenciaPorClave() {
        val json = """
            {"libraries":[
              {"uniqueId":"androidx.activity:activity","name":"Activity","artifactVersion":"1.13.0",
               "developers":[{"name":"The Android Open Source Project"}],"website":"https://developer.android.com","licenses":["Apache-2.0"]},
              {"uniqueId":"x.y:z","name":"","artifactVersion":"","developers":[],"licenses":["2895aeae"]},
              {"uniqueId":"sin.licencia:lib","name":"Sin licencia","licenses":[]}
            ],
            "licenses":{
              "Apache-2.0":{"hash":"a","name":"Apache License 2.0","url":"https://www.apache.org/licenses/LICENSE-2.0","spdxId":"Apache-2.0"},
              "2895aeae":{"hash":"2895aeae","name":"Go License","url":"https://golang.org/LICENSE"}
            }}
        """.trimIndent()

        val libs = BibliotecasDelJson.leer(json)

        assertEquals(listOf("Activity", "Sin licencia", "x.y:z"), libs.map { it.nombre })
        val activity = libs.first()
        assertEquals("1.13.0", activity.version)
        assertEquals(listOf("The Android Open Source Project"), activity.autores)
        assertEquals("Apache-2.0", activity.licencia?.spdxId)
        assertEquals(TextoDeLicencia.APACHE, TextoDeLicencia.porSpdx(activity.licencia?.spdxId))
        // Sin spdxId ni texto: solo el nombre y el enlace.
        val go = libs.first { it.nombre == "x.y:z" }
        assertEquals("Go License", go.licencia?.nombre)
        assertNull(go.licencia?.spdxId)
        assertNull(go.version)
        assertNull(libs.first { it.nombre == "Sin licencia" }.licencia)
    }

    @Test
    fun unTextoPartidoA75ColumnasSeLeeComoParrafos() {
        val texto = """
            PREAMBLE
            The goals of the Open Font License (OFL) are to stimulate worldwide
            development of collaborative font projects.

            1) Neither the Font Software nor any of its individual components,
            in Original or Modified Versions, may be sold by itself.

            2) Original or Modified Versions of the Font Software may be bundled,
            redistributed and/or sold with any software.
        """.trimIndent()

        val parrafos = texto.comoParrafos()

        assertEquals(
            "PREAMBLE The goals of the Open Font License (OFL) are to stimulate worldwide development of collaborative font projects.\n\n" +
                "1) Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.\n\n" +
                "2) Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software.",
            parrafos
        )
    }
}
