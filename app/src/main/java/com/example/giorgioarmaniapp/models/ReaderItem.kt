package com.example.giorgioarmaniapp.models


import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class ReaderItem : BaseObservable() {

    var index: Int = 0

    @get:Bindable
    var deviceNumber: String? = null
        set(value) {
            field = value
            notifyChange()
        }

    @get:Bindable
    var deviceModel: String? = null
        set(value) {
            field = value
            notifyChange()
        }

    @get:Bindable
    var deviceSerialNumber: String? = null
        set(value) {
            field = value
            notifyChange()
        }

    @get:Bindable
    var isSelected: Boolean = false
        set(value) {
            field = value
            notifyChange()
        }
}