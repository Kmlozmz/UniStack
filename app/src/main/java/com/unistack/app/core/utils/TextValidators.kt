package com.unistack.app.core.utils

import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * El resultado de validar un texto. El mensaje es un recurso y se resuelve **al leerlo**, en el
 * idioma de la app: asi el validador sigue siendo puro y las pruebas no necesitan recursos.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorRes: Int? = null,
    val errorArgs: List<Any> = emptyList()
) {
    val errorMessage: String?
        get() = errorRes?.let { Textos.get(it, *errorArgs.toTypedArray()) }
}

object TextValidators {
    private val letterRegex = Regex(".*\\p{L}.*")
    private val repetitionRegex = Regex("(.)\\1{3,}") // 4 or more repeated characters

    fun normalizeText(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    private fun commonValidation(
        text: String,
        minLength: Int,
        maxLength: Int,
        emptyError: Int,
        shortError: Int,
        longError: Int,
        repetitiveError: Int,
        noLetterError: Int
    ): ValidationResult {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return ValidationResult(false, emptyError)
        }
        if (normalized.length < minLength) {
            return ValidationResult(false, shortError, listOf(minLength))
        }
        if (normalized.length > maxLength) {
            return ValidationResult(false, longError, listOf(maxLength))
        }
        if (repetitionRegex.containsMatchIn(normalized)) {
            return ValidationResult(false, repetitiveError)
        }
        if (!letterRegex.matches(normalized)) {
            return ValidationResult(false, noLetterError)
        }
        return ValidationResult(true)
    }

    fun validateDisplayName(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 2,
            maxLength = 30,
            emptyError = R.string.validate_empty,
            shortError = R.string.validate_min_chars,
            longError = R.string.validate_max_chars,
            repetitiveError = R.string.validate_repetitions,
            noLetterError = R.string.validate_needs_letter
        )
    }

    fun validateSubjectName(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 3,
            maxLength = 40,
            emptyError = R.string.validate_subject_empty,
            shortError = R.string.validate_name_short,
            longError = R.string.validate_name_long,
            repetitiveError = R.string.validate_repetitions,
            noLetterError = R.string.validate_needs_letters
        )
    }

    fun validateActivityName(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 3,
            maxLength = 40,
            emptyError = R.string.validate_activity_empty,
            shortError = R.string.validate_name_short,
            longError = R.string.validate_name_long,
            repetitiveError = R.string.validate_repetitions,
            noLetterError = R.string.validate_needs_letter
        )
    }

    fun validateCustomCareer(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 4,
            maxLength = 60,
            emptyError = R.string.validate_career_empty,
            shortError = R.string.validate_name_short,
            longError = R.string.validate_name_long,
            repetitiveError = R.string.validate_repetitions,
            noLetterError = R.string.validate_needs_letters
        )
    }
}
