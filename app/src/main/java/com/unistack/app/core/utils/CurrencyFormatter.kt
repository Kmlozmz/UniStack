package com.unistack.app.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.feature_user.domain.CurrencyPreference
import java.util.Locale

object CurrencyFormatter {
    /**
     * Formatea un valor numérico según la divisa seleccionada, añadiendo el código ISO
     * (p. ej. "$ 50.000 COP", "$ 50 USD", "50 € EUR") para que la moneda seleccionada
     * sea explícita e inequívoca en toda la app.
     */
    fun format(
        value: Int,
        preference: CurrencyPreference = CurrencyPreference.COP,
        includeCode: Boolean = true
    ): String {
        val base = when (preference) {
            CurrencyPreference.COP -> {
                val formatted = value
                    .toString()
                    .reversed()
                    .chunked(3)
                    .joinToString(".")
                    .reversed()
                "$$formatted"
            }
            CurrencyPreference.USD, CurrencyPreference.MXN -> {
                val formatted = String.format(Locale.US, "%,d", value)
                "$$formatted"
            }
            CurrencyPreference.EUR -> {
                val formatted = String.format(Locale.GERMANY, "%,d", value)
                "$formatted €"
            }
            CurrencyPreference.ARS, CurrencyPreference.CLP -> {
                val formatted = String.format(Locale.GERMANY, "%,d", value)
                "$$formatted"
            }
            CurrencyPreference.PEN -> {
                val formatted = String.format(Locale.US, "%,d", value)
                "S/ $formatted"
            }
        }
        return if (includeCode) "$base ${preference.code}" else base
    }
}

/**
 * Obtiene y formatea el valor numérico con la divisa actualmente activa en Accesibilidad.
 */
@Composable
@ReadOnlyComposable
fun formatCurrency(value: Int, includeCode: Boolean = true): String {
    val currency = LocalAccessibilityPreferences.current.currency
    return CurrencyFormatter.format(value, currency, includeCode)
}
