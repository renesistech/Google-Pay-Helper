/*
 * Copyright (C) 2021 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renesistech.subscriptionhelper

import android.app.Activity
import android.util.Log
import androidx.constraintlayout.widget.StateSet.TAG
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * The repository uses data from the Billing data source and the game state model together to give
 * a unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
internal class BillingRepository  constructor(
    private val billingDataSource: BillingDataSource,
    private val defaultScope: CoroutineScope
) {
    private val snackbarMessage: MutableSharedFlow<Int> = MutableSharedFlow()
    private val newPurchaseSku: MutableSharedFlow<SubscribedPlan> = MutableSharedFlow()
    val messages: Flow<Int> get() = snackbarMessage

    val newPurchases: Flow<SubscribedPlan>
    get() = newPurchaseSku

    private suspend fun sendMessage(stringId: Int) {
        snackbarMessage.emit(stringId)
    }

    private fun postMessagesFromBillingFlow() {
        defaultScope.launch {
            try {
                billingDataSource.getNewPurchases().collect { skuItem ->
                    newPurchaseSku.emit(skuItem)
                    billingDataSource.refreshPurchases()
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Collection complete")
            }
            Log.d(TAG, "Collection Coroutine Scope Exited")
        }
    }


    /**
     * Automatic support for upgrading/downgrading subscription.
     * @param activity
     * @param sku
     */
    fun buySku(activity: Activity, sku: String, oldSku: String? = null) {
        if (oldSku == null) {
            billingDataSource.launchBillingFlow(activity, sku)
        } else {
            billingDataSource.launchBillingFlow(activity, sku, oldSku)
        }
    }

    /**
     * Return Flow that indicates whether the sku is currently purchased.
     *
     * @param sku the SKU to get and observe the value for
     * @return Flow that returns true if the sku is purchased.
     */
    private fun isPurchased(sku: String): Flow<Boolean> {
        return billingDataSource.isPurchased(sku)
    }

    private suspend fun refreshPurchases() {
        billingDataSource.refreshPurchases()
    }

    private val billingLifecycleObserver: LifecycleObserver
        get() = billingDataSource

    // There's lots of information in SkuDetails, but our app only needs a few things, since our
    // goods never go on sale, have introductory pricing, etc.
    private fun getSkuTitle(sku: String): Flow<String> {
        return billingDataSource.getSkuTitle(sku)
    }

    private fun getSkuPrice(sku: String): Flow<String> {
        return billingDataSource.getSkuPrice(sku)
    }

    private fun getSkuDescription(sku: String): Flow<String> {
        return billingDataSource.getSkuDescription(sku)
    }


    private val billingFlowInProcess: Flow<Boolean>
        get() = billingDataSource.getBillingFlowInProcess()



    init {
        postMessagesFromBillingFlow()

        // Since both are tied to application lifecycle, we can launch this scope to collect
        // consumed purchases from the billing data source while the app process is alive.
        defaultScope.launch {
            billingDataSource.getConsumedPurchases().collect {
                for (sku in it) {
//                    if (sku == SKU_GAS) {
//                      TODO: Check here already purchased.
//                    }
                }
            }
        }
    }
}
