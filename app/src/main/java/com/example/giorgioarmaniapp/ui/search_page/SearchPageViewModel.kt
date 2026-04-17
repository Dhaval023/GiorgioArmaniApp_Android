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
        try {
            BaseViewModel.rfidModel.performInventory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopInventory() {
        try {
            BaseViewModel.rfidModel.stopInventory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun statusEvent(type: STATUS_EVENT_TYPE) {
        Log.d("RFID", "Status: $type")
    }

    private var currentTagPattern: String? = null

    private fun decodeSGTIN96(hex: String): String? {
        if (hex.length != 24 || !hex.startsWith("30")) return null
        try {
            var binary = java.math.BigInteger(hex, 16).toString(2)
            while (binary.length < 96) binary = "0" + binary

            val partition = binary.substring(11, 14).toInt(2)
            val prefixBitsArray = intArrayOf(40, 37, 34, 30, 27, 24, 20)
            val itemBitsArray = intArrayOf(4, 7, 10, 14, 17, 20, 24)
            val prefixDigitsArray = intArrayOf(12, 11, 10, 9, 8, 7, 6)
            val itemDigitsArray = intArrayOf(1, 2, 3, 4, 5, 6, 7)

            val prefixBits = prefixBitsArray[partition]
            val itemBits = itemBitsArray[partition]
            val prefixDigits = prefixDigitsArray[partition]
            val itemDigits = itemDigitsArray[partition]

            var prefixVal =
                java.math.BigInteger(binary.substring(14, 14 + prefixBits), 2).toString()
            while (prefixVal.length < prefixDigits) prefixVal = "0" + prefixVal

            var itemVal = java.math.BigInteger(
                binary.substring(14 + prefixBits, 14 + prefixBits + itemBits),
                2
            ).toString()
            while (itemVal.length < itemDigits) itemVal = "0" + itemVal

            val indicator = itemVal.substring(0, 1)
            val itemRef = itemVal.substring(1)

            val unchecksummed = indicator + prefixVal + itemRef
            var sum = 0
            for (i in unchecksummed.indices) {
                val digit = unchecksummed[i] - '0'
                sum += digit * if (i % 2 == 0) 3 else 1
            }
            val checksum = (10 - (sum % 10)) % 10

            return unchecksummed + checksum
        } catch (e: Exception) {
            return null
        }
    }

    private fun tagReadEvent(tags: Array<TagData>) {

        if (!tagFinderStatus) {
            for (tag in tags) {
                val tagId = tag.tagID

                val decodedGtin = decodeSGTIN96(tagId)

                val match = tagId.contains(textGTINValue, ignoreCase = true) ||
                        (decodedGtin != null && decodedGtin.contains(
                            textGTINValue,
                            ignoreCase = true
                        ))

                if (match) {
                    Log.d("RFID", "MATCH FOUND → $tagId (Decoded: $decodedGtin)")

                    currentTagPattern = tagId
                    _tagPattern.postValue(tagId)
                    tagFinderStatus = true

                    stopInventory()

                    // Ensure smooth transition from inventory to tag locating like in C#
                    Handler(Looper.getMainLooper()).postDelayed({
                        hhTriggerEvent(true)
                    }, 500) // Increase delay slightly for smooth transition

                    break
                }
            }
        } else {
            for (tag in tags) {
                val dist = tag.LocationInfo?.relativeDistance ?: continue

                _relativeDistance.postValue(dist.toString())
                _distanceBoxHeight.postValue((dist.toInt() * 3))
            }
        }
    }

    fun hhTriggerEvent(pressed: Boolean) {

        try {
            if (tagFinderStatus && !currentTagPattern.isNullOrEmpty()) {
                val pattern = currentTagPattern ?: return

                if (pressed) {
                    BaseViewModel.rfidModel.locate(true, pattern, null)
                } else {
                    BaseViewModel.rfidModel.locate(false, pattern, null)
                    _relativeDistance.postValue("0")
                    _distanceBoxHeight.postValue(0)
                }
            } else {
                if (pressed) {
                    performInventory()
                } else {
                    stopInventory()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearSearch() {
        stopInventory()
        resetSearch()
    }

    private fun resetSearch() {
        Log.d("RFID", "RESET")

        tagFinderStatus = false
        currentTagPattern = null
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
        // Inventory will be started by the physical trigger only
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

    fun onAlertHandled() {
        _alertEvent.value = null
    }

    fun onConfirmHandled() {
        _confirmEvent.value = null
    }

    override fun onCleared() {
        updateOut()
        super.onCleared()
    }
}