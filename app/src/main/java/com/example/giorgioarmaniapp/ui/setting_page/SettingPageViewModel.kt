package com.example.giorgioarmaniapp.ui.login_page.home_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.BuildConfig
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE

class SettingPageViewModel : ViewModel() {

    private val _inboundPower = MutableLiveData<String>()
    val inboundPower: LiveData<String> = _inboundPower
    private val _outboundPower = MutableLiveData<String>()
    val outboundPower: LiveData<String> = _outboundPower
    private val _stocktakePower = MutableLiveData<String>()
    val stocktakePower: LiveData<String> = _stocktakePower
    private val _appVersion = MutableLiveData<String>()
    val appVersion: LiveData<String> = _appVersion
    private val _alertEvent = MutableLiveData<Pair<String, String>?>()
    val alertEvent: LiveData<Pair<String, String>?> = _alertEvent
    private val _confirmEvent = MutableLiveData<Pair<String, String>?>()
    val confirmEvent: LiveData<Pair<String, String>?> = _confirmEvent

    init {
        try {
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(
                ENUM_TRIGGER_MODE.RFID_MODE, true
            )
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(
                ENUM_TRIGGER_MODE.BARCODE_MODE, false
            )
            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        _inboundPower.value   = Settings.inboundRFIDPower.toString()
        _outboundPower.value  = Settings.outboundRFIDPower.toString()
        _stocktakePower.value = Settings.stockTakeRFIDPower.toString()
        _appVersion.value     = BuildConfig.VERSION_NAME
    }

    fun onSetInboundClicked(value: String) {
        try {
            val power = value.toIntOrNull() ?: 0
            if (power > 270) {
                _alertEvent.value = Pair(
                    "Alert",
                    "RFID power cannot be more than 270. Please set 270 for the Maximum power."
                )
            } else {
                _confirmEvent.value = Pair(
                    "Are you sure you want set this Inbound RFID Power?",
                    "inbound:$value"
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onSetOutboundClicked(value: String) {
        try {
            val power = value.toIntOrNull() ?: 0
            if (power > 270) {
                _alertEvent.value = Pair(
                    "Alert",
                    "RFID power cannot be more than 270. Please set 270 for the Maximum power."
                )
            } else {
                _confirmEvent.value = Pair(
                    "Are you sure you want set this Outbound RFID Power?",
                    "outbound:$value"
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onSetStockTakeClicked(value: String) {
        try {
            val power = value.toIntOrNull() ?: 0
            if (power > 270) {
                _alertEvent.value = Pair(
                    "Alert",
                    "RFID power cannot be more than 270. Please set 270 for the Maximum power."
                )
            } else {
                _confirmEvent.value = Pair(
                    "Are you sure you want set this Stocktake RFID Power?",
                    "stocktake:$value"
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onConfirmed(action: String) {
        val parts = action.split(":")
        if (parts.size != 2) return
        val type  = parts[0]
        val value = parts[1]

        when (type) {
            "inbound" -> {
                Settings.inboundRFIDPower = value.toIntOrNull() ?: 0
                _inboundPower.value = value
                Log.d("SettingPage", "Inbound set to $value")
            }
            "outbound" -> {
                Settings.outboundRFIDPower = value.toIntOrNull() ?: 0
                _outboundPower.value = value
                Log.d("SettingPage", "Outbound set to $value")
            }
            "stocktake" -> {
                Settings.stockTakeRFIDPower = value.toIntOrNull() ?: 0
                _stocktakePower.value = value
                Log.d("SettingPage", "StockTake set to $value")
            }
        }
    }

    fun onAlertHandled()   { _alertEvent.value   = null }
    fun onConfirmHandled() { _confirmEvent.value = null }
}