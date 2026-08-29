package com.unistack.app.feature_notes.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.data.NoteAttachmentStore
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
    private val gradesRepository: GradesRepository,
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val attachmentStore: NoteAttachmentStore
) : ViewModel() {

    val notes: StateFlow<List<QuickNote>> = notesRepository.notes
    val attachments: StateFlow<List<NoteAttachment>> = notesRepository.attachments
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects
    val sessions: StateFlow<List<ClassSession>> = scheduleRepository.sessions
    val userProfile = userRepository.userProfile

    fun noteById(noteId: String?): QuickNote? {
        if (noteId.isNullOrBlank()) return null
        return notes.value.firstOrNull { it.id == noteId }
    }

    fun attachmentsOf(noteId: String?): List<NoteAttachment> {
        if (noteId == null) return emptyList()
        return attachments.value.filter { it.noteId == noteId }
    }

    fun subjectById(subjectId: String?): Subject? {
        if (subjectId == null) return null
        return subjects.value.firstOrNull { it.id == subjectId }
    }

    /**
     * Guarda lo escrito y devuelve el identificador de la nota, o nulo si no había nada.
     *
     * Una nota en blanco no se guarda: abrir el editor y salir sin escribir no puede dejar
     * papeles vacíos por la lista. Y si se borra todo el texto de una nota que ya existía, se
     * borra la nota —dejar una tarjeta vacía es peor que no dejar nada.
     */
    fun saveNote(
        noteId: String?,
        body: String,
        subjectId: String?,
        format: NoteFormat = defaultFormat()
    ): String? {
        val existing = noteById(noteId)
        val trimmed = body.trimEnd()

        /*
         * Una nota sin texto se descarta, salvo que lleve algo colgado.
         *
         * Una foto de la pizarra sin una sola palabra es una nota perfectamente valida —de
         * hecho es la mas comun—, asi que «vacia» no puede significar solo «sin letras».
         */
        if (NoteText.isEmpty(trimmed) && attachmentsOf(existing?.id).isEmpty()) {
            if (existing != null) notesRepository.deleteNote(existing.id)
            return null
        }

        val now = System.currentTimeMillis()
        val validSubjectId = subjectId?.takeIf { id -> subjects.value.any { it.id == id } }

        if (existing == null) {
            val note = QuickNote(
                id = "note-" + UUID.randomUUID(),
                body = trimmed,
                subjectId = validSubjectId,
                format = format,
                pinned = false,
                createdAt = now,
                updatedAt = now
            )
            notesRepository.addNote(note)
            return note.id
        }

        // Sin cambios no se toca nada: reescribir por salir de la pantalla movería la nota al
        // principio de la lista sin que nadie haya escrito una letra.
        if (existing.body == trimmed &&
            existing.subjectId == validSubjectId &&
            existing.format == format
        ) {
            return existing.id
        }

        notesRepository.updateNote(
            existing.copy(
                body = trimmed,
                subjectId = validSubjectId,
                format = format,
                updatedAt = now
            )
        )
        return existing.id
    }

/**
     * El identificador de la nota que se esta escribiendo, creandola si todavia no existe.
     *
     * Hace falta para colgar algo: un adjunto necesita una nota a la que pertenecer, y en una
     * nota nueva el primer gesto puede ser perfectamente la foto y no la primera letra.
     */
    fun ensureNoteId(noteId: String?, body: String, subjectId: String?, format: NoteFormat): String {
        val existente = noteById(noteId)
        if (existente != null) return existente.id
        val now = System.currentTimeMillis()
        val nota = QuickNote(
            id = "note-" + UUID.randomUUID(),
            body = body.trimEnd(),
            subjectId = subjectId?.takeIf { id -> subjects.value.any { it.id == id } },
            format = format,
            pinned = false,
            createdAt = now,
            updatedAt = now
        )
        notesRepository.addNote(nota)
        return nota.id
    }

    /**
     * Copia dentro de UniStack lo que se acaba de elegir y lo cuelga de la nota.
     *
     * Devuelve falso si no se pudo leer o si pasa del tamano maximo, para que la pantalla lo
     * diga en vez de dejar un hueco donde el usuario espera su archivo.
     */
    fun attach(noteId: String, uri: Uri): Boolean {
        if (attachmentsOf(noteId).size >= Attachments.MAX_PER_NOTE) return false
        val guardado = attachmentStore.import(uri) ?: return false
        val now = System.currentTimeMillis()
        notesRepository.addAttachment(
            NoteAttachment(
                id = "att-" + UUID.randomUUID(),
                noteId = noteId,
                kind = guardado.kind,
                displayName = guardado.displayName,
                storedName = guardado.storedName,
                mimeType = guardado.mimeType,
                sizeBytes = guardado.sizeBytes,
                durationMillis = null,
                createdAt = now
            )
        )
        return true
    }

    /** Lo mismo, para un archivo que ya se escribio dentro (la camara y el grabador). */
    fun attachStoredFile(
        noteId: String,
        storedName: String,
        displayName: String,
        mimeType: String,
        kind: AttachmentKind,
        durationMillis: Long? = null
    ): Boolean {
        val archivo = attachmentStore.file(storedName)
        if (!archivo.exists() || archivo.length() == 0L) {
            attachmentStore.delete(storedName)
            return false
        }
        if (attachmentsOf(noteId).size >= Attachments.MAX_PER_NOTE) {
            attachmentStore.delete(storedName)
            return false
        }
        val now = System.currentTimeMillis()
        notesRepository.addAttachment(
            NoteAttachment(
                id = "att-" + UUID.randomUUID(),
                noteId = noteId,
                kind = kind,
                displayName = displayName,
                storedName = storedName,
                mimeType = mimeType,
                sizeBytes = archivo.length(),
                durationMillis = durationMillis,
                createdAt = now
            )
        )
        return true
    }

    fun removeAttachment(attachmentId: String) = notesRepository.deleteAttachment(attachmentId)

    fun attachmentFileExists(attachment: NoteAttachment): Boolean =
        attachmentStore.exists(attachment.storedName)

    fun attachmentUri(attachment: NoteAttachment): Uri? =
        runCatching { attachmentStore.shareUri(attachment.storedName) }.getOrNull()

    fun attachmentPath(attachment: NoteAttachment): String =
        attachmentStore.file(attachment.storedName).absolutePath

    fun newAttachmentFile(extension: String): Pair<String, java.io.File> =
        attachmentStore.newFileFor(extension)

    fun discardStoredFile(storedName: String) = attachmentStore.delete(storedName)

    fun deleteNote(noteId: String) = notesRepository.deleteNote(noteId)

    fun setPinned(noteId: String, pinned: Boolean) = notesRepository.setPinned(noteId, pinned)

    fun setLayout(layout: NotesLayout) {
        val profile = userProfile.value ?: return
        if (profile.notesLayout == layout) return
        userRepository.saveUserProfile(
            profile.copy(notesLayout = layout, updatedAt = System.currentTimeMillis())
        )
    }

    /**
     * Con qué formato nace una nota nueva.
     *
     * Sale del ajuste global, que es solo el punto de partida: dentro del editor se cambia esta
     * nota sin tocar el ajuste, y cada nota se guarda con el suyo.
     */
    fun defaultFormat(): NoteFormat = userProfile.value?.noteFormatDefault ?: NoteFormat.MARKDOWN

    fun setDefaultFormat(format: NoteFormat) {
        val profile = userProfile.value ?: return
        if (profile.noteFormatDefault == format) return
        userRepository.saveUserProfile(
            profile.copy(noteFormatDefault = format, updatedAt = System.currentTimeMillis())
        )
    }
}
