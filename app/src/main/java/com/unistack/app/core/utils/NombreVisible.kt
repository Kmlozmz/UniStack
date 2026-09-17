package com.unistack.app.core.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * El nombre con el que el sistema enseña un archivo elegido, o nulo si no lo dice.
 *
 * Lo usan los adjuntos de notas y de tareas. Estaba escrito dos veces, igual, uno en cada
 * almacén.
 */
internal fun Context.nombreVisibleDe(uri: Uri): String? = runCatching {
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indice >= 0 && cursor.moveToFirst()) cursor.getString(indice) else null
        }
}.getOrNull()
