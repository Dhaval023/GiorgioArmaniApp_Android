package com.example.giorgioarmaniapp.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.zebra.rfid.api3.*
import java.util.concurrent.Executors

class ReaderModel private constructor(context: Context) :
    Readers.RFIDReaderEventHandler,
    RfidEventsListener {

    var readersList: MutableList<ReaderDevice> = mutableListOf()
    var rfidReader: RFIDReader? = null
    private var readers: Readers? = null
    private var readerDevice: ReaderDevice? = null
    var isBatchMode = false

    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        private var instance: ReaderModel? = null

        fun getInstance(context: Context): ReaderModel {
            if (instance == null) {
                instance = ReaderModel(context)
            }
            return instance!!
        }
    }

    init {
        Readers.attach(this)
    }

    fun setup() {
        executor.execute {
            getAvailableReaders()
            connectReader(0)
        }
    }

    var tagRead: ((Array<TagData>) -> Unit)? = null
    var triggerEvent: ((Boolean) -> Unit)? = null
    var statusEvent: ((IEvents.StatusEventData) -> Unit)? = null
    var readerConnectionEvent: ((Boolean) -> Unit)? = null
    var readerAppearanceEvent: ((Boolean) -> Unit)? = null
    var barcodeEvent: ((String, Int, Short) -> Unit)? = null

    val isConnected: Boolean
        get() = try {
            rfidReader?.isConnected ?: false
        } catch (e: Exception) {
            false
        }
    fun getAvailableReaders(): List<ReaderDevice> {
        readersList.clear()

        try {
            readers = Readers(appContext, ENUM_TRANSPORT.SERVICE_SERIAL)
            readersList = readers!!.GetAvailableRFIDReaderList()
        } catch (e: Exception) {
            try { readers?.Dispose() } catch (ex: Exception) { }
            readers = null
        }

        if (readersList.isEmpty()) {
            try { readers?.Dispose() } catch (ex: Exception) { }
            readers = Readers(appContext, ENUM_TRANSPORT.SERVICE_USB)
            readersList = readers!!.GetAvailableRFIDReaderList()
        }

        if (readersList.isEmpty()) {
            try { readers?.Dispose() } catch (ex: Exception) { }
            readers = Readers(appContext, ENUM_TRANSPORT.BLUETOOTH)
            readersList = readers!!.GetAvailableRFIDReaderList()
        }

        return readersList
    }

    fun connectReader(index: Int) {
        executor.execute {
            connectReaderSync(index)
        }
    }
    @Synchronized
    fun connectReaderSync(index: Int) {
        try {
            isBatchMode = false
            Log.d("RFID", "Available readers: ${readersList.size}")

            if (readersList.isNotEmpty() && index < readersList.size) {
                readerDevice = readersList[index]
                rfidReader   = readerDevice!!.rfidReader

                rfidReader?.connect()
                configureReader()

                readerConnectionEvent?.invoke(true)
                Log.d("RFID", "Connected ${rfidReader?.hostName}")
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            readerConnectionEvent?.invoke(false)
            e.printStackTrace()
            showAlert("Connection failed\n${e.results}")
            Log.d("RFID", e.statusDescription ?: "")
            if (e.results == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                isBatchMode = true
            }
        }
    }

    @Synchronized
    fun configureReader() {
        val reader = rfidReader ?: return

        if (reader.isConnected) {
            try {
                reader.Events.addEventsListener(this)

                reader.Events.setHandheldEvent(true)

                reader.Events.setTagReadEvent(true)
                reader.Events.setAttachTagDataWithReadEvent(false)
                reader.Events.setInventoryStartEvent(true)
                reader.Events.setInventoryStopEvent(true)
                reader.Events.setOperationEndSummaryEvent(true)
                reader.Events.setReaderDisconnectEvent(true)
                reader.Events.setBatteryEvent(true)
                reader.Events.setPowerEvent(true)
                reader.Events.setTemperatureAlarmEvent(true)
                reader.Events.setBufferFullEvent(true)
                reader.Events.setBufferFullWarningEvent(true)

                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true)

                reader.Config.saveConfig()

                val singulation = reader.Config.Antennas.getSingulationControl(1)
                singulation.session = SESSION.SESSION_S0
                singulation.Action.inventoryState = INVENTORY_STATE.INVENTORY_STATE_A
                singulation.Action.setPerformStateAwareSingulationAction(false)
                reader.Config.Antennas.setSingulationControl(1, singulation)

                val antennaRfConfig = reader.Config.Antennas.getAntennaRfConfig(1)
                antennaRfConfig.setrfModeTableIndex(0)
                antennaRfConfig.tari = 0
                antennaRfConfig.transmitPowerIndex = 270
                reader.Config.Antennas.setAntennaRfConfig(1, antennaRfConfig)

                val tagFields = arrayOf(TAG_FIELD.PEAK_RSSI, TAG_FIELD.TAG_SEEN_COUNT)
                reader.Config.tagStorageSettings.setTagFields(tagFields)

                if (reader.ReaderCapabilities.modelName.contains("RFD8500")) {
                    reader.Config.setBatchMode(BATCH_MODE.DISABLE)
                    reader.Config.dpoState = DYNAMIC_POWER_OPTIMIZATION.DISABLE
                    reader.Config.beeperVolume = BEEPER_VOLUME.HIGH_BEEP
                }

                val hostName     = reader.hostName
                val region       = reader.Config.regulatoryConfig.region
                val modelName    = reader.ReaderCapabilities.modelName
                val serialNumber = reader.ReaderCapabilities.serialNumber

                Log.d("RFID", "HostName:$hostName Region:$region ModelName:$modelName SerialNumber:$serialNumber")

                reader.Config.getDeviceStatus(true, true, true)

            } catch (e: InvalidUsageException) {
                e.printStackTrace()
            } catch (e: OperationFailureException) {
                e.printStackTrace()
                showAlert(e)
            }
        }
    }

    fun setTriggerMode() {
        try {
            if (rfidReader != null && rfidReader?.isConnected == false) {
                connectReader(0)
            }
        } catch (e: Exception) {
            Log.e("RFID", "RFID Reader Connection error")
        }

        if (isConnected) {
            executor.execute {
                try {
                    rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)
                } catch (e: OperationFailureException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setBarcodeTriggerMode() {
        try {
            if (rfidReader != null && rfidReader?.isConnected == false) {
                connectReader(0)
            }
        } catch (e: Exception) {
            Log.e("RFID", "RFID Reader Connection error")
        }

        if (isConnected) {
            executor.execute {
                try {
                    rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true)
                } catch (e: OperationFailureException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Synchronized
    fun performInventory(): Boolean {
        return try {
            rfidReader?.reinitTransport()
            rfidReader?.Actions?.Inventory?.perform()
            true
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
            false
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            showAlert(e)
            false
        }
    }
    @Synchronized
    fun stopInventory() {
        try {
            rfidReader?.Actions?.Inventory?.stop()
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            showAlert(e)
        }
    }
    @Synchronized
    fun locate(start: Boolean, tagPattern: String?, tagMask: String?) {
        try {
            if (start) {
                rfidReader?.Actions?.TagLocationing?.Perform(tagPattern, tagMask, null)
            } else {
                rfidReader?.Actions?.TagLocationing?.Stop()
            }
        } catch (e: InvalidUsageException) {
            e.printStackTrace()
        } catch (e: OperationFailureException) {
            e.printStackTrace()
            showAlert(e)
        }
    }
    @Synchronized
    fun disconnect() {
        Log.d("RFID", "Disconnect ${rfidReader?.hostName}")
        rfidReader?.let {
            try {
                it.disconnect()
                readerConnectionEvent?.invoke(false)
            } catch (e: InvalidUsageException) {
                e.printStackTrace()
                Log.d("RFID", e.info ?: "")
            }
        }
    }

    fun deInit() {
        try {
            Readers::class.java.methods.forEach { method ->
                Log.d("ZebraSDK_Readers", "Method: ${method.name}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            readers?.Dispose()
        } catch (e: Exception) {
            Log.e("RFID", "readers.Dispose() failed", e)
        }
        readers = null
    }


    override fun eventReadNotify(readEvent: RfidReadEvents?) {
        val tags = rfidReader?.Actions?.getReadTags(100)
        if (tags != null) {
            executor.execute { tagRead?.invoke(tags) }
        }
    }
    override fun eventStatusNotify(rfidStatusEvents: RfidStatusEvents?) {
        rfidStatusEvents ?: return

        val statusData = rfidStatusEvents.StatusEventData
        Log.d("RFID", "Status Notification: ${statusData.statusEventType}")

        when (statusData.statusEventType) {

            STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                val handheldEvent = statusData.HandheldTriggerEventData.handheldEvent
                if (handheldEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    executor.execute { triggerEvent?.invoke(true) }
                }
                if (handheldEvent == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    executor.execute { triggerEvent?.invoke(false) }
                }
            }

            STATUS_EVENT_TYPE.INVENTORY_START_EVENT -> {
                executor.execute { statusEvent?.invoke(statusData) }
            }

            STATUS_EVENT_TYPE.INVENTORY_STOP_EVENT -> {
                executor.execute { statusEvent?.invoke(statusData) }
            }

            STATUS_EVENT_TYPE.OPERATION_END_SUMMARY_EVENT -> {
                val summary = statusData.OperationEndSummaryData
                val rounds   = summary.totalRounds
                val totalTags = summary.totalTags
                val timeMs   = summary.totalTimeuS / 1000
                Log.d("RFID", "Summary: Rounds:$rounds Tags:$totalTags Time:$timeMs")
                executor.execute { statusEvent?.invoke(statusData) }
            }

            STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> {
                executor.execute { readerConnectionEvent?.invoke(false) }
                showAlert("Reader Disconnected")
            }

            STATUS_EVENT_TYPE.BATTERY_EVENT -> {
                val battery = statusData.BatteryData
                Log.d("RFID", "Battery: Cause:${battery.cause} Charging:${battery.charging} Level:${battery.level}")
            }

            STATUS_EVENT_TYPE.POWER_EVENT -> {
                val power = statusData.PowerData
                Log.d("RFID", "PowerData: Cause:${power.cause} Current:${power.current} Voltage:${power.voltage} Power:${power.power}")
            }

            STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT -> {
                val temp = statusData.TemperatureAlarmData
                Log.d("RFID", "TemperatureAlarmEvent: AlarmLevel:${temp.alarmLevel} AmbientTemp:${temp.ambientTemp}")
            }

            STATUS_EVENT_TYPE.BUFFER_FULL_WARNING_EVENT -> {
                Log.d("RFID", "BufferFullWarningEvent")
            }

            STATUS_EVENT_TYPE.BUFFER_FULL_EVENT -> {
                Log.d("RFID", "BufferFullEvent")
            }

            else -> {}
        }
    }
    override fun RFIDReaderAppeared(readerDevice: ReaderDevice?) {
        readerDevice?.let {
            readersList.add(it)
            readerAppearanceEvent?.invoke(true)
        }
    }
    override fun RFIDReaderDisappeared(readerDevice: ReaderDevice?) {
        readerDevice?.let {
            readersList.remove(it)
            readerAppearanceEvent?.invoke(false)
        }
    }
    private fun showAlert(e: OperationFailureException) {
        Log.e("RFID", e.vendorMessage ?: "OperationFailureException")
    }
    fun showAlert(message: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
        }
        Log.d("RFID", message)
    }
}