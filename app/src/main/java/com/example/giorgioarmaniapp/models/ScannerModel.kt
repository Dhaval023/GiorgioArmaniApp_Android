package com.example.giorgioarmaniapp.models

import android.content.Context
import android.util.Log
import com.zebra.scannercontrol.*
import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import java.io.StringReader
import java.lang.StringBuilder
import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList

class ScannerModel private constructor() : IDcsSdkApiDelegate {

    private var scannerId: Int = 0
    private var isConnected: Boolean = false
    private var deviceName: String = ""
    private var sFWVersion: String = ""

    companion object {
        private var instance: ScannerModel? = null
        var scannerList: MutableList<DCSScannerInfo> = CopyOnWriteArrayList()
        private var sdkHandler: SDKHandler? = null

        fun getInstance(): ScannerModel {
            if (instance == null) instance = ScannerModel()
            return instance!!
        }
    }

    // 🔥 EVENTS (Converted from C# delegates)
    var scannerConnectionEvent: ((String) -> Unit)? = null
    var currentProgress: ((Int) -> Unit)? = null
    var fwVersionEvent: ((String) -> Unit)? = null
    var barcodeEvent: ((String, String) -> Unit)? = null

    fun isConnected() = isConnected
    fun getDeviceName() = deviceName
    fun getFWVersion() = sFWVersion

    @Synchronized
    fun setupSDKHandler(context: Context, hostName: String?) {
        if (sdkHandler == null) {
            sdkHandler = SDKHandler(context)

            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC)
            sdkHandler!!.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)

            sdkHandler!!.dcssdkSetDelegate(this)

            var mask = 0
            mask = mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
            mask = mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
            mask = mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
            mask = mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
            mask = mask or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value

            sdkHandler!!.dcssdkSubsribeForEvents(mask)
        }

        val scanners = sdkHandler?.dcssdkGetAvailableScannersList()
        scannerList.clear()
        scanners?.let { scannerList.addAll(it) }

        hostName?.let {
            scannerList.forEach { device ->
                if (device.scannerName.contains(it)) {
                    connectScanner(device.scannerID)
                }
            }
        }
    }

    fun connectScanner(id: Int) {
        try {
            sdkHandler?.dcssdkEstablishCommunicationSession(id)
        } catch (e: Exception) {
            Log.e("Scanner", e.toString())
        }

        getScannerFirmwareVersion(id)
    }

    fun disconnectScanner(hostName: String?) {
        hostName?.let {
            scannerList.forEach { device ->
                if (device.scannerName.contains(it)) {
                    sdkHandler?.dcssdkTerminateCommunicationSession(device.scannerID)
                }
            }
        }
    }

    fun getScannerFirmwareVersion(scannerID: Int) {
        val inXml =
            "<inArgs><scannerID>$scannerID</scannerID><cmdArgs><arg-xml><attrib_list>20012</attrib_list></arg-xml></cmdArgs></inArgs>"

        CoroutineScope(Dispatchers.IO).launch {
            val outXml = StringBuilder()
            val success = executeCommand(
                scannerID,
                DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,
                outXml,
                inXml
            )

            if (success) {
                val fw = getSingleStringValue(outXml.toString())
                sFWVersion = fw

                withContext(Dispatchers.Main) {
                    fwVersionEvent?.invoke(fw)
                }
            }
        }
    }

    private fun getSingleStringValue(xml: String): String {
        return try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setInput(StringReader(xml))

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "value") {
                    return parser.nextText().trim()
                }
                event = parser.next()
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }

    // ================= EVENTS =================

    override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, scannerId: Int) {
        val data = barcodeData?.toString(Charsets.UTF_8) ?: ""
        val type = BarcodeTypes.getBarcodeTypeName(barcodeType)

        barcodeEvent?.invoke(data, type)
    }

    override fun dcssdkEventCommunicationSessionEstablished(scannerInfo: DCSScannerInfo?) {
        isConnected = true
        deviceName = scannerInfo?.scannerName ?: ""
        scannerId = scannerInfo?.scannerID ?: 0

        scannerConnectionEvent?.invoke(deviceName)
    }

    override fun dcssdkEventCommunicationSessionTerminated(scannerId: Int) {
        isConnected = false
        deviceName = ""
        sFWVersion = ""

        scannerConnectionEvent?.invoke("")
    }

    override fun dcssdkEventFirmwareUpdate(event: FirmwareUpdateEvent?) {
        event ?: return

        when (event.eventType) {
            DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_DL_PROGRESS -> {
                val progress = event.currentRecord * 100 / event.maxRecords
                currentProgress?.invoke(progress)
            }

            DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_SESS_END -> {
                currentProgress?.invoke(100)
            }

            else -> {}
        }
    }

    override fun dcssdkEventScannerAppeared(scannerInfo: DCSScannerInfo?) {}
    override fun dcssdkEventScannerDisappeared(scannerId: Int) {}
    override fun dcssdkEventBinaryData(data: ByteArray?, scannerId: Int) {}
    override fun dcssdkEventImage(data: ByteArray?, scannerId: Int) {}
    override fun dcssdkEventVideo(data: ByteArray?, scannerId: Int) {}
    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {}

    private suspend fun executeCommand(
        scannerId: Int,
        opCode: DCSSDKDefs.DCSSDK_COMMAND_OPCODE,
        outXml: StringBuilder,
        inXml: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val result = sdkHandler?.dcssdkExecuteCommandOpCodeInXMLForScanner(
                opCode,
                inXml,
                outXml,
                scannerId
            )
            result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS
        }
    }
}
