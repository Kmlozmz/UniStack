package com.unistack.app.feature_user.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.unistack.app.BuildConfig
import com.unistack.app.feature_user.domain.AccountAuthService
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.LinkedAccount
import kotlinx.coroutines.tasks.await

class FirebaseGoogleAuthService : AccountAuthService {

    override suspend fun signInWithGoogle(context: Context): Result<LinkedAccount> = runCatching {
        ensureFirebaseConfigured(context)
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        check(webClientId.isNotBlank()) {
            "Falta googleWebClientId en local.properties para activar Google Sign-In."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credential = CredentialManager.create(context)
            .getCredential(context = context, request = request)
            .credential

        val googleCredential = when {
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    GoogleIdTokenCredential.createFrom(credential.data)
                } catch (exception: GoogleIdTokenParsingException) {
                    throw IllegalStateException("No se pudo leer la credencial de Google.", exception)
                }
            }
            else -> throw IllegalStateException("La credencial recibida no es de Google.")
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user ?: error("Firebase no devolvió usuario autenticado.")

        LinkedAccount(
            provider = AuthProvider.GOOGLE,
            providerUserId = user.uid,
            displayName = user.displayName ?: googleCredential.displayName,
            email = user.email ?: googleCredential.id,
            photoUrl = user.photoUrl?.toString() ?: googleCredential.profilePictureUri?.toString()
        )
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        Firebase.auth.signOut()
    }

    private fun ensureFirebaseConfigured(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Falta app/google-services.json para inicializar Firebase."
        }
    }
}
