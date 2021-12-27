# Google-Pay-Helper

Library to implement the Google Pay subscriptions using a simple callback approach 

# Download 

Download the latest AAR from Maven Central or grab via Gradle:

Add it in your root build.gradle at the end of repositories:

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Step 2. Add the dependency ( App Level )

```
dependencies {
	    implementation 'com.github.renesistech:Google-Pay-Helper:0.1.0'
}
```

# LIBRARY USAGE

STEP 1 :

Configure your project on firebase and download play-services.json


STEP 2 :

Add the dependency as shown in download section


STEP 3 : 

Generate release build and upload to playstore in alpha mode and create subscription that you need

STEP 4 : 

Get your SKU list and security Base 64 key from playstore and save it  secure way ( Server side recomended )


STEP 5 : 

Start and initialize Billing sdks using library
```
val renesisBillingHelper = RenesisBillingHelper("Your Activity", "SKU ARRAY")
renesisBillingHelper.initializeBilling("USER ID", "BASE64 SECURITY KEY ")
```
User Id will be used for saving purchases in local database so you can re-sync the record in case of failure of api

STEP 6 :

Purchase Subscription using purchase method by passing SKU and if user is already subscribed to a plan then pass OLD SKU otherwise pass it as null
```
 renesisBillingHelper.purchaseSku("SKU TO BUY", "OLD SUBSCRIBED SKU - CAN BE NULL ") {subscriptionResult->
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
            
```







