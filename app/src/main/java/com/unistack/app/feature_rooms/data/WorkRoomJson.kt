package com.unistack.app.feature_rooms.data

import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.EventType
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.MessageKind
import com.unistack.app.feature_rooms.domain.NotifyMode
import com.unistack.app.feature_rooms.domain.PartAttachment
import com.unistack.app.feature_rooms.domain.PartReview
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.PollOption
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomComment
import com.unistack.app.feature_rooms.domain.RoomEvent
import com.unistack.app.feature_rooms.domain.RoomMaterial
import com.unistack.app.feature_rooms.domain.RoomMember
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.RoomProduce
import com.unistack.app.feature_rooms.domain.RoomRequest
import com.unistack.app.feature_rooms.domain.RoomRules
import com.unistack.app.feature_rooms.domain.RoomTask
import com.unistack.app.feature_rooms.domain.RoomType
import com.unistack.app.feature_rooms.domain.SharedMode
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import com.unistack.app.feature_rooms.domain.ChangeRule
import com.unistack.app.feature_rooms.domain.LateRule
import com.unistack.app.feature_rooms.domain.StructureRule
import com.unistack.app.feature_rooms.domain.VisibilityRule
import com.unistack.app.feature_rooms.domain.ReminderRule
import org.json.JSONArray
import org.json.JSONObject

/**
 * La sala como documento JSON.
 *
 * Las claves son nombres persistidos: no se renombran nunca. Todo campo se lee con valor por
 * defecto, así un documento viejo (o uno que llegue de la nube a medias) sigue abriendo.
 */
object WorkRoomJson {

    fun encode(r: WorkRoom): String = JSONObject().apply {
        put("id", r.id); put("title", r.title); putOpt("subjectId", r.subjectId)
        put("type", r.type.name); put("produces", r.produces.name); put("split", r.split.name)
        put("capacity", r.capacity); put("entryOpen", r.entryOpen); put("code", r.code)
        put("dueEpochDay", r.dueEpochDay); put("internalDates", r.internalDates)
        put("rules", JSONObject().apply {
            put("change", r.rules.change.name); put("late", r.rules.late.name)
            put("structure", r.rules.structure.name); put("visibility", r.rules.visibility.name)
            put("reminders", r.rules.reminders.name)
        })
        put("leaderId", r.leaderId); put("meId", r.meId)
        put("members", arr(r.members) { m -> JSONObject().put("id", m.id).put("name", m.name).put("color", m.colorIndex).put("joinedAt", m.joinedAt) })
        put("removedIds", JSONArray(r.removedIds))
        put("entryHandledIds", JSONArray(r.entryHandledIds))
        put("parts", arr(r.parts) { encodePart(it) })
        put("tasks", arr(r.tasks) { t -> JSONObject().put("id", t.id).put("name", t.name).put("mine", t.mine).put("done", t.done) })
        put("materials", arr(r.materials) { encodeMaterial(it) })
        put("messages", arr(r.messages) { encodeMessage(it) })
        put("events", arr(r.events) { e ->
            JSONObject().put("id", e.id).put("type", e.type.name).put("actorId", e.actorId).put("text", e.text)
                .put("detail", e.detail).put("createdAt", e.createdAt).put("forMe", e.forMe).putOpt("partId", e.targetPartId)
        })
        put("requests", arr(r.requests) { q ->
            JSONObject().put("id", q.id).put("type", q.type.name).put("fromId", q.fromId).putOpt("partId", q.partId)
                .putOpt("otherPartId", q.otherPartId).put("note", q.note).put("createdAt", q.createdAt).putOpt("resolution", q.resolution)
        })
        putOpt("pinnedMessageId", r.pinnedMessageId); put("chatMuted", r.chatMuted); put("notifyMode", r.notifyMode.name)
        putOpt("myAlias", r.myAlias); put("bibStyle", r.bibliographyStyle)
        put("lastSeenEventsAt", r.lastSeenEventsAt); put("lastSeenChatAt", r.lastSeenChatAt)
        put("createdAt", r.createdAt); put("updatedAt", r.updatedAt); putOpt("closedAt", r.closedAt)
        putOpt("finalFormat", r.finalFormat); put("finalDestinations", JSONArray(r.finalDestinations))
        putOpt("finalFile", r.finalFile); putOpt("periodLabel", r.periodLabel); putOpt("finalGrade", r.finalGrade)
    }.toString()

