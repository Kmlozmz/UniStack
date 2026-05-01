package com.unistack.app.core.utils

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

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
        emptyError: String,
        shortError: String,
        longError: String,
        repetitiveError: String,
        noLetterError: String
    ): ValidationResult {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return ValidationResult(false, emptyError)
        }
        if (normalized.length < minLength) {
            return ValidationResult(false, shortError)
        }
        if (normalized.length > maxLength) {
            return ValidationResult(false, longError)
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
            emptyError = "No puede estar vacío",
            shortError = "Debe tener al menos 2 caracteres",
            longError = "No puede exceder 30 caracteres",
            repetitiveError = "Contiene repeticiones inválidas",
            noLetterError = "Debe contener al menos una letra"
        )
    }

    fun validateSubjectName(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 3,
            maxLength = 40,
            emptyError = "Ingresa el nombre de la materia",
            shortError = "El nombre es muy corto (mínimo 3 caracteres)",
            longError = "El nombre es muy largo (máximo 40 caracteres)",
            repetitiveError = "Contiene repeticiones inválidas",
            noLetterError = "Debe contener letras (no solo números/símbolos)"
        )
    }

    fun validateActivityName(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 3,
            maxLength = 40,
            emptyError = "Ingresa el nombre de la actividad",
            shortError = "El nombre es muy corto (mínimo 3 caracteres)",
            longError = "El nombre es muy largo (máximo 40 caracteres)",
            repetitiveError = "Contiene repeticiones inválidas",
            noLetterError = "Debe contener al menos una letra"
        )
    }

    fun validateCustomCareer(text: String): ValidationResult {
        return commonValidation(
            text = text,
            minLength = 4,
            maxLength = 60,
            emptyError = "Ingresa el nombre de la carrera o programa",
            shortError = "El nombre es muy corto (mínimo 4 caracteres)",
            longError = "El nombre es muy largo (máximo 60 caracteres)",
            repetitiveError = "Contiene repeticiones inválidas",
            noLetterError = "Debe contener letras (no solo números/símbolos)"
        )
    }
}
