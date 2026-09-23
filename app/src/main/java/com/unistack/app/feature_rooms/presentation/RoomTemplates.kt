package com.unistack.app.feature_rooms.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_rooms.domain.RoomProduce
import com.unistack.app.feature_rooms.domain.RoomType

/**
 * Lo que trae cada tipo: sólo un punto de partida. La app sugiere; quien crea la sala decide.
 * Los nombres son de recursos (se traducen) y se copian a la sala al crearla.
 */
object RoomTemplates {

    val order = listOf(RoomType.ENSAYO, RoomType.LAB, RoomType.EXPO, RoomType.PROYECTO, RoomType.INVESTIGACION,
        RoomType.RESENA, RoomType.CAMPO, RoomType.MAQUETA, RoomType.CERO)

    fun icon(t: RoomType): ImageVector = when (t) {
        RoomType.ENSAYO -> Icons.Rounded.Edit
        RoomType.LAB -> Icons.Rounded.Science
        RoomType.EXPO -> Icons.Rounded.Slideshow
        RoomType.PROYECTO -> Icons.Rounded.Build
        RoomType.INVESTIGACION -> Icons.Rounded.Search
        RoomType.RESENA -> Icons.Rounded.MenuBook
        RoomType.CAMPO -> Icons.Rounded.Poll
        RoomType.MAQUETA -> Icons.Rounded.ViewInAr
        RoomType.CERO -> Icons.Rounded.NoteAdd
    }

    private fun nameRes(t: RoomType) = when (t) {
        RoomType.ENSAYO -> R.string.rooms_type_ensayo
        RoomType.LAB -> R.string.rooms_type_lab
        RoomType.EXPO -> R.string.rooms_type_expo
        RoomType.PROYECTO -> R.string.rooms_type_proyecto
        RoomType.INVESTIGACION -> R.string.rooms_type_inv
        RoomType.RESENA -> R.string.rooms_type_resena
        RoomType.CAMPO -> R.string.rooms_type_campo
        RoomType.MAQUETA -> R.string.rooms_type_maqueta
        RoomType.CERO -> R.string.rooms_type_cero
    }

    fun typeName(t: RoomType): String = Textos.get(nameRes(t))
    fun typeNameRes(t: RoomType): Int = nameRes(t)

    fun produces(t: RoomType): RoomProduce = when (t) {
        RoomType.EXPO -> RoomProduce.SLIDES
        RoomType.PROYECTO, RoomType.MAQUETA -> RoomProduce.NOTHING
        RoomType.CAMPO -> RoomProduce.BOTH
        else -> RoomProduce.DOC
    }

    private val extents = mapOf(
        RoomType.ENSAYO to listOf(1, 2, 2, 2, 1, 0),
        RoomType.LAB to listOf(1, 2, 1, 1, 2, 2, 1, 0),
        RoomType.EXPO to listOf(2, 3, 3, 3, 1),
        RoomType.PROYECTO to listOf(0, 0, 0, 0, 0, 0),
        RoomType.INVESTIGACION to listOf(2, 3, 2, 3, 2, 1, 0),
        RoomType.RESENA to listOf(0, 1, 2, 1),
        RoomType.CAMPO to listOf(1, 0, 2, 2, 3),
        RoomType.MAQUETA to listOf(0, 0, 0, 0),
        RoomType.CERO to emptyList()
    )

    private fun partsRes(t: RoomType) = when (t) {
        RoomType.ENSAYO -> R.array.rooms_tpl_ensayo
        RoomType.LAB -> R.array.rooms_tpl_lab
        RoomType.EXPO -> R.array.rooms_tpl_expo
        RoomType.PROYECTO -> R.array.rooms_tpl_proyecto
        RoomType.INVESTIGACION -> R.array.rooms_tpl_inv
        RoomType.RESENA -> R.array.rooms_tpl_resena
        RoomType.CAMPO -> R.array.rooms_tpl_campo
        RoomType.MAQUETA -> R.array.rooms_tpl_maqueta
        RoomType.CERO -> null
    }

    private fun sugRes(t: RoomType) = when (t) {
        RoomType.ENSAYO -> R.array.rooms_sug_ensayo
        RoomType.LAB -> R.array.rooms_sug_lab
        RoomType.EXPO -> R.array.rooms_sug_expo
        RoomType.PROYECTO -> R.array.rooms_sug_proyecto
        RoomType.INVESTIGACION -> R.array.rooms_sug_inv
        RoomType.RESENA -> R.array.rooms_sug_resena
        RoomType.CAMPO -> R.array.rooms_sug_campo
        RoomType.MAQUETA -> R.array.rooms_sug_maqueta
        RoomType.CERO -> R.array.rooms_sug_cero
    }

    fun parts(t: RoomType): List<Pair<String, Int>> {
        val res = partsRes(t) ?: return emptyList()
        val ex = extents[t].orEmpty()
        return Textos.lista(res).mapIndexed { i, n -> n to ex.getOrElse(i) { 0 } }
    }

    fun suggestions(t: RoomType): List<String> = Textos.lista(sugRes(t))

    fun tasks(t: RoomType): List<String> = when (t) {
        RoomType.EXPO -> listOf(Textos.get(R.string.rooms_task_rehearse))
        RoomType.MAQUETA -> listOf(Textos.get(R.string.rooms_task_bring_model))
        else -> emptyList()
    }
}
