package com.example.giorgioarmaniapp.helper.base

import android.content.Context
import android.content.SharedPreferences
import com.example.giorgioarmaniapp.models.GTINPatternModel.GTINPattern
import com.example.giorgioarmaniapp.service.RestService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Settings {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    val service = RestService()

    private const val PREFIX_GTIN_LIST_KEY = "PrifixGTINList_key"

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
            val value = prefs.getString(PREFIX_GTIN_LIST_KEY, "") ?: ""
            return if (value.isEmpty()) {
                ArrayList<GTINPattern>()
            } else {
                try {
                    val type = object : TypeToken<List<GTINPattern>>() {}.type
                    gson.fromJson(value, type)
                } catch (e: Exception) {
                    ArrayList<GTINPattern>()
                }
            }
        }
        set(value) {
            val data = gson.toJson(value)
            prefs.edit().putString(PREFIX_GTIN_LIST_KEY, data).apply()
        }
}
