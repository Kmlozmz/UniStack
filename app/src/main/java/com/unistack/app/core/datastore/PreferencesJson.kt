package com.unistack.app.core.datastore

import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.SavedGradeScenario
import org.json.JSONArray
import org.json.JSONObject

/*
 * Las preferencias en JSON, las mismas en el teléfono y en la copia de seguridad.
 *
 * La copia tenía su propia versión escrita a mano y se había quedado atrás: no llevaba el tema,
 * ni el movimiento, ni las opciones de componentes que llegaron después. Cada ajuste nuevo había
 * que añadirlo en dos sitios, y el segundo se olvidaba siempre. Ahora los dos pasan por aquí.
 *
 * Al leer, lo que falta sale de `base`. En el teléfono son los valores por defecto; al restaurar
 * es lo que ya está puesto, para que una copia antigua no apague lo que no conocía.
 */

object AppearancePreferencesJson {

    fun encode(value: AppearancePreferences): JSONObject = JSONObject()
        .put("themeId", value.themeId)
        .put("backgroundStyle", value.backgroundStyle.name)
        .put("customBackgroundColor", value.customBackgroundColor)
        .put("customThemeBase", value.customThemeBase.name)
        .put("accentStyle", value.accentStyle.name)
        .put("customAccentColor", value.customAccentColor)
        .put("accentIntensity", value.accentIntensity.name)
        .put("surfaceStyle", value.surfaceStyle.name)
        .put("shadowIntensity", value.shadowIntensity.name)
        .put("outlineWeight", value.outlineWeight.name)
        .put("cornerStyle", value.cornerStyle.name)
        .put("interfaceDensity", value.interfaceDensity.name)
        .put("motionPreference", value.motionPreference.name)
        .put("motion", encodeMotion(value.motion))
        .put("textScale", value.textScale.name)
        .put("typographyStyle", value.typographyStyle.name)
        .put("textScalePercent", value.textScalePercent)
        .put("lineHeightStyle", value.lineHeightStyle.name)
        .put("decimalPlaces", value.decimalPlaces)
        .put("bottomBarStyle", value.bottomBarStyle.name)
        .put("buttonShapeV2", value.buttonShape.name)
        .put("buttonSize", value.buttonSize.name)
        .put("textFieldStyle", value.textFieldStyle.name)
        .put("chipStyle", value.chipStyle.name)
        .put("iconStyle", value.iconStyle.name)
        .put("settingsIconStyle", value.settingsIconStyle.name)
        .put("badgeShape", value.badgeShape.name)
        .put("listDividers", value.listDividers)
        .put("firstDayOfWeek", value.firstDayOfWeek.name)
        .put("sectionColorsEnabled", value.sectionColorsEnabled)
        .put("academicIndicatorStyle", value.academicIndicatorStyle.name)
        .put("switchIconStyle", value.switchIconStyle.name)
        .put("subjectOrder", JSONArray(value.subjectOrder))
        .put("academicProgressShape", value.academicProgressShape.name)
        .put("showHomeGreeting", value.showHomeGreeting)
        .put("showHomeHero", value.showHomeHero)
        .put("showHomeAgenda", value.showHomeAgenda)
        .put("showHomeSnapshot", value.showHomeSnapshot)
        .put("homeSectionOrder", JSONArray(value.homeSectionOrder.map { it.name }))
        .put("heroAutoRotate", value.heroAutoRotate)
        .put("heroShowsGrades", value.heroShowsGrades)
        .put("heroShowsTasks", value.heroShowsTasks)
        .put("heroShowsExpenses", value.heroShowsExpenses)
        .put("initialTab", value.initialTab.name)
        .put("visualPreset", value.visualPreset.name)

