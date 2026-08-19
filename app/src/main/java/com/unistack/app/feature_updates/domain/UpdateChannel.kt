package com.unistack.app.feature_updates.domain

/**
 * Qué versiones recibe quien usa la app.
 *
 * **Los tres canales son públicos distintos, no escalones de una escalera.** Alpha y beta se
 * prueban con gente distinta y por motivos distintos: la alpha para ver si algo funciona, la
 * beta para ver si algo aguanta el uso real. Tenerlos como escalera hacía que un solo código
 * abriera los dos, que no es lo que se quiere de un permiso.
 *
 * Lo único que comparten es la versión definitiva: **todos los canales la reciben**. Quien está
 * probando una alpha tiene que poder pasar a la versión buena cuando salga, o se queda anclado
 * en un preestreno viejo para siempre.
 *
 * Esto decide qué ofrece la app. No impide instalar nada: los APK están publicados y quien tenga
 * el enlace descarga el que quiera.
 */
enum class UpdateChannel(
    val label: String,
    val description: String
) {
    /** Solo versiones definitivas. Lo que recibe todo el mundo, sin código. */
    STABLE(
        label = "Estable",
        description = "Solo versiones terminadas. Es lo recomendable si usas la app en serio."
    ),

    /** Betas y definitivas. Para quien ayuda a probar antes de publicar. */
    BETA(
        label = "Beta",
        description = "Versiones casi listas, para ayudar a probarlas antes de que salgan."
    ),

    /** Alphas y definitivas. Para quien prueba lo que se acaba de escribir. */
    ALPHA(
        label = "Alpha",
        description = "Lo más nuevo en cuanto existe. Puede fallar y perder datos."
    );

    /** Si una versión con este nombre debe ofrecerse en este canal. */
    fun accepts(versionName: String): Boolean {
        val stage = Stage.of(versionName)
        // La definitiva llega a todos: es la salida de cualquier preestreno.
        if (stage == Stage.FINAL) return true
        return when (this) {
            STABLE -> false
            BETA -> stage == Stage.BETA
            ALPHA -> stage == Stage.ALPHA
        }
    }

    /**
     * El peldaño al que pertenece un nombre de versión.
     *
     * Las candidatas (`rc`) cuentan como beta: son lo que está a punto de salir, que es
     * exactamente lo que quien prueba betas quiere en las manos. Un sufijo que no reconocemos se
     * trata como alpha, el círculo más pequeño, para no colarlo donde hay más gente.
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

    companion object {
        /**
         * El canal que abre la propia versión instalada, o null si es una definitiva.
         *
         * El código es para **entrar** a un canal, y quien tiene una beta puesta ya está
         * dentro: pedírselo para seguir donde está no protege nada y lo deja sin recibir
         * actualizaciones de lo que lleva instalado, que es el único sitio del que no puede
         * salir sin desinstalar.
         *
         * Una definitiva no abre nada: es lo que recibe todo el mundo por defecto.
         */
        fun ofInstalled(versionName: String): UpdateChannel? = when (Stage.of(versionName)) {
            Stage.FINAL -> null
            Stage.BETA -> BETA
            Stage.ALPHA -> ALPHA
        }
    }
}
