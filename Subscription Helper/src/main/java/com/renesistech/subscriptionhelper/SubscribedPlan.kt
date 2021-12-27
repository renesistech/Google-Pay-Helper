package com.renesistech.subscriptionhelper

import java.io.Serializable

data class SubscribedPlan(
    var user_id:String="",
    var plan_sku:String = "",
    var package_name:String = "",
    var purchase_token:String = "",
    var order_id:String = ""
) : Serializable
