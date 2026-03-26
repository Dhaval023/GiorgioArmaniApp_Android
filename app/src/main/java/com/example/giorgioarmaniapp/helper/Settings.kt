package com.example.giorgioarmaniapp.helper.base

import android.content.Context
import android.content.SharedPreferences
import com.example.giorgioarmaniapp.models.GTINPatternModel.GTINPattern
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.isNotEmpty

object Settings {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("GiorgioArmaniPrefs", Context.MODE_PRIVATE)
    }

    var userId: Int
        get() = prefs.getInt("UserId", 0)
        set(value) = prefs.edit().putInt("UserId", value).apply()

    var userType: String
        get() = prefs.getString("UserType", "") ?: ""
        set(value) = prefs.edit().putString("UserType", value).apply()

    var userName: String
        get() = prefs.getString("UserName", "") ?: ""
        set(value) = prefs.edit().putString("UserName", value).apply()

    var password: String
        get() = prefs.getString("Password", "") ?: ""
        set(value) = prefs.edit().putString("Password", value).apply()

    var storeId: String
        get() = prefs.getString("StoreId", "") ?: ""
        set(value) = prefs.edit().putString("StoreId", value).apply()

    var storeName: String
        get() = prefs.getString("StoreName", "") ?: ""
        set(value) = prefs.edit().putString("StoreName", value).apply()

    var deviceToken: String
        get() = prefs.getString("DeviceToken", "") ?: ""
        set(value) = prefs.edit().putString("DeviceToken", value).apply()

    var userLoggedIn: Boolean
        get() = prefs.getBoolean("UserLoggedIn", false)
        set(value) = prefs.edit().putBoolean("UserLoggedIn", value).apply()

    var inboundRFIDPower: Int
        get() = prefs.getInt("InboundRFIDPower", 0)
        set(value) = prefs.edit().putInt("InboundRFIDPower", value).apply()

    var outboundRFIDPower: Int
        get() = prefs.getInt("OutboundRFIDPower", 0)
        set(value) = prefs.edit().putInt("OutboundRFIDPower", value).apply()

    var stockTakeRFIDPower: Int
        get() = prefs.getInt("StockTakeRFIDPower", 0)
        set(value) = prefs.edit().putInt("StockTakeRFIDPower", value).apply()

    var prefixGTINList: List<GTINPattern>
        get() {
            val json = prefs.getString("PrifixGTINList_key", "") ?: ""
            if (json.isEmpty()) return emptyList()
            val type = object : TypeToken<List<GTINPattern>>() {}.type
            return gson.fromJson(json, type)
        }
        set(value) {
            val json = if (value.isNotEmpty()) gson.toJson(value) else ""
            prefs.edit().putString("PrifixGTINList_key", json).apply()
        }
}