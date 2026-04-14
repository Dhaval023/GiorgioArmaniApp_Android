package com.example.giorgioarmaniapp.ui.login_page.search_page

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.*
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.*

class SearchPageViewModel : ViewModel() {

    var textGTINValue: String = ""
    private val _tagPattern = MutableLiveData<String?>()
    private val _relativeDistance = MutableLiveData("0")
    val relativeDistance: LiveData<String> = _relativeDistance
    private val _distanceBoxHeight = MutableLiveData(0)
    val distanceBoxHeight: LiveData<Int> = _distanceBoxHeight
    private val _isEnabledTextGTIN = MutableLiveData(true)
    val isEnabledTextGTIN: LiveData<Boolean> = _isEnabledTextGTIN
    private val _alertEvent = MutableLiveData<String?>()
    val alertEvent: LiveData<String?> = _alertEvent
    private val _confirmEvent = MutableLiveData<Pair<String, String>?>()
    val confirmEvent: LiveData<Pair<String, String>?> = _confirmEvent
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings
    private var tagFinderStatus = false
    var isBusy: Boolean = false

    init {
        try {
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(
                ENUM_TRIGGER_MODE.RFID_MODE, true
            )
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(
                ENUM_TRIGGER_MODE.BARCODE_MODE, false
            )
            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateIn() {
        BaseViewModel.updateIn(
            onTagRead = { tagReadEvent(it) },
            onTrigger = { hhTriggerEvent(it) },
            onStatus = { statusEvent(it.statusEventType) },
            onConnection = {}
        )
    }

    fun updateOut() {
        BaseViewModel.updateOut()
    }

    fun performInventory() {
        try { BaseViewModel.rfidModel.performInventory() }
        catch (e: Exception) { e.printStackTrace() }
    }

    fun stopInventory() {
        try { BaseViewModel.rfidModel.stopInventory() }
        catch (e: Exception) { e.printStackTrace() }
    }

    private fun statusEvent(type: STATUS_EVENT_TYPE) {
        Log.d("RFID", "Status: $type")
    }

    private fun tagReadEvent(tags: Array<TagData>) {

        if (!tagFinderStatus) {
            for (tag in tags) {
                val tagId = tag.tagID

                val match = tagId.contains(textGTINValue, ignoreCase = true)

                if (match) {
                    Log.d("RFID", "MATCH FOUND → $tagId")

                    _tagPattern.postValue(tagId)
                    tagFinderStatus = true

                    stopInventory()

                    Handler(Looper.getMainLooper()).postDelayed({
                        hhTriggerEvent(true)
                    }, 200)

                    break
                }
            }
        } else {
            for (tag in tags) {
                val dist = tag.LocationInfo?.relativeDistance ?: continue

                _relativeDistance.postValue(dist.toString())
                _distanceBoxHeight.postValue((dist * 3).toInt())
            }
        }
    }

    fun hhTriggerEvent(pressed: Boolean) {

        try {
            if (tagFinderStatus) {

                val pattern = _tagPattern.value ?: return

                BaseViewModel.rfidModel.locate(pressed, pattern, null)

                if (pressed) {
                    performInventory()
                } else {
                    stopInventory()
                    resetSearch()
                }

            } else {
                if (pressed) performInventory() else stopInventory()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetSearch() {
        Log.d("RFID", "RESET")

        tagFinderStatus = false
        _tagPattern.postValue(null)
        _relativeDistance.postValue("0")
        _distanceBoxHeight.postValue(0)

        _isEnabledTextGTIN.postValue(true)
    }

    fun searchTag() {
        if (textGTINValue.isEmpty()) {
            _alertEvent.value = "Enter Product ID"
            return
        }

        _confirmEvent.value = Pair("Start searching?", "search")
    }

    fun onSearchConfirmed() {
        tagFinderStatus = false
        updateIn()
        _isEnabledTextGTIN.value = false
        performInventory()
    }

    fun showLoading(loading: Boolean) {
        _isLoading.value = loading
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

    fun onAlertHandled() { _alertEvent.value = null }
    fun onConfirmHandled() { _confirmEvent.value = null }

    override fun onCleared() {
        updateOut()
        super.onCleared()
    }
}