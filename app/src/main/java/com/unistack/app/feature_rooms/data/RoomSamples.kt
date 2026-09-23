package com.unistack.app.feature_rooms.data

import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.EventType
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.PartAttachment
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.PollOption
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomComment
import com.unistack.app.feature_rooms.domain.RoomEvent
import com.unistack.app.feature_rooms.domain.RoomMaterial
import com.unistack.app.feature_rooms.domain.RoomMember
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.RoomRequest
import com.unistack.app.feature_rooms.domain.RoomType
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom

/**
 * La sala de los artifacts, para el banco de pruebas: «La ciudad como texto» con Alex (líder),
 * Sam, Nico y Dani, las seis secciones en sus estados, el material, el chat, los pedidos y las
 * novedades. Todo lleva el prefijo `prueba-` para poder recogerlo sin tocar nada de verdad.
 */
object RoomSamples {
    const val PREFIX = "prueba-sala"
    private const val DAY = 86_400_000L

    fun leaderRoom(today: Long, now: Long, subjectId: String?, id: String = "$PREFIX-lider"): WorkRoom {
        val ago: (Double) -> Long = { d -> now - (d * DAY).toLong() }
        val alex = RoomMember("prueba-m-alex", "Alex", 0, ago(9.0))
        val sam = RoomMember("prueba-m-sam", "Sam", 1, ago(8.0))
        val nico = RoomMember("prueba-m-nico", "Nico", 2, ago(8.0))
        val dani = RoomMember("prueba-m-dani", "Dani", 3, ago(7.0))
        val vale = RoomMember("prueba-m-vale", "Vale", 4, ago(0.05))
        val parts = listOf(
            RoomPart("prueba-p1", "Introducción", 1, ownerIds = listOf(alex.id), dueEpochDay = today - 3, state = PartState.DELIVERED, deliveredAt = ago(3.0),
                text = "La ciudad no sólo se habita: se lee. Calles, avisos y muros funcionan como un texto que cada quien interpreta según su lugar en ella. En este ensayo proponemos leer Bogotá como ese texto.",
                attachments = listOf(PartAttachment("prueba-a1", MaterialType.DOC, "Intro_v2.docx", "Word", alex.id, ago(3.0), mapOf("👍" to listOf(sam.id, nico.id), "🔥" to listOf(dani.id)))),
                comments = listOf(RoomComment("prueba-c1", sam.id, "Me gusta cómo abre, conecta bien con mi parte.", ago(2.0)))),
            RoomPart("prueba-p2", "Marco teórico", 2, ownerIds = listOf(sam.id), dueEpochDay = today - 1, state = PartState.DELIVERED, deliveredAt = ago(1.0),
                text = "Seguimos a Lefebvre (el derecho a la ciudad) y a De Certeau: caminar es un acto de enunciación, la ciudad un sistema que el peatón reescribe. Silva añade los imaginarios urbanos.",
                attachments = listOf(PartAttachment("prueba-a2", MaterialType.DOC, "Marco_teorico.docx", "Word", sam.id, ago(1.0), mapOf("👍" to listOf(alex.id))))),
            RoomPart("prueba-p3", "Argumento 1", 2, ownerIds = listOf(nico.id), dueEpochDay = today - 1, state = PartState.PENDING,
                note = "Un ejemplo concreto: los grafitis como réplica a la ciudad oficial.",
                attachments = listOf(PartAttachment("prueba-a4", MaterialType.NOTE, "Ideas sueltas: grafitis como réplica", "Apunte", nico.id, ago(4.0), mapOf("👀" to listOf(alex.id)))),
                comments = listOf(RoomComment("prueba-c2", alex.id, "Nico, ¿cómo vas? Se venció ayer.", ago(1.0)))),
            RoomPart("prueba-p4", "Argumento 2", 2, ownerIds = listOf(dani.id), dueEpochDay = today + 4, state = PartState.DRAFT,
                note = "Un ejemplo concreto de Bogotá (murales, grafitis o avisos) leído como texto. Máximo una cita larga.",
                text = "Los murales del centro no son decoración: son respuestas. Donde la ciudad oficial pone avisos y prohibiciones, los muros contestan con rostros, consignas y memoria.",
                attachments = listOf(PartAttachment("prueba-a5", MaterialType.LINK, "Mapa de murales — Bogotá", "bogota.gov.co", dani.id, ago(0.1), mapOf("❤️" to listOf(sam.id)), url = "https://bogota.gov.co"))),
            RoomPart("prueba-p5", "Conclusión", 1, ownerIds = listOf(alex.id), dueEpochDay = today + 5, note = "Retomar la idea de leer la ciudad y cerrar con una pregunta."),
            RoomPart("prueba-p6", "Bibliografía", 1, dueEpochDay = today + 6)
        )
        val materials = listOf(
            RoomMaterial("prueba-mat3", MaterialType.DOC, "Rúbrica del ensayo.pdf", "PDF", alex.id, ago(6.0), "Léanla antes de empezar: el 40 % es argumentación", pinned = true, seenBy = listOf(alex.id, sam.id, dani.id)),
            RoomMaterial("prueba-mat1", MaterialType.LINK, "Lefebvre — El derecho a la ciudad (resumen)", "revistas.unal.edu.co", sam.id, ago(4.0), "Lo usé para el marco", partId = "prueba-p2",
                seenBy = listOf(sam.id, alex.id), reactions = mapOf("👍" to listOf(alex.id, nico.id)), url = "https://revistas.unal.edu.co"),
            RoomMaterial("prueba-mat10", MaterialType.SOURCE, "Imaginarios urbanos", "Libro · cap. 2", nico.id, ago(5.0), "Cap. 2 sirve para los grafitis", partId = "prueba-p3",
                tagged = listOf(dani.id), seenBy = listOf(nico.id, dani.id), author = "Silva, A.", year = "2006", publisher = "Arango Editores"),
            RoomMaterial("prueba-mat12", MaterialType.SOURCE, "La invención de lo cotidiano", "Libro", sam.id, ago(4.0), author = "De Certeau, M.", year = "1990", publisher = "Universidad Iberoamericana", seenBy = listOf(sam.id)),
            RoomMaterial("prueba-mat13", MaterialType.SOURCE, "El derecho a la ciudad", "Libro", sam.id, ago(4.0), author = "Lefebvre, H.", year = "1968", publisher = "Península", seenBy = listOf(sam.id)),
            RoomMaterial("prueba-mat2", MaterialType.LINK, "Mapa de murales de Bogotá", "bogota.gov.co", dani.id, ago(0.1), "Para escoger los ejemplos", partId = "prueba-p4",
                tagged = listOf(alex.id), seenBy = listOf(dani.id, sam.id), reactions = mapOf("🔥" to listOf(sam.id)), url = "https://bogota.gov.co"),
            RoomMaterial("prueba-mat8", MaterialType.VIDEO, "Entrevista: la ciudad y sus signos", "YouTube · 14 min", sam.id, ago(3.0), "Del minuto 4 al 9", tagged = listOf(alex.id, dani.id),
                seenBy = listOf(sam.id), url = "https://youtube.com"),
            RoomMaterial("prueba-mat11", MaterialType.NOTE, "Ideas para unir los dos argumentos", "Apunte", alex.id, ago(1.0), "Unir los argumentos por la idea de «réplica»: los muros responden a la ciudad oficial.", partId = "prueba-p5", seenBy = listOf(alex.id))
        )
        val messages = listOf(
            ChatMessage("prueba-msg0", alex.id, text = "Buenas! Ya repartí las partes, cualquier cosa me dicen", createdAt = ago(2.2)),
            ChatMessage("prueba-msg1", sam.id, text = "Perfecto. Yo empiezo hoy con el [[Marco teórico]]", createdAt = ago(2.1), reactions = mapOf("👍" to listOf(alex.id, nico.id))),
            ChatMessage("prueba-msg2", sam.id, text = "Marco teórico", createdAt = ago(1.05), systemType = "DELIVERED"),
            ChatMessage("prueba-msg3", sam.id, text = "Listo mi marco, lo pueden revisar 🙌", createdAt = ago(1.0)),
            ChatMessage("prueba-msg4", nico.id, text = "Perdón, esta noche subo [[Argumento 1]] sin falta", createdAt = ago(0.3), reactions = mapOf("🔥" to listOf(alex.id))),
            ChatMessage("prueba-msg5", alex.id, text = "¿Nos vemos para unir todo?", createdAt = ago(0.25), kind = com.unistack.app.feature_rooms.domain.MessageKind.POLL,
                pollOptions = listOf(PollOption("Jueves 4 pm", listOf(alex.id, sam.id)), PollOption("Viernes 10 am", listOf(nico.id)))),
            ChatMessage("prueba-msg6", dani.id, text = "Yo subí el mapa de murales, me sirve para el 2", createdAt = ago(0.1)),
            ChatMessage("prueba-msg7", sam.id, text = "@Alex ¿cómo vas con la conclusión? Te dejé un comentario", createdAt = ago(0.05))
        )
        val events = listOf(
            RoomEvent("prueba-ev1", EventType.DELIVERED, sam.id, "Marco teórico", createdAt = ago(1.05), targetPartId = "prueba-p2"),
            RoomEvent("prueba-ev2", EventType.COMMENT, alex.id, "Argumento 1", "Nico, ¿cómo vas? Se venció ayer.", ago(1.0), targetPartId = "prueba-p3"),
            RoomEvent("prueba-ev3", EventType.MATERIAL, dani.id, "Mapa de murales de Bogotá", createdAt = ago(0.1), targetPartId = "prueba-p4"),
            RoomEvent("prueba-ev4", EventType.TAGGED, dani.id, "Mapa de murales de Bogotá", createdAt = ago(0.1)),
            RoomEvent("prueba-ev5", EventType.MENTION, sam.id, "@Alex ¿cómo vas con la conclusión? Te dejé un comentario", createdAt = ago(0.05)),
            RoomEvent("prueba-ev6", EventType.JOINED, vale.id, "Vale", createdAt = ago(0.05))
        )
        val requests = listOf(
            RoomRequest("prueba-rq1", RequestType.MORE_TIME, nico.id, "prueba-p3", note = "Tuve parciales, el jueves lo subo", createdAt = ago(0.5)),
            RoomRequest("prueba-rq2", RequestType.SWAP, dani.id, "prueba-p4", "prueba-p5", "Me va mejor con el cierre", ago(0.4)),
            RoomRequest("prueba-rq3", RequestType.HELP, sam.id, "prueba-p6", note = "¿Usamos APA?", createdAt = ago(0.3))
        )
        return WorkRoom(
            id = id, title = "La ciudad como texto", subjectId = subjectId, type = RoomType.ENSAYO, split = SplitMode.ASSIGN, capacity = 6,
            code = "7K4P2M", dueEpochDay = today + 7, leaderId = alex.id, meId = alex.id,
            members = listOf(alex, sam, nico, dani, vale), parts = parts, materials = materials, messages = messages, events = events, requests = requests,
            lastSeenEventsAt = ago(0.2), lastSeenChatAt = ago(0.2), createdAt = ago(9.0), updatedAt = now
        )
    }

