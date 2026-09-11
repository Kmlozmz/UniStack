package com.unistack.app.core.utils

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.unistack.app.feature_user.domain.AppLanguage
import java.util.Locale

/**
 * Los textos de la app para el código que no es Compose: ViewModels, dominio, notificaciones.
 *
 * **Existe porque los textos vivían en el código.** Durante meses cada frase que salía de un
 * ViewModel o del dominio era un `if (isEnglish) "..." else "..."`: traducía, pero a un tercer
 * idioma no se llegaba sin tocar cincuenta ficheros, y `strings.xml` —que es donde el resto de
 * la app ya tenía todo— se quedaba a medias. Con esto, el código que no puede llamar a
 * `stringResource` pide el mismo recurso y ya.
 *
 * **Resuelve con el idioma elegido, no con el del sistema.** El contexto de la aplicación
 * lleva la configuración del teléfono; si el usuario puso «English» con el teléfono en
 * español, `applicationContext.getString` seguiría en español. Aquí se mira la preferencia
 * persistida y se crea un contexto con ese idioma, que es lo que hace también la Activity.
 * Un aviso programado desde un worker sale, así, en el idioma de la app.
 *
 * El proveedor se pone al arrancar la app; las pruebas ponen el suyo leyendo los XML.
 */
object Textos {
    /** Quién resuelve un recurso. Nulo hasta que alguien lo ponga. */
    @Volatile
    var proveedor: ((id: Int, args: Array<out Any>) -> String)? = null

    fun get(@StringRes id: Int, vararg args: Any): String {
        val p = proveedor ?: error("Textos sin proveedor: la app lo pone en UniStackApplication")
        return p(id, args)
    }

    /** El proveedor de la app, apoyado en el contexto de la aplicación. */
    fun desde(context: Context) {
        val app = context.applicationContext
        var cache: Pair<Locale, Context>? = null
        proveedor = { id, args ->
            val idioma = when (LocaleHelper.getPersistedLanguage(app)) {
                AppLanguage.SPANISH -> Locale.forLanguageTag("es")
                AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
                AppLanguage.SYSTEM -> app.resources.configuration.locales[0] ?: Locale.getDefault()
            }
            val ctx = cache?.takeIf { it.first == idioma }?.second ?: run {
                val config = Configuration(app.resources.configuration).apply { setLocale(idioma) }
                app.createConfigurationContext(config).also { cache = idioma to it }
            }
            ctx.getString(id, *args)
        }
    }
}