    fun decode(json: String): WorkRoom {
        val o = JSONObject(json)
        val rules = o.optJSONObject("rules") ?: JSONObject()
        return WorkRoom(
            id = o.getString("id"),
            title = o.optString("title"),
            subjectId = o.optStr("subjectId"),
            type = enumOr(o.optString("type"), RoomType.CERO),
            produces = enumOr(o.optString("produces"), RoomProduce.DOC),
            split = enumOr(o.optString("split"), SplitMode.ASSIGN),
            capacity = o.optInt("capacity", 6),
            entryOpen = o.optBoolean("entryOpen", true),
            code = o.optString("code"),
            dueEpochDay = o.optLong("dueEpochDay"),
            internalDates = o.optBoolean("internalDates", true),
            rules = RoomRules(
                change = enumOr(rules.optString("change"), ChangeRule.ASK),
                late = enumOr(rules.optString("late"), LateRule.MARKED),
                structure = enumOr(rules.optString("structure"), StructureRule.LEADER),
                visibility = enumOr(rules.optString("visibility"), VisibilityRule.ALWAYS),
                reminders = enumOr(rules.optString("reminders"), ReminderRule.ONE_DAY)
            ),
            leaderId = o.optString("leaderId"),
            meId = o.optString("meId"),
            members = list(o.optJSONArray("members")) { RoomMember(it.getString("id"), it.optString("name"), it.optInt("color"), it.optLong("joinedAt")) },
            removedIds = strings(o.optJSONArray("removedIds")),
            entryHandledIds = strings(o.optJSONArray("entryHandledIds")),
            parts = list(o.optJSONArray("parts")) { decodePart(it) },
            tasks = list(o.optJSONArray("tasks")) { RoomTask(it.getString("id"), it.optString("name"), it.optBoolean("mine"), it.optBoolean("done")) },
            materials = list(o.optJSONArray("materials")) { decodeMaterial(it) },
            messages = list(o.optJSONArray("messages")) { decodeMessage(it) },
            events = list(o.optJSONArray("events")) {
                RoomEvent(it.getString("id"), enumOr(it.optString("type"), EventType.COMMENT), it.optString("actorId"), it.optString("text"),
                    it.optString("detail"), it.optLong("createdAt"), it.optBoolean("forMe"), it.optStr("partId"))
            },
            requests = list(o.optJSONArray("requests")) {
                RoomRequest(it.getString("id"), enumOr(it.optString("type"), RequestType.HELP), it.optString("fromId"), it.optStr("partId"),
                    it.optStr("otherPartId"), it.optString("note"), it.optLong("createdAt"),
                    if (it.has("resolution") && !it.isNull("resolution")) it.optBoolean("resolution") else null)
            },
            pinnedMessageId = o.optStr("pinnedMessageId"),
            chatMuted = o.optBoolean("chatMuted"),
            notifyMode = enumOr(o.optString("notifyMode"), NotifyMode.ALL),
            myAlias = o.optStr("myAlias"),
            bibliographyStyle = o.optString("bibStyle", "APA"),
            lastSeenEventsAt = o.optLong("lastSeenEventsAt"),
            lastSeenChatAt = o.optLong("lastSeenChatAt"),
            createdAt = o.optLong("createdAt"),
            updatedAt = o.optLong("updatedAt"),
            closedAt = if (o.has("closedAt") && !o.isNull("closedAt")) o.optLong("closedAt") else null,
            finalFormat = o.optStr("finalFormat"),
            finalDestinations = strings(o.optJSONArray("finalDestinations")),
            periodLabel = o.optStr("periodLabel"),
            finalFile = o.optStr("finalFile"),
            finalGrade = if (o.has("finalGrade") && !o.isNull("finalGrade")) o.optDouble("finalGrade") else null
        )
    }

