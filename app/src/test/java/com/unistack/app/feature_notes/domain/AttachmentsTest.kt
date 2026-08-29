package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Como se nombra y se mide lo que cuelga de una nota.
 *
 * Son cuentas pequeñas y por eso se equivocan calladas: un tamaño mal formateado no rompe nada,
 * simplemente dice «0 KB» debajo de una foto de tres megas.
 */
class AttachmentsTest {

    @Test
    fun elTipoSaleDelMimeYNoDeQuienLoEligio() {
        assertEquals(AttachmentKind.IMAGE, Attachments.kindFor("image/jpeg"))
        assertEquals(AttachmentKind.AUDIO, Attachments.kindFor("audio/mp4"))
        assertEquals(AttachmentKind.FILE, Attachments.kindFor("application/pdf"))
    }

    /*
     * El selector de archivos tambien devuelve fotos: quien busca un PDF y de paso toca una
     * imagen espera verla como imagen, no como una fila gris con un icono de documento.
     */
    @Test
    fun unaImagenElegidaDesdeArchivosSigueSiendoUnaImagen() {
        assertEquals(AttachmentKind.IMAGE, Attachments.kindFor("IMAGE/PNG"))
    }

    @Test
    fun sinTipoConocidoSeTrataComoArchivo() {
        assertEquals(AttachmentKind.FILE, Attachments.kindFor(null))
        assertEquals(AttachmentKind.FILE, Attachments.kindFor(""))
    }

    @Test
    fun elTamanoSeDiceEnLaUnidadQueSeEntiende() {
        assertEquals("512 B", Attachments.formatSize(512))
        assertEquals("2 KB", Attachments.formatSize(2048))
        assertEquals("840 KB", Attachments.formatSize(840 * 1024))
        assertEquals("1,5 MB", Attachments.formatSize((1.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun laDuracionVaEnMinutosYSegundos() {
        assertEquals("0:00", Attachments.formatDuration(0))
        assertEquals("0:07", Attachments.formatDuration(7_400))
        assertEquals("1:12", Attachments.formatDuration(72_000))
        assertEquals("10:00", Attachments.formatDuration(600_000))
    }

    /** Un valor imposible no puede acabar enseñando un tiempo negativo. */
    @Test
    fun unaDuracionNegativaSeQuedaEnCero() {
        assertEquals("0:00", Attachments.formatDuration(-500))
    }

    /**
     * La extension importa: es lo que hace que el telefono sepa con que abrir el archivo.
     *
     * Si viene en el nombre original se respeta, porque es la que puso quien lo creo; si no, se
     * deduce del tipo, y en ultimo caso queda una generica antes que ninguna.
     */
    @Test
    fun laExtensionSaleDelNombreYSiNoDelTipo() {
        assertEquals("pdf", Attachments.extensionFor("application/pdf", "Taller_3.pdf"))
        assertEquals("jpg", Attachments.extensionFor("image/jpeg", null))
        assertEquals("m4a", Attachments.extensionFor("audio/mp4", null))
        assertEquals("bin", Attachments.extensionFor("application/x-raro", null))
    }

    @Test
    fun unNombreSinPuntoNoInventaExtension() {
        assertEquals("png", Attachments.extensionFor("image/png", "captura"))
    }

    @Test
    fun loQueLlegaSinNombreRecibeUnoQueSeEntiende() {
        assertEquals("Foto", Attachments.fallbackName(AttachmentKind.IMAGE, 0))
        assertEquals("Grabación", Attachments.fallbackName(AttachmentKind.AUDIO, 0))
        assertEquals("Archivo", Attachments.fallbackName(AttachmentKind.FILE, 0))
    }
}
