package com.unistack.app.feature_rooms.domain

/**
 * Una sala de trabajo en grupo.
 *
 * Se guarda entera como un documento —como se guardará en la nube— y no repartida en tablas:
 * todo lo que pasa dentro (partes, aportes, chat, material, pedidos, novedades) vive y viaja
 * con la sala. Hoy es local; cuando llegue Firebase cada sala será un documento igual a este.
 */
data class WorkRoom(
    val id: String,
    val title: String,
    val subjectId: String? = null,
    val type: RoomType = RoomType.CERO,
    val produces: RoomProduce = RoomProduce.DOC,
    val split: SplitMode = SplitMode.ASSIGN,
    val capacity: Int = 6,
    val entryOpen: Boolean = true,
    val code: String,
    val dueEpochDay: Long,
    val internalDates: Boolean = true,
    val rules: RoomRules = RoomRules(),
    val leaderId: String,
    val meId: String,
    val members: List<RoomMember> = emptyList(),
    val removedIds: List<String> = emptyList(),
    /** Quienes entraron y el líder ya atendió (les dio parte o los dejó «para después»). */
    val entryHandledIds: List<String> = emptyList(),
    val parts: List<RoomPart> = emptyList(),
    val tasks: List<RoomTask> = emptyList(),
    val materials: List<RoomMaterial> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val events: List<RoomEvent> = emptyList(),
    val requests: List<RoomRequest> = emptyList(),
    val pinnedMessageId: String? = null,
    val chatMuted: Boolean = false,
    val notifyMode: NotifyMode = NotifyMode.ALL,
    val myAlias: String? = null,
    val bibliographyStyle: String = "APA",
    val lastSeenEventsAt: Long = 0L,
    val lastSeenChatAt: Long = 0L,
    val createdAt: Long,
    val updatedAt: Long,
    val closedAt: Long? = null,
    val finalFormat: String? = null,
    /** El archivo del trabajo final ya creado (nombre guardado en los archivos de la sala). */
    val finalFile: String? = null,
    val finalDestinations: List<String> = emptyList(),
    val periodLabel: String? = null,
    val finalGrade: Double? = null
) {
    val isLeader: Boolean get() = meId == leaderId
    val isClosed: Boolean get() = closedAt != null
    val activeMembers: List<RoomMember> get() = members.filter { it.id !in removedIds }
    fun member(id: String?): RoomMember? = members.firstOrNull { it.id == id }
    fun nameOf(id: String?): String = when {
        id == null -> ""
        id == meId && myAlias != null -> myAlias
        else -> member(id)?.name.orEmpty()
    }
    val allDelivered: Boolean get() = parts.isNotEmpty() && parts.all { it.state == PartState.DELIVERED }
    val deliveredCount: Int get() = parts.count { it.state == PartState.DELIVERED }
    fun part(id: String?): RoomPart? = parts.firstOrNull { it.id == id }
    fun partsOf(memberId: String): List<RoomPart> = parts.filter { memberId in it.ownerIds }
}

enum class RoomType { ENSAYO, LAB, EXPO, PROYECTO, INVESTIGACION, RESENA, CAMPO, MAQUETA, CERO }
enum class RoomProduce { DOC, SLIDES, BOTH, NOTHING }
enum class SplitMode { ASSIGN, FREE, DRAW, MIXED }
enum class NotifyMode { ALL, MINE, NONE }

data class RoomRules(
    val change: ChangeRule = ChangeRule.ASK,
    val late: LateRule = LateRule.MARKED,
    val structure: StructureRule = StructureRule.LEADER,
    val visibility: VisibilityRule = VisibilityRule.ALWAYS,
    val reminders: ReminderRule = ReminderRule.ONE_DAY
)

enum class ChangeRule { NO, ASK, FREE }
enum class LateRule { NO, MARKED, GRACE }
enum class StructureRule { LEADER, ALL }
enum class VisibilityRule { ALWAYS, ON_SUBMIT }
enum class ReminderRule { NONE, ONE_DAY, THREE_DAYS }

data class RoomMember(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val joinedAt: Long
)

