package com.example.giorgioarmaniapp.models

import com.google.gson.annotations.SerializedName

class StockTakeSelectionModel {

    data class ResponseStockTakeSelectionListModel(
        @SerializedName("Gender") val gender: List<String>,
        @SerializedName("Category") val category: List<String>,
        @SerializedName("Brands") val brands: List<String>
    )

    data class ResponseStockTakeResultModel(
        @SerializedName("results") val results: ResponseStockTakeSelectionListModel,
        @SerializedName("ERROR") val error: String?,
        @SerializedName("SUCCESS") val success: String?
    )
}