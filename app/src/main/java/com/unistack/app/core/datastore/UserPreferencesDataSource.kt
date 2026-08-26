package com.unistack.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.LinkedAccount
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.SavedGradeScenario
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.TextScalePreference
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val PREFERRED_NAME = stringPreferencesKey("preferred_name")
        val ACCOUNT_PROVIDER = stringPreferencesKey("account_provider")
        val ACCOUNT_PROVIDER_USER_ID = stringPreferencesKey("account_provider_user_id")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_PHOTO_URL = stringPreferencesKey("account_photo_url")
        val LOCAL_PHOTO_URI = stringPreferencesKey("local_photo_uri")
        val SYNC_STATUS = stringPreferencesKey("sync_status")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val EDUCATION_LEVEL = stringPreferencesKey("education_level")
        val STUDY_AREA = stringPreferencesKey("study_area")
        val CAREER_OR_PROGRAM = stringPreferencesKey("career_or_program")
        val GRADE_LEVEL = stringPreferencesKey("grade_level")
        val INSTITUTION_NAME = stringPreferencesKey("institution_name")
        val GRADING_SCALE = stringPreferencesKey("grading_scale")
        val CUSTOM_GRADE_MAX = doublePreferencesKey("custom_grade_max")
        val PASSING_GRADE = doublePreferencesKey("passing_grade")
        val TARGET_AVERAGE = doublePreferencesKey("target_average")
        val ENABLED_MODULES = stringSetPreferencesKey("enabled_modules")
        val VISUAL_PREFERENCE = stringPreferencesKey("visual_preference")
        val APPEARANCE_PREFERENCES_JSON = stringPreferencesKey("appearance_preferences_json")
        val ACCESSIBILITY_PREFERENCES_JSON = stringPreferencesKey("accessibility_preferences_json")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val ACADEMIC_WORK_REMINDERS_ENABLED = booleanPreferencesKey("academic_work_reminders_enabled")
        val OVERDUE_REMINDERS_ENABLED = booleanPreferencesKey("overdue_reminders_enabled")
        val GRADE_INSIGHT_REMINDERS_ENABLED = booleanPreferencesKey("grade_insight_reminders_enabled")
        val PENDING_GRADE_REMINDERS_ENABLED = booleanPreferencesKey("pending_grade_reminders_enabled")
        val REMINDER_LEAD_HOURS = intPreferencesKey("reminder_lead_hours")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        val WEEKLY_BUDGET = intPreferencesKey("weekly_budget")
        val MONTHLY_BUDGET = intPreferencesKey("monthly_budget")
        val EXPENSE_ALERT_THRESHOLD_PERCENT = intPreferencesKey("expense_alert_threshold_percent")
        val ENABLED_EXPENSE_CATEGORIES = stringSetPreferencesKey("enabled_expense_categories")
        val GRADE_SCENARIOS_JSON = stringPreferencesKey("grade_scenarios_json")
        val ACADEMIC_PERIOD_SCHEME_JSON = stringPreferencesKey("academic_period_scheme_json")
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val CREATED_AT = longPreferencesKey("created_at")
        val UPDATED_AT = longPreferencesKey("updated_at")
    }

    val userProfileFlow: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        val userId = UserIds.normalize(prefs[Keys.USER_ID] ?: return@map null)
        val educationLevelStr = prefs[Keys.EDUCATION_LEVEL] ?: return@map null
        val gradingScaleStr = prefs[Keys.GRADING_SCALE] ?: return@map null

        val educationLevel = educationLevelStr.toEducationLevelOrNull()
            ?: return@map null
        val gradingScale = gradingScaleStr.toGradingScaleOrNull()
            ?: return@map null
        val studyArea = prefs[Keys.STUDY_AREA]?.let {
            runCatching { StudyArea.valueOf(it) }.getOrNull()
        }
        val enabledModules = prefs[Keys.ENABLED_MODULES]
            ?.mapNotNull { runCatching { AppModule.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?: setOf(AppModule.GRADES, AppModule.TASKS)
        val visualPreference = prefs[Keys.VISUAL_PREFERENCE]
            ?.let { runCatching { VisualPreference.valueOf(it) }.getOrNull() }
            ?: VisualPreference.SYSTEM
        val accountProvider = prefs[Keys.ACCOUNT_PROVIDER]
            ?.let { runCatching { AuthProvider.valueOf(it) }.getOrNull() }
            ?: AuthProvider.LOCAL
        val syncStatus = prefs[Keys.SYNC_STATUS]
            ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
            ?: SyncStatus.LOCAL_ONLY

        UserProfile(
            userId = userId,
            preferredName = prefs[Keys.PREFERRED_NAME] ?: "",
            accountProvider = accountProvider,
            accountProviderUserId = prefs[Keys.ACCOUNT_PROVIDER_USER_ID],
            accountEmail = prefs[Keys.ACCOUNT_EMAIL],
            accountPhotoUrl = prefs[Keys.ACCOUNT_PHOTO_URL],
            localPhotoUri = prefs[Keys.LOCAL_PHOTO_URI],
            syncStatus = syncStatus,
            lastSyncAt = prefs[Keys.LAST_SYNC_AT],
            educationLevel = educationLevel,
            studyArea = studyArea,
            careerOrProgram = prefs[Keys.CAREER_OR_PROGRAM],
            gradeLevel = prefs[Keys.GRADE_LEVEL],
            institutionName = prefs[Keys.INSTITUTION_NAME],
            gradingScale = gradingScale,
            customGradeMax = prefs[Keys.CUSTOM_GRADE_MAX]?.coerceIn(1.0, 100.0) ?: 100.0,
            passingGrade = prefs[Keys.PASSING_GRADE] ?: 3.0,
            targetAverage = prefs[Keys.TARGET_AVERAGE] ?: 4.0,
            enabledModules = enabledModules,
            visualPreference = visualPreference,
            appearancePreferences = parseAppearancePreferences(prefs[Keys.APPEARANCE_PREFERENCES_JSON]),
            accessibilityPreferences = parseAccessibilityPreferences(prefs[Keys.ACCESSIBILITY_PREFERENCES_JSON]),
            taskRemindersEnabled = prefs[Keys.TASK_REMINDERS_ENABLED] ?: true,
            academicWorkRemindersEnabled = prefs[Keys.ACADEMIC_WORK_REMINDERS_ENABLED] ?: true,
            overdueRemindersEnabled = prefs[Keys.OVERDUE_REMINDERS_ENABLED] ?: true,
            gradeInsightRemindersEnabled = prefs[Keys.GRADE_INSIGHT_REMINDERS_ENABLED] ?: true,
            pendingGradeRemindersEnabled = prefs[Keys.PENDING_GRADE_REMINDERS_ENABLED] ?: true,
            reminderLeadHours = prefs[Keys.REMINDER_LEAD_HOURS] ?: 24,
            quietHoursEnabled = prefs[Keys.QUIET_HOURS_ENABLED] ?: false,
            quietHoursStartHour = prefs[Keys.QUIET_HOURS_START]?.takeIf { it in 0..23 },
            quietHoursEndHour = prefs[Keys.QUIET_HOURS_END]?.takeIf { it in 0..23 },
            weeklyBudget = prefs[Keys.WEEKLY_BUDGET] ?: 0,
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0,
            expenseAlertThresholdPercent = prefs[Keys.EXPENSE_ALERT_THRESHOLD_PERCENT] ?: 80,
            enabledExpenseCategories = prefs[Keys.ENABLED_EXPENSE_CATEGORIES]
                ?.mapNotNull { runCatching { ExpenseCategory.valueOf(it) }.getOrNull() }
                ?.toSet()
                ?.ifEmpty { ExpenseCategory.entries.toSet() }
                ?: ExpenseCategory.entries.toSet(),
            gradeScenarios = parseGradeScenarios(prefs[Keys.GRADE_SCENARIOS_JSON]),
            academicPeriodScheme = parseAcademicPeriodScheme(prefs[Keys.ACADEMIC_PERIOD_SCHEME_JSON]),
            setupCompleted = prefs[Keys.SETUP_COMPLETED] ?: false,
            createdAt = prefs[Keys.CREATED_AT] ?: 0L,
            updatedAt = prefs[Keys.UPDATED_AT] ?: 0L
        )
    }

    val currentUserFlow: Flow<AppUser> = userProfileFlow.map { profile ->
        if (profile != null) {
            AppUser(
                userId = UserIds.normalize(profile.userId),
                displayName = profile.preferredName.takeIf { it.isNotBlank() },
                email = profile.accountEmail,
                photoUrl = profile.accountPhotoUrl,
                authProvider = profile.accountProvider,
                providerUserId = profile.accountProviderUserId,
                syncStatus = profile.syncStatus
            )
        } else {
            AppUser(
                userId = UserIds.LOCAL,
                displayName = null,
                email = null,
                photoUrl = null
            )
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = UserIds.normalize(profile.userId)
            prefs[Keys.PREFERRED_NAME] = profile.preferredName
            prefs[Keys.EDUCATION_LEVEL] = profile.educationLevel.name
            prefs[Keys.GRADING_SCALE] = profile.gradingScale.name
            prefs[Keys.CUSTOM_GRADE_MAX] = profile.customGradeMax.coerceIn(1.0, 100.0)
            prefs[Keys.PASSING_GRADE] = profile.passingGrade
            prefs[Keys.TARGET_AVERAGE] = profile.targetAverage
            prefs[Keys.SETUP_COMPLETED] = profile.setupCompleted
            prefs[Keys.CREATED_AT] = profile.createdAt
            prefs[Keys.UPDATED_AT] = profile.updatedAt
            prefs[Keys.ENABLED_MODULES] = profile.enabledModules.map { it.name }.toSet()
            prefs[Keys.VISUAL_PREFERENCE] = profile.visualPreference.name
            prefs[Keys.APPEARANCE_PREFERENCES_JSON] = profile.appearancePreferences.normalized().toJsonString()
            prefs[Keys.ACCESSIBILITY_PREFERENCES_JSON] = profile.accessibilityPreferences.toJsonString()
            prefs[Keys.TASK_REMINDERS_ENABLED] = profile.taskRemindersEnabled
            prefs[Keys.ACADEMIC_WORK_REMINDERS_ENABLED] = profile.academicWorkRemindersEnabled
            prefs[Keys.OVERDUE_REMINDERS_ENABLED] = profile.overdueRemindersEnabled
            prefs[Keys.GRADE_INSIGHT_REMINDERS_ENABLED] = profile.gradeInsightRemindersEnabled
            prefs[Keys.PENDING_GRADE_REMINDERS_ENABLED] = profile.pendingGradeRemindersEnabled
            prefs[Keys.REMINDER_LEAD_HOURS] = profile.reminderLeadHours
            prefs[Keys.QUIET_HOURS_ENABLED] = profile.quietHoursEnabled
            if (profile.quietHoursStartHour != null) {
                prefs[Keys.QUIET_HOURS_START] = profile.quietHoursStartHour
            } else {
                prefs.remove(Keys.QUIET_HOURS_START)
            }
            if (profile.quietHoursEndHour != null) {
                prefs[Keys.QUIET_HOURS_END] = profile.quietHoursEndHour
            } else {
                prefs.remove(Keys.QUIET_HOURS_END)
            }
            prefs[Keys.WEEKLY_BUDGET] = profile.weeklyBudget
            prefs[Keys.MONTHLY_BUDGET] = profile.monthlyBudget
            prefs[Keys.EXPENSE_ALERT_THRESHOLD_PERCENT] = profile.expenseAlertThresholdPercent
            prefs[Keys.ENABLED_EXPENSE_CATEGORIES] = profile.enabledExpenseCategories.map { it.name }.toSet()
            prefs[Keys.GRADE_SCENARIOS_JSON] = profile.gradeScenarios.toJsonArrayString()
            prefs[Keys.ACADEMIC_PERIOD_SCHEME_JSON] = profile.academicPeriodScheme.toJsonString()
            prefs[Keys.ACCOUNT_PROVIDER] = profile.accountProvider.name
            prefs[Keys.SYNC_STATUS] = profile.syncStatus.name

            if (profile.lastSyncAt != null) {
                prefs[Keys.LAST_SYNC_AT] = profile.lastSyncAt
            } else {
                prefs.remove(Keys.LAST_SYNC_AT)
            }
            if (profile.accountProviderUserId != null) {
                prefs[Keys.ACCOUNT_PROVIDER_USER_ID] = profile.accountProviderUserId
            } else {
                prefs.remove(Keys.ACCOUNT_PROVIDER_USER_ID)
            }
            if (profile.accountEmail != null) {
                prefs[Keys.ACCOUNT_EMAIL] = profile.accountEmail
            } else {
                prefs.remove(Keys.ACCOUNT_EMAIL)
            }
            if (profile.accountPhotoUrl != null) {
                prefs[Keys.ACCOUNT_PHOTO_URL] = profile.accountPhotoUrl
            } else {
                prefs.remove(Keys.ACCOUNT_PHOTO_URL)
            }
            if (profile.localPhotoUri != null) {
                prefs[Keys.LOCAL_PHOTO_URI] = profile.localPhotoUri
            } else {
                prefs.remove(Keys.LOCAL_PHOTO_URI)
            }

            if (profile.studyArea != null) {
                prefs[Keys.STUDY_AREA] = profile.studyArea.name
            } else {
                prefs.remove(Keys.STUDY_AREA)
            }
            if (profile.careerOrProgram != null) {
                prefs[Keys.CAREER_OR_PROGRAM] = profile.careerOrProgram
            } else {
                prefs.remove(Keys.CAREER_OR_PROGRAM)
            }
            if (profile.gradeLevel != null) {
                prefs[Keys.GRADE_LEVEL] = profile.gradeLevel
            } else {
                prefs.remove(Keys.GRADE_LEVEL)
            }
            if (profile.institutionName != null) {
                prefs[Keys.INSTITUTION_NAME] = profile.institutionName
            } else {
                prefs.remove(Keys.INSTITUTION_NAME)
            }
        }
    }

    private fun AppearancePreferences.toJsonString(): String = JSONObject()
        .put("backgroundStyle", backgroundStyle.name)
        .put("customBackgroundColor", customBackgroundColor)
        .put("customThemeBase", customThemeBase.name)
        .put("accentStyle", accentStyle.name)
        .put("customAccentColor", customAccentColor)
        .put("accentIntensity", accentIntensity.name)
        .put("surfaceStyle", surfaceStyle.name)
        .put("cornerStyle", cornerStyle.name)
        .put("interfaceDensity", interfaceDensity.name)
        .put("motionPreference", motionPreference.name)
        .put("textScale", textScale.name)
        .put("typographyStyle", typographyStyle.name)
        .put("decimalPlaces", decimalPlaces)
        .put("bottomBarStyle", bottomBarStyle.name)
        .put("academicIndicatorStyle", academicIndicatorStyle.name)
        .put("switchIconStyle", switchIconStyle.name)
        .put("academicProgressShape", academicProgressShape.name)
        .put("showHomeGreeting", showHomeGreeting)
        .put("showHomeHero", showHomeHero)
        .put("showHomeAgenda", showHomeAgenda)
        .put("showHomeSnapshot", showHomeSnapshot)
        .put("homeSectionOrder", JSONArray(homeSectionOrder.map { it.name }))
        .put("heroAutoRotate", heroAutoRotate)
        .put("heroShowsGrades", heroShowsGrades)
        .put("heroShowsTasks", heroShowsTasks)
        .put("heroShowsExpenses", heroShowsExpenses)
        .put("initialTab", initialTab.name)
        .put("visualPreset", visualPreset.name)
        .toString()

    private fun AccessibilityPreferences.toJsonString(): String = JSONObject()
        .put("appLanguage", appLanguage.name)
        .put("highContrastEnabled", highContrastEnabled)
        .put("use24HourTime", use24HourTime)
        .put("textScale", textScale.name)
        .put("motionPreference", motionPreference.name)
        .put("heroAnimationEnabled", heroAnimationEnabled)
        .toString()

    private fun parseAccessibilityPreferences(raw: String?): AccessibilityPreferences {
        if (raw.isNullOrBlank()) return AccessibilityPreferences()
        return runCatching {
            val json = JSONObject(raw)
            AccessibilityPreferences(
                appLanguage = json.enumOrDefault("appLanguage", AppLanguage.SYSTEM),
                highContrastEnabled = json.optBoolean("highContrastEnabled", false),
                use24HourTime = json.optBoolean("use24HourTime", true),
                textScale = json.enumOrDefault("textScale", TextScalePreference.STANDARD),
                motionPreference = json.enumOrDefault("motionPreference", MotionPreference.FULL),
                heroAnimationEnabled = json.optBoolean("heroAnimationEnabled", true)
            )
        }.getOrDefault(AccessibilityPreferences())
    }

    private fun parseAppearancePreferences(raw: String?): AppearancePreferences {
        if (raw.isNullOrBlank()) return AppearancePreferences.defaults()
        return runCatching {
            val json = JSONObject(raw)
            val defaults = AppearancePreferences.defaults()
            AppearancePreferences(
                backgroundStyle = json.enumOrDefault("backgroundStyle", defaults.backgroundStyle),
                customBackgroundColor = json.optIntOrNull("customBackgroundColor"),
                customThemeBase = json.enumOrDefault("customThemeBase", defaults.customThemeBase),
                accentStyle = json.enumOrDefault("accentStyle", defaults.accentStyle),
                customAccentColor = json.optIntOrNull("customAccentColor"),
                accentIntensity = json.enumOrDefault("accentIntensity", defaults.accentIntensity),
                surfaceStyle = json.enumOrDefault("surfaceStyle", defaults.surfaceStyle),
                cornerStyle = json.enumOrDefault("cornerStyle", defaults.cornerStyle),
                interfaceDensity = json.enumOrDefault("interfaceDensity", defaults.interfaceDensity),
                motionPreference = json.enumOrDefault("motionPreference", defaults.motionPreference),
                textScale = json.enumOrDefault("textScale", defaults.textScale),
                typographyStyle = json.enumOrDefault("typographyStyle", defaults.typographyStyle),
                decimalPlaces = json.optInt("decimalPlaces", defaults.decimalPlaces),
                bottomBarStyle = json.enumOrDefault("bottomBarStyle", defaults.bottomBarStyle),
                // Clave nueva a propósito: la anterior guardaba «FADE» en los teléfonos que
                // pasaron por las alphas, y ese valor —que entonces era el de por defecto, no una
                // elección— se quedaba pisando el empuje. Con otra clave, todos empiezan por el
                // valor de hoy y quien quiera el fundido lo vuelve a elegir.
                academicIndicatorStyle = json.enumOrDefault(
                    "academicIndicatorStyle",
                    defaults.academicIndicatorStyle
                ),
                switchIconStyle = json.enumOrDefault("switchIconStyle", defaults.switchIconStyle),
                academicProgressShape = json.enumOrDefault(
                    "academicProgressShape",
                    defaults.academicProgressShape
                ),
                showHomeGreeting = json.optBoolean("showHomeGreeting", defaults.showHomeGreeting),
                showHomeHero = json.optBoolean("showHomeHero", defaults.showHomeHero),
                showHomeAgenda = json.optBoolean("showHomeAgenda", defaults.showHomeAgenda),
                showHomeSnapshot = json.optBoolean("showHomeSnapshot", defaults.showHomeSnapshot),
                homeSectionOrder = json.optJSONArray("homeSectionOrder")
                    ?.let { array ->
                        (0 until array.length()).mapNotNull { index ->
                            runCatching { HomeSection.valueOf(array.optString(index)) }.getOrNull()
                        }
                    }
                    ?.ifEmpty { defaults.homeSectionOrder }
                    ?: defaults.homeSectionOrder,
                heroAutoRotate = json.optBoolean("heroAutoRotate", defaults.heroAutoRotate),
                heroShowsGrades = json.optBoolean("heroShowsGrades", defaults.heroShowsGrades),
                heroShowsTasks = json.optBoolean("heroShowsTasks", defaults.heroShowsTasks),
                heroShowsExpenses = json.optBoolean("heroShowsExpenses", defaults.heroShowsExpenses),
                initialTab = json.enumOrDefault("initialTab", defaults.initialTab),
                visualPreset = json.enumOrDefault("visualPreset", defaults.visualPreset)
            ).normalized()
        }.getOrDefault(AppearancePreferences.defaults())
    }

    private inline fun <reified T : Enum<T>> JSONObject.enumOrDefault(key: String, default: T): T {
        val raw = optString(key)
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }

    private fun JSONObject.optIntOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        return optLong(key).toInt()
    }

    suspend fun updatePreferredName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PREFERRED_NAME] = name
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun markSetupCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.SETUP_COMPLETED] = true
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun linkAccount(account: LinkedAccount) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCOUNT_PROVIDER] = account.provider.name
            prefs[Keys.ACCOUNT_PROVIDER_USER_ID] = account.providerUserId
            prefs[Keys.SYNC_STATUS] = SyncStatus.READY_FOR_BACKUP.name
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()

            if (!account.displayName.isNullOrBlank()) {
                prefs[Keys.PREFERRED_NAME] = account.displayName
            }
            if (!account.email.isNullOrBlank()) {
                prefs[Keys.ACCOUNT_EMAIL] = account.email
            } else {
                prefs.remove(Keys.ACCOUNT_EMAIL)
            }
            if (!account.photoUrl.isNullOrBlank()) {
                prefs[Keys.ACCOUNT_PHOTO_URL] = account.photoUrl
            } else {
                prefs.remove(Keys.ACCOUNT_PHOTO_URL)
            }
        }
    }

    suspend fun unlinkAccount() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCOUNT_PROVIDER] = AuthProvider.LOCAL.name
            prefs[Keys.SYNC_STATUS] = SyncStatus.LOCAL_ONLY.name
            prefs[Keys.UPDATED_AT] = System.currentTimeMillis()
            prefs.remove(Keys.ACCOUNT_PROVIDER_USER_ID)
            prefs.remove(Keys.ACCOUNT_EMAIL)
            prefs.remove(Keys.ACCOUNT_PHOTO_URL)
            prefs.remove(Keys.LAST_SYNC_AT)
        }
    }

    private fun parseGradeScenarios(json: String?): List<SavedGradeScenario> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        SavedGradeScenario(
                            id = item.optString("id"),
                            subjectId = item.optString("subjectId"),
                            subjectName = item.optString("subjectName"),
                            name = item.optString("name"),
                            targetAverage = item.optDouble("targetAverage"),
                            neededGrade = if (item.isNull("neededGrade")) null else item.optDouble("neededGrade"),
                            createdAt = item.optLong("createdAt")
                        )
                    )
                }
            }.filter { it.id.isNotBlank() && it.subjectId.isNotBlank() && it.name.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun List<SavedGradeScenario>.toJsonArrayString(): String {
        val array = JSONArray()
        forEach { scenario ->
            array.put(
                JSONObject()
                    .put("id", scenario.id)
                    .put("subjectId", scenario.subjectId)
                    .put("subjectName", scenario.subjectName)
                    .put("name", scenario.name)
                    .put("targetAverage", scenario.targetAverage)
                    .put("neededGrade", scenario.neededGrade)
                    .put("createdAt", scenario.createdAt)
            )
        }
        return array.toString()
    }

    private fun parseAcademicPeriodScheme(json: String?): AcademicPeriodScheme {
        if (json.isNullOrBlank()) return AcademicPeriodScheme.default()
        return runCatching {
            val root = JSONObject(json)
            val label = runCatching {
                AcademicPeriodLabel.valueOf(root.optString("label", AcademicPeriodLabel.CORTE.name))
            }.getOrDefault(AcademicPeriodLabel.CORTE)
            val array = root.optJSONArray("periods") ?: JSONArray()
            val periods = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val order = item.optInt("order", index + 1)
                    val weight = item.optDouble("weight", 0.0)
                    if (weight > 0.0) {
                        add(
                            AcademicPeriod(
                                id = item.optString("id", "period-$order"),
                                name = item.optString("name", "${label.singular} $order"),
                                weight = weight,
                                order = order
                            )
                        )
                    }
                }
            }.sortedBy { it.order }
            AcademicPeriodScheme(label = label, periods = periods)
                .takeIf { it.isValid }
                ?: AcademicPeriodScheme.default()
        }.getOrDefault(AcademicPeriodScheme.default())
    }

    private fun AcademicPeriodScheme.toJsonString(): String {
        val array = JSONArray()
        periods.sortedBy { it.order }.forEach { period ->
            array.put(
                JSONObject()
                    .put("id", period.id)
                    .put("name", period.name)
                    .put("weight", period.weight)
                    .put("order", period.order)
            )
        }
        return JSONObject()
            .put("label", label.name)
            .put("periods", array)
            .toString()
    }

    private fun String.toEducationLevelOrNull(): EducationLevel? {
        return when (this) {
            "SCHOOL" -> EducationLevel.SECONDARY
            "TECHNICAL", "INDEPENDENT_COURSE" -> EducationLevel.OTHER
            else -> runCatching { EducationLevel.valueOf(this) }.getOrNull()
        }
    }

    private fun String.toGradingScaleOrNull(): GradingScale? {
        return when (this) {
            "ZERO_TO_ONE_HUNDRED" -> GradingScale.ZERO_TO_HUNDRED
            "ZERO_TO_TEN", "LETTERS" -> GradingScale.CUSTOM
            else -> runCatching { GradingScale.valueOf(this) }.getOrNull()
        }
    }
}
