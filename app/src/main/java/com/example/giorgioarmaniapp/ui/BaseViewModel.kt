
package com.example.giorgioarmaniapp.ui.login_page

import com.example.giorgioarmaniapp.models.ReaderModel
import com.zebra.rfid.api3.IEvents
import com.zebra.rfid.api3.TagData

object BaseViewModel {

    lateinit var rfidModel: ReaderModel

    var isConnected: Boolean = false

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

    fun updateOut() {
        rfidModel.tagRead               = null
        rfidModel.triggerEvent          = null
        rfidModel.statusEvent           = null
        rfidModel.readerConnectionEvent = null
    }

    fun updateBarcodeIn(
        onBarcode: ((String, Int, Short) -> Unit)? = null
    ) {
        rfidModel.barcodeEvent = onBarcode
    }

    fun updateBarcodeOut() {
        rfidModel.barcodeEvent = null
    }
}

