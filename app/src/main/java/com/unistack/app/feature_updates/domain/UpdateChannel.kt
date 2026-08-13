package com.unistack.app.feature_updates.domain

/**
 * Hasta qué punto de la escalera acepta actualizaciones quien usa la app.
 *
 * Sin esto, el actualizador ofrecía la publicación más reciente fuera cual fuera: alguien con
 * la versión estable instalada recibía la siguiente alpha como si fuera una actualización
 * normal. El canal es un suelo de estabilidad, no un filtro exclusivo: quien está en `ALPHA`
 * también recibe las betas y las definitivas, porque una definitiva posterior siempre es la
 * versión buena de lo que estaba probando.
 *
 * **No es un candado.** Los APK están en un repositorio público y cualquiera puede descargar el
 * que quiera a mano. Esto decide qué te *ofrece* la app, que es de lo que se trata: nadie
 * debería acabar en una alpha sin haberlo pedido.
 */
enum class UpdateChannel(
    val label: String,
    val description: String
) {
    /** Solo versiones definitivas. Lo que recibe todo el mundo por defecto. */
    STABLE(
        label = "Estable",
        description = "Solo versiones terminadas. Es lo recomendable si usas la app en serio."
    ),

    /** Definitivas, candidatas y betas: probadas, pero todavía sin cerrar. */
    BETA(
        label = "Beta",
        description = "Versiones casi listas, para ayudar a probarlas antes de que salgan."
    ),

    /** Todo, incluidas las alphas, que pueden traer cosas a medias. */
    ALPHA(
        label = "Alpha",
        description = "Lo más nuevo en cuanto existe. Puede fallar y perder datos."
    );

    /** Si una versión con este nombre debe ofrecerse en este canal. */
    fun accepts(versionName: String): Boolean {
        val stage = Stage.of(versionName)
        return stage.ordinal >= minimumStage.ordinal
    }

    private val minimumStage: Stage
        get() = when (this) {
            STABLE -> Stage.FINAL
            BETA -> Stage.BETA
            ALPHA -> Stage.ALPHA
        }

    /**
     * El peldaño al que pertenece un nombre de versión, ordenado de menos a más estable.
     *
     * Las candidatas (`rc`) van con las betas: quien acepta betas quiere probar lo que está a
     * punto de salir, y una `rc` es exactamente eso. Un sufijo que no reconocemos —`dev` o
     * cualquier invento— se trata como lo más inestable, para no colarlo en un canal tranquilo.
     */
    private enum class Stage {
        ALPHA,
        BETA,
        FINAL;

        companion object {
            fun of(versionName: String): Stage {
                val suffix = versionName
                    .substringAfter('-', missingDelimiterValue = "")
                    .lowercase()
                return when {
                    suffix.isBlank() -> FINAL
                    suffix.startsWith("beta") || suffix.startsWith("rc") -> BETA
                    else -> ALPHA
                }
            }
        }
    }
}
