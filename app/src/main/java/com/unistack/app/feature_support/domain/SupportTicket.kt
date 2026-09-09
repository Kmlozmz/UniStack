package com.unistack.app.feature_support.domain

/**
 * Por qué alguien escribe.
 *
 * Eran dos —fallo e idea— y cada uno tenía su propia puerta en Ayuda. Se juntaron en un solo
 * formulario con este selector arriba: quien escribe no siempre sabe de antemano si lo suyo es
 * un fallo o una petición, y obligarle a elegir antes de contar nada dejaba fuera todo lo que
 * no era ninguna de las dos.
 */
enum class TicketKind(private val spanishLabel: String, val emoji: String) {
    BUG("Fallo", "🐞"),
    IDEA("Sugerencia", "💡"),
    OTHER("Otro", "💬");

    val label: String
        get() = if (java.util.Locale.getDefault().language == "en") {
            when (this) {
                BUG -> "Bug"
                IDEA -> "Suggestion"
                OTHER -> "Other"
            }
        } else {
            spanishLabel
        }
}

/**
 * Lo que la app sabe de sí misma y del teléfono.
 *
 * Va al final de cada ticket porque es lo primero que hay que preguntar si no está: sin versión
 * ni modelo, la mitad de los reportes acaban en «¿qué versión tienes?» y ahí se pierde el hilo.
 *
 * El número de SDK acompaña a la versión de Android porque no siempre coinciden con lo que uno
 * espera —las capas de fabricante cambian la etiqueta— y es el número con el que se comprueba
 * si algo aplica a esa versión.
 */
data class TicketContext(
    val appVersion: String,
    val androidVersion: String,
    val androidSdk: Int,
    val device: String
)

/** El destino de los tickets: el grupo público y el tema de cada motivo. */
object SupportChannel {
    const val HANDLE = "unistacksoporte"
    const val GROUP = "https://t.me/$HANDLE"

    private const val BUGS_TOPIC = 2
    private const val IDEAS_TOPIC = 3

    /**
     * El tema del grupo al que va cada motivo, o nulo si no tiene uno propio.
     *
     * «Otro» no tiene tema: cae en el general del grupo. Es lo correcto mientras no exista uno,
     * porque mandarlo a Fallos o a Sugerencias ensuciaría dos listas que sirven justamente para
     * separar. Si algún día se crea un tema para lo demás, aquí va su número.
     */
    fun topicFor(kind: TicketKind): Int? = when (kind) {
        TicketKind.BUG -> BUGS_TOPIC
        TicketKind.IDEA -> IDEAS_TOPIC
        TicketKind.OTHER -> null
    }

    /**
     * El enlace web del tema. Es el que entiende cualquiera, y el que falla si no hay red.
     */
    fun webUrlFor(kind: TicketKind): String =
        topicFor(kind)?.let { "$GROUP/$it" } ?: GROUP

    /**
     * El enlace interno de Telegram para el mismo sitio.
     *
     * Se intenta primero porque no pasa por el navegador ni por el dominio: `t.me` es una
     * página web, y abrirla exige resolver el dominio y cargarla para que ella reenvíe a la
     * app. Con una VPN de por medio —o sin datos— eso termina en un error de DNS y el ticket
     * se queda a medio camino. `tg://` va directo a la app instalada.
     */
    fun appUriFor(kind: TicketKind): String =
        topicFor(kind)
            ?.let { "tg://resolve?domain=$HANDLE&thread=$it" }
            ?: "tg://resolve?domain=$HANDLE"
}

/**
 * El texto del ticket, listo para pegar.
 *
 * Telegram no deja rellenar el mensaje de un grupo desde un enlace —eso solo funciona con
 * bots—, así que la app copia esto al portapapeles y abre el tema: quien reporta solo tiene que
 * pegar. Se hace así, y no mandándolo la app por su cuenta, porque enviarlo directo obligaría a
 * llevar el token del bot dentro del APK, donde cualquiera lo saca; y además el mensaje sale de
 * su propia cuenta, que es lo que permite responderle.
 *
 * El contacto solo aparece si se escribió. Es opcional a propósito: el mensaje ya sale de una
 * cuenta de Telegram con la que se puede responder, y pedir un correo obligatorio para algo que
 * casi siempre es responder ahí mismo sobra.
 */
fun buildTicket(
    kind: TicketKind,
    text: String,
    contact: String?,
    context: TicketContext
): String = buildString {
    append(kind.emoji)
    append(' ')
    appendLine(kind.label)
    appendLine()
    appendLine(text.trim())
    appendLine()
    appendLine("---")
    appendLine("UniStack ${context.appVersion}")
    appendLine("Android ${context.androidVersion} (SDK ${context.androidSdk}) · ${context.device}")
    contact?.trim()?.takeIf { it.isNotEmpty() }?.let { append("Contacto: $it") }
}
