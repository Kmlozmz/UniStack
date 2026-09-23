package com.unistack.app.feature_rooms.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Lo que se junta para el trabajo final: portada, secciones en orden y bibliografía. */
data class WorkDoc(
    val title: String,
    val kicker: String,
    val kind: String,
    val authors: List<String>,
    val due: String,
    val dueLabel: String,
    val sections: List<WorkSection>,
    val bibliographyTitle: String,
    val bibliography: List<String>,
    val accent: Int
)

data class WorkSection(val title: String, val text: String, val owner: String)

/**
 * Escribe el trabajo en cada formato sin librerías: el PDF con el lienzo de Android (hojas A4 o
 * diapositivas 16:9), el Word y el PowerPoint como paquetes Office mínimos y el texto plano.
 */
object WorkExport {

    // ---------------------------------------------------------------- PDF (documento A4)

    fun writePdfDocument(doc: WorkDoc, out: File): Int {
        val pdf = PdfDocument()
        val w = 595; val h = 842; val margin = 64f
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.SERIF; textSize = 10.5f; color = Color.rgb(34, 34, 34) }
        val head = TextPaint(body).apply { typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD); textSize = 14f }
        val small = TextPaint(body).apply { textSize = 8.5f; color = Color.rgb(119, 119, 119) }
        var pageNo = 0
        var page: PdfDocument.Page? = null
        var y = 0f
        fun finish() { page?.let { pdf.finishPage(it) }; page = null }
        fun newPage(): PdfDocument.Page {
            finish()
            pageNo++
            val p = pdf.startPage(PdfDocument.PageInfo.Builder(w, h, pageNo).create())
            if (pageNo > 1) {
                val n = "$pageNo"
                p.canvas.drawText(n, (w - small.measureText(n)) / 2f, h - 36f, small)
            }
            page = p; y = margin
            return p
        }
        // Portada
        val cover = newPage().canvas
        val center: (String, TextPaint, Float) -> Unit = { t, p, yy -> cover.drawText(t, (w - p.measureText(t)) / 2f, yy, p) }
        center(doc.kicker.uppercase(), TextPaint(small).apply { letterSpacing = 0.15f }, 250f)
        val titleLayout = layout(doc.title, TextPaint(head).apply { textSize = 24f }, (w - 2 * margin).toInt(), Layout.Alignment.ALIGN_CENTER)
        cover.save(); cover.translate(margin, 300f); titleLayout.draw(cover); cover.restore()
        val afterTitle = 300f + titleLayout.height
        center(doc.kind, TextPaint(body).apply { textSkewX = -0.25f; textSize = 12f; color = Color.rgb(85, 85, 85) }, afterTitle + 24f)
        center(doc.authors.joinToString(" · "), body, afterTitle + 90f)
        center("${doc.dueLabel}: ${doc.due}", small, afterTitle + 108f)
        // Secciones
        fun flow(l: StaticLayout, x: Float = margin) {
            for (line in 0 until l.lineCount) {
                val lh = (l.getLineBottom(line) - l.getLineTop(line)).toFloat()
                if (page == null || y + lh > h - margin) newPage()
                val c = page!!.canvas
                c.save(); c.translate(x, y - l.getLineTop(line)); c.clipRect(0f, l.getLineTop(line).toFloat(), l.width.toFloat(), l.getLineBottom(line).toFloat()); l.draw(c); c.restore()
                y += lh
            }
        }
        newPage()
        doc.sections.forEachIndexed { i, s ->
            if (y > margin && y + 60 > h - margin) newPage()
            if (y > margin) y += 18f
            flow(layout("${i + 1}. ${s.title}", head, (w - 2 * margin).toInt()))
            y += 8f
            s.text.split(Regex("\n{2,}")).filter { it.isNotBlank() }.forEach { para ->
                flow(layout(para.trim(), body, (w - 2 * margin).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.45f))
                y += 7f
            }
        }
        if (doc.bibliography.isNotEmpty()) {
            if (y + 80 > h - margin) newPage() else y += 22f
            flow(layout(doc.bibliographyTitle, head, (w - 2 * margin).toInt()))
            y += 8f
            doc.bibliography.forEach { ref ->
                val l = layout(ref, body, (w - 2 * margin - 18).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.4f)
                flow(l, margin + 18f)
                y += 5f
            }
        }
        finish()
        out.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return pageNo
    }

    // ---------------------------------------------------------------- PDF (diapositivas 16:9)

    fun writePdfSlides(doc: WorkDoc, out: File): Int {
        val pdf = PdfDocument()
        val w = 960; val h = 540
        var n = 0
        fun slide(draw: (android.graphics.Canvas) -> Unit) {
            n++
            val p = pdf.startPage(PdfDocument.PageInfo.Builder(w, h, n).create())
            draw(p.canvas); pdf.finishPage(p)
        }
        val white = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; typeface = Typeface.DEFAULT_BOLD; textSize = 44f }
        val ink = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(34, 34, 34); textSize = 22f }
        val headP = TextPaint(ink).apply { typeface = Typeface.DEFAULT_BOLD; textSize = 34f }
        val soft = TextPaint(ink).apply { textSize = 15f; color = Color.rgb(119, 119, 119) }
        val fill = Paint().apply { color = doc.accent }
        slide { c ->
            c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), fill)
            val t = layout(doc.title, white, w - 140)
            c.save(); c.translate(70f, 170f); t.draw(c); c.restore()
            c.drawText(doc.kicker, 70f, 190f + t.height, TextPaint(white).apply { textSize = 20f; typeface = Typeface.DEFAULT; alpha = 220 })
            c.drawText(doc.authors.joinToString(" · "), 70f, 250f + t.height, TextPaint(white).apply { textSize = 17f; typeface = Typeface.DEFAULT; alpha = 200 })
        }
        doc.sections.forEachIndexed { i, s ->
            slide { c ->
                c.drawColor(Color.WHITE)
                c.drawRect(0f, 0f, w.toFloat(), 12f, fill)
                c.drawText("${i + 1}. ${s.title}", 60f, 90f, headP)
                var y = 140f
                bullets(s.text).forEach { b ->
                    val l = layout("•  $b", ink, w - 140, Layout.Alignment.ALIGN_NORMAL, 1.3f)
                    if (y + l.height < h - 60) { c.save(); c.translate(70f, y); l.draw(c); c.restore(); y += l.height + 14f }
                }
                if (s.owner.isNotBlank()) c.drawText(s.owner, w - 60f - soft.measureText(s.owner), h - 30f, soft)
            }
        }
        if (doc.bibliography.isNotEmpty()) slide { c ->
            c.drawColor(Color.WHITE)
            c.drawRect(0f, 0f, w.toFloat(), 12f, fill)
            c.drawText(doc.bibliographyTitle, 60f, 90f, headP)
            var y = 140f
            doc.bibliography.take(8).forEach { b ->
                val l = layout(b, TextPaint(ink).apply { textSize = 17f }, w - 140, Layout.Alignment.ALIGN_NORMAL, 1.25f)
                if (y + l.height < h - 40) { c.save(); c.translate(70f, y); l.draw(c); c.restore(); y += l.height + 10f }
            }
        }
        out.outputStream().use { pdf.writeTo(it) }
        pdf.close()
        return n
    }

    /** Las frases de una sección que caben como viñetas (hasta cinco). */
    fun bullets(text: String): List<String> =
        text.split(Regex("(?<=[.!?])\\s+|\\n+")).map { it.trim().trimEnd('.') }.filter { it.length > 12 }.take(5)

    private fun layout(text: String, paint: TextPaint, width: Int, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL, spacing: Float = 1.15f): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(10)).setAlignment(align).setLineSpacing(0f, spacing).build()

    // ---------------------------------------------------------------- texto plano

    fun writeText(doc: WorkDoc, out: File): Int {
        val sb = StringBuilder()
        sb.appendLine(doc.title.uppercase()).appendLine(doc.kicker).appendLine(doc.authors.joinToString(", ")).appendLine()
        doc.sections.forEachIndexed { i, s -> sb.appendLine("${i + 1}. ${s.title.uppercase()}").appendLine().appendLine(s.text.trim()).appendLine() }
        if (doc.bibliography.isNotEmpty()) { sb.appendLine(doc.bibliographyTitle.uppercase()); doc.bibliography.forEach { sb.appendLine("- $it") } }
        out.writeText(sb.toString())
        return sb.split(Regex("\\s+")).count { it.isNotBlank() }
    }

    // ---------------------------------------------------------------- Word (.docx)

    fun writeDocx(doc: WorkDoc, out: File) {
        val body = StringBuilder()
        fun p(text: String, style: String? = null, align: String? = null, italic: Boolean = false, size: Int? = null, hanging: Boolean = false, color: String? = null) {
            body.append("<w:p><w:pPr>")
            if (style != null) body.append("<w:pStyle w:val=\"$style\"/>")
            if (hanging) body.append("<w:ind w:left=\"720\" w:hanging=\"720\"/>")
            if (align != null) body.append("<w:jc w:val=\"$align\"/>")
            body.append("</w:pPr><w:r><w:rPr>")
            if (italic) body.append("<w:i/>")
            if (size != null) body.append("<w:sz w:val=\"$size\"/>")
            if (color != null) body.append("<w:color w:val=\"$color\"/>")
            body.append("</w:rPr><w:t xml:space=\"preserve\">${xml(text)}</w:t></w:r></w:p>")
        }
        repeat(6) { body.append("<w:p/>") }
        p(doc.kicker.uppercase(), align = "center", size = 18, color = "777777")
        repeat(3) { body.append("<w:p/>") }
        p(doc.title, "Title", "center")
        p(doc.kind, align = "center", italic = true, color = "555555")
        repeat(4) { body.append("<w:p/>") }
        p(doc.authors.joinToString(" · "), align = "center")
        p("${doc.dueLabel}: ${doc.due}", align = "center", size = 18, color = "777777")
        body.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>")
        doc.sections.forEachIndexed { i, s ->
            p("${i + 1}. ${s.title}", "Heading1")
            s.text.split(Regex("\n{2,}|\n")).filter { it.isNotBlank() }.forEach { p(it.trim(), align = "both") }
        }
        if (doc.bibliography.isNotEmpty()) {
            p(doc.bibliographyTitle, "Heading1")
            doc.bibliography.forEach { p(it, hanging = true) }
        }
        val document = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><w:body>$body<w:sectPr><w:footerReference w:type="default" r:id="rId2"/><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/><w:titlePg/></w:sectPr></w:body></w:document>"""
        val styles = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/><w:sz w:val="24"/><w:lang w:val="es-ES"/></w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="160" w:line="360" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults><w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style><w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:pPr><w:spacing w:after="240"/></w:pPr><w:rPr><w:b/><w:sz w:val="48"/></w:rPr></w:style><w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="360" w:after="160"/><w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:b/><w:sz w:val="28"/></w:rPr></w:style></w:styles>"""
        val footer = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:p><w:pPr><w:jc w:val="center"/></w:pPr><w:fldSimple w:instr="PAGE"><w:r><w:t>1</w:t></w:r></w:fldSimple></w:p></w:ftr>"""
        zip(out, linkedMapOf(
            "[Content_Types].xml" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/><Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/><Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/></Types>""",
            "_rels/.rels" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>""",
            "word/_rels/document.xml.rels" to """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/></Relationships>""",
            "word/document.xml" to document,
            "word/styles.xml" to styles,
            "word/footer1.xml" to footer
        ))
    }

    // ---------------------------------------------------------------- PowerPoint (.pptx)

    fun writePptx(doc: WorkDoc, out: File): Int {
        val accent = String.format("%06X", doc.accent and 0xFFFFFF)
        val slides = ArrayList<String>()
        val cx = 12192000L; val cy = 6858000L
        var shapeId = 2
        fun sp(x: Long, y: Long, w: Long, h: Long, fill: String?, paragraphs: String): String {
            val id = shapeId++
            return "<p:sp><p:nvSpPr><p:cNvPr id=\"$id\" name=\"Shape $id\"/><p:cNvSpPr txBox=\"1\"/><p:nvPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x=\"$x\" y=\"$y\"/><a:ext cx=\"$w\" cy=\"$h\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>" +
                (if (fill != null) "<a:solidFill><a:srgbClr val=\"$fill\"/></a:solidFill>" else "<a:noFill/>") +
                "</p:spPr><p:txBody><a:bodyPr wrap=\"square\" lIns=\"91440\" tIns=\"45720\" rIns=\"91440\" bIns=\"45720\"><a:normAutofit/></a:bodyPr><a:lstStyle/>$paragraphs</p:txBody></p:sp>"
        }
        fun run(text: String, size: Int, bold: Boolean = false, color: String = "222222") =
            "<a:r><a:rPr lang=\"es-ES\" sz=\"$size\"${if (bold) " b=\"1\"" else ""} dirty=\"0\"><a:solidFill><a:srgbClr val=\"$color\"/></a:solidFill></a:rPr><a:t>${xml(text)}</a:t></a:r>"
        fun para(r: String, bullet: Boolean = false, align: String? = null) =
            "<a:p>" + (if (bullet) "<a:pPr marL=\"342900\" indent=\"-342900\"><a:buFont typeface=\"Arial\"/><a:buChar char=\"•\"/></a:pPr>" else if (align != null) "<a:pPr algn=\"$align\"/>" else "") + r + "</a:p>"
        fun slideXml(shapes: String) = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>$shapes</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>"""

        shapeId = 2
        slides += slideXml(
            sp(0, 0, cx, cy, accent, para(run("", 1000))) +
                sp(800000, 2000000, cx - 1600000, 1500000, null, para(run(doc.title, 4400, true, "FFFFFF"))) +
                sp(800000, 3550000, cx - 1600000, 500000, null, para(run(doc.kicker, 2000, false, "FFFFFF"))) +
                sp(800000, 4300000, cx - 1600000, 500000, null, para(run(doc.authors.joinToString(" · "), 1600, false, "FFFFFF")))
        )
        doc.sections.forEachIndexed { i, s ->
            shapeId = 2
            val bl = bullets(s.text).ifEmpty { listOf(s.text.take(160)) }.filter { it.isNotBlank() }
            slides += slideXml(
                sp(0, 0, cx, 120000, accent, para(run("", 100))) +
                    sp(600000, 450000, cx - 1200000, 900000, null, para(run("${i + 1}. ${s.title}", 3200, true))) +
                    sp(600000, 1500000, cx - 1200000, 4300000, null, bl.joinToString("") { para(run(it, 2000), bullet = true) }.ifEmpty { para(run(" ", 2000)) }) +
                    (if (s.owner.isNotBlank()) sp(cx - 4600000, cy - 700000, 4000000, 400000, null, para(run(s.owner, 1200, false, "777777"), align = "r")) else "")
            )
        }
        if (doc.bibliography.isNotEmpty()) {
            shapeId = 2
            slides += slideXml(
                sp(0, 0, cx, 120000, accent, para(run("", 100))) +
                    sp(600000, 450000, cx - 1200000, 900000, null, para(run(doc.bibliographyTitle, 3200, true))) +
                    sp(600000, 1500000, cx - 1200000, 4600000, null, doc.bibliography.take(10).joinToString("") { para(run(it, 1500)) })
            )
        }
        val ns = "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\""
        val files = linkedMapOf<String, String>()
        files["[Content_Types].xml"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/ppt/presentation.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml\"/>" +
            "<Override PartName=\"/ppt/slideMasters/slideMaster1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml\"/>" +
            "<Override PartName=\"/ppt/slideLayouts/slideLayout1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml\"/>" +
            "<Override PartName=\"/ppt/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/>" +
            slides.indices.joinToString("") { "<Override PartName=\"/ppt/slides/slide${it + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>" } + "</Types>"
        files["_rels/.rels"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"ppt/presentation.xml\"/></Relationships>"
        files["ppt/presentation.xml"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<p:presentation $ns saveSubsetFonts=\"1\"><p:sldMasterIdLst><p:sldMasterId id=\"2147483648\" r:id=\"rId1\"/></p:sldMasterIdLst><p:sldIdLst>" +
            slides.indices.joinToString("") { "<p:sldId id=\"${256 + it}\" r:id=\"rId${it + 3}\"/>" } + "</p:sldIdLst><p:sldSz cx=\"$cx\" cy=\"$cy\"/><p:notesSz cx=\"6858000\" cy=\"9144000\"/></p:presentation>"
        files["ppt/_rels/presentation.xml.rels"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" Target=\"slideMasters/slideMaster1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"theme/theme1.xml\"/>" +
            slides.indices.joinToString("") { "<Relationship Id=\"rId${it + 3}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide${it + 1}.xml\"/>" } + "</Relationships>"
        val emptyTree = "<p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld>"
        files["ppt/slideMasters/slideMaster1.xml"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<p:sldMaster $ns>$emptyTree<p:clrMap bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/><p:sldLayoutIdLst><p:sldLayoutId id=\"2147483649\" r:id=\"rId1\"/></p:sldLayoutIdLst></p:sldMaster>"
        files["ppt/slideMasters/_rels/slideMaster1.xml.rels"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" Target=\"../slideLayouts/slideLayout1.xml\"/><Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" Target=\"../theme/theme1.xml\"/></Relationships>"
        files["ppt/slideLayouts/slideLayout1.xml"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<p:sldLayout $ns type=\"blank\" preserve=\"1\">${emptyTree.replace("<p:cSld>", "<p:cSld name=\"Blank\">")}<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>"
        files["ppt/slideLayouts/_rels/slideLayout1.xml.rels"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" Target=\"../slideMasters/slideMaster1.xml\"/></Relationships>"
        files["ppt/theme/theme1.xml"] = theme(accent)
        slides.forEachIndexed { i, s ->
            files["ppt/slides/slide${i + 1}.xml"] = s
            files["ppt/slides/_rels/slide${i + 1}.xml.rels"] = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" Target=\"../slideLayouts/slideLayout1.xml\"/></Relationships>"
        }
        zip(out, files)
        return slides.size
    }

    private fun theme(accent: String): String {
        fun c(name: String, v: String) = "<a:$name><a:srgbClr val=\"$v\"/></a:$name>"
        val fills = "<a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill>".repeat(3)
        val lines = listOf(6350, 12700, 19050).joinToString("") { "<a:ln w=\"$it\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln>" }
        val effects = "<a:effectStyle><a:effectLst/></a:effectStyle>".repeat(3)
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"UniStack\"><a:themeElements>" +
            "<a:clrScheme name=\"UniStack\">" + c("dk1", "000000") + c("lt1", "FFFFFF") + c("dk2", "1F2430") + c("lt2", "F2F3F5") + c("accent1", accent) + c("accent2", "2B6CD9") +
            c("accent3", "0A8A31") + c("accent4", "8C6800") + c("accent5", "7446D6") + c("accent6", "C8321F") + c("hlink", "2B6CD9") + c("folHlink", "7446D6") + "</a:clrScheme>" +
            "<a:fontScheme name=\"UniStack\"><a:majorFont><a:latin typeface=\"Calibri\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:majorFont><a:minorFont><a:latin typeface=\"Calibri\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:minorFont></a:fontScheme>" +
            "<a:fmtScheme name=\"UniStack\"><a:fillStyleLst>$fills</a:fillStyleLst><a:lnStyleLst>$lines</a:lnStyleLst><a:effectStyleLst>$effects</a:effectStyleLst><a:bgFillStyleLst>$fills</a:bgFillStyleLst></a:fmtScheme>" +
            "</a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/></a:theme>"
    }

    private fun zip(out: File, files: Map<String, String>) {
        ZipOutputStream(out.outputStream()).use { z ->
            files.forEach { (name, content) ->
                z.putNextEntry(ZipEntry(name)); z.write(content.toByteArray(Charsets.UTF_8)); z.closeEntry()
            }
        }
    }

    private fun xml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        .filter { it == '\t' || it == '\n' || it == '\r' || it >= ' ' }
}
