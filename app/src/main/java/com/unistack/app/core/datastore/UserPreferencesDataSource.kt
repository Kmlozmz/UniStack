package com.unistack.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.LinkedAccount
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataSource(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val PREFERRED_NAME = stringPreferencesKey("preferred_name")
        val ACCOUNT_PROVIDER = stringPreferencesKey("account_provider")
        val ACCOUNT_PROVIDER_USER_ID = stringPreferencesKey("account_provider_user_id")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_PHOTO_URL = stringPreferencesKey("account_photo_url")
        val SYNC_STATUS = stringPreferencesKey("sync_status")
        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val EDUCATION_LEVEL = stringPreferencesKey("education_level")
        val STUDY_AREA = stringPreferencesKey("study_area")
        val CAREER_OR_PROGRAM = stringPreferencesKey("career_or_program")
        val GRADE_LEVEL = stringPreferencesKey("grade_level")
        val GRADING_SCALE = stringPreferencesKey("grading_scale")
        val PASSING_GRADE = doublePreferencesKey("passing_grade")
        val TARGET_AVERAGE = doublePreferencesKey("target_average")
        val ENABLED_MODULES = stringSetPreferencesKey("enabled_modules")
        val VISUAL_PREFERENCE = stringPreferencesKey("visual_preference")
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val CREATED_AT = longPreferencesKey("created_at")
        val UPDATED_AT = longPreferencesKey("updated_at")
    }

    val userProfileFlow: Flow<UserProfile?> = context.dataStore.data.map { prefs ->
        val userId = UserIds.normalize(prefs[Keys.USER_ID] ?: return@map null)
        val educationLevelStr = prefs[Keys.EDUCATION_LEVEL] ?: return@map null
        val gradingScaleStr = prefs[Keys.GRADING_SCALE] ?: return@map null

        val educationLevel = runCatching { EducationLevel.valueOf(educationLevelStr) }.getOrNull()
            ?: return@map null
        val gradingScale = runCatching { GradingScale.valueOf(gradingScaleStr) }.getOrNull()
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
            ?: VisualPreference.LIGHT
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
            syncStatus = syncStatus,
            lastSyncAt = prefs[Keys.LAST_SYNC_AT],
            educationLevel = educationLevel,
            studyArea = studyArea,
            careerOrProgram = prefs[Keys.CAREER_OR_PROGRAM],
            gradeLevel = prefs[Keys.GRADE_LEVEL],
            gradingScale = gradingScale,
            passingGrade = prefs[Keys.PASSING_GRADE] ?: 3.0,
            targetAverage = prefs[Keys.TARGET_AVERAGE] ?: 4.0,
            enabledModules = enabledModules,
            visualPreference = visualPreference,
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
            prefs[Keys.PASSING_GRADE] = profile.passingGrade
            prefs[Keys.TARGET_AVERAGE] = profile.targetAverage
            prefs[Keys.SETUP_COMPLETED] = profile.setupCompleted
            prefs[Keys.CREATED_AT] = profile.createdAt
            prefs[Keys.UPDATED_AT] = profile.updatedAt
            prefs[Keys.ENABLED_MODULES] = profile.enabledModules.map { it.name }.toSet()
            prefs[Keys.VISUAL_PREFERENCE] = profile.visualPreference.name
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
        }
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
}
