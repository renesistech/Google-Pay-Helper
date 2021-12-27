package com.renesistech.subscriptionhelper

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.renesistech.subscriptionhelper.BillingAppConstants.DATABASE_NAME

internal class DataManager {
    private var context: Context? = null
    private var pref: SharedPreferences
    private lateinit var gson: Gson

    constructor(context: Context) {
        this.context = context
        pref = context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE)
        gson = Gson()
    }


    fun setPurchasedPlan(userId:String,subscribedPlan: SubscribedPlan) {
        pref.edit().putString(userId,gson.toJson(subscribedPlan)).apply()
    }
    fun getPreviousPurchasedPlan(userId: String):SubscribedPlan?{
        val planString = pref.getString(userId, "")
        if(planString.isNullOrEmpty()){
            return null
        } else {
            return gson.fromJson(planString,SubscribedPlan::class.java)
        }
    }



    fun setPlanNull(userId: String) {
        pref.edit().putString(userId,"").apply()
    }

}