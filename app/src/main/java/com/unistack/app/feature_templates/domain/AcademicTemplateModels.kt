package com.unistack.app.feature_templates.domain

data class ChecklistItem(
    val id: String,
    val title: String,
    val detail: String
)

data class EssayTemplate(
    val id: String,
    val title: String,
    val description: String,
    val sections: List<TemplateSection>
)

data class TemplateSection(
    val title: String,
    val prompt: String
)

data class ApaTip(
    val title: String,
    val description: String
)

object AcademicTemplateLibrary {
    val checklist = listOf(
        ChecklistItem("topic", "Tema delimitado", "Define una pregunta concreta y el alcance del trabajo."),
        ChecklistItem("sources", "Fuentes revisadas", "Usa fuentes académicas, institucionales o bibliografía del curso."),
        ChecklistItem("thesis", "Tesis o propósito", "Resume en una frase qué vas a defender, explicar o comparar."),
        ChecklistItem("outline", "Estructura lista", "Ordena introducción, desarrollo, evidencias y cierre."),
        ChecklistItem("draft", "Borrador completo", "Escribe una versión completa antes de pulir estilo y citas."),
        ChecklistItem("references", "Referencias verificadas", "Comprueba que toda cita tenga su referencia final."),
        ChecklistItem("review", "Revisión final", "Lee en voz alta, corrige formato y confirma requisitos de entrega.")
    )

    val essayTemplates = listOf(
        EssayTemplate(
            id = "argumentative",
            title = "Ensayo argumentativo",
            description = "Para defender una postura con razones y evidencias.",
            sections = listOf(
                TemplateSection("Título", "Plantea el tema y deja clara la postura central."),
                TemplateSection("Introducción", "Contextualiza el problema y presenta tu tesis."),
                TemplateSection("Argumento 1", "Expón la razón principal y respáldala con evidencia."),
                TemplateSection("Argumento 2", "Agrega una segunda razón o un caso comparativo."),
                TemplateSection("Contraargumento", "Reconoce una objeción y responde con criterio."),
                TemplateSection("Conclusión", "Cierra retomando la tesis y la implicación principal.")
            )
        ),
        EssayTemplate(
            id = "reading_report",
            title = "Informe de lectura",
            description = "Para resumir, analizar y valorar un texto académico.",
            sections = listOf(
                TemplateSection("Referencia del texto", "Autor, año, título y datos básicos de la fuente."),
                TemplateSection("Idea central", "Explica el objetivo o argumento principal del autor."),
                TemplateSection("Conceptos clave", "Lista términos o categorías importantes."),
                TemplateSection("Análisis", "Relaciona el texto con clase, evidencia o contexto."),
                TemplateSection("Valoración", "Indica aportes, límites o preguntas abiertas."),
                TemplateSection("Cierre", "Resume qué aprendiste y por qué importa.")
            )
        ),
        EssayTemplate(
            id = "research_outline",
            title = "Proyecto de investigación",
            description = "Para organizar una propuesta corta de investigación.",
            sections = listOf(
                TemplateSection("Problema", "Describe la situación que quieres investigar."),
                TemplateSection("Pregunta", "Formula una pregunta clara y respondible."),
                TemplateSection("Objetivo general", "Define qué busca lograr el trabajo."),
                TemplateSection("Justificación", "Explica relevancia académica o práctica."),
                TemplateSection("Método", "Resume cómo recolectarás o analizarás información."),
                TemplateSection("Resultados esperados", "Describe el tipo de respuesta que esperas construir.")
            )
        )
    )

    val apaTips = listOf(
        ApaTip("Márgenes y fuente", "Usa márgenes uniformes, texto legible e interlineado consistente según la guía del curso."),
        ApaTip("Cita narrativa", "Autor (año) plantea la idea cuando el autor hace parte de la oración."),
        ApaTip("Cita parentética", "Idea resumida o citada seguida de (Autor, año) cuando el énfasis está en el contenido."),
        ApaTip("Referencia básica", "Autor. (Año). Título de la obra. Editorial, revista o sitio según corresponda."),
        ApaTip("Coherencia", "Mantén el mismo criterio de citas y referencias en todo el documento.")
    )
}
