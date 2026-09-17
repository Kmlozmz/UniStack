package com.unistack.app.feature_profile.presentation

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_notes.data.NoteAttachmentStore
import com.unistack.app.feature_tasks.data.TaskAttachmentStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/*
 * La copia es un zip con el JSON y los archivos, y restaurar tiene que devolverlos a su sitio.
 *
 * Es lo que se pierde al desinstalar: sin los archivos dentro, las notas volvían con el nombre
 * de su foto y sin la foto.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class BackupArchiveTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun attachmentsAndPhotoTravelInsideTheZip() {
        val foto = NoteAttachmentStore(context).file("note-foto.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val audio = TaskAttachmentStore(context).file("task-audio.m4a").apply { writeBytes(byteArrayOf(4, 5)) }
        val retrato = File(context.filesDir, "profile-photo-123.jpg").apply { writeBytes(byteArrayOf(6)) }
        val json = JSONObject()
            .put("schemaVersion", 12)
            .put("profile", JSONObject().put("photoFile", retrato.name))
            .put("notes", conAdjunto("note-1", foto.name))
            .put("tasks", conAdjunto("task-1", audio.name))
            .toString()

        val zip = ByteArrayOutputStream().also { BackupArchive.write(context, it, json) }.toByteArray()
        foto.delete()
        audio.delete()
        retrato.delete()

        assertEquals(json, BackupArchive.readJson(ByteArrayInputStream(zip)))
        val direccion = BackupArchive.restoreFiles(context, ByteArrayInputStream(zip), json)

        assertArrayEquals(byteArrayOf(1, 2, 3), foto.readBytes())
        assertArrayEquals(byteArrayOf(4, 5), audio.readBytes())
        assertArrayEquals(byteArrayOf(6), retrato.readBytes())
        assertEquals(Uri.fromFile(retrato).toString(), direccion)
    }

    @Test
    fun aPlainJsonFromBeforeStillReads() {
        val json = """{"schemaVersion": 11, "notes": []}"""

        assertEquals(json, BackupArchive.readJson(ByteArrayInputStream(json.toByteArray())))
        assertNull(BackupArchive.restoreFiles(context, ByteArrayInputStream(json.toByteArray()), json))
    }

    @Test
    fun nothingIsWrittenOutsideTheAttachmentFolders() {
        val trampa = "../../trampa.txt"
        val json = JSONObject()
            .put("schemaVersion", 12)
            .put("notes", conAdjunto("note-1", trampa))
            .toString()
        val zip = ByteArrayOutputStream().also { salida ->
            ZipOutputStream(salida).use { archivo ->
                archivo.putNextEntry(ZipEntry("unistack-copia.json"))
                archivo.write(json.toByteArray())
                archivo.closeEntry()
                archivo.putNextEntry(ZipEntry("adjuntos/notas/$trampa"))
                archivo.write(byteArrayOf(9))
                archivo.closeEntry()
            }
        }.toByteArray()

        BackupArchive.restoreFiles(context, ByteArrayInputStream(zip), json)

        assertFalse(File(context.filesDir, "trampa.txt").exists())
        assertFalse(File(context.filesDir.parentFile, "trampa.txt").exists())
    }

    private fun conAdjunto(id: String, storedName: String): JSONArray = JSONArray().put(
        JSONObject()
            .put("id", id)
            .put("attachments", JSONArray().put(JSONObject().put("id", "$id-adjunto").put("storedName", storedName)))
    )
}
