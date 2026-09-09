package com.unistack.app

import com.unistack.app.feature_support.domain.TicketContext
import com.unistack.app.feature_support.domain.TicketKind
import com.unistack.app.feature_support.domain.buildTicket
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.exportText
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class LocalizationEnglishTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.ENGLISH)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun ticketKindLabelsInEnglish() {
        assertEquals("Bug", TicketKind.BUG.label)
        assertEquals("Suggestion", TicketKind.IDEA.label)
        assertEquals("Other", TicketKind.OTHER.label)
    }

    @Test
    fun buildTicketInEnglish() {
        val ticket = buildTicket(
            TicketKind.BUG,
            "Issue description",
            contact = "@user",
            context = TicketContext("1.6.0", "14", 34, "Pixel 8")
        )
        assertTrue(ticket.startsWith("🐞 Bug"))
    }

    @Test
    fun taskDateUtilsInEnglish() {
        val today = LocalDate.of(2026, 5, 12)
        assertEquals("overdue yesterday", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.minusDays(1)), today))
        assertEquals("due today", TaskDateUtils.dueText(TaskDateUtils.toMillis(today), today))
        assertEquals("due tomorrow", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.plusDays(1)), today))
        assertEquals("due in 3 days", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.plusDays(3)), today))
        assertEquals("overdue 2 days ago", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.minusDays(2)), today))
        assertEquals(
            "due tomorrow 09:15",
            TaskDateUtils.dueText(TaskDateUtils.toMillis(today.plusDays(1), LocalTime.of(9, 15)), today)
        )
    }

    @Test
    fun academicTemplateExportInEnglish() {
        val template = AcademicTemplateLibrary.essayTemplates.first()
        val exported = template.exportText(completedChecklistIds = listOf("topic"))
        assertTrue(exported.contains("Structure") || exported.contains("Estructura"))
    }
}
