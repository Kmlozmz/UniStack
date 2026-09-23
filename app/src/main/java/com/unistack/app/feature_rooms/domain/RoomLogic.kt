package com.unistack.app.feature_rooms.domain

import java.util.UUID
import kotlin.random.Random

/**
 * Todo lo que le pasa a una sala, como funciones puras: reciben la sala y devuelven la nueva.
 *
 * La pantalla no toca listas a mano: pide «entrega esta parte» y aquí se decide qué más cambia
 * (el estado, el aviso en el chat —sólo entregas y fechas— y la novedad para los demás).
 */
object RoomLogic {

    fun newId(prefix: String) = "$prefix-${UUID.randomUUID()}"

    private const val CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    fun newCode(random: Random = Random.Default): String = (1..6).map { CODE_CHARS[random.nextInt(CODE_CHARS.length)] }.joinToString("")

    /** Fecha interna de la parte i: escalonada, la última 3 días antes de la entrega. */
    fun internalDue(index: Int, count: Int, todayEpochDay: Long, dueEpochDay: Long): Long {
        val span = (dueEpochDay - 3 - todayEpochDay).coerceAtLeast(2)
        return todayEpochDay + maxOf(2L, Math.round(span.toDouble() * (index + 1) / count.coerceAtLeast(1)))
    }

    fun withInternalDates(room: WorkRoom, todayEpochDay: Long): WorkRoom =
        if (!room.internalDates) room.copy(parts = room.parts.map { it.copy(dueEpochDay = null) })
        else room.copy(parts = room.parts.mapIndexed { i, p -> p.copy(dueEpochDay = internalDue(i, room.parts.size, todayEpochDay, room.dueEpochDay)) })

    fun partDue(room: WorkRoom, part: RoomPart): Long = part.dueEpochDay ?: room.dueEpochDay

    // ---------- estados de cara a la sala ----------

    enum class Status { OVERDUE, FREE, IN_PROGRESS, PENDING, DELIVERED }

    fun status(room: WorkRoom, part: RoomPart, todayEpochDay: Long): Status = when {
        part.state == PartState.DELIVERED -> Status.DELIVERED
        part.isFree -> Status.FREE
        partDue(room, part) < todayEpochDay -> Status.OVERDUE
        part.state == PartState.DRAFT || part.state == PartState.REVIEW || part.hasContent -> Status.IN_PROGRESS
        else -> Status.PENDING
    }

    // ---------- miembros ----------

    fun join(room: WorkRoom, name: String, now: Long): WorkRoom {
        val id = newId("member")
        val color = room.members.size % 12
        val m = RoomMember(id, name, color, now)
        return room.copy(members = room.members + m, events = room.events + event(EventType.JOINED, id, name, now = now))
    }

    fun remove(room: WorkRoom, memberId: String): WorkRoom = room.copy(
        removedIds = room.removedIds + memberId,
        parts = room.parts.map { p -> if (memberId in p.ownerIds) p.copy(ownerIds = p.ownerIds - memberId) else p }
    )

    fun passLeadership(room: WorkRoom, memberId: String): WorkRoom = room.copy(leaderId = memberId)

    /** Quien entró sin parte y espera que el líder se la dé (sólo cuando el líder reparte). */
    fun pendingEntry(room: WorkRoom): RoomMember? =
        if (!room.isLeader || room.split != SplitMode.ASSIGN) null
        else room.activeMembers.firstOrNull { m -> m.id != room.meId && m.id !in room.entryHandledIds && room.partsOf(m.id).isEmpty() }

    fun handleEntry(room: WorkRoom, memberId: String) = room.copy(entryHandledIds = (room.entryHandledIds + memberId).distinct())

    /** Lo que el líder tiene por atender: pedidos abiertos, quien entró, vencidas y entregadas sin su visto bueno. */
    fun openRequests(room: WorkRoom) = room.requests.filter { it.resolution == null }
    fun overdue(room: WorkRoom, today: Long) = room.parts.filter { status(room, it, today) == Status.OVERDUE }
    fun free(room: WorkRoom) = room.parts.filter { it.isFree && it.state != PartState.DELIVERED }
    fun toApprove(room: WorkRoom) = room.parts.filter { it.state == PartState.DELIVERED && !it.approved }
    fun pendingCount(room: WorkRoom) = openRequests(room).size + (if (pendingEntry(room) != null) 1 else 0)

    // ---------- reparto ----------

    fun assign(room: WorkRoom, partId: String, memberIds: List<String>, now: Long): WorkRoom {
        val r = room.copy(parts = room.parts.map { if (it.id == partId) it.copy(ownerIds = memberIds) else it })
        val p = r.part(partId) ?: return r
        return r.copy(events = r.events + memberIds.filter { it != room.meId }.map {
            event(EventType.ASSIGNED, room.meId, p.name, now = now, forMe = it == room.meId, partId = partId)
        })
    }

