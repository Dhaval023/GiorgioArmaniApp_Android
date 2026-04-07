package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giorgioarmaniapp.models.enums.OutboundMenuEnums
import com.example.giorgioarmaniapp.models.statics.OutboundMainPageMenuModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE

class OutboundMainPageViewModel : ViewModel() {

    private val _outboundMenuItems =
        MutableLiveData<List<OutboundMainPageMenuModel>>(emptyList())
    val outboundMenuItems: LiveData<List<OutboundMainPageMenuModel>>
        get() = _outboundMenuItems

    private val _navigateTo = MutableLiveData<OutboundMenuEnums?>()
    val navigateTo: LiveData<OutboundMenuEnums?> get() = _navigateTo

    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    var isBusy: Boolean = false

    init {
        bindData()
    }

    fun stopReadingMode() {
        try {
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun bindData() {
        try {
            val items = listOf(
                OutboundMainPageMenuModel(
                    title = "Stock Transfer",
                    outboundMenuNavType = OutboundMenuEnums.STOCKTRANSFER
                ),
                OutboundMainPageMenuModel(
                    title = "Consolidated Stock Transfer",
                    outboundMenuNavType = OutboundMenuEnums.CONSOLIDATEDSTOCKTRANSFER
                )
            )
            _outboundMenuItems.value = items
        } catch (ex: Exception) {
        }
    }

    fun outboundMenuItemTap(model: OutboundMainPageMenuModel?) {
        try {
            if (model == null) return
            _navigateTo.value = model.outboundMenuNavType
        } catch (ex: Exception) {
            // silent catch — matches original
        }
    }

    fun onNavigateToHandled() {
        _navigateTo.value = null
    }


    fun navigateToSettingsPage() {
        if (isBusy) return
        isBusy = true

        _navigateToSettings.postValue(true)

        isBusy = false
    }

    fun onNavigateToSettingsHandled() {
        _navigateToSettings.value = false
    }

}
