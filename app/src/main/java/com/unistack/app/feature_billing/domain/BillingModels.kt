package com.unistack.app.feature_billing.domain

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class ProProduct(
    val productId: String,
    val title: String,
    val description: String,
    val price: String
)

data class BillingState(
    val isLoading: Boolean = true,
    val isBillingAvailable: Boolean = false,
    val isPro: Boolean = false,
    val products: List<ProProduct> = emptyList(),
    val message: String? = null,
    val errorMessage: String? = null
)

interface BillingRepository {
    val state: StateFlow<BillingState>

    fun start()
    fun refreshPurchases()
    fun launchPurchase(activity: Activity, productId: String)
    fun end()
}