    private fun encodePart(p: RoomPart) = JSONObject().apply {
        put("id", p.id); put("name", p.name); put("extent", p.extent); put("sharers", p.sharers)
        put("ownerIds", JSONArray(p.ownerIds)); put("note", p.note); putOpt("dueEpochDay", p.dueEpochDay)
        put("state", p.state.name); put("text", p.text); putOpt("fileName", p.fileName); putOpt("file", p.file); putOpt("fileMime", p.fileMime); put("slides", p.slides)
        putOpt("presenterId", p.presenterId); put("minutes", p.minutes); put("script", p.script)
        put("approved", p.approved); putOpt("reviewerId", p.reviewerId); put("sharedMode", p.sharedMode.name)
        p.review?.let { put("review", JSONObject().put("byId", it.byId).put("ok", it.ok).put("note", it.note).put("at", it.at)) }
        put("attachments", arr(p.attachments) { a ->
            JSONObject().put("id", a.id).put("type", a.type.name).put("name", a.name).put("subtitle", a.subtitle).put("byId", a.byId).put("createdAt", a.createdAt).put("reactions", reactions(a.reactions))
                .putOpt("file", a.file).putOpt("mime", a.mime).put("url", a.url)
        })
        put("comments", arr(p.comments) { encodeComment(it) })
        putOpt("deliveredAt", p.deliveredAt)
    }

    private fun decodePart(o: JSONObject) = RoomPart(
        id = o.getString("id"), name = o.optString("name"), extent = o.optInt("extent"), sharers = o.optInt("sharers", 1),
        ownerIds = strings(o.optJSONArray("ownerIds")), note = o.optString("note"),
        dueEpochDay = if (o.has("dueEpochDay") && !o.isNull("dueEpochDay")) o.optLong("dueEpochDay") else null,
        state = enumOr(o.optString("state"), PartState.PENDING), text = o.optString("text"), fileName = o.optStr("fileName"), file = o.optStr("file"), fileMime = o.optStr("fileMime"),
        slides = o.optInt("slides"), presenterId = o.optStr("presenterId"), minutes = o.optInt("minutes", 3), script = o.optString("script"),
        approved = o.optBoolean("approved"), reviewerId = o.optStr("reviewerId"),
        sharedMode = enumOr(o.optString("sharedMode"), SharedMode.PIECES),
        review = o.optJSONObject("review")?.let { PartReview(it.optString("byId"), it.optBoolean("ok"), it.optString("note"), it.optLong("at")) },
        attachments = list(o.optJSONArray("attachments")) {
            PartAttachment(it.getString("id"), enumOr(it.optString("type"), MaterialType.DOC), it.optString("name"), it.optString("subtitle"), it.optString("byId"), it.optLong("createdAt"), reactions(it.optJSONObject("reactions")),
                it.optStr("file"), it.optStr("mime"), it.optString("url"))
        },
        comments = list(o.optJSONArray("comments")) { decodeComment(it) },
        deliveredAt = if (o.has("deliveredAt") && !o.isNull("deliveredAt")) o.optLong("deliveredAt") else null
    )

    private fun encodeComment(c: RoomComment) =
        JSONObject().put("id", c.id).put("byId", c.byId).put("text", c.text).put("createdAt", c.createdAt).put("review", c.isReview)

    private fun decodeComment(o: JSONObject) =
        RoomComment(o.getString("id"), o.optString("byId"), o.optString("text"), o.optLong("createdAt"), o.optBoolean("review"))

