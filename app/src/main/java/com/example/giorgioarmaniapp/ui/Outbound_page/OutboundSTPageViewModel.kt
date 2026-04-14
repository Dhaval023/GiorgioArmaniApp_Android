package com.example.giorgioarmaniapp.ui.login_page.Outbound_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.OutBoundStockModel
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.IEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.TagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutboundSTPageViewModel : ViewModel() {

    private val tagReadLock = Any()
    private val tagListDict: MutableMap<String, String> = mutableMapOf()
    private var totalTagCount = 0
    var isBusy = false
    var activeOnFocus: ((Boolean) -> Unit)? = null

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _scanOptions = MutableLiveData<MutableList<ScanOptionModel>>(mutableListOf())
    val scanOptions: LiveData<MutableList<ScanOptionModel>> get() = _scanOptions

    private val _storeCodeList = MutableLiveData<List<OutBoundStockModel.STOutBoundStockListModel>>(emptyList())
    val storeCodeList: LiveData<List<OutBoundStockModel.STOutBoundStockListModel>> get() = _storeCodeList

    private val _selectedStoreCode = MutableLiveData<OutBoundStockModel.STOutBoundStockListModel?>()
    var selectedStoreCode: OutBoundStockModel.STOutBoundStockListModel?
        get() = _selectedStoreCode.value
        set(value) {
            _selectedStoreCode.value = value
            if (value != null) getLocationList(value)
        }
    val selectedStoreCodeLive: LiveData<OutBoundStockModel.STOutBoundStockListModel?> get() = _selectedStoreCode

    private val _locationList = MutableLiveData<List<OutBoundStockModel.LocationList>>(emptyList())
    val locationList: LiveData<List<OutBoundStockModel.LocationList>> get() = _locationList

    private val _selectedLocation = MutableLiveData<OutBoundStockModel.LocationList?>()
    var selectedLocation: OutBoundStockModel.LocationList?
        get() = _selectedLocation.value
        set(value) { _selectedLocation.value = value }
    val selectedLocationLive: LiveData<OutBoundStockModel.LocationList?> get() = _selectedLocation

    private val _productIDCode = MutableLiveData("")
    val productIDCodeLive: LiveData<String> get() = _productIDCode
    var productIDCode: String
        get() = _productIDCode.value ?: ""
        set(value) { _productIDCode.value = value }

    private val _allOutboundItems = MutableLiveData<MutableList<OutBoundStockModel.OutBoundStockListModel>>(mutableListOf())
    val allOutboundItems: LiveData<MutableList<OutBoundStockModel.OutBoundStockListModel>> get() = _allOutboundItems

    private val _isPickerVisible = MutableLiveData(true)
    val isPickerVisible: LiveData<Boolean> get() = _isPickerVisible

    private val _isLocationPickerVisible = MutableLiveData(true)
    val isLocationPickerVisible: LiveData<Boolean> get() = _isLocationPickerVisible

    private val _isTextBoxVisible = MutableLiveData(false)
    val isTextBoxVisible: LiveData<Boolean> get() = _isTextBoxVisible

    private val _isRFIDViewVisible = MutableLiveData(true)
    val isRFIDViewVisible: LiveData<Boolean> get() = _isRFIDViewVisible

    private val _isBarcodeViewVisible = MutableLiveData(false)
    val isBarcodeViewVisible: LiveData<Boolean> get() = _isBarcodeViewVisible

    private val _clearBarcodeField = MutableLiveData(false)
    val clearBarcodeField: LiveData<Boolean> get() = _clearBarcodeField

    fun onBarcodeFieldCleared() {
        _clearBarcodeField.value = false
    }

    private val _stockTransferScannedQTYTotalCount = MutableLiveData(0)
    val stockTransferScannedQTYTotalCount: LiveData<Int> get() = _stockTransferScannedQTYTotalCount

    private val _alertMessage = MutableLiveData<String?>()
    val alertMessage: LiveData<String?> get() = _alertMessage

    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    private val _showDeleteConfirmation = MutableLiveData<OutBoundStockModel.OutBoundStockListModel?>()
    val showDeleteConfirmation: LiveData<OutBoundStockModel.OutBoundStockListModel?> get() = _showDeleteConfirmation

    private val _navigateToPreview = MutableLiveData<OutBoundStockModel.OutboundPreviewNavArgs?>()
    val navigateToPreview: LiveData<OutBoundStockModel.OutboundPreviewNavArgs?> get() = _navigateToPreview

    init {
        tagListDict.clear()
        _allOutboundItems.value = mutableListOf()
        bindData()
        outBoundListData()
        try {
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateIn() {
        BaseViewModel.updateIn(
            onTagRead    = { tags -> tagReadEvent(tags) },
            onTrigger    = { pressed -> hhtTriggerEvent(pressed) },
            onStatus     = { event -> statusEvent(event) },
            onConnection = { connected -> readerConnectionEvent(connected) }
        )
    }

    fun updateOut() { BaseViewModel.updateOut() }

    fun updateBarcodeIn() {
        BaseViewModel.updateBarcodeIn { barcode, count, rssi -> barcodeEvent(barcode, count, rssi) }
    }

    fun updateBarcodeOut() { BaseViewModel.updateBarcodeOut() }

    fun navigateToSettingsPage() {
        if (isBusy) return
        isBusy = true
        _navigateToSettings.postValue(true)
    }

    fun onNavigateToSettingsHandled() { 
        _navigateToSettings.value = false 
        isBusy = false
    }
    
    fun onNavigateToPreviewHandled() { _navigateToPreview.value = null }
    fun onAlertShown() { _alertMessage.value = null }

    fun bindData() {
        try {
            val scanList = mutableListOf(
                ScanOptionModel(id = 1, title = "RFID"),
                ScanOptionModel(id = 2, title = "Barcode"),
                ScanOptionModel(id = 3, title = "Text")
            )
            scanList.first().updateSelected(true)
            _scanOptions.value = scanList
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "bindData error", ex)
        }
    }

    fun outBoundListData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    Settings.service.getOutboundList(Settings.storeId)
                }
                if (result != null && result.results != null && result.results.isNotEmpty()) {
                    _storeCodeList.value = result.results
                    selectedStoreCode = result.results.firstOrNull()
                } else {
                    _alertMessage.value = result?.error ?: "Unknown error"
                }
            } catch (ex: Exception) {
                Log.e("OUTBOUND", "outBoundListData error", ex)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun outboundStoreCodeSave() {
        try {
            if (selectedStoreCode?.storeCode == "Select Store Code") {
                _alertMessage.value = "Please Select Store Code"
                return
            }
            if (selectedStoreCode?.storeCode != null) {
                _isPickerVisible.value = false
            }
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "outboundStoreCodeSave error", ex)
        }
    }

    fun getLocationList(stCode: OutBoundStockModel.STOutBoundStockListModel) {
        try {
            val list = stCode.locationList?.toMutableList() ?: mutableListOf()
            _locationList.value = list
            _selectedLocation.value = list.firstOrNull()
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "getLocationList error", ex)
        }
    }

    fun outboundLocationSave() {
        try {
            if (selectedStoreCode?.storeCode == "Select Store Code") {
                _alertMessage.value = "Please Select Store Code"
                return
            }
            if (selectedLocation?.locationName == "Select Location") {
                _alertMessage.value = "Please Select Location"
                return
            }
            if ((selectedLocation?.id ?: 0) != 0) {
                _isLocationPickerVisible.value = false
            }
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "outboundLocationSave error", ex)
        }
    }

    fun scanOptionSetting(model: ScanOptionModel?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(100)
                if (model == null) return@launch
                val currentOptions = _scanOptions.value ?: return@launch
                val currentlySelected = currentOptions.find { it.isSelected }

                if (currentlySelected?.id != model.id) {
                    _productIDCode.value = ""

                    val updatedList = currentOptions.map { s ->
                        val isSelected = s.id == model.id
                        s.copy(isSelected = isSelected, checkImage = if (isSelected) "selected" else "unselected")
                    }.toMutableList()
                    _scanOptions.value = updatedList

                    when (model.id) {
                        1 -> {
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
                            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                            _isRFIDViewVisible.value = true
                            _isBarcodeViewVisible.value = false
                            _isTextBoxVisible.value = false
                        }
                        2 -> {
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true)
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false)
                            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                            _isBarcodeViewVisible.value = true
                            _isRFIDViewVisible.value = false
                            _isTextBoxVisible.value = false
                            activeOnFocus?.invoke(true)
                        }
                        3 -> {
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false)
                            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
                            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                            _isTextBoxVisible.value = true
                            _isBarcodeViewVisible.value = false
                            _isRFIDViewVisible.value = false
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.e("OUTBOUND", "scanOptionSetting error", ex)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @Synchronized
    fun tagReadEvent(aryTags: Array<TagData>) {
        synchronized(tagReadLock) {
            for (tagData in aryTags) {
                val tagID = tagData.tagID
                Log.d("OUTBOUND", "Tag received: $tagID")
                if (tagID != null && !tagListDict.containsKey(tagID)) {
                    updateList(tagID)
                }
                totalTagCount += tagData.tagSeenCount
                if (tagData.opCode == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                    tagData.opStatus == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                    tagData.memoryBankData.isNotEmpty()
                ) {
                    Log.d("OUTBOUND", "Mem Bank Data ${tagData.memoryBankData}")
                }
            }
        }
    }

    fun hhtTriggerEvent(pressed: Boolean) {
        if (pressed) {
            if (_isRFIDViewVisible.value == true) performInventory()
        } else {
            stopInventory()
        }
    }

    @Synchronized
    private fun stopInventory() { BaseViewModel.rfidModel.stopInventory() }

    @Synchronized
    private fun performInventory() {
        totalTagCount = 0
        BaseViewModel.rfidModel.performInventory()
    }

    @Synchronized
    fun statusEvent(statusEvent: IEvents.StatusEventData) {
        if (statusEvent.statusEventType == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
            Log.d("OUTBOUND", "Inventory started")
        }
        if (statusEvent.statusEventType == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
            Log.d("OUTBOUND", "Inventory stopped. Unique tags: ${tagListDict.size}")
        }
    }

    private fun updateList(tag: String) {
        try {
            val resolvedTag = extractGTINFromEPC(tag)
            Log.d("OUTBOUND", "updateList: raw=$tag  resolved=$resolvedTag")
            if (resolvedTag != null) {
                val tagWithoutIndicator = resolvedTag.substring(1)
                tagListDict[tag] = tagWithoutIndicator
                updateScanQTY(tagWithoutIndicator)
            }
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "updateList error tag=$tag", ex)
        }
    }

    private fun hexToBinary(hex: String): String {
        return hex.chunked(1).joinToString("") { hexChar ->
            Integer.toBinaryString(hexChar.toInt(16)).padStart(4, '0')
        }
    }

    private fun extractGTINFromEPC(tagID: String): String? {
        return try {
            val binary = hexToBinary(tagID)
            if (binary.length < 96) return null

            val partition = binary.substring(11, 14).toInt(2)
            val (cpBits, cpDig, irBits, irDig) = when (partition) {
                0 -> arrayOf(40, 12, 4,  1)
                1 -> arrayOf(37, 11, 7,  2)
                2 -> arrayOf(34, 10, 10, 3)
                3 -> arrayOf(30, 9,  14, 4)
                4 -> arrayOf(27, 8,  17, 5)
                5 -> arrayOf(24, 7,  20, 6)
                6 -> arrayOf(20, 6,  24, 7)
                else -> return null
            }

            val companyPrefix = binary.substring(14, 14 + cpBits).toLong(2).toString().padStart(cpDig, '0')
            val itemRefWithIndicator = binary.substring(14 + cpBits, 14 + cpBits + irBits).toLong(2).toString().padStart(irDig, '0')

            val indicator = itemRefWithIndicator.substring(0, 1)
            val itemRefDigits = itemRefWithIndicator.substring(1)

            val gtin13 = "$indicator$companyPrefix$itemRefDigits"
            val checkDigit = calculateGTINCheckDigit(gtin13)
            val finalGtin = "$gtin13$checkDigit"

            Log.d("OUTBOUND", "extractGTINFromEPC: gtin=$finalGtin")
            return finalGtin
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "extractGTINFromEPC error", ex)
            null
        }
    }
    private fun calculateGTINCheckDigit(gtin13: String): Int {
        var sum = 0
        for (i in gtin13.indices) {
            val digit = gtin13[i].digitToInt()
            sum += if (i % 2 == 0) digit * 3 else digit
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    fun barcodeEvent(barcode: String, barcodeCount: Int, barcodeRssi: Short) {
        Log.d("OUTBOUND", "barcodeEvent: $barcode")
        viewModelScope.launch(Dispatchers.Main) {
            updateScanQTY(barcode)
        }
    }

    fun readerConnectionEvent(connection: Boolean) {
        BaseViewModel.isConnected = connection
        Log.d("OUTBOUND", "Reader connection: $connection")
    }

    fun addInboundItem() {
        try {
            val code = productIDCode
            if (code.isNotEmpty()) {
                Log.d("OUTBOUND", "addInboundItem: $code")
                updateScanQTY(code)
                productIDCode = ""
            }
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "addInboundItem error", ex)
        }
    }

    fun addBarcodes(code: String) {
        try {
            if (code.isNotEmpty()) {
                Log.d("OUTBOUND", "addBarcodes: $code")
                updateScanQTY(code)
            }
            _clearBarcodeField.postValue(true)
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "addBarcodes error", ex)
        }
    }

    fun updateScanQTY(gtin: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (gtin.isEmpty()) return@launch

                Log.d("OUTBOUND", "updateScanQTY: gtin=$gtin  prefixListSize=${Settings.prefixGTINList.size}")
                
                val matchingPrefix = Settings.prefixGTINList.find { it.name != null && gtin.startsWith(it.name!!) }
                
                if (matchingPrefix != null) {
                    val tempList = _allOutboundItems.value?.toMutableList() ?: mutableListOf()
                    val existingItem = tempList.find { it.globalTradeItemNumber == gtin }
                    if (existingItem != null) {
                        existingItem.scannedQTY += 1
                        Log.d("OUTBOUND", "updateScanQTY: incremented $gtin -> ${existingItem.scannedQTY}")
                    } else {
                        tempList.add(
                            OutBoundStockModel.OutBoundStockListModel(
                                globalTradeItemNumber = gtin,
                                actualQuantityDelivered = 0,
                                scannedQTY = 1,
                                isDelTag = true
                            )
                        )
                        Log.d("OUTBOUND", "updateScanQTY: added $gtin  list size=${tempList.size}")
                    }
                    _allOutboundItems.value = tempList
                    _stockTransferScannedQTYTotalCount.value = tempList.sumOf { it.scannedQTY }
                } else {
                    Log.w("OUTBOUND", "updateScanQTY: no prefix matched for gtin=$gtin")
                }
            } catch (ex: Exception) {
                Log.e("OUTBOUND", "updateScanQTY error gtin=$gtin", ex)
            }
        }
    }

    fun stockTransferScanTotalCount() {
        try {
            _stockTransferScannedQTYTotalCount.postValue(
                _allOutboundItems.value?.sumOf { it.scannedQTY } ?: 0
            )
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "stockTransferScanTotalCount error", ex)
        }
    }

    fun deleteTag(selectedItem: OutBoundStockModel.OutBoundStockListModel) {
        _showDeleteConfirmation.value = selectedItem
    }

    fun onDismissDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }

    fun confirmDeleteTag(selectedItem: OutBoundStockModel.OutBoundStockListModel) {
        try {
            val tempList = _allOutboundItems.value?.toMutableList() ?: return
            tempList.remove(selectedItem)
            _allOutboundItems.value = tempList
            
            tagListDict.entries.removeIf { it.value == selectedItem.globalTradeItemNumber }
            
            stockTransferScanTotalCount()
            _showDeleteConfirmation.value = null
            Log.d("OUTBOUND", "deleteTag: removed ${selectedItem.globalTradeItemNumber}")
        } catch (ex: Exception) {
            Log.e("OUTBOUND", "deleteTag error", ex)
        }
    }

    fun nextOutboundPreviewList() {
        viewModelScope.launch {
            try {
                if (selectedStoreCode?.storeCode == "Select Store Code") {
                    _alertMessage.postValue("Please Check Store Code")
                    return@launch
                }
                if (selectedLocation?.locationName == "Select Location") {
                    _alertMessage.postValue("Please Check Location!")
                    return@launch
                }
                if (_isLocationPickerVisible.value == true) {
                    _alertMessage.postValue("Please Save Store Code/Location!")
                    return@launch
                }
                val items = _allOutboundItems.value ?: emptyList()
                if (items.isEmpty()) {
                    _alertMessage.postValue("Please Check List Data")
                    return@launch
                }
                
                _isLoading.value = true
                val toStoreCode = selectedStoreCode?.storeCode ?: ""
                val combinedString = items.joinToString(" or ") { "GTINCode eq '${it.globalTradeItemNumber}'" }
                val result = withContext(Dispatchers.IO) {
                    Settings.service.getSOHStockTakePendingList(Settings.storeId, combinedString)
                }
                if (result != null && result.d?.results?.isNotEmpty() == true) {
                    _navigateToPreview.postValue(
                        OutBoundStockModel.OutboundPreviewNavArgs(
                            allOutboundItems = items.toList(),
                            sohResult = result,
                            toStoreCode = toStoreCode,
                            locationId = selectedLocation?.id ?: 0
                        )
                    )
                } else {
                    _alertMessage.postValue("SAP API down or please check the products in the list.")
                }
            } catch (ex: Exception) {
                Log.e("OUTBOUND", "nextOutboundPreviewList error", ex)
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
