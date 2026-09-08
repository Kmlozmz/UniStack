package com.unistack.app.core.utils

import com.unistack.app.feature_user.domain.CurrencyPreference
import java.util.Locale

object CurrencyFormatter {
    fun formatCop(value: Int): String {
        val formatted = value
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "$$formatted"
    }

    fun format(value: Int, preference: CurrencyPreference = CurrencyPreference.COP): String {
        return when (preference) {
            CurrencyPreference.COP -> formatCop(value)
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
    }
}
