package com.example.giorgioarmaniapp.ui.login_page.consolidateStockTransfer_page

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.helper.isInternetAvailable
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.service.RestService
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.TagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConsolidatedSTJobPageViewModel : ViewModel() {

    companion object {
        private const val TAG = "ConsolidatedSTVM"
        private val tagListDict = HashMap<String, String>()
    }

    var isBusy: Boolean = false
    private val _pageTitle = MutableLiveData("")
    val pageTitle: LiveData<String> = _pageTitle
    fun setPageTitle(title: String) { _pageTitle.value = title }

    private val _pendingOutBoundData = MutableLiveData<OutBoundStockModel.PendingOutboundResult?>()
    val pendingOutBoundData: LiveData<OutBoundStockModel.PendingOutboundResult?> = _pendingOutBoundData

    fun setPendingOutBoundData(data: OutBoundStockModel.PendingOutboundResult?) {
        _pendingOutBoundData.value = data
        if (data != null) {
            _consolidatedSTItems.value = data.itemList?.toMutableList()
        }
    }

    private val _scanOptions = MutableLiveData<List<ScanOptionModel>>(emptyList())
    val scanOptions: LiveData<List<ScanOptionModel>> = _scanOptions

    private val _consolidatedSTItems = MutableLiveData<MutableList<OutBoundStockModel.OutBoundStockListModel>>(mutableListOf())
    val consolidatedSTItems: LiveData<MutableList<OutBoundStockModel.OutBoundStockListModel>> = _consolidatedSTItems

    private val _isRFIDViewVisible    = MutableLiveData(true)
    val isRFIDViewVisible:    LiveData<Boolean> = _isRFIDViewVisible

    private val _isBarcodeViewVisible = MutableLiveData(false)
    val isBarcodeViewVisible: LiveData<Boolean> = _isBarcodeViewVisible

    private val _isTextBoxVisible     = MutableLiveData(false)
    val isTextBoxVisible:     LiveData<Boolean> = _isTextBoxVisible

    private val _expectedQTYTotalCount = MutableLiveData(0)
    val consolidatedSTExpectedQTYTotalCount: LiveData<Int> = _expectedQTYTotalCount

    private val _scannedQTYTotalCount  = MutableLiveData(0)
    val consolidatedSTScannedQTYTotalCount:  LiveData<Int> = _scannedQTYTotalCount

    private val _productIDcode = MutableLiveData("")
    val productIDcode: LiveData<String> = _productIDcode
    fun setProductIDcode(value: String) { _productIDcode.value = value }

    private val _errorMessage        = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage      = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack

    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    private val _showLoading = MutableLiveData(false)
    val showLoading: LiveData<Boolean> = _showLoading
    private val _requestBarcodeFocus = MutableLiveData(false)
    val requestBarcodeFocus: LiveData<Boolean> = _requestBarcodeFocus

    private var rfidReader: com.zebra.rfid.api3.RFIDReader? = null
    private val tagReadLock = Any()
    private var totalTagCount = 0

    init {
        tagListDict.clear()
        buildScanOptions()
    }

    fun initReader(reader: com.zebra.rfid.api3.RFIDReader?) {
        rfidReader = reader
        try {
            reader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,    true)
            reader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
        } catch (e: Exception) {
            Log.e(TAG, "initReader: ${e.message}")
        }
    }

    fun onResume() {
        consolidatedSTScanTotalCount()
        applyRFIDPower(Settings.outboundRFIDPower)
        _requestBarcodeFocus.value = true
    }

    private fun applyRFIDPower(powerIndex: Int) {
        try {
            val antennaConfig = rfidReader?.Config?.Antennas?.getAntennaRfConfig(1)
            if (antennaConfig != null) {
                antennaConfig.transmitPowerIndex = powerIndex
                rfidReader?.Config?.Antennas?.setAntennaRfConfig(1, antennaConfig)
                Log.d(TAG, "RFID TX power → index $powerIndex")
            }
        } catch (e: Exception) {
            Log.e(TAG, "applyRFIDPower: ${e.message}")
        }
    }

    fun onPause() {
        Log.d(TAG, "onPause: releasing reader events")
    }

    private fun buildScanOptions() {
        _scanOptions.value = listOf(
            ScanOptionModel(id = 1, title = "RFID",    isSelected = true),
            ScanOptionModel(id = 2, title = "Barcode", isSelected = false),
            ScanOptionModel(id = 3, title = "Text",    isSelected = false)
        )
    }

    fun onScanOptionSelected(model: ScanOptionModel) {
        viewModelScope.launch {
            _scanOptions.value = _scanOptions.value?.map { s ->
                s.copy(isSelected = s.id == model.id)
            }

            when (model.id) {
                1 -> {
                    setReaderMode(rfid = true, barcode = false)
                    _isRFIDViewVisible.value    = true
                    _isBarcodeViewVisible.value = false
                    _isTextBoxVisible.value     = false
                }
                2 -> {
                    setReaderMode(rfid = false, barcode = true)
                    _isRFIDViewVisible.value    = false
                    _isBarcodeViewVisible.value = true
                    _isTextBoxVisible.value     = false
                    _requestBarcodeFocus.value  = true
                }
                3 -> {
                    setReaderMode(rfid = false, barcode = false)
                    _isRFIDViewVisible.value    = false
                    _isBarcodeViewVisible.value = false
                    _isTextBoxVisible.value     = true
                }
            }
        }
    }

    private fun setReaderMode(rfid: Boolean, barcode: Boolean) {
        try {
            rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,    rfid)
            rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, barcode)
            rfidReader?.Config?.saveConfig()
        } catch (e: Exception) {
            Log.e(TAG, "setReaderMode: ${e.message}")
        }
    }


    fun onTriggerPressed() {
        if (_isRFIDViewVisible.value == true) performInventory()
    }

    fun onTriggerReleased() = stopInventory()

    @Synchronized
    private fun performInventory() {
        totalTagCount = 0
        try { rfidReader?.Actions?.Inventory?.perform() } catch (e: Exception) { Log.e(TAG, "performInventory: ${e.message}") }
    }

    @Synchronized
    private fun stopInventory() {
        try { rfidReader?.Actions?.Inventory?.stop() } catch (e: Exception) { Log.e(TAG, "stopInventory: ${e.message}") }
    }

    @Synchronized
    fun onTagRead(tags: Array<TagData>) {
        synchronized(tagReadLock) {
            for (tag in tags) {
                val tagID = tag.tagID ?: continue
                if (!tagListDict.containsKey(tagID)) {
                    decodeAndUpdate(tagID, tag.tagSeenCount, tag.peakRSSI)
                }
                totalTagCount += tag.tagSeenCount

                if (tag.opCode   == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                    tag.opStatus == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                    tag.memoryBankData?.isNotEmpty() == true) {
                    Log.d(TAG, "MemBank: ${tag.memoryBankData}")
                }
            }
        }
    }

    private fun decodeAndUpdate(rawTag: String, count: Int, rssi: Short) {
        try {
            val resolvedTag = extractGTINFromEPC(rawTag)
            if (resolvedTag != null) {
                tagListDict[rawTag] = resolvedTag
                updateScanQTY(resolvedTag)
            }
        } catch (e: Exception) {
            Log.e(TAG, "decodeAndUpdate: ${e.message}")
        }
    }

    private fun hexToBinary(hex: String): String {
        return hex.chunked(2)
            .joinToString("") { byte ->
                Integer.toBinaryString(byte.toInt(16)).padStart(8, '0')
            }
    }

    private fun extractGTINFromEPC(tagID: String): String? {
        return try {
            val binary = hexToBinary(tagID)
            if (binary.length < 96) return null
            val partition = binary.substring(14, 17).toInt(2)
            val (companyPrefixBits, companyPrefixDigits, itemRefBits, itemRefDigits) = when (partition) {
                0 -> arrayOf(40, 12, 4,  1)
                1 -> arrayOf(37, 11, 7,  2)
                2 -> arrayOf(34, 10, 10, 3)
                3 -> arrayOf(30, 9,  14, 4)
                4 -> arrayOf(27, 8,  17, 5)
                5 -> arrayOf(24, 7,  20, 6)
                6 -> arrayOf(20, 6,  24, 7)
                else -> return null
            }
            val companyPrefix = binary.substring(17, 17 + companyPrefixBits).toLong(2).toString().padStart(companyPrefixDigits, '0')
            val itemRef = binary.substring(17 + companyPrefixBits, 17 + companyPrefixBits + itemRefBits).toLong(2).toString().padStart(itemRefDigits, '0')
            val gtinWithoutCheck = "0$companyPrefix$itemRef"
            val checkDigit = calculateGTINCheckDigit(gtinWithoutCheck)
            "$gtinWithoutCheck$checkDigit"
        } catch (ex: Exception) { null }
    }

    private fun calculateGTINCheckDigit(gtin13: String): Int {
        var sum = 0
        for (i in gtin13.indices) {
            val digit = gtin13[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        return (10 - (sum % 10)) % 10
    }

    fun onStatusEvent(eventType: String) {
        if (eventType == "INVENTORY_STOP") {
            Log.d(TAG, "Unique tags: ${tagListDict.size}  Total seen: $totalTagCount")
        }
    }

    fun onBarcodeScanned(code: String) {
        if (code.isNotEmpty()) updateScanQTY(code)
    }

    fun onAddManualItem() {
        val code = _productIDcode.value?.trim() ?: return
        if (code.isNotEmpty()) {
            updateScanQTY(code)
            _productIDcode.value = ""
        }
    }

    private fun updateScanQTY(gtin: String) {
        val prefixList = Settings.prefixGTINList ?: return
        for (prefix in prefixList) {
            if (prefix.name != null && gtin.startsWith(prefix.name)) {
                val currentList = _consolidatedSTItems.value ?: mutableListOf()
                val matches = currentList.filter { it.globalTradeItemNumber == gtin }
                if (matches.isNotEmpty()) {
                    matches.forEach { it.scannedQTY += 1 }
                    _consolidatedSTItems.postValue(currentList)
                }
                consolidatedSTScanTotalCount()
                break
            }
        }
    }

    fun deleteTag(item: OutBoundStockModel.OutBoundStockListModel) {
        val list = _consolidatedSTItems.value ?: return
        list.remove(item)
        tagListDict.entries.removeAll { it.value == item.globalTradeItemNumber }
        _consolidatedSTItems.value = list
        consolidatedSTScanTotalCount()
    }

    fun consolidatedSTScanTotalCount() {
        val list = _consolidatedSTItems.value ?: return
        _expectedQTYTotalCount.postValue(list.sumOf { it.actualQuantityDelivered })
        _scannedQTYTotalCount.postValue(list.sumOf { it.scannedQTY })
    }

    fun submitConsolidatedST(restService: RestService , context: Context) {
        viewModelScope.launch {
            val items = _consolidatedSTItems.value

            if (!isInternetAvailable(context)) {
                _errorMessage.postValue("No Internet Connection")
                return@launch
            }

            if (items.isNullOrEmpty()) {
                _errorMessage.value = "Please Check List Data"
                return@launch
            }

            val pendingData  = _pendingOutBoundData.value
            val toStoreCode  = pendingData?.toStore
            val id           = pendingData?.id ?: 0
            val transferType = "c"
            val userId       = Settings.userId.toString()
            val storeId      = Settings.storeId.toString()

            if (userId.isNullOrEmpty() || toStoreCode.isNullOrEmpty()) {
                _errorMessage.value = "Please Check Data"
                return@launch
            }

            _showLoading.value = true
            try {
                val payload = OutBoundStockModel.OutboundPendingListResult(
                    itemList = items.toList()
                )

                val response = withContext(Dispatchers.IO) {
                    restService.submitOutboundList(
                        fromStoreCode = storeId,
                        toStoreCode = toStoreCode,
                        userID = userId,
                        transferType = transferType,
                        id = id,
                        listModel = payload
                    )
                }

                if (response?.success?.contains("Successfully", true) == true) {

                    _consolidatedSTItems.value = mutableListOf()
                    tagListDict.clear()
                    _successMessage.value = "Data are Successfully Submitted"
                    _navigateBack.value = true

                } else {
                    _errorMessage.value = response?.error ?: "Submission failed"
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Something went wrong"
                e.printStackTrace()
            } finally {
                _showLoading.value = false
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

    fun clearNavigateBack()      { _navigateBack.value        = false }
    fun clearNavigateSettings()  { _navigateToSettings.value  = false }
    fun clearBarcodeFocus()      { _requestBarcodeFocus.value = false }
    fun clearError()             { _errorMessage.value        = null  }
    fun clearSuccess()           { _successMessage.value      = null  }

    override fun onCleared() {
        super.onCleared()
        tagListDict.clear()
    }
}