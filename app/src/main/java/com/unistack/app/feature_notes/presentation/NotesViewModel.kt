package com.unistack.app.feature_notes.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesRepository
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
    private val userRepository: UserRepository
) : ViewModel() {

    val notes: StateFlow<List<QuickNote>> = notesRepository.notes
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects
    val userProfile = userRepository.userProfile

    fun noteById(noteId: String?): QuickNote? {
        if (noteId.isNullOrBlank()) return null
        return notes.value.firstOrNull { it.id == noteId }
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

        if (NoteText.isEmpty(trimmed)) {
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