    fun decode(json: JSONObject, base: AppearancePreferences): AppearancePreferences = AppearancePreferences(
        themeId = json.optString("themeId").ifBlank { base.themeId },
        backgroundStyle = json.enumOr("backgroundStyle", base.backgroundStyle),
        customBackgroundColor = json.intOrNull("customBackgroundColor"),
        customThemeBase = json.enumOr("customThemeBase", base.customThemeBase),
        accentStyle = json.enumOr("accentStyle", base.accentStyle),
        customAccentColor = json.intOrNull("customAccentColor"),
        accentIntensity = json.enumOr("accentIntensity", base.accentIntensity),
        surfaceStyle = json.enumOr("surfaceStyle", base.surfaceStyle),
        shadowIntensity = json.enumOr("shadowIntensity", base.shadowIntensity),
        outlineWeight = json.enumOr("outlineWeight", base.outlineWeight),
        cornerStyle = json.enumOr("cornerStyle", base.cornerStyle),
        interfaceDensity = json.enumOr("interfaceDensity", base.interfaceDensity),
        motionPreference = json.enumOr("motionPreference", base.motionPreference),
        motion = decodeMotion(json.optJSONObject("motion"), base.motion),
        textScale = json.enumOr("textScale", base.textScale),
        typographyStyle = json.enumOr("typographyStyle", base.typographyStyle),
        textScalePercent = json.optInt("textScalePercent", base.textScalePercent),
        lineHeightStyle = json.enumOr("lineHeightStyle", base.lineHeightStyle),
        decimalPlaces = json.optInt("decimalPlaces", base.decimalPlaces),
        bottomBarStyle = json.enumOr("bottomBarStyle", base.bottomBarStyle),
        // Clave nueva a propósito: «Medios» era el de por defecto y quedó guardado en todos
        // los teléfonos sin que nadie lo eligiera. Con otra clave, todos pasan a la pastilla
        // de ahora y quien quiera los de antes los vuelve a elegir.
        buttonShape = json.enumOr("buttonShapeV2", base.buttonShape),
        buttonSize = json.enumOr("buttonSize", base.buttonSize),
        textFieldStyle = json.enumOr("textFieldStyle", base.textFieldStyle),
        chipStyle = json.enumOr("chipStyle", base.chipStyle),
        iconStyle = json.enumOr("iconStyle", base.iconStyle),
        settingsIconStyle = json.enumOr("settingsIconStyle", base.settingsIconStyle),
        badgeShape = json.enumOr("badgeShape", base.badgeShape),
        listDividers = json.optBoolean("listDividers", base.listDividers),
        firstDayOfWeek = json.enumOr("firstDayOfWeek", base.firstDayOfWeek),
        sectionColorsEnabled = json.optBoolean("sectionColorsEnabled", base.sectionColorsEnabled),
        // Clave nueva a propósito: la anterior guardaba «FADE» en los teléfonos que
        // pasaron por las alphas, y ese valor —que entonces era el de por defecto, no una
        // elección— se quedaba pisando el empuje. Con otra clave, todos empiezan por el
        // valor de hoy y quien quiera el fundido lo vuelve a elegir.
        academicIndicatorStyle = json.enumOr("academicIndicatorStyle", base.academicIndicatorStyle),
        switchIconStyle = json.enumOr("switchIconStyle", base.switchIconStyle),
        subjectOrder = json.optJSONArray("subjectOrder")
            ?.let { array -> (0 until array.length()).map(array::optString) }
            ?.filter { it.isNotBlank() }
            ?: base.subjectOrder,
        academicProgressShape = json.enumOr("academicProgressShape", base.academicProgressShape),
        showHomeGreeting = json.optBoolean("showHomeGreeting", base.showHomeGreeting),
        showHomeHero = json.optBoolean("showHomeHero", base.showHomeHero),
        showHomeAgenda = json.optBoolean("showHomeAgenda", base.showHomeAgenda),
        showHomeSnapshot = json.optBoolean("showHomeSnapshot", base.showHomeSnapshot),
        homeSectionOrder = json.optJSONArray("homeSectionOrder")
            ?.let { array ->
                (0 until array.length()).mapNotNull { index ->
                    HomeSection.entries.firstOrNull { it.name == array.optString(index) }
                }
            }
            ?.ifEmpty { base.homeSectionOrder }
            ?: base.homeSectionOrder,
        heroAutoRotate = json.optBoolean("heroAutoRotate", base.heroAutoRotate),
        heroShowsGrades = json.optBoolean("heroShowsGrades", base.heroShowsGrades),
        heroShowsTasks = json.optBoolean("heroShowsTasks", base.heroShowsTasks),
        heroShowsExpenses = json.optBoolean("heroShowsExpenses", base.heroShowsExpenses),
        initialTab = json.enumOr("initialTab", base.initialTab),
        visualPreset = json.enumOr("visualPreset", base.visualPreset)
    ).normalized()

    /**
     * El movimiento, guardado **por el catálogo** y no campo a campo.
     *
     * Son veinticinco gestos con sus variantes: escritos a mano serían cincuenta líneas aquí y
     * otras cincuenta al leer, y cada gesto nuevo obligaría a tocar las dos. Recorriendo
     * [MotionCatalog] se guarda y se lee solo, y añadir un gesto es añadirlo allí.
     */
    private fun encodeMotion(value: MotionPreferences): JSONObject {
        val json = JSONObject()
        MotionCatalog.gestures.forEach { gesto -> json.put(gesto.id, gesto.read(value).id) }
        MotionCatalog.toggles.forEach { toggle -> json.put(toggle.id, toggle.read(value)) }
        return json
    }

    private fun decodeMotion(json: JSONObject?, base: MotionPreferences): MotionPreferences {
        if (json == null) return base
        var prefs = base
        MotionCatalog.gestures.forEach { gesto ->
            val guardada = json.optString(gesto.id)
            // Una variante que ya no existe —renombrada, o retirada— se queda en la de base
            // en vez de tumbar la lectura entera de las preferencias.
            gesto.options.firstOrNull { it.id == guardada }?.let { prefs = gesto.write(prefs, it) }
        }
        MotionCatalog.toggles.forEach { toggle ->
            if (json.has(toggle.id)) prefs = toggle.write(prefs, json.optBoolean(toggle.id, toggle.read(prefs)))
        }
        return prefs
    }
}

