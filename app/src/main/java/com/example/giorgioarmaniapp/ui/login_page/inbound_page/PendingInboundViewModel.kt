package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import androidx.lifecycle.*
import com.example.giorgioarmaniapp.models.InboundPendingListModel.InboundPendingListResult
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.launch

class PendingInboundViewModel : ViewModel() {

    val pendingInboundList = MutableLiveData<List<InboundPendingListResult>?>()
    val myPendingInboundList = MutableLiveData<List<InboundPendingListResult>?>()

    val inboundSearchText = MutableLiveData("")

    val isVisibleInboundList = MutableLiveData(true)
    val isNotFound = MutableLiveData(false)
    val isLoading = MutableLiveData(false)

    fun loadInboundList(storeCode: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true

                val response = RestService().getInboundPendingList(storeCode)

                if (response != null && !response.results.isNullOrEmpty()) {

                    myPendingInboundList.value = response.results
                    pendingInboundList.value = response.results

                    isVisibleInboundList.value = true
                    isNotFound.value = false

                } else {
                    // empty or error case
                    pendingInboundList.value = emptyList()
                    isNotFound.value = true
                    isVisibleInboundList.value = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                isNotFound.value = true
                isVisibleInboundList.value = false
            } finally {
                isLoading.value = false
            }
        }
    }

    fun filterList(search: String?) {
        val originalList = myPendingInboundList.value ?: return

        if (search.isNullOrEmpty()) {
            pendingInboundList.value = originalList
            isNotFound.value = false
            isVisibleInboundList.value = true
        } else {
            val filtered = originalList.filter {
                it.deliveryNumber?.contains(search, true) == true ||
                        it.outboundNumber?.contains(search, true) == true
            }

            pendingInboundList.value = filtered

            isNotFound.value = filtered.isEmpty()
            isVisibleInboundList.value = filtered.isNotEmpty()
        }
    }
}