package com.unistack.app.feature_billing.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.unistack.app.BuildConfig
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_billing.domain.BillingState
import com.unistack.app.feature_billing.domain.ProProduct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayBillingRepository(
    context: Context
) : BillingRepository, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val productId = BuildConfig.PRO_MONTHLY_PRODUCT_ID
    private val productDetailsById = mutableMapOf<String, ProductDetails>()

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _state = MutableStateFlow(BillingState())
    override val state: StateFlow<BillingState> = _state

    override fun start() {
        if (billingClient.isReady) {
            queryProductsAndPurchases()
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _state.update { it.copy(isBillingAvailable = true, errorMessage = null) }
                        queryProductsAndPurchases()
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isBillingAvailable = false,
                                errorMessage = billingResult.debugMessage.ifBlank {
                                    "Google Play Billing no está disponible."
                                }
                            )
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _state.update {
                        it.copy(
                            isBillingAvailable = false,
                            errorMessage = "Se perdió la conexión con Google Play Billing."
                        )
                    }
                }
            }
        )
    }

    override fun refreshPurchases() {
        if (!billingClient.isReady) {
            start()
            return
        }
        queryProductsAndPurchases()
    }

    override fun launchPurchase(activity: Activity, productId: String) {
        val details = productDetailsById[productId]
        if (details == null) {
            _state.update {
                it.copy(errorMessage = "El producto Pro todavía no está disponible en Google Play.")
            }
            return
        }

        val offerToken = details.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken

        if (offerToken.isNullOrBlank()) {
            _state.update {
                it.copy(errorMessage = "El producto Pro no tiene una oferta activa en Play Console.")
            }
            return
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    override fun end() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach(::handlePurchase)
                queryPurchases()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _state.update { it.copy(message = "Compra cancelada.", errorMessage = null) }
            }
            else -> {
                _state.update {
                    it.copy(
                        errorMessage = billingResult.debugMessage.ifBlank {
                            "No se pudo completar la compra."
                        }
                    )
                }
            }
        }
    }

    private fun queryProductsAndPurchases() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val details = productDetailsResult.productDetailsList
                    productDetailsById.clear()
                    productDetailsById.putAll(details.associateBy { it.productId })
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isBillingAvailable = true,
                            products = details.map(::toProduct),
                            errorMessage = null
                        )
                    }
                    queryPurchases()
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isBillingAvailable = false,
                            errorMessage = billingResult.debugMessage.ifBlank {
                                "No se pudieron cargar productos de Play Billing."
                            }
                        )
                    }
                }
            }
        }
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach(::handlePurchase)
                val hasPro = purchases.any { it.isActiveProPurchase() }
                _state.update {
                    it.copy(
                        isLoading = false,
                        isPro = hasPro,
                        message = if (hasPro) "UniStack Pro activo." else it.message,
                        errorMessage = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = billingResult.debugMessage.ifBlank {
                            "No se pudo verificar compras activas."
                        }
                    )
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!purchase.isActiveProPurchase()) return
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.update {
                        it.copy(isPro = true, message = "UniStack Pro activo.", errorMessage = null)
                    }
                }
            }
        } else {
            _state.update { it.copy(isPro = true, message = "UniStack Pro activo.", errorMessage = null) }
        }
    }

    private fun Purchase.isActiveProPurchase(): Boolean {
        return purchaseState == Purchase.PurchaseState.PURCHASED &&
            products.contains(productId)
    }

    private fun toProduct(details: ProductDetails): ProProduct {
        val offer = details.subscriptionOfferDetails?.firstOrNull()
        val pricing = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return ProProduct(
            productId = details.productId,
            title = details.title,
            description = details.description,
            price = pricing?.formattedPrice ?: "Precio no disponible"
        )
    }
}
