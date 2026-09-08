package com.unistack.app.core.utils

import android.content.Context
import android.content.res.Configuration
import com.unistack.app.feature_user.domain.AppLanguage
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "app_locale_preferences"
    private const val KEY_LANGUAGE = "app_language"

    /**
     * Obtiene el idioma persistido para inicializar el contexto de la Activity antes
     * de que el DataStore asíncrono esté listo. Si ocurre cualquier fallo o no hay
     * preferencia guardada, devuelve AppLanguage.SYSTEM de forma segura.
     */
    fun getPersistedLanguage(context: Context): AppLanguage {
        return runCatching {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.name)
            name?.let { AppLanguage.valueOf(it) } ?: AppLanguage.SYSTEM
        }.getOrDefault(AppLanguage.SYSTEM)
    }

    /**
     * Persiste el idioma seleccionado en SharedPreferences síncronas.
     */
    fun persistLanguage(context: Context, language: AppLanguage) {
        runCatching {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language.name)
                .apply()
        }
    }

    /**
     * Aplica la configuración de idioma a un contexto base de Activity.
     * Si la preferencia es SYSTEM, se conserva la configuración del sistema operativo sin alterarla.
     */
    fun applyLocale(context: Context, language: AppLanguage = getPersistedLanguage(context)): Context {
        if (language == AppLanguage.SYSTEM) {
            return context
        }

        return runCatching {
            val targetLocale = when (language) {
                AppLanguage.SPANISH -> Locale.forLanguageTag("es")
                AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
                AppLanguage.SYSTEM -> return context
            }
            Locale.setDefault(targetLocale)

            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(targetLocale)
            context.createConfigurationContext(configuration)
        }.getOrDefault(context)
    }
}
