package com.unistack.app.feature_notes.domain

import kotlinx.coroutines.flow.StateFlow

interface NotesRepository {
    /** Fijadas primero, y dentro de cada grupo la más reciente arriba. */
    val notes: StateFlow<List<QuickNote>>

    /** Todo lo colgado de todas las notas, para repartirlo por `noteId` donde haga falta. */
    val attachments: StateFlow<List<NoteAttachment>>

    fun addNote(note: QuickNote)
    fun updateNote(note: QuickNote)

    /** Se lleva por delante los adjuntos de la nota, filas y archivos. */
    fun deleteNote(noteId: String)
    fun setPinned(noteId: String, pinned: Boolean)

    /** Archivar, mandar a la papelera o devolver a la lista, sin tocar el contenido. */
    fun setState(noteId: String, archived: Boolean, deletedAt: Long?)

    /** Borra de verdad lo que lleve en la papelera desde antes de [olderThan]. */
    fun purgeTrash(olderThan: Long)

    fun addAttachment(attachment: NoteAttachment)
    fun deleteAttachment(attachmentId: String)
}