    fun take(room: WorkRoom, partId: String): WorkRoom =
        room.copy(parts = room.parts.map { if (it.id == partId && it.isFree) it.copy(ownerIds = listOf(room.meId)) else it })

    /** Sortea las libres entre quien menos tiene. */
    fun drawFree(room: WorkRoom, random: Random = Random.Default): WorkRoom {
        val people = room.activeMembers.map { it.id }.toMutableList()
        if (people.isEmpty()) return room
        val load = people.associateWith { id -> room.parts.count { id in it.ownerIds } }.toMutableMap()
        val parts = room.parts.map { p ->
            if (!p.isFree) p else {
                val min = load.values.min()
                val pick = load.filterValues { it == min }.keys.toList().shuffled(random).first()
                load[pick] = load.getValue(pick) + 1
                p.copy(ownerIds = listOf(pick))
            }
        }
        return room.copy(parts = parts)
    }

    fun drawAll(room: WorkRoom, random: Random = Random.Default): WorkRoom =
        drawFree(room.copy(parts = room.parts.map { it.copy(ownerIds = emptyList()) }), random)

    // ---------- mi parte ----------

    fun writePart(room: WorkRoom, partId: String, text: String, fileName: String? = null): WorkRoom = room.copy(parts = room.parts.map {
        if (it.id != partId) it else it.copy(text = text, fileName = fileName ?: it.fileName,
            state = if (it.state == PartState.PENDING && text.isNotBlank()) PartState.DRAFT else it.state)
    })

    fun uploadSlides(room: WorkRoom, partId: String, fileName: String, slides: Int): WorkRoom = room.copy(parts = room.parts.map {
        if (it.id != partId) it else it.copy(fileName = fileName, slides = slides, state = if (it.state == PartState.PENDING) PartState.DRAFT else it.state)
    })

    fun updatePart(room: WorkRoom, partId: String, f: (RoomPart) -> RoomPart) = room.copy(parts = room.parts.map { if (it.id == partId) f(it) else it })

    fun deliver(room: WorkRoom, partId: String, now: Long): WorkRoom {
        val p = room.part(partId) ?: return room
        val r = updatePart(room, partId) { it.copy(state = PartState.DELIVERED, deliveredAt = now) }
        return r.copy(
            messages = r.messages + ChatMessage(newId("msg"), room.meId, text = p.name, createdAt = now, systemType = "DELIVERED"),
            events = r.events + event(EventType.DELIVERED, room.meId, p.name, now = now, partId = partId)
        )
    }

    fun reopen(room: WorkRoom, partId: String) = updatePart(room, partId) { it.copy(state = PartState.DRAFT, review = null, approved = false) }

    fun askReview(room: WorkRoom, partId: String, reviewerId: String?, now: Long): WorkRoom {
        val r = updatePart(room, partId) { it.copy(state = PartState.REVIEW, reviewerId = reviewerId, review = null) }
        return r.copy(events = r.events + event(EventType.REVIEW_ASKED, room.meId, room.part(partId)?.name.orEmpty(), now = now, partId = partId))
    }

    fun review(room: WorkRoom, partId: String, byId: String, ok: Boolean, note: String, now: Long): WorkRoom {
        val r = updatePart(room, partId) {
            it.copy(state = if (it.state == PartState.REVIEW) PartState.DRAFT else it.state, review = PartReview(byId, ok, note, now),
                comments = it.comments + RoomComment(newId("com"), byId, note, now, isReview = true))
        }
        return r
    }

    fun comment(room: WorkRoom, partId: String, text: String, now: Long): WorkRoom {
        val p = room.part(partId) ?: return room
        val r = updatePart(room, partId) { it.copy(comments = it.comments + RoomComment(newId("com"), room.meId, text, now)) }
        return r.copy(events = r.events + event(EventType.COMMENT, room.meId, p.name, detail = text, now = now, partId = partId))
    }

    fun attach(room: WorkRoom, partId: String, type: MaterialType, name: String, subtitle: String, now: Long) =
        updatePart(room, partId) { it.copy(attachments = it.attachments + PartAttachment(newId("att"), type, name, subtitle, room.meId, now)) }

    fun approve(room: WorkRoom, partId: String) = updatePart(room, partId) { it.copy(approved = true) }

    fun moveDue(room: WorkRoom, partId: String, days: Int): WorkRoom =
        updatePart(room, partId) { it.copy(dueEpochDay = (it.dueEpochDay ?: room.dueEpochDay) + days) }

    fun moveFinalDue(room: WorkRoom, newDue: Long, alsoInternal: Boolean, now: Long): WorkRoom {
        val delta = newDue - room.dueEpochDay
        val parts = if (alsoInternal) room.parts.map { p ->
            if (p.state == PartState.DELIVERED || p.dueEpochDay == null) p else p.copy(dueEpochDay = p.dueEpochDay + delta)
        } else room.parts
        return room.copy(dueEpochDay = newDue, parts = parts,
            messages = room.messages + ChatMessage(newId("msg"), room.meId, text = newDue.toString(), createdAt = now, systemType = "DUE"),
            events = room.events + event(EventType.DUE_CHANGED, room.meId, newDue.toString(), detail = room.dueEpochDay.toString(), now = now))
    }

