package com.example.giorgioarmaniapp.ui.login_page.home_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.GTINPatternModel
import com.example.giorgioarmaniapp.models.enums.HomeMenuEnums
import com.example.giorgioarmaniapp.models.statics.HomePageMenuModel
import com.example.giorgioarmaniapp.service.RestService
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomePageViewModel : ViewModel() {

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String> = _storeName

    private val _employeeName = MutableLiveData<String>()
    val employeeName: LiveData<String> = _employeeName

    private val _menuItems = MutableLiveData<List<HomePageMenuModel>>()
    val menuItems: LiveData<List<HomePageMenuModel>> = _menuItems

    private var _gtinPatternList: ArrayList<GTINPatternModel.GTINPattern> = ArrayList()
    var gtinPatternList: ArrayList<GTINPatternModel.GTINPattern>
        get() = _gtinPatternList
        set(value) {
            _gtinPatternList = value
        }

    var isBusy: Boolean = false

    private val restService = RestService()

    init {
        _storeName.value = Settings.storeName ?: "Store"
        _employeeName.value = Settings.userName ?: "Employee"
        bindData()
    }

    fun stopReadingMode() {
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
    }

    fun bindData() {
        try {
            val tempMenuItems = ArrayList<HomePageMenuModel>()

            tempMenuItems.add(HomePageMenuModel().apply {
                title = "Inbound"
                homeMenuNavType = HomeMenuEnums.INBOUND
            })

            tempMenuItems.add(HomePageMenuModel().apply {
                title = "Outbound"
                homeMenuNavType = HomeMenuEnums.OUTBOUND
            })

            tempMenuItems.add(HomePageMenuModel().apply {
                title = "Search"
                homeMenuNavType = HomeMenuEnums.SEARCH
            })

            tempMenuItems.add(HomePageMenuModel().apply {
                title = "Stocktake"
                homeMenuNavType = HomeMenuEnums.STOCKTAKE
            })

            _menuItems.value = tempMenuItems

            GTINPatternList()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun menuItemTap(model: HomePageMenuModel?) {
        viewModelScope.launch {
            try {
                if (handleInternetConnection()) {
                    if (model == null) return@launch

                    when (model.homeMenuNavType) {

                        HomeMenuEnums.INBOUND -> {
                            showLoading()
                            delay(50)
                            navigate("PendingInboundPage")
                        }

                        HomeMenuEnums.OUTBOUND -> {
                            showLoading()
                            delay(50)
                            navigate("OutboundMainPage")
                        }

                        HomeMenuEnums.SEARCH -> {
                            showLoading()
                            delay(50)
                            navigate("SearchPage")
                        }

                        HomeMenuEnums.STOCKTAKE -> {
                            showLoading()
                            delay(50)
                            navigate("StockTakeSelectionPage")
                        }

                        else -> {}
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun clearUserData() {
        Settings.userId = 0
        Settings.userType = ""
        Settings.userName = ""
        Settings.password = ""
        Settings.storeId = ""
        Settings.storeName = ""
    }

    fun logout(onResult: (Boolean) -> Unit) {
        onResult(true)
        clearUserData()
    }

    fun navigateToSettingsPage() {
        if (isBusy) return
        isBusy = true

        try {
            navigate("PasscodePopup")
        } catch (ex: Exception) {
            Log.d("ERROR", ex.toString())
        }

        isBusy = false
    }

    fun GTINPatternList() {
        viewModelScope.launch {
            try {
                val response = restService.gtinPatternPost()

                if (response != null &&
                    response.results != null &&
                    response.success == "SUCCESSFUL"
                ) {
                    Settings.prefixGTINList = ArrayList(response.results)
                } else {
                    showAlert("Alert", "No Data Found for GTINPattern.")
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private suspend fun handleInternetConnection(): Boolean {
        return true
    }

    private fun showLoading() {
        // TODO: Add loader
    }

    private fun navigate(page: String) {
        // TODO: Navigation logic
    }

    private fun showAlert(title: String, message: String) {
        Log.d("ALERT", "$title: $message")
    }
}