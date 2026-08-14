package com.unistack.app.core.utils

/**
 * En qué peldaño de la escalera está esta compilación.
 *
 * Sale del nombre de versión, que ya lo dice: `0.0.0-dev.x`, `1.0.0-alpha.5`, `1.0.0-beta.1`,
 * `1.0.0`. No hace falta un interruptor aparte que alguien tenga que acordarse de mover.
 */
enum class BuildStage {
    DEV,
    ALPHA,
    BETA,
    RC,
    STABLE;

    /**
     * Si esta compilación puede abrir lo que está a medio hacer.
     *
     * Dev y alpha son las que se usan para probar, y ahí conviene llegar a todo. Una beta va a
     * gente que la usa de verdad para su semestre: enseñarle una pantalla incompleta gasta la
     * confianza que hace falta para que reporte lo que sí importa.
     */
    val allowsUnfinished: Boolean get() = this == DEV || this == ALPHA

    companion object {
        fun of(versionName: String): BuildStage {
            val suffix = versionName.substringAfter('-', "").lowercase()
            return when {
                suffix.startsWith("dev") -> DEV
                suffix.startsWith("alpha") -> ALPHA
                suffix.startsWith("beta") -> BETA
                suffix.startsWith("rc") -> RC
                else -> STABLE
            }
        }
    }
}
