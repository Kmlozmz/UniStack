package com.unistack.app.feature_tasks.domain

import com.unistack.app.feature_notes.domain.AttachmentKind

/**
 * Lo que cuelga de una tarea: la foto de un enunciado, el PDF de una rúbrica, un audio.
 *
 * Calcado de `NoteAttachment` (misma tabla de reglas: `AttachmentKind`, `Attachments`, se copia
 * dentro de UniStack) con `taskId` en vez de `noteId`. Se reutiliza el tipo y las reglas de
 * Notas en vez de duplicarlas — un adjunto es un adjunto, tenga materia o no.
 */
data class TaskAttachment(
    val id: String,
    val taskId: String,
    val kind: AttachmentKind,
    val displayName: String,
    val storedName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMillis: Long? = null,
    val createdAt: Long
)
