package com.unistack.app.core.fallos

import android.content.Context
import java.io.File

/**
 * El único fallo que está esperando a que alguien lo cuente.
 *
 * **Un archivo suelto, no una base ni una preferencia.** Lo escribe un proceso que se está
 * muriendo: abrir Room o DataStore ahí es pedirle a una app rota que haga justo lo que sabe
 * hacer mal. Un `File.writeText` es una llamada al sistema y ya está.
 *
 * **Sólo cabe uno.** No hay historial, ni pantalla de registros, ni nada que el usuario tenga
 * que ir a buscar a Ajustes: hay un fallo pendiente o no lo hay. Si la app se cierra tres
 * veces seguidas por lo mismo, el informe que queda es el último, que es el que sirve. Quien
 * quiera conservar uno lo guarda como archivo desde la propia pantalla y deja de ser cosa de
 * la app.
 */
object AlmacenDeFallos {
    private const val NOMBRE = "fallo-pendiente.json"

    private fun archivo(context: Context) = File(context.applicationContext.filesDir, NOMBRE)

    fun guardar(context: Context, informe: InformeDeFallo) {
        runCatching { archivo(context).writeText(informe.comoJson()) }
    }

    fun pendiente(context: Context): InformeDeFallo? = runCatching {
        val f = archivo(context)
        if (!f.exists()) return null
        InformeDeFallo.deJson(f.readText())
    }.getOrNull()

    /**
     * Se llama cuando el usuario ya vio la pantalla, decida lo que decida.
     *
     * Contarlo, guardarlo o no hacer nada son las tres salidas, y las tres cierran el asunto:
     * lo que no puede pasar es que la pantalla vuelva a salir en el siguiente arranque por un
     * fallo que el usuario ya despachó.
     */
    fun limpiar(context: Context) {
        runCatching { archivo(context).delete() }
    }
}
