package com.unistack.app.feature_notes.data

import com.unistack.app.feature_notes.data.local.NoteAttachmentDao
import com.unistack.app.feature_notes.data.local.NoteDao
import com.unistack.app.feature_notes.data.local.toDomain
import com.unistack.app.feature_notes.data.local.toEntity
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Quien se lleva los archivos cuando desaparece la fila que los nombraba.
 *
 * Es una funcion y no el almacen entero para que el repositorio se pueda probar sin tocar el
 * disco, que es donde estan las pruebas que de verdad importan: las de que ninguna columna se
 * quede sin escribir.
 */
fun interface NoteFileVault {
    fun remove(storedNames: List<String>)
}

class RoomNotesRepository(
    private val noteDao: NoteDao,
    private val attachmentDao: NoteAttachmentDao,
    private val userRepository: UserRepository,
    /** El texto de la hoja de antes, si queda alguno por rescatar. Se pide una sola vez. */
    private val legacySheet: () -> String? = { null },
    private val fileVault: NoteFileVault = NoteFileVault { }
) : NotesRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userId: String
        get() = UserIds.normalize(userRepository.currentUser.value.userId)

    private val userIds: List<String>
        get() = UserIds.storageIdsFor(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val notes: StateFlow<List<QuickNote>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            noteDao.observeNotesForUsers(ids).map { entities -> entities.map { it.toDomain() } }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val attachments: StateFlow<List<NoteAttachment>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            attachmentDao.observeAttachmentsForUsers(ids).map { filas -> filas.map { it.toDomain() } }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        scope.launch { rescueLegacySheet() }
    }

    override fun addNote(note: QuickNote) {
        scope.launch { noteDao.insertNote(note.toEntity(userId)) }
    }

    override fun updateNote(note: QuickNote) {
        scope.launch {
            noteDao.updateNoteFields(
                noteId = note.id,
                userIds = userIds,
                title = note.title,
                body = note.body,
                subjectId = note.subjectId,
                format = note.format.name,
                pinned = note.pinned,
                reminderAt = note.reminderAt,
                colorArgb = note.colorArgb,
                archived = note.archived,
                deletedAt = note.deletedAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * Borrar una nota se lleva tambien lo que colgaba de ella.
     *
     * Primero se apuntan los nombres, luego se van las filas y al final los archivos. Al reves
     * —borrar la fila y despues buscar los archivos— dejaria copias huerfanas ocupando sitio sin
     * que nada en la app supiera de ellas.
     */
    override fun deleteNote(noteId: String) {
        scope.launch {
            val ids = userIds
            val archivos = attachmentDao.storedNamesOfNote(noteId, ids)
            attachmentDao.deleteAttachmentsOfNote(noteId, ids)
            noteDao.deleteNoteById(noteId, ids)
            if (archivos.isNotEmpty()) fileVault.remove(archivos)
        }
    }

    /**
     * Mover a la papelera, archivar, o devolver a la lista.
     *
     * Es una consulta propia y no un `updateNote` porque no toca el contenido: cambiar el estado
     * de una nota mientras alguien la tiene abierta no puede pisarle lo que esta escribiendo.
     */
    override fun setState(noteId: String, archived: Boolean, deletedAt: Long?) {
        scope.launch {
            noteDao.updateNoteState(
                noteId = noteId,
                userIds = userIds,
                archived = archived,
                deletedAt = deletedAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * Vaciar lo que lleve mas de una semana en la papelera.
     *
     * Una papelera que no se vacia sola deja de ser una papelera y pasa a ser un almacen: acaba
     * ocupando mas que las notas de verdad, con sus fotos dentro.
     */
    override fun purgeTrash(olderThan: Long) {
        scope.launch {
            val ids = userIds
            val caducadas = noteDao.expiredInTrash(ids, olderThan)
            caducadas.forEach { id ->
                val archivos = attachmentDao.storedNamesOfNote(id, ids)
                attachmentDao.deleteAttachmentsOfNote(id, ids)
                noteDao.deleteNoteById(id, ids)
                if (archivos.isNotEmpty()) fileVault.remove(archivos)
            }
        }
    }

    override fun setPinned(noteId: String, pinned: Boolean) {
        scope.launch {
            noteDao.updateNotePinned(
                noteId = noteId,
                userIds = userIds,
                pinned = pinned,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun addAttachment(attachment: NoteAttachment) {
        scope.launch { attachmentDao.insertAttachment(attachment.toEntity(userId)) }
    }

    override fun deleteAttachment(attachmentId: String) {
        scope.launch {
            val ids = userIds
            val archivo = attachmentDao.storedNameOf(attachmentId, ids)
            attachmentDao.deleteAttachmentById(attachmentId, ids)
            if (archivo != null) fileVault.remove(listOf(archivo))
        }
    }

    /**
     * La hoja de antes se convierte en la primera nota.
     *
     * Espera a que el perfil esté leído de disco antes de nada: el identificador de usuario llega
     * de la misma lectura, y sellar la nota con el usuario local mientras el de verdad todavía no
     * ha cargado la dejaría guardada donde nadie la ve.
     */
    private suspend fun rescueLegacySheet() {
        userRepository.userProfile.filterNotNull().first()
        val text = legacySheet() ?: return
        val now = System.currentTimeMillis()
        noteDao.insertNote(
            QuickNote(
                id = "note-" + UUID.randomUUID(),
                body = text,
                subjectId = null,
                format = NoteFormat.PLAIN,
                pinned = false,
                createdAt = now,
                updatedAt = now
            ).toEntity(userId)
        )
    }
}
