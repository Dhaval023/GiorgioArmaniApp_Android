package com.example.giorgioarmaniapp.models

import com.google.gson.annotations.SerializedName

class StockTakeModel {

    data class StockTakeListModel(
        @SerializedName("StorageLocation")
        val storageLocation: String?,
        @SerializedName("GlobalTradeItemNumber")
        val globalTradeItemNumber: String?,
        @SerializedName("StyleWhereASNID")
        val styleWhereASNID: String?,
        @SerializedName("SOHQuantity")
        var sohQuantity: Int,
        @SerializedName("ScannedQTY")
        var scannedQTY: Int,
        @SerializedName("IsDelTag")
        val isDelTag: Boolean,
        @SerializedName("InvalidGTINNumberCLR")
        val invalidGTINNumberCLR: Int
    )

    data class StockTakeListResult(
        @SerializedName("StoreCode")
        val storeCode: String?,
        @SerializedName("IsCompleted")
        val isCompleted: Boolean,
        @SerializedName("ItemList")
        val itemList: List<StockTakeListModel>
    )

    data class ResponseStockTakeListModel(
        @SerializedName("results")
        val results: List<StockTakeListResult>,
        @SerializedName("ERROR")
        val error: String?,
        @SerializedName("SUCCESS")
        val success: String?,
        @SerializedName("fileName")
        val fileName: String?
    )

    data class ResponseStockTakeFilenameModel(
        @SerializedName("results")
        val results: List<Any>,
        @SerializedName("ERROR")
        val error: String?,
        @SerializedName("SUCCESS")
        val success: String?
    )
}