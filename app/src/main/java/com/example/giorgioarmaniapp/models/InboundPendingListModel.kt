package com.example.giorgioarmaniapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class InboundPendingListModel {

    @Parcelize
    data class InboundPendingModel(

        @SerializedName("ArticleNumber")
        val articleNumber: String?,

        @SerializedName("GlobalTradeItemNumber")
        val globalTradeItemNumber: String?,

        @SerializedName("DeliveryItem")
        val deliveryItem: String?,

        @SerializedName("ActualQuantityDelivered")
        var actualQuantityDelivered: Int,

        @SerializedName("ScannedQTY")
        var scannedQTY: Int,

        @SerializedName("IsDelTag")
        var isDelTag: Boolean,

        // Store color as String (e.g. "#FF0000")
        @SerializedName("InvalidGTINNumberCLR")
        val invalidGTINNumberCLR: String?,

        @SerializedName("IsInvalidCount")
        var isInvalidCount: Boolean
    ) : Parcelable

    // --- InboundPendingListResult ---
    @Parcelize
    data class InboundPendingListResult(

        @SerializedName("DeliveryNumber")
        val deliveryNumber: String?,

        @SerializedName("OutboundNumber")
        val outboundNumber: String?,

        @SerializedName("ItemList")
        val itemList: List<InboundPendingModel>?,

        // UI state (not from API)
        var isExpander: Boolean = true
    ) : Parcelable

    // --- ResponseInboundPendingList ---
    data class ResponseInboundPendingList(

        @SerializedName("results")
        val results: List<InboundPendingListResult>?,

        @SerializedName("ERROR")
        val error: String?,

        @SerializedName("SUCCESS")
        val success: String?
    )

    data class ResponseSubmitInboundPendingList(

        @SerializedName("results")
        val results: List<InboundPendingModel>?,

        @SerializedName("ERROR")
        val error: String?,

        @SerializedName("SUCCESS")
        val success: String?
    )
}