    private fun encodeMaterial(m: RoomMaterial) = JSONObject().apply {
        put("id", m.id); put("type", m.type.name); put("name", m.name); put("subtitle", m.subtitle); put("byId", m.byId)
        put("createdAt", m.createdAt); put("note", m.note); putOpt("partId", m.partId); put("tagged", JSONArray(m.tagged))
        put("pinned", m.pinned); put("seenBy", JSONArray(m.seenBy)); put("reactions", reactions(m.reactions))
        put("comments", arr(m.comments) { encodeComment(it) })
        put("author", m.author); put("year", m.year); put("publisher", m.publisher)
        putOpt("file", m.file); putOpt("mime", m.mime); put("url", m.url)
    }

    private fun decodeMaterial(o: JSONObject) = RoomMaterial(
        id = o.getString("id"), type = enumOr(o.optString("type"), MaterialType.DOC), name = o.optString("name"),
        subtitle = o.optString("subtitle"), byId = o.optString("byId"), createdAt = o.optLong("createdAt"), note = o.optString("note"),
        partId = o.optStr("partId"), tagged = strings(o.optJSONArray("tagged")), pinned = o.optBoolean("pinned"),
        seenBy = strings(o.optJSONArray("seenBy")), reactions = reactions(o.optJSONObject("reactions")),
        comments = list(o.optJSONArray("comments")) { decodeComment(it) },
        author = o.optString("author"), year = o.optString("year"), publisher = o.optString("publisher"),
        file = o.optStr("file"), mime = o.optStr("mime"), url = o.optString("url")
    )

    private fun encodeMessage(m: ChatMessage) = JSONObject().apply {
        put("id", m.id); putOpt("byId", m.byId); put("kind", m.kind.name); put("text", m.text); put("createdAt", m.createdAt)
        putOpt("replyToId", m.replyToId); put("reactions", reactions(m.reactions))
        put("poll", arr(m.pollOptions) { JSONObject().put("label", it.label).put("voters", JSONArray(it.voters)) })
        put("seconds", m.seconds); put("subtitle", m.subtitle); put("saved", m.savedToMaterial); putOpt("system", m.systemType)
        putOpt("file", m.file); putOpt("mime", m.mime)
    }

    private fun decodeMessage(o: JSONObject) = ChatMessage(
        id = o.getString("id"), byId = o.optStr("byId"), kind = enumOr(o.optString("kind"), MessageKind.TEXT), text = o.optString("text"),
        createdAt = o.optLong("createdAt"), replyToId = o.optStr("replyToId"), reactions = reactions(o.optJSONObject("reactions")),
        pollOptions = list(o.optJSONArray("poll")) { PollOption(it.optString("label"), strings(it.optJSONArray("voters"))) },
        seconds = o.optInt("seconds"), subtitle = o.optString("subtitle"), savedToMaterial = o.optBoolean("saved"), systemType = o.optStr("system"),
        file = o.optStr("file"), mime = o.optStr("mime")
    )

    private fun reactions(map: Map<String, List<String>>) = JSONObject().apply { map.forEach { (k, v) -> put(k, JSONArray(v)) } }
    private fun reactions(o: JSONObject?): Map<String, List<String>> {
        if (o == null) return emptyMap()
        return o.keys().asSequence().associateWith { strings(o.optJSONArray(it)) }
    }

    private fun <T> arr(items: List<T>, f: (T) -> JSONObject) = JSONArray().apply { items.forEach { put(f(it)) } }
    private fun <T> list(a: JSONArray?, f: (JSONObject) -> T): List<T> =
        if (a == null) emptyList() else (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(f) }
    private fun strings(a: JSONArray?): List<String> = if (a == null) emptyList() else (0 until a.length()).map { a.optString(it) }
    private fun JSONObject.optStr(key: String): String? = if (has(key) && !isNull(key)) optString(key) else null
    private inline fun <reified E : Enum<E>> enumOr(value: String, default: E): E =
        runCatching { enumValueOf<E>(value) }.getOrDefault(default)
}
