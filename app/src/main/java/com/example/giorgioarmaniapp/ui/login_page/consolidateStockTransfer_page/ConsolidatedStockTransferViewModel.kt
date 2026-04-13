package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.helper.isInternetAvailable
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

class ConsolidatedStockTransferViewModel : ViewModel() {

    val pageTitle = MutableLiveData("Pending OutBound List")
    val pendingList = MutableLiveData<List<OutBoundStockModel.PendingOutboundResult>?>()
    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    private val _navigateToJob = MutableLiveData<OutBoundStockModel.PendingOutboundResult?>()
    val navigateToJob: LiveData<OutBoundStockModel.PendingOutboundResult?> get() = _navigateToJob

    val errorMessage = MutableLiveData<String?>()
    val successMessage = MutableLiveData<String?>()
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    var isBusy: Boolean = false
    private val restService = RestService()

    fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                if (!isInternetAvailable(context)) {
                    errorMessage.postValue("No Internet Connection")
                    return@launch
                }
                _isLoading.value = true
                val response = restService.getPendingConsolidatedOutboundList(Settings.storeId)

                if (response != null) {
                    if (!response.error.isNullOrEmpty()) {
                        errorMessage.value = response.error
                    }
                    if (!response.success.isNullOrEmpty()) {
                        successMessage.value = response.success
                    }

                    if (response.results != null) {
                        pendingList.value = response.results
                    } else {
                        pendingList.value = emptyList()
                    }

                } else {
                    pendingList.value = emptyList()
                    errorMessage.value = "Failed to fetch data from server"
                }
//                val itemList1 = listOf(
//                    OutBoundStockModel.OutBoundStockListModel(
//                        globalTradeItemNumber = "GTIN001",
//                        actualQuantityDelivered = 10,
//                        scannedQTY = 2,
//                        isDelTag = false
//                    ),
//                    OutBoundStockModel.OutBoundStockListModel(
//                        globalTradeItemNumber = "GTIN002",
//                        actualQuantityDelivered = 5,
//                        scannedQTY = 1,
//                        isDelTag = false
//                    )
//                )
//
//                val itemList2 = listOf(
//                    OutBoundStockModel.OutBoundStockListModel(
//                        globalTradeItemNumber = "GTIN003",
//                        actualQuantityDelivered = 8,
//                        scannedQTY = 0,
//                        isDelTag = false
//                    )
//                )
//
//                // Dummy Pending List
//                val dummyList = listOf(
//                    OutBoundStockModel.PendingOutboundResult(
//                        id = 1,
//                        toStore = "Store A",
//                        itemList = itemList1
//                    ),
//                    OutBoundStockModel.PendingOutboundResult(
//                        id = 2,
//                        toStore = "Store B",
//                        itemList = itemList2
//                    ),
//                    OutBoundStockModel.PendingOutboundResult(
//                        id = 3,
//                        toStore = "Store C",
//                        itemList = emptyList()
//                    )
//                )
//
//                pendingList.value = dummyList
//                successMessage.value = "Dummy data loaded"
//                successMessage.value = "Dummy data loaded"
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unknown error occurred"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onListItemClicked(item: OutBoundStockModel.PendingOutboundResult) {
        if (isBusy) return
        isBusy = true
        _navigateToJob.postValue(item)
        isBusy = false
    }

    fun onNavigateToJobHandled() {
        _navigateToJob.value = null
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

    fun clearSuccessMessage() {
        successMessage.value = null
    }
}
