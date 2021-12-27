package com.renesistech.googlesubscriptionsapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.renesistech.subscriptionhelper.BillingCodes
import com.renesistech.subscriptionhelper.RenesisBillingHelper
import com.renesistech.subscriptionhelper.SubscriptionResult

class MainActivity : AppCompatActivity() {

    private val base64SecurityKey = "Your google subscription key - Get it from playstore"
    private val SUBSCRIPTION_SKUS = arrayOf("com.monthly","com.yearly")
    private val renesisBillingHelper by lazy { RenesisBillingHelper(this, SUBSCRIPTION_SKUS) }
    private val btnBuy: Button by lazy { findViewById(R.id.btnBuy) }
    private val btnUpdate: Button by lazy { findViewById(R.id.btnUpdate) }
    private val userId: String = "123"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startBilling()
    }

    private fun startBilling() {
        renesisBillingHelper.initializeBilling(userId, base64SecurityKey)
        btnBuy.setOnClickListener {
            renesisBillingHelper.purchaseSku(SUBSCRIPTION_SKUS[0], null) {
                handlePurchase(it)
            }
        }
        btnUpdate.setOnClickListener {
            renesisBillingHelper.purchaseSku(SUBSCRIPTION_SKUS[1], SUBSCRIPTION_SKUS[0]) {
                handlePurchase(it)
            }
        }
    }


    private fun handlePurchase(subscriptionResult: SubscriptionResult) {
        when (subscriptionResult.code) {
            BillingCodes.NEW_PLAN_PURCHASED -> {
                showMessage("Plan purchased successfully")
                /* Send data to your own server == subscriptionResult.subscribedPlan
                 After submitting data to your server make sure to acknowledge purchase
                 so data can be removed from local database == renesisBillingHelper.acknowledgePurchaseSynced
                  pass user id as parameter so it will not remove other's user data */
            }
            BillingCodes.BILLING_NOT_INITIALIZED -> {
                showMessage("You have not initialized billing . (initializeBilling) method not called")
                /* Please call the method  == renesisBillingHelper.initializeBilling(userId, baseKey)
                   before calling the purchaseSku method */
            }
            BillingCodes.PLAN_NOT_SYNCED_TO_SERVER -> {
                showMessage("Plan already purchased and saved in local but not acknowledged yet")
                renesisBillingHelper.acknowledgePurchaseSynced(userId)
                /* It will happen if you will purchased a plan but not acknowledged( not submitted to server )
                   You can user the purchased data for submitting to server == subscriptionResult.subscribedPlan
                   After submitting this data to server make sure to acknowledge purchase
                   so data can be removed from local database == renesisBillingHelper.acknowledgePurchaseSynced
                   pass user id as parameter so it will not remove other's user data */
            }
            BillingCodes.BILLING_ERROR -> {
                showMessage("General Error" + subscriptionResult.message)
                /* It will happen if google will through any error , You can try again purchase or check your security key and configurations */
            }
        }
    }


    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}