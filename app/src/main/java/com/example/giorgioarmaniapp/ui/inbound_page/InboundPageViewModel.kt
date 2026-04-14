package com.example.giorgioarmaniapp.ui.login_page.inbound_page

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.helper.base.Settings.prefixGTINList
import com.example.giorgioarmaniapp.helper.isInternetAvailable
import com.example.giorgioarmaniapp.models.InboundPendingListModel
import com.example.giorgioarmaniapp.models.TagItem
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.example.giorgioarmaniapp.ui.login_page.BaseViewModel
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.IEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import com.zebra.rfid.api3.TagData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Timer
import java.util.TimerTask


class InboundPageViewModel : ViewModel() {

    private val _newAllItems =
        MutableLiveData<MutableList<InboundPendingListModel.InboundPendingModel>>(mutableListOf())
    val newAllItems: LiveData<MutableList<InboundPendingListModel.InboundPendingModel>>
        get() = _newAllItems

    private val _selectedProductTag =
        MutableLiveData<InboundPendingListModel.InboundPendingModel?>()
    val selectedProductTag: LiveData<InboundPendingListModel.InboundPendingModel?>
        get() = _selectedProductTag

    // allItems holds raw TagItem objects (RFID/Barcode raw reads)
    private val allItems: MutableList<TagItem> = mutableListOf()

    private val tagListDict: MutableMap<String, String> = mutableMapOf()

    private val _listAvailable = MutableLiveData(false)
    val listAvailable: LiveData<Boolean> get() = _listAvailable

    private var startTime: Date = Date()
    private var totalTagCount = 0

    var activeOnFocus: ((Boolean) -> Unit)? = null

    private val _uniqueTags = MutableLiveData("0")
    val uniqueTags: LiveData<String> get() = _uniqueTags

    private val _totalTags = MutableLiveData("0")
    val totalTags: LiveData<String> get() = _totalTags

    private val _totalTime = MutableLiveData("00:00:00")
    val totalTime: LiveData<String> get() = _totalTime

    private val _readerConnection = MutableLiveData<String>()
    val readerConnection: LiveData<String> get() = _readerConnection

    private val _readerStatus = MutableLiveData<String>()
    val readerStatus: LiveData<String> get() = _readerStatus

    private var aTimer: Timer? = null
    private val tagReadLock = Any()

    var isBusy: Boolean = false

    private val _productIDCode = MutableLiveData("")
    var productIDCode: String
        get() = _productIDCode.value ?: ""
        set(value) { _productIDCode.value = value }

    private val _barcodeOrProductcode = MutableLiveData("")
    var barcodeOrProductcode: String
        get() = _barcodeOrProductcode.value ?: ""
        set(value) {
            _barcodeOrProductcode.value = value
            if (value.isNotEmpty()) addBarcodes(value)
        }

    private val _isTextBoxVisible = MutableLiveData(false)
    val isTextBoxVisible: LiveData<Boolean> get() = _isTextBoxVisible

    private val _isRFIDViewVisible = MutableLiveData(true)
    val isRFIDViewVisible: LiveData<Boolean> get() = _isRFIDViewVisible

    private val _isBarcodeViewVisible = MutableLiveData(false)
    val isBarcodeViewVisible: LiveData<Boolean> get() = _isBarcodeViewVisible

    private val _scanOptions =
        MutableLiveData<MutableList<ScanOptionModel>>(mutableListOf())
    val scanOptions: LiveData<MutableList<ScanOptionModel>> get() = _scanOptions

    private val _pendingInboundData =
        MutableLiveData<InboundPendingListModel.InboundPendingListResult?>()
    var pendingInboundData: InboundPendingListModel.InboundPendingListResult?
        get() = _pendingInboundData.value
        set(value) {
            _pendingInboundData.value = value
            if (value != null) {
                _newAllItems.value = value.itemList?.toMutableList() ?: mutableListOf()
            }
        }

    private val _inboundExpectedQTYTotalCount = MutableLiveData(0)
    val inboundExpectedQTYTotalCount: LiveData<Int> get() = _inboundExpectedQTYTotalCount

    private val _inboundScannedQTYTotalCount = MutableLiveData(0)
    val inboundScannedQTYTotalCount: LiveData<Int> get() = _inboundScannedQTYTotalCount

