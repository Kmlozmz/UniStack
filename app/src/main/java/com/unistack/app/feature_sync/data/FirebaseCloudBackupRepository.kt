package com.unistack.app.feature_sync.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupState
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * La copia en la nube guarda lo mismo que el archivo: el JSON de [LocalBackupRepository].
 *
 * Tenía su propio mapeo campo a campo, que se quedó en la versión 7 del formato: no llevaba
 * periodos, ni apuntes, ni la mitad del perfil, y restaurar solo añadía encima de lo que
 * hubiera. Con el mismo JSON, lo que entra en una copia entra en la otra, y restaurar reemplaza
 * igual en las dos. Los archivos adjuntos no caben en un documento de Firestore, así que esos
 * solo viajan en el archivo.
 */
class FirebaseCloudBackupRepository(
    private val context: Context,
    private val userRepository: UserRepository,
    private val localBackupRepository: LocalBackupRepository
) : CloudBackupRepository {
    private val _state = MutableStateFlow(CloudBackupState())
    override val state: StateFlow<CloudBackupState> = _state

    override val isConfigured: Boolean
        get() = runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
            FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)

    override suspend fun backupNow(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val now = System.currentTimeMillis()
        val payload = mapOf(
            "updatedAt" to now,
            // Sin sangrías: un documento no pasa de un mega, y los espacios no aportan nada.
            JSON_FIELD to JSONObject(localBackupRepository.exportBackupJson()).toString()
        )

        backupDocument(userId).set(payload).await()

        _state.update {
            it.copy(
                inProgress = false,
                lastBackupAt = now,
                message = Textos.get(R.string.cloud_backup_updated),
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: Textos.get(R.string.cloud_backup_update_failed)
            )
        }
    }

    override suspend fun restoreLatest(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val snapshot = backupDocument(userId).get().await()
        val json = snapshot.getString(JSON_FIELD)
        check(snapshot.exists() && !json.isNullOrBlank()) { Textos.get(R.string.cloud_backup_none) }
        localBackupRepository.restoreBackupJson(json).getOrThrow()

        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                inProgress = false,
                lastRestoreAt = now,
                message = Textos.get(R.string.cloud_backup_restored),
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: Textos.get(R.string.cloud_backup_restore_failed)
            )
        }
    }

    private fun backupDocument(userId: String) = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("backups")
        .document("current")

    private fun linkedUserId(): String {
        val user = userRepository.currentUser.value
        val providerUserId = user.providerUserId
        check(user.isLinked && !providerUserId.isNullOrBlank()) {
            Textos.get(R.string.cloud_backup_link_first)
        }
        return providerUserId
    }

    private fun ensureFirebaseConfigured() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            Textos.get(R.string.cloud_backup_no_config)
        }
    }

    private companion object {
        const val JSON_FIELD = "json"
    }
}
