package com.unistack.app.core.utils

object TextValidators {
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun normalizeText(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    fun isValidDisplayName(text: String): Boolean = validateDisplayName(text).isValid

    fun isValidAcademicName(text: String): Boolean = validateSubjectName(text).isValid

    fun validateDisplayName(text: String): ValidationResult {
        val value = normalizeText(text)
        if (value.isEmpty()) return error("Ingresa un nombre valido")
        if (value.length < 2) return error("El nombre parece demasiado corto")
        if (value.length > 30) return error("El nombre no puede superar 30 caracteres")
        if (!hasLetter(value)) return error("El nombre debe contener letras")
        if (value.any { it.isDigit() }) return error("No uses numeros en tu nombre")
        if (hasInvalidNameCharacters(value)) return error("Usa solo letras, espacios y guiones")
        if (isRepeatedNoise(value)) return error("Ingresa un nombre mas claro")
        return ok()
    }

    fun validateSubjectName(text: String): ValidationResult {
        val value = normalizeText(text)
        if (value.isEmpty()) return error("Ingresa un nombre de materia valido")
        if (value.length < 3) return error("El nombre de la materia parece demasiado corto")
        if (value.length > 40) return error("El nombre no puede superar 40 caracteres")
        if (!hasLetter(value)) return error("El nombre debe contener letras")
        if (hasInvalidAcademicCharacters(value)) return error("Evita caracteres innecesarios")
        if (isRepeatedNoise(value)) return error("Usa un nombre mas claro para la materia")
        if (countDigits(value) > 4) return error("Evita demasiados numeros en el nombre")
        if (countDigits(value) > 0 && !value.contains(" ")) return error("Acompana los numeros con palabras claras")
        if (letterRatio(value) < 0.55) return error("Usa un nombre mas claro para la materia")
        return ok()
    }

    fun validateActivityName(text: String): ValidationResult {
        val value = normalizeText(text)
        if (value.isEmpty()) return error("Ingresa un nombre de actividad valido")
        if (value.length < 3) return error("El nombre de la actividad parece demasiado corto")
        if (value.length > 40) return error("El nombre no puede superar 40 caracteres")
        if (!hasLetter(value)) return error("El nombre debe contener letras")
        if (hasInvalidAcademicCharacters(value)) return error("Evita caracteres innecesarios")
        if (isRepeatedNoise(value)) return error("Usa un nombre mas claro")
        if (countDigits(value) > 5) return error("Evita demasiados numeros")
        if (letterRatio(value) < 0.45) return error("Usa un nombre mas claro")
        return ok()
    }

    fun validateCustomCareer(text: String): ValidationResult {
        val value = normalizeText(text)
        if (value.isEmpty()) return error("Ingresa un programa valido")
        if (value.length < 3) return error("El programa parece demasiado corto")
        if (value.length > 60) return error("El programa no puede superar 60 caracteres")
        if (!hasLetter(value)) return error("El programa debe contener letras")
        if (hasInvalidAcademicCharacters(value)) return error("Evita caracteres innecesarios")
        if (isRepeatedNoise(value)) return error("Usa un nombre de programa mas claro")
        if (countDigits(value) > 4) return error("Evita demasiados numeros")
        if (letterRatio(value) < 0.55) return error("Usa un nombre de programa mas claro")
        return ok()
    }

    private fun hasLetter(value: String): Boolean = value.any { it.isLetter() }

    private fun countDigits(value: String): Int = value.count { it.isDigit() }

    private fun letterRatio(value: String): Double {
        val compact = value.filterNot { it.isWhitespace() }
        if (compact.isEmpty()) return 0.0
        return compact.count { it.isLetter() }.toDouble() / compact.length.toDouble()
    }

    private fun isRepeatedNoise(value: String): Boolean {
        val compact = value.filterNot { it.isWhitespace() }
        return compact.length >= 4 && compact.all { it == compact.first() }
    }

    private fun hasInvalidNameCharacters(value: String): Boolean {
        return value.any { char ->
            !(char.isLetter() || char.isWhitespace() || char == '-' || char == '.')
        }
    }

    private fun hasInvalidAcademicCharacters(value: String): Boolean {
        return value.any { char ->
            !(char.isLetterOrDigit() || char.isWhitespace() || char == '-' || char == '.' || char == ',' || char == '&' || char == '/' || char == '(' || char == ')')
        }
    }

    private fun ok(): ValidationResult = ValidationResult(isValid = true)

    private fun error(message: String): ValidationResult = ValidationResult(isValid = false, errorMessage = message)
}