object AccessibilityPreferencesJson {

    fun encode(value: AccessibilityPreferences): JSONObject = JSONObject()
        .put("appLanguage", value.appLanguage.name)
        .put("highContrastEnabled", value.highContrastEnabled)
        .put("use24HourTime", value.use24HourTime)
        .put("textScale", value.textScale.name)
        .put("motionPreference", value.motionPreference.name)
        .put("heroAnimationEnabled", value.heroAnimationEnabled)
        .put("contrast", value.contrast.name)
        .put("colorBlindPalette", value.colorBlindPalette.name)
        .put("shapesBesidesColor", value.shapesBesidesColor)
        .put("boldText", value.boldText)
        .put("readingFont", value.readingFont.name)
        .put("touchTargetSize", value.touchTargetSize.name)
        .put("reduceTransparency", value.reduceTransparency)
        .put("oneHandedMode", value.oneHandedMode)
        .put("undoDuration", value.undoDuration.name)
        .put("spokenDescriptions", value.spokenDescriptions)
        .put("confirmIrreversible", value.confirmIrreversible)
        .put("keepScreenOn", value.keepScreenOn)
        .put("dateFormat", value.dateFormat.name)
        .put("currency", value.currency.name)

    fun decode(json: JSONObject, base: AccessibilityPreferences): AccessibilityPreferences = AccessibilityPreferences(
        appLanguage = json.enumOr("appLanguage", base.appLanguage),
        highContrastEnabled = json.optBoolean("highContrastEnabled", base.highContrastEnabled),
        use24HourTime = json.optBoolean("use24HourTime", base.use24HourTime),
        textScale = json.enumOr("textScale", base.textScale),
        motionPreference = json.enumOr("motionPreference", base.motionPreference),
        heroAnimationEnabled = json.optBoolean("heroAnimationEnabled", base.heroAnimationEnabled),
        dateFormat = json.enumOr("dateFormat", base.dateFormat),
        currency = json.enumOr("currency", base.currency),
        contrast = json.enumOr("contrast", base.contrast),
        colorBlindPalette = json.enumOr("colorBlindPalette", base.colorBlindPalette),
        shapesBesidesColor = json.optBoolean("shapesBesidesColor", base.shapesBesidesColor),
        boldText = json.optBoolean("boldText", base.boldText),
        readingFont = json.enumOr("readingFont", base.readingFont),
        touchTargetSize = json.enumOr("touchTargetSize", base.touchTargetSize),
        reduceTransparency = json.optBoolean("reduceTransparency", base.reduceTransparency),
        oneHandedMode = json.optBoolean("oneHandedMode", base.oneHandedMode),
        undoDuration = json.enumOr("undoDuration", base.undoDuration),
        spokenDescriptions = json.optBoolean("spokenDescriptions", base.spokenDescriptions),
        confirmIrreversible = json.optBoolean("confirmIrreversible", base.confirmIrreversible),
        keepScreenOn = json.optBoolean("keepScreenOn", base.keepScreenOn)
    )
}

/** Los escenarios de nota guardados en la calculadora. */
object GradeScenariosJson {

    fun encode(scenarios: List<SavedGradeScenario>): JSONArray = JSONArray(
        scenarios.map { scenario ->
            JSONObject()
                .put("id", scenario.id)
                .put("subjectId", scenario.subjectId)
                .put("subjectName", scenario.subjectName)
                .put("name", scenario.name)
                .put("targetAverage", scenario.targetAverage)
                .put("neededGrade", scenario.neededGrade)
                .put("createdAt", scenario.createdAt)
        }
    )

    fun decode(array: JSONArray?): List<SavedGradeScenario> {
        if (array == null) return emptyList()
        return (0 until array.length())
            .mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                SavedGradeScenario(
                    id = item.optString("id"),
                    subjectId = item.optString("subjectId"),
                    subjectName = item.optString("subjectName"),
                    name = item.optString("name"),
                    targetAverage = item.optDouble("targetAverage"),
                    neededGrade = if (item.isNull("neededGrade")) null else item.optDouble("neededGrade"),
                    createdAt = item.optLong("createdAt")
                )
            }
            .filter { it.id.isNotBlank() && it.subjectId.isNotBlank() && it.name.isNotBlank() }
    }
}

private inline fun <reified T : Enum<T>> JSONObject.enumOr(key: String, base: T): T {
    val raw = optString(key)
    return enumValues<T>().firstOrNull { it.name == raw } ?: base
}

private fun JSONObject.intOrNull(key: String): Int? {
    if (!has(key) || isNull(key)) return null
    return optLong(key).toInt()
}
