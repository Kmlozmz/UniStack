package com.unistack.app.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UniStackEntryPoint {
    fun userRepository(): UserRepository
    fun billingRepository(): BillingRepository
}

@Composable
fun rememberUniStackEntryPoint(): UniStackEntryPoint {
    val context = LocalContext.current
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            UniStackEntryPoint::class.java
        )
    }
}