    /** La misma sala vista por Dani, un compañero: sin nada de líder y con un pedido suyo esperando. */
    fun memberRoom(today: Long, now: Long, subjectId: String?): WorkRoom {
        val base = leaderRoom(today, now, subjectId, "$PREFIX-comp")
        return base.copy(meId = "prueba-m-dani", members = base.members.filter { it.id != "prueba-m-vale" },
            requests = listOf(RoomRequest("prueba-rq9", RequestType.MORE_TIME, "prueba-m-dani", "prueba-p4", note = "Tengo parcial el jueves", createdAt = now - DAY / 4)),
            events = base.events.filter { it.type != EventType.JOINED })
    }

    /** Todas las secciones entregadas: al líder le sale «Crear trabajo». */
    fun allDelivered(room: WorkRoom, now: Long): WorkRoom = room.copy(
        requests = emptyList(),
        entryHandledIds = room.members.map { it.id },
        parts = room.parts.map { p ->
            val owner = p.ownerIds.ifEmpty { listOf("prueba-m-sam") }
            p.copy(ownerIds = owner, state = PartState.DELIVERED, deliveredAt = p.deliveredAt ?: (now - DAY / 2), text = p.text.ifBlank {
                "Texto entregado de ${p.name}. La ciudad se deja leer, pero también responde: cada muro intervenido es una frase nueva en un texto que nadie escribe solo."
            })
        }
    )
}
