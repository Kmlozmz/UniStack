package com.unistack.app.core.utils

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import com.unistack.app.feature_user.domain.AppLanguage
import java.util.Locale
import androidx.annotation.ArrayRes

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

    /** Quién resuelve un `string-array`. La app lo pone junto al otro. */
    @Volatile
    var proveedorDeListas: ((id: Int) -> List<String>)? = null

    fun get(@StringRes id: Int, vararg args: Any): String {
        val p = proveedor ?: error("Textos sin proveedor: la app lo pone en UniStackApplication")
        return p(id, args)
    }

    fun lista(@ArrayRes id: Int): List<String> {
        val p = proveedorDeListas ?: error("Textos sin proveedor de listas")
        return p(id)
    }

    /** El proveedor de la app, apoyado en el contexto de la aplicación. */
    fun desde(context: Context) {
        val app = context.applicationContext
        var cache: Pair<Locale, Context>? = null
        fun contexto(): Context {
            val idioma = when (LocaleHelper.getPersistedLanguage(app)) {
                AppLanguage.SPANISH -> Locale.forLanguageTag("es")
                AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
                AppLanguage.SYSTEM -> app.resources.configuration.locales[0] ?: Locale.getDefault()
            }
            return cache?.takeIf { it.first == idioma }?.second ?: run {
                val config = Configuration(app.resources.configuration).apply { setLocale(idioma) }
                app.createConfigurationContext(config).also { cache = idioma to it }
            }
        }
        proveedor = { id, args -> contexto().getString(id, *args) }
        proveedorDeListas = { id -> contexto().resources.getStringArray(id).toList() }
    }
}
