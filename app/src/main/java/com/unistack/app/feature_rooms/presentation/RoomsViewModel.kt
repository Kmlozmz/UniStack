package com.unistack.app.feature_rooms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.MessageKind
import com.unistack.app.feature_rooms.domain.NotifyMode
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomMaterial
import com.unistack.app.feature_rooms.domain.RoomMember
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.RoomProduce
import com.unistack.app.feature_rooms.domain.RoomRules
import com.unistack.app.feature_rooms.domain.RoomTask
import com.unistack.app.feature_rooms.domain.RoomType
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import com.unistack.app.feature_rooms.domain.WorkRoomsRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * Un solo ViewModel para toda la sección: cada pantalla pide la sala por id y le manda
 * acciones con nombre («entrega», «pide revisión»); lo que cambia lo decide [RoomLogic].
 */
@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val repository: WorkRoomsRepository,
    gradesRepository: GradesRepository,
    private val userRepository: UserRepository,
    val files: com.unistack.app.feature_rooms.data.RoomFileStore
) : ViewModel() {

    val rooms: StateFlow<List<WorkRoom>> = repository.rooms
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects
    val myName: StateFlow<String> = userRepository.userProfile
        .map { it?.preferredName?.trim()?.takeIf { n -> n.isNotEmpty() } ?: Textos.get(R.string.rooms_you) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun today(): Long = LocalDate.now().toEpochDay()
    private fun now() = System.currentTimeMillis()

    fun room(id: String): WorkRoom? = rooms.value.firstOrNull { it.id == id }
    fun update(id: String, f: (WorkRoom) -> WorkRoom) = repository.update(id, f)

    // ---------- crear ----------

    fun create(draft: RoomDraft): WorkRoom {
        val now = now()
        val meId = RoomLogic.newId("member")
        val me = RoomMember(meId, myName.value.ifBlank { Textos.get(R.string.rooms_you) }, 0, now)
        val parts = draft.parts.map { p ->
            RoomPart(RoomLogic.newId("part"), p.name, p.extent, p.sharers, if (p.mine) listOf(meId) else emptyList(), p.note)
        }
        var room = WorkRoom(
            id = RoomLogic.newId("room"),
            title = draft.title.trim().ifBlank { draft.type?.let { RoomTemplates.typeName(it) } ?: Textos.get(R.string.rooms_untitled) },
            subjectId = draft.subjectId,
            type = draft.type ?: RoomType.CERO,
            produces = draft.produces,
            split = draft.split,
            capacity = draft.capacity,
            entryOpen = draft.entryOpen,
            code = RoomLogic.newCode(),
            dueEpochDay = today() + draft.dueInDays,
            internalDates = draft.internalDates,
            rules = draft.rules,
            leaderId = meId,
            meId = meId,
            members = listOf(me),
            parts = parts,
            tasks = draft.tasks.map { RoomTask(RoomLogic.newId("task"), it.first, it.second) },
            materials = draft.materials.map {
                RoomMaterial(RoomLogic.newId("mat"), it.type, it.name, it.subtitle, meId, now, pinned = true, seenBy = listOf(meId))
            },
            createdAt = now,
            updatedAt = now,
            lastSeenEventsAt = now,
            lastSeenChatAt = now
        )
        room = RoomLogic.withInternalDates(room, today())
        repository.save(room)
        return room
    }

    fun delete(id: String) = repository.delete(id)

    // ---------- atajos con reloj ----------

    fun deliver(roomId: String, partId: String) = update(roomId) { RoomLogic.deliver(it, partId, now()) }
    fun reopenPart(roomId: String, partId: String) = update(roomId) { RoomLogic.reopen(it, partId) }
    fun write(roomId: String, partId: String, text: String, fileName: String? = null) = update(roomId) { RoomLogic.writePart(it, partId, text, fileName) }
    fun uploadSlides(roomId: String, partId: String, fileName: String, slides: Int) = update(roomId) { RoomLogic.uploadSlides(it, partId, fileName, slides) }
    fun updatePart(roomId: String, partId: String, f: (RoomPart) -> RoomPart) = update(roomId) { RoomLogic.updatePart(it, partId, f) }
    fun askReview(roomId: String, partId: String, reviewerId: String?) = update(roomId) { RoomLogic.askReview(it, partId, reviewerId, now()) }
    fun comment(roomId: String, partId: String, text: String) = update(roomId) { RoomLogic.comment(it, partId, text, now()) }
    fun attach(roomId: String, partId: String, type: MaterialType, name: String, subtitle: String) = update(roomId) { RoomLogic.attach(it, partId, type, name, subtitle, now()) }
    fun approve(roomId: String, partId: String) = update(roomId) { RoomLogic.approve(it, partId) }
    fun assign(roomId: String, partId: String, ids: List<String>) = update(roomId) { RoomLogic.assign(it, partId, ids, now()) }
    fun take(roomId: String, partId: String) = update(roomId) { RoomLogic.take(it, partId) }
    fun drawFree(roomId: String) = update(roomId) { RoomLogic.drawFree(it) }
    fun moveDue(roomId: String, partId: String, days: Int) = update(roomId) { RoomLogic.moveDue(it, partId, days) }
    fun moveFinalDue(roomId: String, newDue: Long, alsoInternal: Boolean) = update(roomId) { RoomLogic.moveFinalDue(it, newDue, alsoInternal, now()) }
    fun request(roomId: String, type: RequestType, partId: String?, note: String = "", otherPartId: String? = null) =
        update(roomId) { RoomLogic.request(it, type, partId, note, now(), otherPartId) }
    fun handleEntry(roomId: String, memberId: String) = update(roomId) { RoomLogic.handleEntry(it, memberId) }
    fun giveEntry(roomId: String, memberId: String, partId: String) = update(roomId) { RoomLogic.handleEntry(RoomLogic.assign(it, partId, listOf(memberId), now()), memberId) }
    fun review(roomId: String, partId: String, ok: Boolean, note: String) = update(roomId) { RoomLogic.review(it, partId, it.meId, ok, note, now()) }
    fun resolve(roomId: String, requestId: String, accept: Boolean) = update(roomId) { RoomLogic.resolve(it, requestId, accept) }
    fun removeMember(roomId: String, memberId: String) = update(roomId) { RoomLogic.remove(it, memberId) }
    fun passLeadership(roomId: String, memberId: String) = update(roomId) { RoomLogic.passLeadership(it, memberId) }
    fun newCode(roomId: String) = update(roomId) { it.copy(code = RoomLogic.newCode()) }
    fun setAlias(roomId: String, alias: String?) = update(roomId) { it.copy(myAlias = alias?.trim()?.ifBlank { null }) }
    fun setNotify(roomId: String, mode: NotifyMode) = update(roomId) { it.copy(notifyMode = mode, chatMuted = mode == NotifyMode.NONE) }
    fun setRules(roomId: String, rules: RoomRules) = update(roomId) { it.copy(rules = rules) }
    fun close(roomId: String, format: String, file: String? = null) = update(roomId) { RoomLogic.close(it, format, now()).copy(finalFile = file ?: it.finalFile) }
    fun addDestination(roomId: String, key: String) = update(roomId) { it.copy(finalDestinations = (it.finalDestinations + key).distinct()) }
    fun reopenRoom(roomId: String) = update(roomId) { RoomLogic.reopenRoom(it) }
    fun markEventsSeen(roomId: String) = update(roomId) { it.copy(lastSeenEventsAt = now()) }
    fun markChatSeen(roomId: String) = update(roomId) { it.copy(lastSeenChatAt = now()) }

    fun send(roomId: String, text: String, replyTo: String? = null, kind: MessageKind = MessageKind.TEXT, subtitle: String = "", seconds: Int = 0, poll: List<String> = emptyList()) =
        update(roomId) {
            RoomLogic.send(it, ChatMessage(RoomLogic.newId("msg"), it.meId, kind, text, now(), replyTo, seconds = seconds, subtitle = subtitle,
                pollOptions = poll.map { l -> com.unistack.app.feature_rooms.domain.PollOption(l) }))
        }

    fun reactMessage(roomId: String, msgId: String, emoji: String) = update(roomId) { r ->
        r.copy(messages = r.messages.map { if (it.id == msgId) it.copy(reactions = RoomLogic.react(it.reactions, emoji, r.meId)) else it })
    }

    fun vote(roomId: String, msgId: String, index: Int) = update(roomId) { r ->
        r.copy(messages = r.messages.map { if (it.id == msgId) RoomLogic.vote(it, index, r.meId) else it })
    }

    fun reactAttachment(roomId: String, partId: String, attId: String, emoji: String) = update(roomId) { r ->
        RoomLogic.updatePart(r, partId) { p -> p.copy(attachments = p.attachments.map { if (it.id == attId) it.copy(reactions = RoomLogic.react(it.reactions, emoji, r.meId)) else it }) }
    }

    fun pin(roomId: String, msgId: String?) = update(roomId) { it.copy(pinnedMessageId = msgId) }

    fun saveMessageToMaterial(roomId: String, msgId: String) = update(roomId) { r ->
        val m = r.messages.firstOrNull { it.id == msgId } ?: return@update r
        val type = when (m.kind) { MessageKind.PHOTO -> MaterialType.PHOTO; MessageKind.LINK -> MaterialType.LINK; MessageKind.VOICE -> MaterialType.AUDIO; else -> MaterialType.DOC }
        val withMat = RoomLogic.addMaterial(r, RoomMaterial(RoomLogic.newId("mat"), type, m.text, m.subtitle, m.byId ?: r.meId, now(), seenBy = listOf(r.meId),
            file = m.file, mime = m.mime, url = if (m.kind == MessageKind.LINK) m.text else ""))
        withMat.copy(messages = withMat.messages.map { if (it.id == msgId) it.copy(savedToMaterial = true) else it })
    }

    fun addMaterial(roomId: String, m: RoomMaterial) = update(roomId) { RoomLogic.addMaterial(it, m) }
    fun updateMaterial(roomId: String, id: String, f: (RoomMaterial) -> RoomMaterial) = update(roomId) { RoomLogic.updateMaterial(it, id, f) }
    fun deleteMaterial(roomId: String, id: String) = update(roomId) { r -> r.copy(materials = r.materials.filterNot { it.id == id }) }

    fun attachFile(roomId: String, partId: String, type: MaterialType, name: String, subtitle: String, file: String?, mime: String?, url: String = "") =
        update(roomId) { r ->
            RoomLogic.updatePart(r, partId) { p ->
                p.copy(attachments = p.attachments + com.unistack.app.feature_rooms.domain.PartAttachment(
                    RoomLogic.newId("att"), type, name, subtitle, r.meId, now(), file = file, mime = mime, url = url))
            }
        }
    fun removeAttachment(roomId: String, partId: String, attId: String) =
        updatePart(roomId, partId) { p -> p.copy(attachments = p.attachments.filterNot { it.id == attId }) }
    fun uploadPartFile(roomId: String, partId: String, stored: com.unistack.app.feature_rooms.data.RoomStoredFile) = update(roomId) { r ->
        val text = files.readText(stored.storedName, stored.mimeType)
        RoomLogic.updatePart(r, partId) { p ->
            p.copy(fileName = stored.displayName, file = stored.storedName, fileMime = stored.mimeType, text = text ?: p.text,
                state = if (p.state == com.unistack.app.feature_rooms.domain.PartState.PENDING) com.unistack.app.feature_rooms.domain.PartState.DRAFT else p.state)
        }
    }
    fun uploadPartSlides(roomId: String, partId: String, stored: com.unistack.app.feature_rooms.data.RoomStoredFile, slides: Int) = update(roomId) { r ->
        RoomLogic.updatePart(r, partId) { p ->
            p.copy(fileName = stored.displayName, file = stored.storedName, fileMime = stored.mimeType, slides = slides.coerceAtLeast(1),
                state = if (p.state == com.unistack.app.feature_rooms.domain.PartState.PENDING) com.unistack.app.feature_rooms.domain.PartState.DRAFT else p.state)
        }
    }
    fun commentMaterial(roomId: String, id: String, text: String) = update(roomId) { r ->
        RoomLogic.updateMaterial(r, id) { it.copy(comments = it.comments + com.unistack.app.feature_rooms.domain.RoomComment(RoomLogic.newId("com"), r.meId, text, now())) }
    }
    fun reactMaterial(roomId: String, id: String, emoji: String) = update(roomId) { r ->
        RoomLogic.updateMaterial(r, id) { it.copy(reactions = RoomLogic.react(it.reactions, emoji, r.meId)) }
    }
    fun seeMaterial(roomId: String, id: String) = update(roomId) { r ->
        RoomLogic.updateMaterial(r, id) { if (r.meId in it.seenBy) it else it.copy(seenBy = it.seenBy + r.meId) }
    }
    fun setBibliographyStyle(roomId: String, style: String) = update(roomId) { it.copy(bibliographyStyle = style) }
    fun rename(roomId: String, title: String, subjectId: String?) = update(roomId) { it.copy(title = title.trim().ifBlank { it.title }, subjectId = subjectId) }
    fun setCapacity(roomId: String, capacity: Int) = update(roomId) { it.copy(capacity = capacity.coerceIn(2, 12)) }
    fun setEntryOpen(roomId: String, open: Boolean) = update(roomId) { it.copy(entryOpen = open) }
    fun leave(roomId: String) = update(roomId) { r -> RoomLogic.remove(r, r.meId) }
    fun sendFile(roomId: String, kind: MessageKind, stored: com.unistack.app.feature_rooms.data.RoomStoredFile, subtitle: String, seconds: Int = 0) =
        update(roomId) {
            RoomLogic.send(it, ChatMessage(RoomLogic.newId("msg"), it.meId, kind, stored.displayName, now(), subtitle = subtitle, seconds = seconds,
                file = stored.storedName, mime = stored.mimeType))
        }

    fun newMaterial(room: WorkRoom, type: MaterialType, name: String, subtitle: String, note: String, partId: String?, tagged: List<String>,
                    author: String = "", year: String = "", publisher: String = "", file: String? = null, mime: String? = null, url: String = "") =
        RoomMaterial(RoomLogic.newId("mat"), type, name, subtitle, room.meId, now(), note, partId, tagged, seenBy = listOf(room.meId),
            author = author, year = year, publisher = publisher, file = file, mime = mime, url = url)
}

/** Lo que se va armando en «Crear sala» antes de guardarla. */
data class RoomDraft(
    val title: String = "",
    val subjectId: String? = null,
    val type: RoomType? = null,
    val produces: RoomProduce = RoomProduce.DOC,
    val parts: List<DraftPart> = emptyList(),
    val usedSuggestions: Set<String> = emptySet(),
    val split: SplitMode = SplitMode.ASSIGN,
    val capacity: Int = 4,
    val dueInDays: Int = 14,
    val internalDates: Boolean = true,
    val tasks: List<Pair<String, Boolean>> = emptyList(),
    val materials: List<DraftMaterial> = emptyList(),
    val rules: RoomRules = RoomRules(),
    val entryOpen: Boolean = true
)

data class DraftPart(
    val key: Long,
    val name: String,
    val extent: Int = 0,
    val sharers: Int = 1,
    val mine: Boolean = false,
    val note: String = ""
)

data class DraftMaterial(val key: Long, val type: MaterialType, val name: String, val subtitle: String)
