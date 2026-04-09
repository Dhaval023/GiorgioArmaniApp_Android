package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

class ConsolidatedStockTransferViewModel : ViewModel() {

    val pageTitle = MutableLiveData("Pending OutBound List")
    val pendingList = MutableLiveData<List<OutBoundStockModel.PendingOutboundResult>?>()
    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    val errorMessage = MutableLiveData<String?>()

    var isBusy: Boolean = false
    private val restService = RestService()

    fun loadData() {
        viewModelScope.launch {
            try {
                val response = restService.getPendingConsolidatedOutboundList(Settings.storeId)

                if (response?.results != null) {
                    pendingList.value = response.results
                } else {
                    pendingList.value = emptyList()
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unknown error occurred"
                e.printStackTrace()
            }
        }
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

    fun clearErrorMessage() {
        errorMessage.value = null
    }
}