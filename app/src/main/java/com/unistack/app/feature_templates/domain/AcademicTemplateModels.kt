package com.unistack.app.feature_templates.domain

import com.unistack.app.core.utils.Textos
import com.unistack.app.R

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
            return listOf(
                ChecklistItem("topic", Textos.get(R.string.tpl_check_topic_title), Textos.get(R.string.tpl_check_topic_detail)),
                ChecklistItem("sources", Textos.get(R.string.tpl_check_sources_title), Textos.get(R.string.tpl_check_sources_detail)),
                ChecklistItem("thesis", Textos.get(R.string.tpl_check_thesis_title), Textos.get(R.string.tpl_check_thesis_detail)),
                ChecklistItem("outline", Textos.get(R.string.tpl_check_outline_title), Textos.get(R.string.tpl_check_outline_detail)),
                ChecklistItem("draft", Textos.get(R.string.tpl_check_draft_title), Textos.get(R.string.tpl_check_draft_detail)),
                ChecklistItem("references", Textos.get(R.string.tpl_check_references_title), Textos.get(R.string.tpl_check_references_detail)),
                ChecklistItem("review", Textos.get(R.string.tpl_check_review_title), Textos.get(R.string.tpl_check_review_detail))
            )
        }

    val essayTemplates: List<EssayTemplate>
        get() {
            return listOf(
                EssayTemplate(
                    id = "argumentative",
                    title = Textos.get(R.string.tpl_essay_argumentative_title),
                    description = Textos.get(R.string.tpl_essay_argumentative_desc),
                    sections = listOf(
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s1_title), Textos.get(R.string.tpl_essay_argumentative_s1_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s2_title), Textos.get(R.string.tpl_essay_argumentative_s2_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s3_title), Textos.get(R.string.tpl_essay_argumentative_s3_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s4_title), Textos.get(R.string.tpl_essay_argumentative_s4_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s5_title), Textos.get(R.string.tpl_essay_argumentative_s5_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_argumentative_s6_title), Textos.get(R.string.tpl_essay_argumentative_s6_prompt))
                    )
                ),
                EssayTemplate(
                    id = "reading_report",
                    title = Textos.get(R.string.tpl_essay_reading_report_title),
                    description = Textos.get(R.string.tpl_essay_reading_report_desc),
                    sections = listOf(
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s1_title), Textos.get(R.string.tpl_essay_reading_report_s1_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s2_title), Textos.get(R.string.tpl_essay_reading_report_s2_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s3_title), Textos.get(R.string.tpl_essay_reading_report_s3_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s4_title), Textos.get(R.string.tpl_essay_reading_report_s4_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s5_title), Textos.get(R.string.tpl_essay_reading_report_s5_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_reading_report_s6_title), Textos.get(R.string.tpl_essay_reading_report_s6_prompt))
                    )
                ),
                EssayTemplate(
                    id = "research_outline",
                    title = Textos.get(R.string.tpl_essay_research_outline_title),
                    description = Textos.get(R.string.tpl_essay_research_outline_desc),
                    sections = listOf(
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s1_title), Textos.get(R.string.tpl_essay_research_outline_s1_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s2_title), Textos.get(R.string.tpl_essay_research_outline_s2_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s3_title), Textos.get(R.string.tpl_essay_research_outline_s3_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s4_title), Textos.get(R.string.tpl_essay_research_outline_s4_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s5_title), Textos.get(R.string.tpl_essay_research_outline_s5_prompt)),
                        TemplateSection(Textos.get(R.string.tpl_essay_research_outline_s6_title), Textos.get(R.string.tpl_essay_research_outline_s6_prompt))
                    )
                )
            )
        }

    val apaTips: List<ApaTip>
        get() {
            return listOf(
                ApaTip(Textos.get(R.string.tpl_apa_1_title), Textos.get(R.string.tpl_apa_1_desc)),
                ApaTip(Textos.get(R.string.tpl_apa_2_title), Textos.get(R.string.tpl_apa_2_desc)),
                ApaTip(Textos.get(R.string.tpl_apa_3_title), Textos.get(R.string.tpl_apa_3_desc)),
                ApaTip(Textos.get(R.string.tpl_apa_4_title), Textos.get(R.string.tpl_apa_4_desc)),
                ApaTip(Textos.get(R.string.tpl_apa_5_title), Textos.get(R.string.tpl_apa_5_desc))
            )
        }
}
