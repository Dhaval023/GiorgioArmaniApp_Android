package com.example.giorgioarmaniapp.models


data class TagItem(
    var invID: String? = null,
    var tagCount: Int = 0,
    var rssi: Int = 0,
    var relativeDistance: Int = 0
)
//
//    var invID: String? = null
//
//    @get:Bindable
//    var tagCount: Int = 0
//        set(value) {
//            field = value
//            notifyChange()
//        }
//
//    @get:Bindable
//    var rssi: Int = 0
//        set(value) {
//            field = value
//            notifyChange()
//        }
//
//    @get:Bindable
//    var relativeDistance: Int = 0
//        set(value) {
//            field = value
//            notifyChange()
//        }
//}