package com.unistack.app.feature_templates.domain

import com.unistack.app.TextosDePrueba
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AcademicTemplateExportTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }
    @Test
    fun exportTextIncludesTemplateChecklistAndApaSections() {
        val template = AcademicTemplateLibrary.essayTemplates.first()

        val exported = template.exportText(completedChecklistIds = listOf("topic"))

        assertTrue(exported.contains(template.title))
        assertTrue(exported.contains("Checklist"))
        assertTrue(exported.contains("[x] Tema delimitado"))
        assertTrue(exported.contains("[ ] Fuentes revisadas"))
        assertTrue(exported.contains("Estructura"))
        assertTrue(exported.contains("APA básico"))
        assertTrue(exported.contains("Referencias APA"))
    }

    @Test
    fun buildApaReferenceDraftFormatsStructuredSources() {
        val exported = buildApaReferenceDraft("García, M. | 2024 | Aprendizaje activo | Revista Educación")

        assertTrue(exported.contains("García, M. (2024). Aprendizaje activo. Revista Educación."))
    }
}
