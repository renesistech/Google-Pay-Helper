package com.renesistech.subscriptionhelper

import java.io.Serializable

data class SubscriptionResult(
    var subscribedPlan:SubscribedPlan ?=null,
    var code:Int = 0,
    var message:String = "",
) : Serializable
