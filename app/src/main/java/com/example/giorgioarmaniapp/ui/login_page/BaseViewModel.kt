//package com.example.giorgioarmaniapp.ui.login_page
//
//import com.example.giorgioarmaniapp.models.ReaderModel
//import com.zebra.rfid.api3.TagData
//import com.zebra.rfid.api3.IEvents
//
//object BaseViewModel {
//    lateinit var rfidModel: ReaderModel
//
//    fun updateIn(
//        onTagRead: ((Array<TagData>) -> Unit)? = null,
//        onTrigger: ((Boolean) -> Unit)? = null,
//        onStatus: ((IEvents.StatusEventData) -> Unit)? = null,
//        onConnection: ((Boolean) -> Unit)? = null
//    ) {
//        rfidModel.tagRead              = onTagRead
//        rfidModel.triggerEvent         = onTrigger
//        rfidModel.statusEvent          = onStatus
//        rfidModel.readerConnectionEvent = onConnection
//    }
//
//    fun updateOut() {
//        rfidModel.tagRead               = null
//        rfidModel.triggerEvent          = null
//        rfidModel.statusEvent           = null
//        rfidModel.readerConnectionEvent = null
//    }
//}
package com.example.giorgioarmaniapp.ui.login_page

import com.example.giorgioarmaniapp.models.ReaderModel
import com.zebra.rfid.api3.IEvents
import com.zebra.rfid.api3.TagData

/**
 * Singleton that holds the shared RFID reader model and wires event callbacks.
 * All ViewModels reference this object directly — they do NOT extend it.
 *
 * Converted from C# BaseViewModel (abstract class) to Kotlin object because
 * Android ViewModel cannot be subclassed from an arbitrary base that also
 * manages static/shared RFID state.
 */
object BaseViewModel {

    lateinit var rfidModel: ReaderModel

    /** Whether the RFID reader is currently connected */
    var isConnected: Boolean = false

    /**
     * Wire this screen's RFID callbacks into the reader model.
     * Call from Fragment.onResume() / onViewCreated().
     * Converted from: public override void UpdateIn() in C# BaseViewModel
     */
    fun updateIn(
        onTagRead: ((Array<TagData>) -> Unit)? = null,
        onTrigger: ((Boolean) -> Unit)? = null,
        onStatus: ((IEvents.StatusEventData) -> Unit)? = null,
        onConnection: ((Boolean) -> Unit)? = null
    ) {
        rfidModel.tagRead               = onTagRead
        rfidModel.triggerEvent          = onTrigger
        rfidModel.statusEvent           = onStatus
        rfidModel.readerConnectionEvent = onConnection
    }

    /**
     * Remove this screen's RFID callbacks from the reader model.
     * Call from Fragment.onPause() / onStop().
     * Converted from: public override void UpdateOut() in C# BaseViewModel
     */
    fun updateOut() {
        rfidModel.tagRead               = null
        rfidModel.triggerEvent          = null
        rfidModel.statusEvent           = null
        rfidModel.readerConnectionEvent = null
    }

    /**
     * Wire barcode callbacks (separate from RFID trigger mode).
     * Converted from: public override void UpdateBarcodeIn() in C# BaseViewModel
     */
    fun updateBarcodeIn(
        onBarcode: ((String, Int, Short) -> Unit)? = null
    ) {
        rfidModel.barcodeEvent = onBarcode
    }

    /**
     * Remove barcode callbacks.
     * Converted from: public override void UpdateBarcodeOut() in C# BaseViewModel
     */
    fun updateBarcodeOut() {
        rfidModel.barcodeEvent = null
    }
}

