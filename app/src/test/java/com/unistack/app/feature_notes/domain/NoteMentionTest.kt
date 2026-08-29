package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * La arroba que vincula una materia sin soltar el teclado.
 *
 * Era `#` en la primera vuelta y hubo que cambiarlo: `#` es un titulo en Markdown, asi que
 * escribir `#Calculo` al principio de una linea creaba un titular en vez de vincular nada.
 */
class NoteMentionTest {

    @Test
    fun laMencionEsLoQueVaEntreLaArrobaYElCursor() {
        val texto = "hoy en @calc"
        val m = NoteMention.at(texto, texto.length)
        assertEquals("calc", m?.token)
        assertEquals(7, m?.start)
        assertEquals(12, m?.end)
    }

    @Test
    fun unaArrobaSolaAbreLaListaEntera() {
        val m = NoteMention.at("hoy en @", 8)
        assertEquals("", m?.token)
    }

    /**
     * Un correo escrito dentro de una nota no puede abrir el selector de materias.
     *
     * Es lo que separa una orden de un texto: la arroba solo cuenta al principio de una palabra.
     */
    @Test
    fun unCorreoNoEsUnaMencion() {
        assertNull(NoteMention.at("escribe a profe@uni.edu", 22))
    }

    @Test
    fun unEspacioCortaLaMencion() {
        assertNull(NoteMention.at("hoy en @calculo vimos", 21))
    }

    @Test
    fun sinArrobaNoHayNada() {
        assertNull(NoteMention.at("hoy en clase", 12))
    }

    @Test
    fun elPrincipioDelTextoTambienVale() {
        assertEquals("cal", NoteMention.at("@cal", 4)?.token)
    }

    @Test
    fun unaPosicionImposibleNoRompe() {
        assertNull(NoteMention.at("hola", 99))
        assertNull(NoteMention.at("hola", -3))
    }

    /*
     * Al aceptar, la arroba se cambia por el nombre de la materia y no se borra sin mas: «hoy en
     * @calculo vimos derivadas» tiene que quedar en «hoy en Calculo II vimos derivadas», no en
     * «hoy en vimos derivadas».
     */
    @Test
    fun aceptarDejaLaFraseEntera() {
        val texto = "hoy en @calc vimos derivadas"
        val m = NoteMention.at(texto, 12)!!
        val cambio = NoteMention.accept(texto, m, "Calculo II")
        assertEquals("hoy en Calculo II vimos derivadas", cambio.text)
    }

    @Test
    fun elCursorSeQuedaDespuesDelNombre() {
        val texto = "@cal"
        val m = NoteMention.at(texto, 4)!!
        val cambio = NoteMention.accept(texto, m, "Física")
        assertEquals("Física", cambio.text)
        assertEquals(6, cambio.selectionStart)
        assertEquals(6, cambio.selectionEnd)
    }
}
