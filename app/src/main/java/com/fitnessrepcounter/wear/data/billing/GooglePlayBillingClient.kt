package com.fitnessrepcounter.wear.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.fitnessrepcounter.wear.domain.model.BillingAvailabilityState
import com.fitnessrepcounter.wear.domain.model.BillingEntitlementStatus
import com.fitnessrepcounter.wear.domain.model.BillingPurchaseLaunchResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GooglePlayBillingClient(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ProBillingClient, PurchasesUpdatedListener {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build(),
        )
        .enableAutoServiceReconnection()
        .build()

    private val _availabilityState = MutableStateFlow(BillingAvailabilityState())
    override val availabilityState: StateFlow<BillingAvailabilityState> = _availabilityState.asStateFlow()

    private val _entitlementStatus = MutableStateFlow(BillingEntitlementStatus.UNKNOWN)
    override val entitlementStatus: StateFlow<BillingEntitlementStatus> = _entitlementStatus.asStateFlow()

    private var productDetails: ProductDetails? = null

    override suspend fun sync() {
        if (!ensureReady()) return
        productDetails = queryProductDetails()
        _availabilityState.update { state ->
            state.copy(
                isBillingReady = true,
                isProductAvailable = productDetails != null,
            )
        }
        refreshPurchases()
    }

    override suspend fun launchPurchase(activity: Activity): BillingPurchaseLaunchResult {
        if (!ensureReady()) {
            return BillingPurchaseLaunchResult.BillingUnavailable
        }

        val details = queryProductDetails() ?: return BillingPurchaseLaunchResult.ProductUnavailable
        productDetails = details
        _availabilityState.update {
            it.copy(
                isBillingReady = true,
                isProductAvailable = true,
                isPurchaseInProgress = true,
            )
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken?.let { offerToken ->
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        return when (billingResult.responseCode) {
            BillingResponseCode.OK -> BillingPurchaseLaunchResult.Launched
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                refreshPurchases()
                BillingPurchaseLaunchResult.AlreadyOwned
            }
            BillingResponseCode.USER_CANCELED -> {
                _availabilityState.update { it.copy(isPurchaseInProgress = false) }
                BillingPurchaseLaunchResult.UserCanceled
            }
            BillingResponseCode.SERVICE_DISCONNECTED,
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.BILLING_UNAVAILABLE,
            -> {
                _availabilityState.update {
                    it.copy(
                        isBillingReady = false,
                        isPurchaseInProgress = false,
                    )
                }
                BillingPurchaseLaunchResult.BillingUnavailable
            }
            else -> {
                _availabilityState.update { it.copy(isPurchaseInProgress = false) }
                BillingPurchaseLaunchResult.Failed(billingResult.debugMessage)
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        scope.launch {
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> processPurchases(
                    purchases = purchases.orEmpty(),
                    canMarkNotOwned = false,
                )
                BillingResponseCode.USER_CANCELED -> {
                    _availabilityState.update { it.copy(isPurchaseInProgress = false) }
                }
                BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    refreshPurchases()
                }
                BillingResponseCode.SERVICE_DISCONNECTED,
                BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingResponseCode.BILLING_UNAVAILABLE,
                -> {
                    _availabilityState.update {
                        it.copy(
                            isBillingReady = false,
                            isPurchaseInProgress = false,
                        )
                    }
                }
                else -> {
                    _availabilityState.update { it.copy(isPurchaseInProgress = false) }
                }
            }
        }
    }

    override fun dispose() {
        billingClient.endConnection()
    }

    private suspend fun refreshPurchases() {
        if (!ensureReady()) return

        val queryParams = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)
            .build()
        val queryResult = suspendCancellableCoroutine<QueryPurchasesResponse> { continuation ->
            billingClient.queryPurchasesAsync(queryParams) { billingResult, purchases ->
                continuation.resume(QueryPurchasesResponse(billingResult, purchases))
            }
        }

        if (queryResult.billingResult.responseCode == BillingResponseCode.OK) {
            processPurchases(
                purchases = queryResult.purchases,
                canMarkNotOwned = true,
            )
        } else if (queryResult.billingResult.responseCode == BillingResponseCode.SERVICE_DISCONNECTED ||
            queryResult.billingResult.responseCode == BillingResponseCode.SERVICE_UNAVAILABLE ||
            queryResult.billingResult.responseCode == BillingResponseCode.BILLING_UNAVAILABLE
        ) {
            _availabilityState.update {
                it.copy(
                    isBillingReady = false,
                    isPurchaseInProgress = false,
                )
            }
        } else {
            _availabilityState.update { it.copy(isPurchaseInProgress = false) }
        }
    }

    private suspend fun processPurchases(
        purchases: List<Purchase>,
        canMarkNotOwned: Boolean,
    ) {
        val relevantPurchases = purchases.filter { purchase ->
            PRO_PRODUCT_ID in purchase.products
        }
        val purchased = relevantPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }

        when {
            purchased != null -> {
                val acknowledged = purchased.isAcknowledged || acknowledgePurchase(purchased)
                if (acknowledged) {
                    _entitlementStatus.value = BillingEntitlementStatus.OWNED
                } else {
                    _entitlementStatus.value = BillingEntitlementStatus.UNKNOWN
                }
            }
            relevantPurchases.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> {
                _entitlementStatus.value = BillingEntitlementStatus.PENDING
            }
            canMarkNotOwned -> {
                _entitlementStatus.value = BillingEntitlementStatus.NOT_OWNED
            }
        }

        _availabilityState.update { state ->
            state.copy(
                isBillingReady = true,
                isProductAvailable = productDetails != null,
                isPurchaseInProgress = false,
            )
        }
    }

    private suspend fun ensureReady(): Boolean {
        if (billingClient.isReady) {
            _availabilityState.update { it.copy(isBillingReady = true) }
            return true
        }

        return suspendCancellableCoroutine { continuation ->
            billingClient.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        val isReady = billingResult.responseCode == BillingResponseCode.OK
                        _availabilityState.update {
                            it.copy(
                                isBillingReady = isReady,
                                isPurchaseInProgress = false,
                            )
                        }
                        continuation.resume(isReady)
                    }

                    override fun onBillingServiceDisconnected() {
                        _availabilityState.update {
                            it.copy(
                                isBillingReady = false,
                                isPurchaseInProgress = false,
                            )
                        }
                    }
                },
            )
        }
    }

    private suspend fun queryProductDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
                        .setProductType(ProductType.INAPP)
                        .build(),
                ),
            )
            .build()

        val queryResult = suspendCancellableCoroutine<QueryProductDetailsResponse> { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, detailsResult ->
                continuation.resume(QueryProductDetailsResponse(billingResult, detailsResult.productDetailsList))
            }
        }

        return if (queryResult.billingResult.responseCode == BillingResponseCode.OK) {
            queryResult.productDetails.firstOrNull()
        } else {
            null
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase): Boolean {
        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val billingResult = suspendCancellableCoroutine<BillingResult> { continuation ->
            billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                continuation.resume(result)
            }
        }

        return billingResult.responseCode == BillingResponseCode.OK
    }

    private data class QueryProductDetailsResponse(
        val billingResult: BillingResult,
        val productDetails: List<ProductDetails>,
    )

    private data class QueryPurchasesResponse(
        val billingResult: BillingResult,
        val purchases: List<Purchase>,
    )

    companion object {
        const val PRO_PRODUCT_ID: String = "wristrep_pro_unlock"
    }
}
