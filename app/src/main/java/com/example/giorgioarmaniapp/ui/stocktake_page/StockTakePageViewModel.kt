package com.example.giorgioarmaniapp.ui.login_page.stocktake_page

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giorgioarmaniapp.helper.base.Settings
import com.example.giorgioarmaniapp.models.StockTakeModel
import com.example.giorgioarmaniapp.models.statics.ScanOptionModel
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.TagData
import com.example.giorgioarmaniapp.models.*
import com.example.giorgioarmaniapp.service.RestService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask
import kotlin.math.ceil
import kotlin.math.min

class StockTakePageViewModel : ViewModel() {

    companion object {
        private const val TAG = "StockTakePageVM"
        private val tagListDict = HashMap<String, String>()
    }

    private val _scanOptions = MutableLiveData<List<ScanOptionModel>>(emptyList())
    val scanOptions: LiveData<List<ScanOptionModel>> = _scanOptions

    private val _isRFIDViewVisible = MutableLiveData(true)
    val isRFIDViewVisible: LiveData<Boolean> = _isRFIDViewVisible

    private val _isBarcodeViewVisible = MutableLiveData(false)
    val isBarcodeViewVisible: LiveData<Boolean> = _isBarcodeViewVisible

    private val _isTextBoxVisible = MutableLiveData(false)
    val isTextBoxVisible: LiveData<Boolean> = _isTextBoxVisible

    private val _isListAvailable = MutableLiveData(false)
    val isListAvailable: LiveData<Boolean> = _isListAvailable

    private val _isNotFound = MutableLiveData(false)
    val isNotFound: LiveData<Boolean> = _isNotFound

    private val _stockTakeScannedItems = MutableLiveData<MutableList<StockTakeModel.StockTakeListModel>>(mutableListOf())
    val stockTakeScannedItems: LiveData<MutableList<StockTakeModel.StockTakeListModel>> = _stockTakeScannedItems

    private val _myStockTakeScannedItems = MutableLiveData<List<StockTakeModel.StockTakeListModel>>(emptyList())
    val myStockTakeScannedItems: LiveData<List<StockTakeModel.StockTakeListModel>> = _myStockTakeScannedItems

    private val _expectedQTYTotalCount = MutableLiveData(0)
    val expectedQTYTotalCount: LiveData<Int> = _expectedQTYTotalCount

    private val _scannedQTYTotalCount = MutableLiveData(0)
    val scannedQTYTotalCount: LiveData<Int> = _scannedQTYTotalCount

    private val _uniqueTags = MutableLiveData("0")
    val uniqueTags: LiveData<String> = _uniqueTags

    private val _totalTags = MutableLiveData("0")
    val totalTags: LiveData<String> = _totalTags

    private val _totalTime = MutableLiveData("00:00:00")
    val totalTime: LiveData<String> = _totalTime

    private val _readerConnection = MutableLiveData("")
    val readerConnection: LiveData<String> = _readerConnection

    private val _readerStatus = MutableLiveData("")
    val readerStatus: LiveData<String> = _readerStatus

    private val _stockTakeSearchText = MutableLiveData("")
    val stockTakeSearchText: LiveData<String> = _stockTakeSearchText

    var selectedGenderText: String = ""
    var selectedCategoryText: String = ""
    var selectedBrandText: String = ""

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack

    private val _showLoading = MutableLiveData(false)
    val showLoading: LiveData<Boolean> = _showLoading
    private var startTime: Long = 0L
    private var totalTagCount = 0
    private val tagReadLock = Any()
    private var timer: Timer? = null
    private var rfidModel: ReaderModel? = null
    private var isBatchMode: Boolean = false
    private var isConnected: Boolean = false

    private var restservice = RestService()

    init {
        tagListDict.clear()
        bindScanOptions()
    }

