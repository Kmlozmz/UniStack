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
    val checklist: List<ChecklistItem>
        get() {
            val isEn = java.util.Locale.getDefault().language == "en"
            return if (isEn) {
                listOf(
                    ChecklistItem("topic", "Defined Topic", "Define a specific question and the scope of the work."),
                    ChecklistItem("sources", "Reviewed Sources", "Use academic, institutional, or course literature."),
                    ChecklistItem("thesis", "Thesis or Purpose", "Summarize in one sentence what you will argue, explain, or compare."),
                    ChecklistItem("outline", "Structure Ready", "Organize introduction, body paragraphs, evidence, and conclusion."),
                    ChecklistItem("draft", "Complete Draft", "Write a full version before polishing style and citations."),
                    ChecklistItem("references", "Verified References", "Check that every in-text citation has a corresponding final reference."),
                    ChecklistItem("review", "Final Review", "Read aloud, check formatting, and verify submission requirements.")
                )
            } else {
                listOf(
                    ChecklistItem("topic", "Tema delimitado", "Define una pregunta concreta y el alcance del trabajo."),
                    ChecklistItem("sources", "Fuentes revisadas", "Usa fuentes académicas, institucionales o bibliografía del curso."),
                    ChecklistItem("thesis", "Tesis o propósito", "Resume en una frase qué vas a defender, explicar o comparar."),
                    ChecklistItem("outline", "Estructura lista", "Ordena introducción, desarrollo, evidencias y cierre."),
                    ChecklistItem("draft", "Borrador completo", "Escribe una versión completa antes de pulir estilo y citas."),
                    ChecklistItem("references", "Referencias verificadas", "Comprueba que toda cita tenga su referencia final."),
                    ChecklistItem("review", "Revisión final", "Lee en voz alta, corrige formato y confirma requisitos de entrega.")
                )
            }
        }

    val essayTemplates: List<EssayTemplate>
        get() {
            val isEn = java.util.Locale.getDefault().language == "en"
            return if (isEn) {
                listOf(
                    EssayTemplate(
                        id = "argumentative",
                        title = "Argumentative Essay",
                        description = "Defend a stance with sound reasoning and evidence.",
                        sections = listOf(
                            TemplateSection("Title", "Introduce the topic and state your central stance clearly."),
                            TemplateSection("Introduction", "Contextualize the issue and present your thesis."),
                            TemplateSection("Argument 1", "State your primary reason and support it with evidence."),
                            TemplateSection("Argument 2", "Add a supporting reason or comparative case."),
                            TemplateSection("Counterargument", "Acknowledge an opposing view and refute it."),
                            TemplateSection("Conclusion", "Wrap up by restating thesis and key takeaway.")
                        )
                    ),
                    EssayTemplate(
                        id = "reading_report",
                        title = "Reading Report",
                        description = "Summarize, analyze, and evaluate an academic text.",
                        sections = listOf(
                            TemplateSection("Source Reference", "Author, year, title, and core source citation details."),
                            TemplateSection("Core Idea", "Explain the author's primary objective or argument."),
                            TemplateSection("Key Concepts", "List essential terms or conceptual frameworks."),
                            TemplateSection("Analysis", "Relate the reading to lectures, evidence, or broader context."),
                            TemplateSection("Critique", "Highlight strengths, limitations, or open questions."),
                            TemplateSection("Takeaway", "Summarize what you learned and why it matters.")
                        )
                    ),
                    EssayTemplate(
                        id = "research_outline",
                        title = "Research Project Proposal",
                        description = "Organize a concise research project outline.",
                        sections = listOf(
                            TemplateSection("Problem Statement", "Describe the problem or context you want to investigate."),
                            TemplateSection("Research Question", "Formulate a clear, answerable research question."),
                            TemplateSection("General Objective", "Define what the project aims to achieve."),
                            TemplateSection("Justification", "Explain academic or practical importance."),
                            TemplateSection("Methodology", "Summarize data collection or analysis methods."),
                            TemplateSection("Expected Outcomes", "Describe what insights you expect to produce.")
                        )
                    )
                )
            } else {
                listOf(
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
            }
        }

    val apaTips: List<ApaTip>
        get() {
            val isEn = java.util.Locale.getDefault().language == "en"
            return if (isEn) {
                listOf(
                    ApaTip("Margins and Font", "Use uniform margins, readable font, and consistent line spacing per course guidelines."),
                    ApaTip("Narrative Citation", "Author (year) introduced the idea when the author is part of the sentence structure."),
                    ApaTip("Parenthetical Citation", "Paraphrased or quoted idea followed by (Author, year) when emphasizing content."),
                    ApaTip("Basic Reference", "Author. (Year). Title of work. Publisher, journal, or website as applicable."),
                    ApaTip("Consistency", "Maintain consistent citation formatting throughout your entire document.")
                )
            } else {
                listOf(
                    ApaTip("Márgenes y fuente", "Usa márgenes uniformes, texto legible e interlineado consistente según la guía del curso."),
                    ApaTip("Cita narrativa", "Autor (año) plantea la idea cuando el autor hace parte de la oración."),
                    ApaTip("Cita parentética", "Idea resumida o citada seguida de (Autor, año) cuando el énfasis está en el contenido."),
                    ApaTip("Referencia básica", "Autor. (Año). Título de la obra. Editorial, revista o sitio según corresponda."),
                    ApaTip("Coherencia", "Mantén el mismo criterio de citas y referencias en todo el documento.")
                )
            }
        }
}
