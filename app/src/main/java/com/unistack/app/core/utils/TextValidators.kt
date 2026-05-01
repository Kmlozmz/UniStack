package com.unistack.app.core.utils

object TextValidators {
    private val letterRegex = Regex(".*\\p{L}.*")

    fun normalizeText(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    fun isValidDisplayName(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized.length in 2..30 && letterRegex.matches(normalized)
    }

    fun isValidAcademicName(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized.length in 3..40 && letterRegex.matches(normalized)
    }
}
