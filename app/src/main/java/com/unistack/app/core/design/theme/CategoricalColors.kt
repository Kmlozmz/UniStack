// design-tokens-exempt: paletas categóricas de identidad.
//
// Los colores de este archivo NO son tokens de marca: son opciones que el usuario elige
// para distinguir sus materias, igual que los colores de una gráfica. Por eso se definen
// como literales y no derivan del acento del tema — si siguieran al acento, todas las
// materias se verían iguales y perderían su función, que es diferenciarse entre sí.
//
// Los seis primeros tipos sí reutilizan los colores semánticos del tema; el resto amplía
// la paleta más allá de lo que el tema cubre.
package com.unistack.app.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Colores de acento para los tipos visuales de materia que el tema no cubre.
 * Se emparejan con [CategoricalSubjectBackgrounds] por el mismo índice conceptual.
 */
internal object CategoricalSubjectAccents {
    val Rose = Color(0xFFE84A8A)
    val Indigo = Color(0xFF4D5BD7)
    val Orange = Color(0xFFF57C00)
    val Cyan = Color(0xFF00A6D6)
    val Lime = Color(0xFF7CB342)
    val Slate = Color(0xFF607D8B)
}

/** Fondos suaves emparejados con [CategoricalSubjectAccents], por tema claro y oscuro. */
internal object CategoricalSubjectBackgrounds {
    fun rose(dark: Boolean) = if (dark) Color(0xFF3B1F2D) else Color(0xFFFFE4EF)
    fun indigo(dark: Boolean) = if (dark) Color(0xFF20274A) else Color(0xFFE5E8FF)
    fun orange(dark: Boolean) = if (dark) Color(0xFF3D2817) else Color(0xFFFFE8D3)
    fun cyan(dark: Boolean) = if (dark) Color(0xFF123444) else Color(0xFFDDF7FF)
    fun lime(dark: Boolean) = if (dark) Color(0xFF243719) else Color(0xFFEAF7D7)
    fun slate(dark: Boolean) = if (dark) Color(0xFF25313A) else Color(0xFFE8EEF2)
}

/**
 * Paleta que se ofrece al usuario al crear o editar una materia.
 * Orden pensado para que colores contiguos se distingan bien entre sí.
 */
val SubjectColorPalette: List<Color> = listOf(
    Color(0xFF10B8AC),
    Color(0xFF58A6FF),
    Color(0xFFFF6B7A),
    Color(0xFF8B5CF6),
    Color(0xFF22C55E),
    Color(0xFFF5C542),
    Color(0xFFE84A8A),
    Color(0xFF6366F1),
    Color(0xFFFF9F43),
    Color(0xFF06B6D4),
    Color(0xFF84CC16),
    Color(0xFF94A3B8),
    Color(0xFFFF4D4D),
    Color(0xFFFF7A1A),
    Color(0xFF00D084),
    Color(0xFF14B8A6),
    Color(0xFF2DD4BF),
    Color(0xFF38BDF8),
    Color(0xFF3B82F6),
    Color(0xFF7C3AED),
    Color(0xFFA855F7),
    Color(0xFFD946EF),
    Color(0xFFF472B6),
    Color(0xFF64748B)
)
