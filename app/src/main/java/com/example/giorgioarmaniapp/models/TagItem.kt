package com.example.giorgioarmaniapp.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class TagItem : BaseObservable() {

    var invID: String? = null

    @get:Bindable
    var tagCount: Int = 0
        set(value) {
            field = value
            notifyChange()
        }

    @get:Bindable
    var rssi: Int = 0
        set(value) {
            field = value
            notifyChange()
        }

    @get:Bindable
    var relativeDistance: Int = 0
        set(value) {
            field = value
            notifyChange()
        }
}