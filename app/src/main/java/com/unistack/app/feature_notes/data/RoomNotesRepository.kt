package com.unistack.app.feature_notes.data

import com.unistack.app.feature_notes.data.local.NoteDao
import com.unistack.app.feature_notes.data.local.toDomain
import com.unistack.app.feature_notes.data.local.toEntity
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

class RoomNotesRepository(
    private val noteDao: NoteDao,
    private val userRepository: UserRepository,
    /** El texto de la hoja de antes, si queda alguno por rescatar. Se pide una sola vez. */
    private val legacySheet: () -> String? = { null }
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
                body = note.body,
                subjectId = note.subjectId,
                format = note.format.name,
                pinned = note.pinned,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteNote(noteId: String) {
        scope.launch { noteDao.deleteNoteById(noteId, userIds) }
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
