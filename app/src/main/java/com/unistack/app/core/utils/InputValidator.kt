package com.unistack.app.core.utils

object InputValidator {
    private val letterRegex = Regex("\\p{L}")
    private val numberOnlyRegex = Regex("^\\d+$")

    fun isHumanName(value: String): Boolean {
        return isTextLike(value = value, minLength = 2, maxLength = 30)
    }

    fun isSubjectName(value: String): Boolean {
        return isTextLike(value = value, minLength = 3, maxLength = 40)
    }

    fun isActivityName(value: String): Boolean {
        return isTextLike(value = value, minLength = 3, maxLength = 50)
    }

    fun isAcademicCustomValue(value: String): Boolean {
        return isTextLike(value = value, minLength = 3, maxLength = 60)
    }

    fun normalized(value: String): String = value.trim().replace(Regex("\\s+"), " ")

    private fun isTextLike(value: String, minLength: Int, maxLength: Int): Boolean {
        val normalized = normalized(value)
        if (normalized.length !in minLength..maxLength) return false
        if (!letterRegex.containsMatchIn(normalized)) return false
        if (numberOnlyRegex.matches(normalized)) return false
        return true
    }
}
