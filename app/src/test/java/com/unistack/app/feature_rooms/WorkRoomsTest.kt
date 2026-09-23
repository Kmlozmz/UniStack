package com.unistack.app.feature_rooms

import com.unistack.app.feature_rooms.data.WorkRoomJson
import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.PartAttachment
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.QrCode
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomMaterial
import com.unistack.app.feature_rooms.domain.RoomMember
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class WorkRoomsTest {

    private val today = 20_000L

    private fun room(): WorkRoom {
        val me = RoomMember("me", "Alex", 0, 1)
        val sam = RoomMember("sam", "Sam", 1, 2)
        val vale = RoomMember("vale", "Vale", 4, 3)
        return WorkRoom(
            id = "r1", title = "La ciudad como texto", code = "7K4P2M", dueEpochDay = today + 10,
            leaderId = "me", meId = "me", members = listOf(me, sam, vale), split = SplitMode.ASSIGN,
            parts = listOf(
                RoomPart("p1", "Introducción", ownerIds = listOf("me"), dueEpochDay = today + 3, text = "Hola mundo",
                    attachments = listOf(PartAttachment("a1", MaterialType.LINK, "Mapa", "bogota.gov.co", "me", 5, mapOf("👍" to listOf("sam")), url = "https://bogota.gov.co"))),
                RoomPart("p2", "Marco", ownerIds = listOf("sam"), dueEpochDay = today - 1),
                RoomPart("p3", "Bibliografía")
            ),
            materials = listOf(RoomMaterial("m1", MaterialType.SOURCE, "Imaginarios urbanos", byId = "sam", createdAt = 1, author = "Silva, A.", year = "2006", publisher = "Arango", file = "room-x.pdf", mime = "application/pdf")),
            messages = listOf(ChatMessage("c1", "sam", text = "Hola @Alex", createdAt = 9, file = "room-y.jpg", mime = "image/jpeg")),
            createdAt = 1, updatedAt = 1
        )
    }

    @Test
    fun jsonRoundTripKeepsEverything() {
        val r = room().copy(entryHandledIds = listOf("vale"), finalFile = "room-final.docx", finalFormat = "docx")
        val back = WorkRoomJson.decode(WorkRoomJson.encode(r))
        assertEquals(r, back)
    }

    @Test
    fun oldJsonWithoutNewFieldsStillReads() {
        val json = WorkRoomJson.encode(room()).replace("\"entryHandledIds\"", "\"x_old\"").replace("\"finalFile\"", "\"y_old\"")
        val back = WorkRoomJson.decode(json)
        assertTrue(back.entryHandledIds.isEmpty())
        assertNull(back.finalFile)
    }

    @Test
    fun statusesAndGroups() {
        val r = room()
        assertEquals(RoomLogic.Status.IN_PROGRESS, RoomLogic.status(r, r.parts[0], today))
        assertEquals(RoomLogic.Status.OVERDUE, RoomLogic.status(r, r.parts[1], today))
        assertEquals(RoomLogic.Status.FREE, RoomLogic.status(r, r.parts[2], today))
        assertEquals(listOf("p2"), RoomLogic.overdue(r, today).map { it.id })
        assertEquals(listOf("p3"), RoomLogic.free(r).map { it.id })
    }

    @Test
    fun pendingEntryOnlyWhenLeaderAssigns() {
        val r = room()
        assertEquals("vale", RoomLogic.pendingEntry(r)?.id)
        assertNull(RoomLogic.pendingEntry(RoomLogic.handleEntry(r, "vale")))
        assertNull(RoomLogic.pendingEntry(r.copy(split = SplitMode.FREE)))
        assertNull(RoomLogic.pendingEntry(r.copy(meId = "sam")))
        assertEquals(1, RoomLogic.pendingCount(r))
    }

    @Test
    fun deliverAddsChatNoticeAndEvent() {
        val r = RoomLogic.deliver(room(), "p1", 100)
        assertEquals(PartState.DELIVERED, r.part("p1")?.state)
        assertEquals("DELIVERED", r.messages.last().systemType)
        assertFalse(r.allDelivered)
    }

    @Test
    fun swapRequestSwapsOwnersWhenAccepted() {
        var r = room().copy(meId = "sam")
        r = RoomLogic.request(r, RequestType.SWAP, "p2", "", 5, otherPartId = "p1")
        val q = r.requests.single()
        r = RoomLogic.resolve(r, q.id, true)
        assertEquals(listOf("sam"), r.part("p1")?.ownerIds)
        assertEquals(listOf("me"), r.part("p2")?.ownerIds)
    }

    @Test
    fun bibliographyInBothStyles() {
        val r = room()
        assertEquals("Silva, A. (2006). Imaginarios urbanos. Arango.", RoomLogic.bibliography(r, "APA").single())
        assertEquals("Silva, A. Imaginarios urbanos. Arango, 2006.", RoomLogic.bibliography(r, "MLA").single())
    }

    @Test
    fun qrHasTheFixedPatterns() {
        val qr = QrCode.encode("unistack://sala/7K4P2M")
        assertEquals(25, qr.size) // versión 2
        // Los tres localizadores: esquina oscura, anillo claro, centro oscuro.
        for ((x, y) in listOf(0 to 0, qr.size - 7 to 0, 0 to qr.size - 7)) {
            assertTrue(qr.isDark(x, y)); assertTrue(qr.isDark(x + 6, y + 6))
            assertFalse(qr.isDark(x + 1, y + 1)); assertTrue(qr.isDark(x + 3, y + 3))
        }
        // Sincronía alterna y el módulo oscuro fijo.
        for (i in 8 until qr.size - 8) assertEquals(i % 2 == 0, qr.isDark(i, 6))
        assertTrue(qr.isDark(8, qr.size - 8))
        // Las dos copias del formato coinciden.
        val a = (0..5).map { qr.isDark(8, it) } + listOf(qr.isDark(8, 7), qr.isDark(8, 8), qr.isDark(7, 8)) + (9 until 15).map { qr.isDark(14 - it, 8) }
        val b = (0 until 8).map { qr.isDark(qr.size - 1 - it, 8) } + (8 until 15).map { qr.isDark(8, qr.size - 15 + it) }
        assertEquals(a, b)
    }
}
