package com.unistack.app.core.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import com.unistack.app.feature_user.domain.AppLanguage
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "app_locale_preferences"
    private const val KEY_LANGUAGE = "app_language"

    /**
     * Obtiene el idioma persistido para inicializar el contexto de la Activity antes
     * de que el DataStore asíncrono esté listo.
     */
    fun getPersistedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.name)
        return runCatching { AppLanguage.valueOf(name ?: AppLanguage.SYSTEM.name) }
            .getOrDefault(AppLanguage.SYSTEM)
    }

    /**
     * Persiste el idioma seleccionado en SharedPreferences síncronas.
     */
    fun persistLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.name)
            .apply()
    }

    /**
     * Resuelve el Locale correspondiente a la preferencia dada.
     * Si es SYSTEM, consulta el Locale configurado en el sistema operativo del teléfono.
     */
    fun resolveLocale(language: AppLanguage): Locale {
        return when (language) {
            AppLanguage.SYSTEM -> {
                val sysLocales = Resources.getSystem().configuration.locales
                if (!sysLocales.isEmpty) sysLocales[0] else Locale.getDefault()
            }
            AppLanguage.SPANISH -> Locale.forLanguageTag("es")
            AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
        }
    }

    /**
     * Crea un ConfigurationContext configurado con el Locale adecuado.
     */
    fun applyLocale(context: Context, language: AppLanguage = getPersistedLanguage(context)): Context {
        val targetLocale = resolveLocale(language)
        Locale.setDefault(targetLocale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(targetLocale)
        configuration.setLayoutDirection(targetLocale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(targetLocale))
        }

        return context.createConfigurationContext(configuration)
    }
}
