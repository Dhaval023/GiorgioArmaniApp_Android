package com.example.giorgioarmaniapp.ui.login_page

import com.example.giorgioarmaniapp.models.ReaderModel
import com.zebra.rfid.api3.TagData
import com.zebra.rfid.api3.IEvents

object BaseViewModel {
    lateinit var rfidModel: ReaderModel

    fun updateIn(
        onTagRead: ((Array<TagData>) -> Unit)? = null,
        onTrigger: ((Boolean) -> Unit)? = null,
        onStatus: ((IEvents.StatusEventData) -> Unit)? = null,
        onConnection: ((Boolean) -> Unit)? = null
    ) {
        rfidModel.tagRead              = onTagRead
        rfidModel.triggerEvent         = onTrigger
        rfidModel.statusEvent          = onStatus
        rfidModel.readerConnectionEvent = onConnection
    }

    fun updateOut() {
        rfidModel.tagRead               = null
        rfidModel.triggerEvent          = null
        rfidModel.statusEvent           = null
        rfidModel.readerConnectionEvent = null
    }
}