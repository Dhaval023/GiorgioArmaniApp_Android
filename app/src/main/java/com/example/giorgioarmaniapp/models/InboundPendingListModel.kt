package com.example.giorgioarmaniapp.models

import com.google.gson.annotations.SerializedName

class InboundPendingListModel {

    data class InboundPendingModel(

        @SerializedName("ArticleNumber")
        val articleNumber: String?,

        @SerializedName("GlobalTradeItemNumber")
        val globalTradeItemNumber: String?,

        @SerializedName("DeliveryItem")
        val deliveryItem: String?,

        @SerializedName("ActualQuantityDelivered")
        val actualQuantityDelivered: Int,

        @SerializedName("ScannedQTY")
        val scannedQTY: Int,

        @SerializedName("IsDelTag")
        val isDelTag: Boolean,

        // Store color as String (e.g. "#FF0000")
        @SerializedName("InvalidGTINNumberCLR")
        val invalidGTINNumberCLR: String?,

        @SerializedName("IsInvalidCount")
        val isInvalidCount: Boolean
    )

    // --- InboundPendingListResult ---
    data class InboundPendingListResult(

        @SerializedName("DeliveryNumber")
        val deliveryNumber: String?,

        @SerializedName("OutboundNumber")
        val outboundNumber: String?,

        @SerializedName("ItemList")
        val itemList: List<InboundPendingModel>?,

        // UI state (not from API)
        var isExpander: Boolean = true
    )

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
