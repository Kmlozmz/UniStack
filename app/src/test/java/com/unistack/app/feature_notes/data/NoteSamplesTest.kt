package com.unistack.app.feature_notes.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.unistack.app.TextosDePrueba

/**
 * Las notas de ejemplo tienen que ejemplificar algo.
 *
 * Es facil que un juego de datos falsos se quede a medias sin que nadie lo note: se anade una
 * funcion nueva y el señuelo sigue siendo el de antes, asi que al mirar la pantalla no se ve
 * justo lo que se acaba de construir. Esta prueba las recorre todas y comprueba que entre ellas
 * aparece cada cosa que el editor sabe pintar y cada estado en que puede estar una nota.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class NoteSamplesTest {

    private lateinit var store: NoteAttachmentStore

    @Before
    fun setUp() {
        TextosDePrueba.instalar()
        store = NoteAttachmentStore(ApplicationProvider.getApplicationContext<Context>())
    }

    private fun muestras() = NoteSamples.build(
        store = store,
        subjectIds = listOf("s1", "s2", "s3"),
        now = 1_800_000_000_000L
    )

    @Test
    fun ningunaSeRepite() {
        val todas = muestras()
        assertEquals(13, todas.size)
        assertEquals(13, todas.map { it.note.body }.toSet().size)
        assertEquals(13, todas.map { it.note.id }.toSet().size)
    }

    /**
     * Los tres estados que no se ven en la lista.
     *
     * Archivada, en la papelera y con el aviso ya pasado son justo los que obligaban a
     * archivar, borrar y esperar a mano para poder mirar esas pantallas.
     */
    @Test
    fun estanLosEstadosQueNoSalenEnLaLista() {
        val todas = muestras()
        assertTrue("falta una archivada", todas.any { it.note.archived })
        assertTrue("falta una en la papelera", todas.any { it.note.deletedAt != null })
        assertTrue(
            "falta una con el aviso ya vencido",
            todas.any { it.note.reminderAt != null && it.note.reminderAt!! < 1_800_000_000_000L }
        )
        assertTrue(
            "falta una con el aviso por llegar",
            todas.any { it.note.reminderAt != null && it.note.reminderAt!! > 1_800_000_000_000L }
        )
    }

    /** Con una sola no se despliega el carrusel: hace falta una nota con varios adjuntos. */
    @Test
    fun hayUnaConVariosAdjuntos() {
        val mayor = muestras().maxOf { it.attachments.size }
        assertTrue("ninguna nota lleva mas de un adjunto", mayor >= 3)
    }

    /*
     * Lo importante: entre las ocho tiene que salir todo lo que el motor sabe formatear. Si
     * manana se anade una marca nueva y no entra aqui, esta prueba no lo dice —no puede— pero
     * si evita que se caiga alguna de las que ya hay.
     */
    @Test
    fun entreTodasSaleCadaCosaQueElEditorSabePintar() {
        val estilos = muestras()
            .flatMap { NoteMarkdown.parse(it.note.body).spans }
            .map { it.style }
            .toSet()

        val imprescindibles = listOf(
            NoteStyle.TITULO1,
            NoteStyle.TITULO2,
            NoteStyle.TITULO3,
            NoteStyle.NEGRITA,
            NoteStyle.CURSIVA,
            NoteStyle.TACHADO,
            NoteStyle.CODIGO,
            NoteStyle.BLOQUE_CODIGO,
            NoteStyle.CITA,
            NoteStyle.VINETA,
            NoteStyle.NUMERO,
            NoteStyle.CASILLA,
            NoteStyle.CASILLA_HECHA,
            NoteStyle.LINEA,
            NoteStyle.ENLACE,
            NoteStyle.TABLA
        )
        imprescindibles.forEach { estilo ->
            assertTrue("falta $estilo en las notas de ejemplo", estilo in estilos)
        }
    }

    /** Alguna es una lista pura, para poder ver el editor de listas y el anillo de avance. */
    @Test
    fun hayUnaListaEntera() {
        val listas = muestras().count {
            com.unistack.app.feature_notes.domain.NoteChecklist.isChecklist(it.note.body)
        }
        assertTrue("no hay ninguna lista pura", listas >= 1)
    }

    @Test
    fun salenLasDosManerasDeEscribir() {
        val formatos = muestras().map { it.note.format }.toSet()
        assertTrue(NoteFormat.MARKDOWN in formatos)
        assertTrue(NoteFormat.PLAIN in formatos)
    }

    @Test
    fun hayUnaFijadaYUnaSinMateria() {
        val todas = muestras()
        assertEquals(1, todas.count { it.note.pinned })
        assertTrue(todas.any { it.note.subjectId == null })
        assertTrue(todas.any { it.note.subjectId != null })
    }

    /** Varios dias, para que el cuaderno tenga mas de una cabecera que enseñar. */
    @Test
    fun estanRepartidasEnVariosDias() {
        val dias = muestras().map { it.note.updatedAt / (24L * 60 * 60 * 1000) }.toSet()
        assertTrue("todas caen el mismo dia", dias.size >= 4)
    }

    /** Los tres tipos de adjunto, y sus archivos escritos de verdad. */
    @Test
    fun hayFotoArchivoYGrabacion() {
        val adjuntos = muestras().flatMap { it.attachments }
        val tipos = adjuntos.map { it.kind }.toSet()
        assertTrue("falta la foto", AttachmentKind.IMAGE in tipos)
        assertTrue("falta el archivo", AttachmentKind.FILE in tipos)
        assertTrue("falta la grabacion", AttachmentKind.AUDIO in tipos)

        adjuntos.forEach { adjunto ->
            assertTrue(
                "el archivo de " + adjunto.displayName + " no se escribio",
                store.exists(adjunto.storedName)
            )
        }
    }

    @Test
    fun lasGrabacionesTienenDuracionYElRestoNo() {
        val adjuntos = muestras().flatMap { it.attachments }
        val audios = adjuntos.filter { it.kind == AttachmentKind.AUDIO }
        assertTrue("no hay ninguna grabacion", audios.isNotEmpty())
        audios.forEach {
            assertTrue(it.durationMillis != null && it.durationMillis!! > 0)
        }
        adjuntos.filter { it.kind != AttachmentKind.AUDIO }.forEach {
            assertTrue(it.durationMillis == null)
        }
    }

    /**
     * Cada adjunto cuelga de su nota, con su identificador de verdad.
     *
     * El identificador real lleva un sufijo al azar que no existe hasta que la nota esta hecha,
     * asi que es facil dejar el nombre base escrito y que el enlace quede roto. Paso una vez.
     */
    @Test
    fun cadaAdjuntoApuntaASuNota() {
        muestras().forEach { muestra ->
            muestra.attachments.forEach { adjunto ->
                assertEquals(muestra.note.id, adjunto.noteId)
            }
        }
    }

    /** Se reconocen para poder quitarlas todas juntas sin tocar las notas de verdad. */
    @Test
    fun seDistinguenDeLasNotasDeVerdad() {
        muestras().forEach { assertTrue(NoteSamples.isSample(it.note.id)) }
        assertTrue(!NoteSamples.isSample("note-abc"))
    }

    @Test
    fun sinMateriasNingunaQuedaVinculada() {
        val sinMaterias = NoteSamples.build(store, emptyList(), 1_800_000_000_000L)
        assertTrue(sinMaterias.all { it.note.subjectId == null })
        assertEquals(13, sinMaterias.size)
    }
}