/** Cuántos la hacen: uno, dos o todos (se guarda como 1, 2 o -1). */
data class RoomPart(
    val id: String,
    val name: String,
    val extent: Int = 0,
    val sharers: Int = 1,
    val ownerIds: List<String> = emptyList(),
    val note: String = "",
    val dueEpochDay: Long? = null,
    val state: PartState = PartState.PENDING,
    val text: String = "",
    val fileName: String? = null,
    /** El archivo subido, copiado dentro de la app (nombre guardado) y su tipo. */
    val file: String? = null,
    val fileMime: String? = null,
    val slides: Int = 0,
    val presenterId: String? = null,
    val minutes: Int = 3,
    val script: String = "",
    val approved: Boolean = false,
    val review: PartReview? = null,
    val reviewerId: String? = null,
    val sharedMode: SharedMode = SharedMode.PIECES,
    val attachments: List<PartAttachment> = emptyList(),
    val comments: List<RoomComment> = emptyList(),
    val deliveredAt: Long? = null
) {
    val isFree: Boolean get() = ownerIds.isEmpty()
    val hasContent: Boolean get() = text.isNotBlank() || fileName != null || slides > 0
    val words: Int get() = text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
}

enum class PartState { PENDING, DRAFT, REVIEW, DELIVERED }
enum class SharedMode { PIECES, ONE_WRITES }

data class PartReview(val byId: String, val ok: Boolean, val note: String, val at: Long)

data class PartAttachment(
    val id: String,
    val type: MaterialType,
    val name: String,
    val subtitle: String,
    val byId: String,
    val createdAt: Long,
    val reactions: Map<String, List<String>> = emptyMap(),
    val file: String? = null,
    val mime: String? = null,
    val url: String = ""
)

data class RoomComment(
    val id: String,
    val byId: String,
    val text: String,
    val createdAt: Long,
    val isReview: Boolean = false
)

data class RoomTask(val id: String, val name: String, val mine: Boolean = false, val done: Boolean = false)

enum class MaterialType { LINK, DOC, PHOTO, VIDEO, AUDIO, SOURCE, NOTE }

data class RoomMaterial(
    val id: String,
    val type: MaterialType,
    val name: String,
    val subtitle: String = "",
    val byId: String,
    val createdAt: Long,
    val note: String = "",
    val partId: String? = null,
    val tagged: List<String> = emptyList(),
    val pinned: Boolean = false,
    val seenBy: List<String> = emptyList(),
    val reactions: Map<String, List<String>> = emptyMap(),
    val comments: List<RoomComment> = emptyList(),
    val author: String = "",
    val year: String = "",
    val publisher: String = "",
    val file: String? = null,
    val mime: String? = null,
    val url: String = ""
)

enum class MessageKind { TEXT, PHOTO, FILE, LINK, VOICE, POLL }

data class ChatMessage(
    val id: String,
    val byId: String?,
    val kind: MessageKind = MessageKind.TEXT,
    val text: String = "",
    val createdAt: Long,
    val replyToId: String? = null,
    val reactions: Map<String, List<String>> = emptyMap(),
    val pollOptions: List<PollOption> = emptyList(),
    val seconds: Int = 0,
    val subtitle: String = "",
    val savedToMaterial: Boolean = false,
    val file: String? = null,
    val mime: String? = null,
    /** Aviso automático: sólo entregas y cambios de fecha (opción c). */
    val systemType: String? = null,
    /** Lo cambió quien lo mandó: sale «editado» junto a la hora. */
    val edited: Boolean = false,
    /** Borrado para todos: queda el hueco «Se borró este mensaje». */
    val deleted: Boolean = false,
    /** La onda de una nota de voz (0-100), unas 40 muestras. */
    val wave: List<Int> = emptyList()
)

data class PollOption(val label: String, val voters: List<String> = emptyList())

enum class EventType { MENTION, COMMENT_ON_MINE, DUE_CHANGED, MATERIAL, DELIVERED, COMMENT, JOINED, REVIEW_ASKED, ASSIGNED, TAGGED }

data class RoomEvent(
    val id: String,
    val type: EventType,
    val actorId: String,
    val text: String,
    val detail: String = "",
    val createdAt: Long,
    val forMe: Boolean = false,
    val targetPartId: String? = null
)

enum class RequestType { MORE_TIME, SWAP, HELP, JOIN, DROP }

data class RoomRequest(
    val id: String,
    val type: RequestType,
    val fromId: String,
    val partId: String? = null,
    val otherPartId: String? = null,
    val note: String = "",
    val createdAt: Long,
    val resolution: Boolean? = null
)