    // ---------- pedidos ----------

    fun request(room: WorkRoom, type: RequestType, partId: String?, note: String, now: Long, otherPartId: String? = null) =
        room.copy(requests = room.requests + RoomRequest(newId("req"), type, room.meId, partId, otherPartId, note, now))

    fun resolve(room: WorkRoom, requestId: String, accept: Boolean): WorkRoom {
        val q = room.requests.firstOrNull { it.id == requestId } ?: return room
        var r = room.copy(requests = room.requests.map { if (it.id == requestId) it.copy(resolution = accept) else it })
        if (accept) {
            r = when (q.type) {
                RequestType.MORE_TIME -> q.partId?.let { moveDue(r, it, 2) } ?: r
                RequestType.SWAP -> {
                    val mine = r.part(q.partId); val other = r.part(q.otherPartId)
                    if (mine != null && other != null) r.copy(parts = r.parts.map {
                        when (it.id) { mine.id -> it.copy(ownerIds = other.ownerIds); other.id -> it.copy(ownerIds = mine.ownerIds); else -> it }
                    }) else r
                }
                RequestType.DROP -> q.partId?.let { pid -> updatePart(r, pid) { it.copy(ownerIds = it.ownerIds - q.fromId) } } ?: r
                RequestType.JOIN -> r
                RequestType.HELP -> r
            }
        }
        return r
    }

    // ---------- chat ----------

    fun send(room: WorkRoom, msg: ChatMessage): WorkRoom {
        var r = room.copy(messages = room.messages + msg)
        Regex("@(\\w+)").findAll(msg.text).forEach { m ->
            val who = r.activeMembers.firstOrNull { it.name.equals(m.groupValues[1], true) }
            if (who != null && who.id != room.meId) r = r.copy(events = r.events + event(EventType.MENTION, room.meId, msg.text, now = msg.createdAt))
        }
        return r
    }

    fun react(reactions: Map<String, List<String>>, emoji: String, who: String): Map<String, List<String>> {
        val cur = reactions[emoji].orEmpty()
        val next = if (who in cur) cur - who else cur + who
        return (reactions + (emoji to next)).filterValues { it.isNotEmpty() }
    }

    fun vote(msg: ChatMessage, index: Int, who: String) = msg.copy(pollOptions = msg.pollOptions.mapIndexed { i, o ->
        val v = o.voters - who
        if (i == index) o.copy(voters = v + who) else o.copy(voters = v)
    })

    // ---------- material ----------

    fun addMaterial(room: WorkRoom, m: RoomMaterial): WorkRoom = room.copy(
        materials = listOf(m) + room.materials,
        events = room.events + event(EventType.MATERIAL, room.meId, m.name, now = m.createdAt, partId = m.partId) +
            m.tagged.map { event(EventType.TAGGED, room.meId, m.name, now = m.createdAt) }
    )

    fun updateMaterial(room: WorkRoom, id: String, f: (RoomMaterial) -> RoomMaterial) =
        room.copy(materials = room.materials.map { if (it.id == id) f(it) else it })

    fun forMe(room: WorkRoom, m: RoomMaterial): Boolean =
        room.meId in m.tagged || (m.partId != null && room.meId in room.part(m.partId)?.ownerIds.orEmpty())

    /** Bibliografía que se arma sola, en APA o MLA. */
    fun bibliography(room: WorkRoom, style: String = room.bibliographyStyle): List<String> =
        room.materials.filter { it.type == MaterialType.SOURCE }.sortedBy { it.author.lowercase() }.map {
            if (style == "MLA") "${it.author} ${it.name}. ${it.publisher}, ${it.year}."
            else "${it.author} (${it.year}). ${it.name}. ${it.publisher}."
        }

    // ---------- cerrar ----------

    fun close(room: WorkRoom, format: String, now: Long) = room.copy(closedAt = now, finalFormat = format)
    fun reopenRoom(room: WorkRoom) = room.copy(closedAt = null)

    // ---------- novedades ----------

    fun event(type: EventType, actor: String, text: String, detail: String = "", now: Long, forMe: Boolean = false, partId: String? = null) =
        RoomEvent(newId("ev"), type, actor, text, detail, now, forMe, partId)

    fun unseenEvents(room: WorkRoom) = room.events.count { it.createdAt > room.lastSeenEventsAt && it.actorId != room.meId }
    fun unseenChat(room: WorkRoom) = room.messages.count { it.createdAt > room.lastSeenChatAt && it.byId != room.meId && it.systemType == null }
}
