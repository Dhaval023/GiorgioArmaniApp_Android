package com.example.giorgioarmaniapp.ui.login_page.search_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.TagData

class SearchPageViewModel : ViewModel() {

    private val _textGTIN = MutableLiveData<String>("")
    val textGTIN: LiveData<String> = _textGTIN
    var textGTINValue: String = ""
        set(value) {
            field = value
            _textGTIN.value = value
        }

    private val _tagPattern = MutableLiveData<String>("")
    val tagPattern: LiveData<String> = _tagPattern

    private val _isEnabledTextGTIN = MutableLiveData<Boolean>(true)
    val isEnabledTextGTIN: LiveData<Boolean> = _isEnabledTextGTIN

    private val _relativeDistance = MutableLiveData<String>("0")
    val relativeDistance: LiveData<String> = _relativeDistance

    private val _distanceBoxHeight = MutableLiveData<Int>(0)
    val distanceBoxHeight: LiveData<Int> = _distanceBoxHeight

    private val _alertEvent = MutableLiveData<String?>()
    val alertEvent: LiveData<String?> = _alertEvent

    private val _confirmEvent = MutableLiveData<Pair<String, String>?>()
    val confirmEvent: LiveData<Pair<String, String>?> = _confirmEvent

    private var tagFinderStatus: Boolean = false
    private val tagReadLock = Any()

    // ─── EPC → GTIN helpers ───────────────────────────────────────────────────

    private fun hexToBinary(hex: String): String {
        return hex.chunked(2)
            .joinToString("") { byte ->
                Integer.toBinaryString(byte.toInt(16)).padStart(8, '0')
            }
    }

    private fun extractGTINFromEPC(tagID: String): String? {
        return try {
            val binary = hexToBinary(tagID)
            if (binary.length < 96) {
                Log.d("SearchPage", "EPC too short: ${binary.length} bits")
                return null
            }

            val partition = binary.substring(14, 17).toInt(2)

            val (companyPrefixBits, companyPrefixDigits, itemRefBits, itemRefDigits) = when (partition) {
                0 -> arrayOf(40, 12, 4,  1)
                1 -> arrayOf(37, 11, 7,  2)
                2 -> arrayOf(34, 10, 10, 3)
                3 -> arrayOf(30, 9,  14, 4)
                4 -> arrayOf(27, 8,  17, 5)
                5 -> arrayOf(24, 7,  20, 6)
                6 -> arrayOf(20, 6,  24, 7)
                else -> {
                    Log.d("SearchPage", "Unknown partition: $partition")
                    return null
                }
            }

            val companyPrefix = binary.substring(17, 17 + companyPrefixBits)
                .toLong(2)
                .toString()
                .padStart(companyPrefixDigits, '0')

            val itemRef = binary.substring(
                17 + companyPrefixBits,
                17 + companyPrefixBits + itemRefBits
            )
                .toLong(2)
                .toString()
                .padStart(itemRefDigits, '0')

            val gtinWithoutCheck = "0$companyPrefix$itemRef"
            val checkDigit = calculateGTINCheckDigit(gtinWithoutCheck)
            "$gtinWithoutCheck$checkDigit"

        } catch (ex: Exception) {
            Log.e("SearchPage", "GTIN extraction failed: ${ex.message}")
            null
        }
    }

