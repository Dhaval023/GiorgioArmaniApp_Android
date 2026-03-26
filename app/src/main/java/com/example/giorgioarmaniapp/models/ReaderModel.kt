package com.example.giorgioarmaniapp.models

import android.content.Context
import android.util.Log
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
        setup(context)
    }

    fun setup(context: Context) {
        executor.execute {
            getAvailableReaders(context)
            connectReader(0)
        }
    }

    // ================= EVENTS =================

    var tagRead: ((Array<TagData>) -> Unit)? = null
    var triggerEvent: ((Boolean) -> Unit)? = null
    var statusEvent: ((IEvents.StatusEventData) -> Unit)? = null
    var readerConnectionEvent: ((Boolean) -> Unit)? = null

    val isConnected: Boolean
        get() = try {
            rfidReader?.isConnected ?: false
        } catch (e: Exception) {
            false
        }

    fun getAvailableReaders(context: Context): List<ReaderDevice> {
        readersList.clear()

        try {
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_SERIAL)
            readersList = readers!!.GetAvailableRFIDReaderList()
        } catch (e: Exception) {
            readers?.Dispose()
            readers = null
        }

        if (readersList.isEmpty()) {
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_USB)
            readersList = readers!!.GetAvailableRFIDReaderList()
        }

        if (readersList.isEmpty()) {
            readers = Readers(context, ENUM_TRANSPORT.BLUETOOTH)
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
            if (readersList.isNotEmpty() && index < readersList.size) {
                readerDevice = readersList[index]
                rfidReader = readerDevice!!.rfidReader

                rfidReader?.connect()
                configureReader()

                readerConnectionEvent?.invoke(true)
                Log.d("RFID", "Connected ${rfidReader?.hostName}")
            }
        } catch (e: Exception) {
            readerConnectionEvent?.invoke(false)
            e.printStackTrace()
        }
    }

    @Synchronized
    fun configureReader() {
        val reader = rfidReader ?: return

        if (reader.isConnected) {
            try {
                reader.Events.addEventsListener(this)

                reader.Events.setTagReadEvent(true)
                reader.Events.setInventoryStartEvent(true)
                reader.Events.setInventoryStopEvent(true)

                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE, true)

                reader.Config.saveConfig()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun eventReadNotify(readEvent: RfidReadEvents?) {
        val tags = rfidReader?.Actions?.getReadTags(100)
        tags?.let {
            executor.execute { tagRead?.invoke(it) }
        }
    }

    override fun eventStatusNotify(statusEventData: RfidStatusEvents?) {
        statusEventData?.let {
            Log.d("RFID", "Status: ${it.StatusEventData.statusEventType}")
        }
    }

    override fun RFIDReaderAppeared(readerDevice: ReaderDevice?) {
        readerDevice?.let {
            readersList.add(it)
        }
    }

    override fun RFIDReaderDisappeared(readerDevice: ReaderDevice?) {
        readerDevice?.let {
            readersList.remove(it)
        }
    }
}
