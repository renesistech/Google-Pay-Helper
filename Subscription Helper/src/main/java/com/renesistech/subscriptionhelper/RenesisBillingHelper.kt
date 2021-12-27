package com.renesistech.subscriptionhelper

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.renesistech.subscriptionhelper.BillingCodes.BILLING_ERROR
import com.renesistech.subscriptionhelper.BillingCodes.BILLING_NOT_INITIALIZED
import com.renesistech.subscriptionhelper.BillingCodes.NEW_PLAN_PURCHASED
import com.renesistech.subscriptionhelper.BillingCodes.PLAN_NOT_SYNCED_TO_SERVER
import java.lang.ref.WeakReference

class RenesisBillingHelper(activityRef: AppCompatActivity, private val skuList: Array<String>) {

    private var currentActivity: WeakReference<AppCompatActivity> = WeakReference(activityRef)
    private var billingRepository: BillingRepository? = null
    private var dataManager: DataManager? = null
    private var currentUserId: String = ""
    private lateinit var resultListener: (SubscriptionResult) -> Unit

    fun initializeBilling(userId: String, securityKey: String) {
        currentActivity.get()?.let {
            dataManager = DataManager(it)
            currentUserId = userId
            BillingAppConstants.BILLING_SECURITY_SIGNATURE = securityKey
            val billingSource = BillingDataSource.getInstance(
                it.applicationContext,
                it.lifecycleScope,
                userId,
                skuList
            )
            billingRepository = BillingRepository(billingSource, it.lifecycleScope)
            billingRepository?.newPurchases?.asLiveData()?.observe(it) { purchasedPlan ->
                dataManager!!.setPurchasedPlan(currentUserId, purchasedPlan)
                if (this::resultListener.isInitialized) {
                    resultListener.invoke(
                        SubscriptionResult(
                            purchasedPlan,
                            NEW_PLAN_PURCHASED,
                            "Plan successfully purchased"
                        )
                    )
                }
            }
            billingRepository?.messages?.asLiveData()?.observe(it) { errorMessage ->
                if (this::resultListener.isInitialized) {
                    resultListener.invoke(SubscriptionResult(null, BILLING_ERROR,""+errorMessage))
                }
            }
        }
    }


    fun purchaseSku(
        sku: String,
        oldSku: String?,
        onCompletion: (SubscriptionResult) -> Unit
    ) {
        resultListener = onCompletion
        if (billingRepository == null) {
            resultListener.invoke(
                SubscriptionResult(
                    null,
                    BILLING_NOT_INITIALIZED,
                    "Billing is not initialized, Did you forgot to call (initializeBilling) method"
                )
            )
        } else {
            currentActivity.get()?.let {
                if (dataManager!!.getPreviousPurchasedPlan(currentUserId) != null) {
                    resultListener.invoke(
                        SubscriptionResult(
                            dataManager!!.getPreviousPurchasedPlan(currentUserId),
                            PLAN_NOT_SYNCED_TO_SERVER,
                            "You already purchased a plan from google but not synced yet"
                        )
                    )
                } else {
                    billingRepository?.buySku(it, sku, oldSku)
                }
            }
        }
    }

    fun acknowledgePurchaseSynced(userId: String): Boolean {
        if (currentActivity.get() == null) {
            return false
        } else if(dataManager!=null) {
            dataManager!!.setPlanNull(userId)
            return true
        } else {
            dataManager= DataManager(currentActivity.get()!!)
            dataManager!!.setPlanNull(userId)
            return true
        }
    }


}