    private val _alertMessage = MutableLiveData<String>()
    val alertMessage: LiveData<String> get() = _alertMessage

    private val _submitSuccess = MutableLiveData(false)
    val submitSuccess: LiveData<Boolean> get() = _submitSuccess

    private val _navigateToSettings = MutableLiveData(false)
    val navigateToSettings: LiveData<Boolean> get() = _navigateToSettings

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        // Code added by Mitesh on 16/05/2023.
        // Dict was holding the old tag data due to which the same tags are not being read,
        // causing the issue in Inbound/Outbound/Stocktake
        tagListDict.clear()

        updateHints()
        bindData()

        try {
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            BaseViewModel.rfidModel.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
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

    fun updateOut() {
        BaseViewModel.updateOut()
    }
    fun updateBarcodeIn() {
        BaseViewModel.updateBarcodeIn { barcode, count, rssi ->
            barcodeEvent(barcode, count, rssi)
        }
    }

    fun updateBarcodeOut() {
        BaseViewModel.updateBarcodeOut()
    }

    fun barcodeEvent(barcode: String, barcodeCount: Int, barcodeRssi: Short) {
        synchronized(tagReadLock) { /* lock */ }
        viewModelScope.launch(Dispatchers.Main) {
            allItems.add(
                TagItem(
                    invID = barcode,
                    tagCount = barcodeCount,
                    rssi = barcodeRssi.toInt()
                )
            )
        }
    }

    @Synchronized
    fun tagReadEvent(aryTags: Array<TagData>) {
        synchronized(tagReadLock) {
            for (tagData in aryTags) {
                println("Tag ID ${tagData.tagID}")
                val tagID = tagData.tagID
                if (tagID != null) {
                    if (!tagListDict.containsKey(tagID)) {
                        updateList(tagID, tagData.tagSeenCount, tagData.peakRSSI)
                    }
                }

                if (tagData.opCode == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                    tagData.opStatus == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS
                ) {
                    if (tagData.memoryBankData.isNotEmpty()) {
                        println("Mem Bank Data ${tagData.memoryBankData}")
                    }
                }
            }
        }
    }

    private fun updateList(tag: String, count: Int, rssi: Short) {
        try {
            val resolvedTag = extractGTINFromEPC(tag)
            if (resolvedTag != null) {
                tagListDict[tag] = resolvedTag
                updateScanQTY(resolvedTag)
            }
        } catch (ex: Exception) {
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

    fun hhtTriggerEvent(pressed: Boolean) {
        if (pressed) {
            if (_isRFIDViewVisible.value == true) {
                performInventory()
                _listAvailable.postValue(true)
            }
        } else {
            stopInventory()
        }
    }

    @Synchronized
    private fun stopInventory() {
        BaseViewModel.rfidModel.stopInventory()
        aTimer?.cancel()
        aTimer = null
    }

    @Synchronized
    private fun performInventory() {
        totalTagCount = 0
        startTime = Date()
        setTimer()
        BaseViewModel.rfidModel.performInventory()
    }

    @Synchronized
    fun statusEvent(statusEvent: IEvents.StatusEventData) {
        if (statusEvent.statusEventType == STATUS_EVENT_TYPE.INVENTORY_START_EVENT) {
            // startTime = Date()
        }
        if (statusEvent.statusEventType == STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT) {
            updateCounts()
        }
    }

    private fun updateCounts() {
        viewModelScope.launch(Dispatchers.Main) {
            _uniqueTags.value = tagListDict.size.toString()
            _totalTags.value = totalTagCount.toString()
            val BraxtonMs = Date().time - startTime.time
            val seconds = (BraxtonMs / 1000) % 60
            val minutes = (BraxtonMs / (1000 * 60)) % 60
            val hours = (BraxtonMs / (1000 * 60 * 60)) % 24
            _totalTime.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    private fun setTimer() {
        aTimer?.cancel()
        aTimer = Timer()
        aTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { updateCounts() }
        }, 1000L, 1000L)
    }

    fun readerConnectionEvent(connection: Boolean) {
        BaseViewModel.isConnected = connection
        updateHints()
        aTimer?.cancel()
        aTimer = null
    }

    private fun updateHints() {
        if (allItems.isEmpty()) {
            _listAvailable.postValue(false)
            _readerConnection.postValue(
                if (BaseViewModel.isConnected) "Connected" else "Not connected"
            )
            if (BaseViewModel.isConnected) {
                _readerStatus.postValue(
                    if (BaseViewModel.rfidModel.isBatchMode)
                        "Inventory is running in batch mode"
                    else
                        "Press and hold the trigger for tag reading"
                )
            }
        } else {
            _listAvailable.postValue(true)
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

    fun bindData() {
        try {
            val scanList = mutableListOf(
                ScanOptionModel(id = 1, title = "RFID", isSelected = true, checkImage = "selected"),
                ScanOptionModel(id = 2, title = "Barcode", isSelected = false, checkImage = "unselected"),
                ScanOptionModel(id = 3, title = "Text", isSelected = false, checkImage = "unselected")
            )
            _scanOptions.value = scanList

            BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
        } catch (ex: Exception) {
        }
    }
    fun addInboundItem() {
        try {
            updateScanQTY(productIDCode)
            productIDCode = ""
        } catch (ex: Exception)
        {

        }
    }

    fun addBarcodes(code: String) {
        try {
            if (_newAllItems.value != null) {
                updateScanQTY(code)
            }
            _barcodeOrProductcode.value = ""
        } catch (ex: Exception) {

        }
    }

    fun updateScanQTY(gtin: String) {
        try {
            for (item in prefixGTINList) {
                val itemName = item.name ?: continue
                if (gtin.startsWith(itemName)) {
                    if (gtin.isEmpty()) {
                        return
                    }

                    val tempList = _newAllItems.value?.toMutableList() ?: mutableListOf()

                    if (tempList.any { it.globalTradeItemNumber?.contains(gtin) == true }) {
                        for (tempGTIN in tempList.filter { it.globalTradeItemNumber == gtin }) {
                            tempGTIN.scannedQTY += 1

                            if (tempGTIN.deliveryItem == "") {
                                tempGTIN.actualQuantityDelivered = tempGTIN.scannedQTY
                                tempGTIN.isDelTag = true
                                tempGTIN.isInvalidCount = false
                            } else if (tempGTIN.scannedQTY > tempGTIN.actualQuantityDelivered) {
                                tempGTIN.isDelTag = false
                                tempGTIN.isInvalidCount = true
                            }
                        }
                        _newAllItems.postValue(tempList)
                    } else {
                        tempList.add(
                            InboundPendingListModel.InboundPendingModel(
                                globalTradeItemNumber = gtin,
                                actualQuantityDelivered = 1,
                                scannedQTY = 1,
                                deliveryItem = "",
                                articleNumber = "",
                                isDelTag = true,
                                isInvalidCount = false,
                                invalidGTINNumberCLR = "#FF0000" // Red
                            )
                        )
                        _newAllItems.postValue(tempList)
                    }

                    inboundScanTotalCount()
                }
            }
        } catch (ex: Exception) {

        }
    }

    fun scanOptionSetting(model: ScanOptionModel?) {
        viewModelScope.launch {
            try {
                if (model == null) return@launch

                val updatedList = _scanOptions.value?.map { s ->
                    val isSelected = s.id == model.id
                    s.copy(isSelected = isSelected, checkImage = if (isSelected) "selected" else "unselected")
                }?.toMutableList()

                if (updatedList != null) {
                    val selected = updatedList.find { it.isSelected }
                    if (selected != null) {
                        when (selected.id) {
                            1 -> {
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
                                BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                                _isRFIDViewVisible.postValue(true)
                                _isBarcodeViewVisible.postValue(false)
                                _isTextBoxVisible.postValue(false)
                            }
                            2 -> {
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true)
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false)
                                BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                                _isBarcodeViewVisible.postValue(true)
                                _isRFIDViewVisible.postValue(false)
                                _isTextBoxVisible.postValue(false)
                                activeOnFocus?.invoke(true)
                            }
                            3 -> {
                                _isTextBoxVisible.postValue(true)
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, false)
                                BaseViewModel.rfidModel.rfidReader?.Config
                                    ?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
                                BaseViewModel.rfidModel.rfidReader?.Config?.saveConfig()
                                _isBarcodeViewVisible.postValue(false)
                                _isRFIDViewVisible.postValue(false)
                            }
                        }
                    }
                }

                _scanOptions.postValue(updatedList ?: mutableListOf())
            } catch (ex: Exception) {

            }
        }
    }

    fun saveListData(context: Context) {
        viewModelScope.launch {
            try {
                if (!isInternetAvailable(context)) {
                    _alertMessage.postValue("No Internet Connection")
                    return@launch
                }
                var tempMore = ""
                var tempLess = ""

                for (item in _newAllItems.value ?: emptyList()) {
                    when {
                        item.scannedQTY > item.actualQuantityDelivered ->
                            tempMore += " ${item.globalTradeItemNumber}"
                        item.scannedQTY < item.actualQuantityDelivered ->
                            tempLess += " ${item.globalTradeItemNumber}"
                    }
                }

                val isDelTagItems = _newAllItems.value?.filter { it.isDelTag } ?: emptyList()
                if (isDelTagItems.isNotEmpty()) {
                    _alertMessage.postValue("Please Delete extra Items Before Submitting")
                    return@launch
                }

                val deliveryNumber = pendingInboundData?.deliveryNumber
                val userId = Settings.userId.toString()

                if (deliveryNumber.isNullOrEmpty() && userId.isEmpty()) {
                    _alertMessage.postValue("Please Check Data")
                    return@launch
                }

                if (tempLess.isNotEmpty() && tempMore.isNotEmpty()) {
                    _alertMessage.postValue(
                        "$tempLess\n scanned quantity/s is less than expected quantity\n" +
                                "$tempMore\n scanned quantity/s is more than expected quantity" +
                                "\nPlease Contact Procurement Team"
                    )
                    return@launch
                }

                if (tempLess.isNotEmpty()) {
                    _alertMessage.postValue(
                        "$tempLess\n scanned quantity/s is less than expected quantity" +
                                "\nPlease Contact Procurement Team"
                    )
                    return@launch
                }

                if (tempMore.isNotEmpty()) {
                    _alertMessage.postValue(
                        "$tempMore\n scanned quantity/s is more than expected quantity" +
                                "\nPlease Contact Procurement Team"
                    )
                    return@launch
                }

                val tempList = InboundPendingListModel.InboundPendingListResult(
                    deliveryNumber = deliveryNumber ?: "",
                    outboundNumber = pendingInboundData?.outboundNumber ?: "",
                    itemList = _newAllItems.value?.toList() ?: emptyList()
                )

                _isLoading.value = true
                withContext(Dispatchers.IO) {
                    val response = Settings.service.submitInboundList(userId, tempList)
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        if (response?.success == "Successfully Submitted") {
                            _submitSuccess.value = true
                            _newAllItems.value = mutableListOf()
                        }
                    }
                }
            } catch (ex: Exception) {
                _isLoading.value = false
            }
        }
    }
    fun onSubmitSuccessHandled() {
        _submitSuccess.value = false
    }


    fun makeEqualQTYTag(selectedItem: InboundPendingListModel.InboundPendingModel) {
        try {
            val tempList = _newAllItems.value?.toMutableList() ?: return
            val index = tempList.indexOf(selectedItem)
            if (index >= 0) {
                val updated = tempList[index].copy(
                    scannedQTY = tempList[index].actualQuantityDelivered,
                    isInvalidCount = false
                )
                tempList.removeAt(index)
                tempList.add(updated)
                _newAllItems.value = tempList
            }
            inboundScanTotalCount()
        } catch (ex: Exception) {
            // silent catch
        }
    }

    fun deleteTag(selectedItem: InboundPendingListModel.InboundPendingModel) {
        try {
            val tempList = _newAllItems.value?.toMutableList() ?: return
            tempList.remove(selectedItem)
            _newAllItems.value = tempList

            val keysToRemove = tagListDict.entries
                .filter { it.value == selectedItem.globalTradeItemNumber }
                .map { it.key }
            keysToRemove.forEach { tagListDict.remove(it) }

            inboundScanTotalCount()
        } catch (ex: Exception) {

        }
    }



    fun inboundScanTotalCount() {
        try {
            val list = _newAllItems.value ?: return
            _inboundExpectedQTYTotalCount.postValue(list.sumOf { it.actualQuantityDelivered })
            _inboundScannedQTYTotalCount.postValue(list.sumOf { it.scannedQTY })
        } catch (ex: Exception) {

        }
    }



    override fun onCleared() {
        super.onCleared()
        aTimer?.cancel()
        aTimer = null
    }
}
