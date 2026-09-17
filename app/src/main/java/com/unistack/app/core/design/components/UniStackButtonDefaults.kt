@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.feature_user.domain.ButtonShapeStyle
import com.unistack.app.feature_user.domain.ButtonSizeStyle

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

    /**
     * El alto de una accion principal anclada, con el tamano que se haya elegido.
     *
     * Estuvo fijo en los 56dp del «medium» de Material, que sigue siendo el de por defecto. El
     * ajuste de tamano de boton existia en el diseno y no llegaba a ningun sitio.
     */
    val PrimaryHeight: Dp
        @Composable
        @ReadOnlyComposable
        get() = when (LocalAppearancePreferences.current.buttonSize) {
            ButtonSizeStyle.PEQUENO -> ButtonDefaults.MinHeight
            ButtonSizeStyle.MEDIO -> ButtonDefaults.MediumContainerHeight
            ButtonSizeStyle.GRANDE -> ButtonDefaults.LargeContainerHeight
        }

    /**
     * Redondo en reposo, esquinas cerradas bajo el dedo.
     *
     * Ese cambio de forma al pulsar es el gesto de Material 3 Expressive, y lo anima el propio
     * componente con el muelle del tema: no hay ninguna animación escrita a mano detrás.
     *
     * La pastilla, que es la de por defecto, se cierra hasta la forma cuadrada: es la que los
     * botones tenían en reposo hasta el 16 sep 2026, y se pidió conservarla justo para cuando
     * se mantienen pulsados. Las otras dos se cierran hasta la de pulsado de Material.
     */
    val shapes: ButtonShapes
        @Composable
        get() = when (LocalAppearancePreferences.current.buttonShape) {
            ButtonShapeStyle.RECTO -> ButtonDefaults.shapes(
                shape = RoundedCornerShape(6.dp),
                pressedShape = ButtonDefaults.pressedShape
            )
            ButtonShapeStyle.MEDIO -> ButtonDefaults.shapes(
                shape = ButtonDefaults.squareShape,
                pressedShape = ButtonDefaults.pressedShape
            )
            // Pastilla: la mitad del alto, que es lo que la deja siempre redonda del todo sea
            // cual sea el tamano elegido.
            ButtonShapeStyle.PASTILLA -> ButtonDefaults.shapes(
                shape = RoundedCornerShape(percent = 50),
                pressedShape = ButtonDefaults.squareShape
            )
        }
}
