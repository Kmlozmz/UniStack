@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp

/**
 * Cómo son los botones de esta app, en un solo sitio.
 *
 * Material trae cinco tamaños de botón y dos familias de forma —redonda y cuadrada—, y por
 * defecto entrega el pequeño y redondo. Para las acciones principales de la app eso se queda
 * corto: son botones anclados al pie de la pantalla, con una sola acción, y pedían el tamaño
 * «medium» y la forma cuadrada. Que resulta ser, medida, exactamente la que tenían antes de
 * migrar a Material: 56dp de alto y 28dp de esquina.
 */
object UniStackButtonDefaults {

    /** 56dp: el tamaño «medium» de Material, el de una acción principal anclada. */
    val PrimaryHeight: Dp
        @Composable
        @ReadOnlyComposable
        get() = ButtonDefaults.MediumContainerHeight

    /**
     * Cuadrado en reposo, esquinas cerradas bajo el dedo.
     *
     * Ese cambio de forma al pulsar es el gesto de Material 3 Expressive, y lo anima el propio
     * componente con el muelle del tema: no hay ninguna animación escrita a mano detrás.
     *
     * Material aprieta las esquinas al pulsar en lugar de redondearlas. Para el gesto contrario
     * basta con intercambiar los dos valores de aquí.
     */
    val shapes: ButtonShapes
        @Composable
        get() = ButtonDefaults.shapes(
            shape = ButtonDefaults.squareShape,
            pressedShape = ButtonDefaults.pressedShape
        )
}
