package com.unistack.app.feature_support.domain

/** Los dos motivos por los que alguien escribe: algo se rompió, o algo falta. */
enum class TicketKind(val label: String, val emoji: String) {
    BUG("Fallo", "🐞"),
    IDEA("Sugerencia", "💡")
}

/**
 * Lo que la app sabe de sí misma y del teléfono.
 *
 * Va al final de cada ticket porque es lo primero que hay que preguntar si no está: sin versión
 * ni modelo, la mitad de los reportes acaban en «¿qué versión tienes?» y ahí se pierde el hilo.
 */
data class TicketContext(
    val appVersion: String,
    val androidVersion: String,
    val device: String
)

/** El destino de los tickets: el grupo público y el tema de cada motivo. */
object SupportChannel {
    const val GROUP = "https://t.me/unistacksoporte"
    private const val BUGS_TOPIC = "https://t.me/unistacksoporte/2"
    private const val IDEAS_TOPIC = "https://t.me/unistacksoporte/3"

    fun topicFor(kind: TicketKind): String = when (kind) {
        TicketKind.BUG -> BUGS_TOPIC
        TicketKind.IDEA -> IDEAS_TOPIC
    }
}

/**
 * El texto del ticket, listo para pegar.
 *
 * Telegram no deja rellenar el mensaje de un grupo desde un enlace —eso solo funciona con
 * bots—, así que la app copia esto al portapapeles y abre el tema: quien reporta solo tiene que
 * pegar. Se hace así, y no mandándolo la app por su cuenta, porque enviarlo directo obligaría a
 * llevar el token del bot dentro del APK, donde cualquiera lo saca; y además el mensaje sale de
 * su propia cuenta, que es lo que permite responderle.
 */
fun buildTicket(kind: TicketKind, text: String, context: TicketContext): String = buildString {
    append(kind.emoji)
    append(' ')
    appendLine(kind.label)
    appendLine()
    appendLine(text.trim())
    appendLine()
    appendLine("---")
    appendLine("UniStack ${context.appVersion}")
    append("Android ${context.androidVersion} · ${context.device}")
}