    fun initReader(model: ReaderModel?, batchMode: Boolean, connected: Boolean) {
        rfidModel = model
        isBatchMode = batchMode
        isConnected = connected
        try {
            model?.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
            model?.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, false)
        } catch (e: Exception) {
            Log.e(TAG, "initReader: ${e.message}")
        }
        updateHints()
    }

    private fun bindScanOptions() {
        val list = listOf(
            ScanOptionModel(id = 1, title = "RFID",    isSelected = true),
            ScanOptionModel(id = 2, title = "Barcode", isSelected = false),
            ScanOptionModel(id = 3, title = "Text",    isSelected = false)
        )
        _scanOptions.value = list
    }

    fun onScanOptionSelected(model: ScanOptionModel) {
        viewModelScope.launch {
            val updated = _scanOptions.value?.map { option ->
                option.copy(isSelected = option.id == model.id)
            } ?: return@launch
            _scanOptions.value = updated

            when (model.id) {
                1 -> {
                    setReaderMode(rfid = true, barcode = false)
                    _isRFIDViewVisible.value = true
                    _isBarcodeViewVisible.value = false
                    _isTextBoxVisible.value = false
                }
                2 -> {
                    setReaderMode(rfid = false, barcode = true)
                    _isRFIDViewVisible.value = false
                    _isBarcodeViewVisible.value = true
                    _isTextBoxVisible.value = false
                }
                3 -> {
                    setReaderMode(rfid = false, barcode = false)
                    _isRFIDViewVisible.value = false
                    _isBarcodeViewVisible.value = false
                    _isTextBoxVisible.value = true
                }
            }
        }
    }

    private fun setReaderMode(rfid: Boolean, barcode: Boolean) {
        try {
            rfidModel?.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,    rfid)
            rfidModel?.rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, barcode)
            rfidModel?.rfidReader?.Config?.saveConfig()
        } catch (e: Exception) {
            Log.e(TAG, "setReaderMode: ${e.message}")
        }
    }

    fun onTriggerPressed() {
        if (_isRFIDViewVisible.value == true) {
            performInventory()
            _isListAvailable.value = true
        }
    }

    fun onTriggerReleased() = stopInventory()

    @Synchronized
    private fun performInventory() {
        totalTagCount = 0
        startTime = System.currentTimeMillis()
        startTimer()
        try { rfidModel?.performInventory() } catch (e: Exception) { Log.e(TAG, "performInventory: ${e.message}") }
    }

    @Synchronized
    private fun stopInventory() {
        try { rfidModel?.stopInventory() } catch (e: Exception) { Log.e(TAG, "stopInventory: ${e.message}") }
        stopTimer()
    }

    @Synchronized
    fun onTagRead(tags: Array<TagData>) {
        synchronized(tagReadLock) {
            for (tag in tags) {
                val tagID = tag.tagID ?: continue
                if (!tagListDict.containsKey(tagID)) {
                    updateList(tagID, tag.tagSeenCount, tag.peakRSSI)
                }
                if (tag.opCode == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                    tag.opStatus == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                    tag.memoryBankData?.isNotEmpty() == true) {
                    Log.d(TAG, "MemBank: ${tag.memoryBankData}")
                }
            }
        }
    }

    private fun updateList(rawTag: String, count: Int, rssi: Short) {
        try {
            val resolvedTag = extractGTINFromEPC(rawTag)
            if (resolvedTag != null) {
                tagListDict[rawTag] = resolvedTag
                updateScanQTY(resolvedTag)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateList: ${e.message}")
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
            updateCounts()
            Log.d(TAG, "Unique: ${tagListDict.size}")
        }
    }

    fun onBarcodeScanned(code: String) {
        if (code.isNotEmpty()) updateScanQTY(code)
    }

    fun onAddManualItem(productCode: String) {
        if (productCode.isNotEmpty()) updateScanQTY(productCode)
    }

    private fun updateScanQTY(gtin: String) {
        val prefixList = Settings.prefixGTINList
        for (item in prefixList) {
            if (item.name != null && gtin.startsWith(item.name)) {
                val currentList = _stockTakeScannedItems.value ?: mutableListOf()
                val matches = currentList.filter { it.globalTradeItemNumber == gtin }
                if (matches.isNotEmpty()) {
                    for (match in matches) {
                        match.scannedQTY += 1
                        if (match.isDelTag) match.sohQuantity = match.scannedQTY
                    }
                    _stockTakeScannedItems.postValue(currentList)
                }
                dataTotalCount()
                break
            }
        }
    }

    fun deleteTag(item: StockTakeModel.StockTakeListModel) {
        val list = _stockTakeScannedItems.value ?: return
        list.remove(item)
        tagListDict.entries.removeAll { it.value == item.globalTradeItemNumber }
        _stockTakeScannedItems.value = list
        dataTotalCount()
    }

    fun submitStockTakeList() {
        viewModelScope.launch {
            if (Settings.storeId.isNullOrEmpty() && Settings.userId.toString().isEmpty()) {
                _errorMessage.value = "Please Check Data"
                return@launch
            }
            val allItems = _stockTakeScannedItems.value ?: return@launch
            val batchSize   = 200
            val totalBatches = ceil(allItems.size.toDouble() / batchSize).toInt()
            var isError = false
            var strError = ""

            _showLoading.value = true
            try {
                for (i in 0 until totalBatches) {
                    val startIndex   = i * batchSize
                    val currentSize  = min(batchSize, allItems.size - startIndex)
                    val batch        = allItems.subList(startIndex, startIndex + currentSize)

                    val payload = StockTakeModel.StockTakeListResult(
                        storeCode = Settings.storeId,
                        itemList = batch,
                        isCompleted = (i == totalBatches - 1)
                    )

                    val response = withContext(Dispatchers.IO) {
                        restservice.submitStockTakeList(Settings.userId.toString(), payload)
                    }

                    if (response?.success == "Successfully Submitted") {
                        Log.d(TAG, "Batch ${i + 1} submitted (${batch.size} items)")
                        delay(500)
                    } else {
                        isError  = true
                        strError = "Batch ${i + 1} failed: ${response?.error ?: "Unknown error"}"
                        break
                    }
                }

                if (isError) {
                    _errorMessage.value = strError
                } else {
                    _successMessage.value = "Data are Successfully Submitted"
                    _navigateBack.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _showLoading.value = false
            }
        }
    }

    private fun updateHints() {
        val items = _stockTakeScannedItems.value
        if (items.isNullOrEmpty()) {
            _isListAvailable.value = false
            _readerConnection.value = if (isConnected) "Connected" else "Not connected"
            _readerStatus.value = when {
                !isConnected -> ""
                isBatchMode  -> "Inventory is running in batch mode"
                else         -> "Press and hold the trigger for tag reading"
            }
        } else {
            _isListAvailable.value = true
        }
    }

    private fun dataTotalCount() {
        val list = _stockTakeScannedItems.value ?: return
        _expectedQTYTotalCount.postValue(list.sumOf { it.sohQuantity })
        _scannedQTYTotalCount.postValue(list.sumOf { it.scannedQTY })
    }

    private fun updateCounts() {
        val elapsed = System.currentTimeMillis() - startTime
        val h  = (elapsed / 3_600_000)
        val m  = (elapsed % 3_600_000) / 60_000
        val s  = (elapsed % 60_000) / 1_000
        _uniqueTags.postValue(tagListDict.size.toString())
        _totalTags.postValue(totalTagCount.toString())
        _totalTime.postValue("%02d:%02d:%02d".format(h, m, s))
    }

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() { updateCounts() }
        }, 1000L, 1000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    fun clearNavigateBack() { _navigateBack.value = false }
    fun clearError()        { _errorMessage.value = null  }
    fun clearSuccess()      { _successMessage.value = null }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        tagListDict.clear()
    }
}
