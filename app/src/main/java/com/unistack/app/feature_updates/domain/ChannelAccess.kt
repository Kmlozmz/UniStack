package com.unistack.app.feature_updates.domain

import java.security.MessageDigest
import java.util.Locale

/**
 * Quién puede recibir preestrenos.
 *
 * Los canales de preestreno se abren con un código que entrega quien publica. La app no guarda
 * el código: guarda su huella, y en cada comprobación la contrasta con la lista publicada. Así
 * retirar una línea de esa lista **revoca de verdad** el acceso, en vez de dejarlo abierto para
 * siempre en el móvil de quien ya lo canjeó.
 *
 * **Esto no impide instalar nada.** Los APK están en un repositorio público y quien tenga el
 * enlace descarga el que quiera. Lo que decide es qué canales ofrece la app, que es lo que
 * evita que alguien acabe en una alpha sin saber dónde se ha metido.
 *
 * La lista es pública, así que solo lleva huellas, nunca códigos. Lo que sostiene el sistema es
 * que el código sea largo: contra uno corto, la huella se rompe probando.
 */
object ChannelAccess {

    /** Huella de un código, tal y como aparece en la lista publicada. */
    fun fingerprint(code: String): String {
        val normalized = code.trim().uppercase(Locale.US).replace("-", "")
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Hasta qué canal abre una huella, según la lista.
     *
     * Un código de alpha abre también beta: quien acepta lo más inestable no tiene por qué
     * pedir otro permiso para lo que ya está más rodado.
     */
    fun channelFor(fingerprint: String, entries: List<AccessEntry>): UpdateChannel? =
        entries.firstOrNull { it.fingerprint.equals(fingerprint, ignoreCase = true) }?.channel

    data class AccessEntry(
        val channel: UpdateChannel,
        val fingerprint: String
    )
}