    private fun calculateGTINCheckDigit(gtin13: String): Int {
        var sum = 0
        for (i in gtin13.indices) {
            val digit = gtin13[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        return (10 - (sum % 10)) % 10
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    init {
        _relativeDistance.value = "0"
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

    // ─── RFID wiring ──────────────────────────────────────────────────────────

    fun updateIn() {
        Log.d("SearchPage", "updateIn() called")
        BaseViewModel.updateIn(
            onTagRead = { tags ->
                Log.d("SearchPage", "onTagRead: ${tags.size} tags")
                tagReadEvent(tags)
            },
            onTrigger = { pressed ->
                Log.d("SearchPage", "onTrigger: $pressed")
                hhTriggerEvent(pressed)
            },
            onStatus = { status ->
                Log.d("SearchPage", "onStatus: ${status.statusEventType}")
                statusEvent(status.statusEventType)
            },
            onConnection = { connected ->
                Log.d("SearchPage", "onConnection: $connected")
            }
        )
    }

    fun updateOut() {
        Log.d("SearchPage", "updateOut() called")
        BaseViewModel.updateOut()
    }

    // ─── Inventory control ────────────────────────────────────────────────────

    @Synchronized
    fun stopInventory() {
        Log.d("SearchPage", "stopInventory() requested")
        try {
            BaseViewModel.rfidModel.stopInventory()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @Synchronized
    fun performInventory() {
        Log.d("SearchPage", "performInventory() requested")
        try {
            BaseViewModel.rfidModel.performInventory()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // ─── RFID event handlers ──────────────────────────────────────────────────

    @Synchronized
    fun statusEvent(statusEventType: STATUS_EVENT_TYPE) {
        when (statusEventType) {
            STATUS_EVENT_TYPE.INVENTORY_START_EVENT ->
                Log.d("SearchPage", "Inventory Started Status Event")
            STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT ->
                Log.d("SearchPage", "Inventory Stopped Status Event")
            else -> {}
        }
    }

    @Synchronized
    fun tagReadEvent(aryTags: Array<TagData>) {
        Log.d("SearchPage", "tagReadEvent: processing ${aryTags.size} tags. tagFinderStatus: $tagFinderStatus")

        if (!tagFinderStatus) {
            // ── Scanning mode: find the matching tag ──
            synchronized(tagReadLock) {
                for (index in aryTags.indices) {
                    val tagID = aryTags[index].tagID
                    Log.d("SearchPage", "Tag ID found: $tagID")
                    updateList(tagID, aryTags[index].tagSeenCount, aryTags[index].peakRSSI)
                }
            }
        } else {
            // ── Locationing mode: use LocationInfo.relativeDistance ──
            for (index in aryTags.indices) {
                try {
                    val locationInfo = aryTags[index].LocationInfo
                    if (locationInfo != null) {
                        val relDist = locationInfo.relativeDistance
                        Log.d("SearchPage", "Locationing - RelativeDistance: $relDist")
                        _relativeDistance.postValue(relDist.toString())
                        updateFillView(relDist)
                    } else {
                        Log.w("SearchPage", "locationInfo is null for tag index $index")
                    }
                } catch (ex: Exception) {
                    Log.e("SearchPage", "Error in locationing tagReadEvent: ${ex.message}")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun updateList(tag: String, count: Int, rssi: Short) {
        try {
            // ── Try GTIN match (standard SGTIN-96 tags) ──
            val resolvedGTIN = extractGTINFromEPC(tag)
            Log.d("SearchPage", "Matching Tag: $tag | Resolved GTIN: $resolvedGTIN | Target: $textGTINValue")

            val matched = when {
                // GTIN decoded successfully and matches
                resolvedGTIN != null && resolvedGTIN == textGTINValue -> true
                // Direct EPC match (e.g. BC-type short tags entered as-is)
                tag == textGTINValue -> true
                else -> false
            }

            if (matched) {
                Log.d("SearchPage", "MATCH FOUND! Tag=$tag — switching to Locationing mode.")
                _tagPattern.postValue(tag)
                tagFinderStatus = true
                stopInventory()
                // Small pause so stopInventory() completes before locate() starts
                Thread.sleep(100)
                hhTriggerEvent(true)
            }

        } catch (ex: Exception) {
            Log.e("SearchPage", "Error in updateList: ${ex.message}")
            ex.printStackTrace()
        }
    }
    fun hhTriggerEvent(pressed: Boolean) {
        Log.d("SearchPage", "hhTriggerEvent: pressed=$pressed, tagFinderStatus=$tagFinderStatus")
        try {
            if (tagFinderStatus) {
                val pattern = _tagPattern.value
                Log.d("SearchPage", "Locationing for pattern: $pattern")

                // locate() MUST be called before performInventory() — mirrors Xamarin
                BaseViewModel.rfidModel.locate(pressed, pattern, null)

                if (pressed) {
                    performInventory()
                } else {
                    stopInventory()
                }
            } else {
                if (pressed) {
                    performInventory()
                } else {
                    stopInventory()
                }
            }
        } catch (ex: Exception) {
            Log.e("SearchPage", "Error in hhTriggerEvent: ${ex.message}")
            ex.printStackTrace()
        }
    }

    private fun updateFillView(relativeDistance: Short) {
        val height = (relativeDistance * 3).toInt()
        Log.d("SearchPage", "Updating UI: RelDist=$relativeDistance, Height=$height")
        _distanceBoxHeight.postValue(height)
        _relativeDistance.postValue(relativeDistance.toString())
    }

    // ─── UI actions ───────────────────────────────────────────────────────────

    fun searchTag() {
        Log.d("SearchPage", "searchTag() clicked. GTIN=$textGTINValue")
        try {
            if (textGTINValue.isEmpty()) {
                _alertEvent.value = "Please Enter Product code"
                return
            }
            _confirmEvent.value = Pair("Are you sure you want to Search?", "search")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onSearchConfirmed() {
        Log.d("SearchPage", "Search Confirmed. Initializing RFID callbacks.")
        tagFinderStatus = false   // reset for fresh search
        updateIn()
        _isEnabledTextGTIN.value = false
        performInventory()        // start scanning immediately
    }

    fun onAlertHandled()   { _alertEvent.value   = null }
    fun onConfirmHandled() { _confirmEvent.value = null }

    override fun onCleared() {
        super.onCleared()
        Log.d("SearchPage", "onCleared() - cleaning up")
        updateOut()
    }
}