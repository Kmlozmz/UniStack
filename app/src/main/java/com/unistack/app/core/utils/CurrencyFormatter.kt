package com.unistack.app.core.utils

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
}
