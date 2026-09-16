package com.unistack.app.core.fallos

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.os.Process
import kotlin.system.exitProcess

/**
 * Se adelanta a Android: caza el fallo, deja el informe escrito y enseña la pantalla de la app
 * en lugar del diálogo gris del sistema.
 *
 * **Las dos puertas salen de aquí.** Siempre se escribe el informe antes de intentar nada más;
 * eso es lo barato y lo que no puede fallar. Después se intenta abrir la pantalla. Si se
 * consigue, el usuario ve el aviso en el acto —la puerta rápida—; si no se consigue, o si el
 * proceso se va de una forma que este manejador no llega a ver (memoria agotada, un cuelgue
 * que Android corta por su cuenta, algo que reviente en código nativo), el informe se queda en
 * disco y la pantalla sale en el siguiente arranque. Es la misma pantalla y el mismo archivo.
 *
 * Y hay un tercer caso, el mejor de los tres: si lo que falla es un hilo de fondo, la app no
 * se cierra en absoluto. Ver [uncaughtException].
 *
 * **Corre dentro de un proceso ya roto**, así que aquí no se hace nada que no sea
 * imprescindible y todo va envuelto: si escribir el informe falla, todavía queremos morirnos
 * bien. Por eso tampoco se delega en el manejador anterior al final —hacerlo devolvería el
 * diálogo del sistema justo después de haber enseñado el nuestro, que es lo que veníamos a
 * evitar— y en su lugar el proceso se cierra en silencio.
 */
class ManejadorDeFallos private constructor(
    private val context: Context
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(hilo: Thread, error: Throwable) {
        runCatching {
            AlmacenDeFallos.guardar(context, InformeDeFallo.de(context, error))
        }

        /*
         * **Si el que revienta no es el hilo principal, la app no se cierra.**
         *
         * Hasta ahora sí: una corrutina que explotaba en un scope suelto subía hasta el
         * manejador por defecto y ese cierra el proceso, aunque la pantalla que el usuario
         * estaba mirando siguiera perfectamente viva. Perder lo que estabas haciendo porque
         * falló una comprobación de actualizaciones en segundo plano no tiene ningún sentido.
         *
         * Aquí ese hilo muere —eso no se puede evitar, ya venía muerto— pero el proceso sigue
         * y el informe se queda esperando. El usuario se entera en el siguiente arranque, por
         * la puerta lenta, que es cuando no le estorba.
         */
        if (hilo != Looper.getMainLooper().thread) return

        runCatching { abrirPantalla() }
        // La pantalla vive en otro proceso, así que este puede irse sin llevársela por delante.
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }

    private fun abrirPantalla() {
        val intent = Intent(context, PantallaDeFalloActivity::class.java).apply {
            // Proceso nuevo, tarea nueva: el nuestro está a punto de desaparecer y con él se
            // iría la tarea a la que perteneciera la actividad.
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
        }
        context.startActivity(intent)
    }

    companion object {
        /**
         * @param context el de la aplicación. Se guarda a propósito: una Activity aquí sería
         *   una fuga, y además para cuando esto corra puede que ya no exista.
         */
        fun instalar(context: Context) {
            /*
             * En las pruebas no, y no es manía: este manejador es del JVM entero, no de
             * Android. Robolectric levanta esta Application para media suite, así que al
             * instalarlo sin mirar cualquier excepción suelta en un hilo de fondo dejaba de
             * imprimirse y pasaba a **matar al ejecutor de pruebas** con el código 10 de
             * `exitProcess`, tirando la tarea entera sin un solo test marcado en rojo.
             *
             * Ahí no hay proceso de Android que cerrar ni pantalla que enseñar, así que lo
             * correcto es no ponerse en medio y dejar el manejador por defecto.
             */
            if (Build.FINGERPRINT == "robolectric") return
            val app = context.applicationContext
            Thread.setDefaultUncaughtExceptionHandler(ManejadorDeFallos(app))
        }
    }
}
