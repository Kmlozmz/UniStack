package com.unistack.app.feature_notes.domain

import kotlinx.coroutines.flow.StateFlow

interface NotesRepository {
    /** Fijadas primero, y dentro de cada grupo la más reciente arriba. */
    val notes: StateFlow<List<QuickNote>>

    fun addNote(note: QuickNote)
    fun updateNote(note: QuickNote)
    fun deleteNote(noteId: String)
    fun setPinned(noteId: String, pinned: Boolean)
}
