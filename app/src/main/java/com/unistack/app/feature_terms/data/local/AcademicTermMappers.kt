package com.unistack.app.feature_terms.data.local

import com.unistack.app.core.datastore.GradingCutSchemeJson
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType

/**
 * Un enum que ya no existe no puede tumbar la lectura.
 *
 * Se guarda el nombre en texto, así que un valor escrito por una versión posterior —o por una
 * restauración de respaldo— puede no significar nada aquí. Se cae al valor de reserva en vez
 * de lanzar: perder el tipo de periodo es una molestia, perder el periodo entero no.
 */
private inline fun <reified T : Enum<T>> String.toEnumOr(fallback: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(fallback)

fun AcademicTermEntity.toDomain(): AcademicTerm = AcademicTerm(
    id = id,
    userId = userId,
    name = name,
    type = type.toEnumOr(AcademicTermType.SEMESTER),
    startEpochDay = startEpochDay,
    plannedEndEpochDay = plannedEndEpochDay,
    closedEpochDay = closedEpochDay,
    /*
     * El estado se deriva de la fecha de cierre, no se lee tal cual.
     *
     * Son el mismo hecho contado dos veces, y si se contradicen manda la fecha: un periodo con
     * fecha de cierre está cerrado aunque la columna diga otra cosa, y uno sin ella sigue
     * activo. Así una escritura a medias no deja un periodo cerrado que se pueda reactivar.
     */
    status = if (closedEpochDay != null) AcademicTermStatus.CLOSED else AcademicTermStatus.ACTIVE,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cutScheme = GradingCutSchemeJson.decode(cutSchemeJson)
)

fun AcademicTerm.toEntity(): AcademicTermEntity = AcademicTermEntity(
    id = id,
    userId = userId,
    name = name,
    type = type.name,
    startEpochDay = startEpochDay,
    plannedEndEpochDay = plannedEndEpochDay,
    closedEpochDay = closedEpochDay,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cutSchemeJson = cutScheme?.let(GradingCutSchemeJson::encode)
)
