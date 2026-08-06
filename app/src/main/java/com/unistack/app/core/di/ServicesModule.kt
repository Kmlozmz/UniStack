package com.unistack.app.core.di

import android.content.Context
import com.unistack.app.feature_billing.data.PlayBillingRepository
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_updates.data.RemoteConfigUpdateRepository
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_user.data.FirebaseGoogleAuthService
import com.unistack.app.feature_user.domain.AccountAuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServicesModule {

    @Provides
    @Singleton
    fun provideAccountAuthService(): AccountAuthService = FirebaseGoogleAuthService()

    @Provides
    @Singleton
    fun provideBillingRepository(@ApplicationContext context: Context): BillingRepository =
        PlayBillingRepository(context)

    @Provides
    @Singleton
    fun provideUpdateRepository(@ApplicationContext context: Context): UpdateRepository =
        RemoteConfigUpdateRepository(context)
}
