package com.unistack.app.feature_user.domain

import android.content.Context

interface AccountAuthService {
    suspend fun signInWithGoogle(context: Context): Result<LinkedAccount>
    suspend fun signOut(): Result<Unit>
}
