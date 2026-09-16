package com.unistack.app.core.datastore

import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import org.json.JSONArray
import org.json.JSONObject

/**
 * El esquema de cortes en texto, igual en las preferencias que en el periodo cerrado.
 *
 * Vivía como dos funciones privadas de las preferencias. Al cerrar un periodo se guarda una copia
 * de su esquema, y tenía que escribirse con el mismo formato para que leerlo no dependa de dónde
 * se guardó.
 */
object GradingCutSchemeJson {

    fun encode(scheme: GradingCutScheme): String {
        val array = JSONArray()
        scheme.cuts.sortedBy { it.order }.forEach { cut ->
            array.put(
                JSONObject()
                    .put("id", cut.id)
                    .put("name", cut.name)
                    .put("weight", cut.weight)
                    .put("order", cut.order)
                    .put("endEpochDay", cut.endEpochDay)
            )
        }
        return JSONObject()
            .put("periods", array)
            .toString()
    }

    /** Nulo si no hay texto o no se puede leer un esquema válido. */
    fun decode(json: String?): GradingCutScheme? {
        if (json.isNullOrBlank()) return null
        return runCatching {
            val root = JSONObject(json)
            val array = root.optJSONArray("periods") ?: JSONArray()
            val cuts = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val order = item.optInt("order", index + 1)
                    val weight = item.optDouble("weight", 0.0)
                    if (weight > 0.0) {
                        add(
                            GradingCut(
                                id = item.optString("id", "period-$order"),
                                name = item.optString("name", "${Corte.Singular} $order"),
                                weight = weight,
                                order = order,
                                endEpochDay = if (item.has("endEpochDay") && !item.isNull("endEpochDay")) {
                                    item.optLong("endEpochDay")
                                } else {
                                    null
                                }
                            )
                        )
                    }
                }
            }.sortedBy { it.order }
            GradingCutScheme(cuts = cuts).takeIf { it.isValid }
        }.getOrNull()
    }
}
