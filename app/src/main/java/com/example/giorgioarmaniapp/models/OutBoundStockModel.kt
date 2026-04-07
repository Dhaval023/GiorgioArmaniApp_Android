package com.example.giorgioarmaniapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

class OutBoundStockModel {

    // --- OutBoundStockListModel ---
    @Parcelize
    data class OutBoundStockListModel(
        @SerializedName("GlobalTradeItemNumber")
        val globalTradeItemNumber: String?,

        @SerializedName("ActualQuantityDelivered")
        val actualQuantityDelivered: Int,

        @SerializedName("ScannedQTY")
        var scannedQTY: Int,

        @SerializedName("IsDelTag")
        val isDelTag: Boolean,

        @SerializedName("InvalidGTINNumberCLR")
        val invalidGTINNumberCLR: String? = null,

        @SerializedName("IsInvalidCount")
        val isInvalidCount: Boolean = false
    ) : Parcelable

    // --- OutboundPendingListResult ---
    data class OutboundPendingListResult(
        @SerializedName("ItemList")
        val itemList: List<OutBoundStockListModel>?
    )

    // --- Submit Response ---
    data class ResponseSubmitOutBoundListModel(
        @SerializedName("results")
        val results: List<OutboundPendingListResult>?,
        @SerializedName("ERROR")
        val error: String?,
        @SerializedName("SUCCESS")
        val success: String?
    )

    // --- Store List ---
    @Parcelize
    data class STOutBoundStockListModel(
        @SerializedName("StoreCode")
        val storeCode: String?,
        @SerializedName("StoreName")
        val storeName: String?,
        @SerializedName("LocationList")
        val locationList: List<LocationList>?,
        @SerializedName("StoreFullName")
        val storeFullName: String?
    ) : Parcelable

    @Parcelize
    data class LocationList(
        @SerializedName("id")
        val id: Int,
        @SerializedName("location_name")
        val locationName: String?
    ) : Parcelable

    data class ResponseOutBoundStockListModel(
        @SerializedName("results")
        val results: List<STOutBoundStockListModel>?,
        @SerializedName("ERROR")
        val error: String?,
        @SerializedName("SUCCESS")
        val success: String?
    )


    data class PendingOutboundResult(
        @SerializedName("id")
        val id: Int,

        @SerializedName("To_Store")
        val toStore: String?,

        @SerializedName("ItemList")
        val itemList: List<OutBoundStockListModel>?
    )

    data class ResponseforPendingOutBoundStockListModel(
        @SerializedName("results")
        val results: List<PendingOutboundResult>?,

        @SerializedName("ERROR")
        val error: String?,

        @SerializedName("SUCCESS")
        val success: String?
    )

    // --- SAP Models ---
    @Parcelize
    data class D(
        @SerializedName("results")
        val results: List<Result>?
    ) : Parcelable

    @Parcelize
    data class Metadata(
        @SerializedName("id")
        val id: String?,
        @SerializedName("uri")
        val uri: String?,
        @SerializedName("type")
        val type: String?
    ) : Parcelable

    @Parcelize
    data class Result(
        @SerializedName("__metadata")
        val metadata: Metadata?,
        @SerializedName("Site")
        val site: String?,
        @SerializedName("ProductType")
        val productType: String?,
        @SerializedName("Style")
        val style: String?,
        @SerializedName("Fabric")
        val fabric: String?,
        @SerializedName("MerchandiseGroup")
        val merchandiseGroup: String?,
        @SerializedName("PosQuantity")
        val posQuantity: String?,
        @SerializedName("SOHQuantity")
        val sohQuantity: String?,
        @SerializedName("CreationDate")
        val creationDate: String?,
        @SerializedName("CreationTime")
        val creationTime: String?,
        @SerializedName("StorageLocation")
        val storageLocation: String?,
        @SerializedName("MaterialNumber")
        val materialNumber: String?,
        @SerializedName("GTINCode")
        val gtinCode: String?,
        @SerializedName("GTINQuantity")
        val gtinQuantity: String?,
        @SerializedName("Brand")
        val brand: String?,
        @SerializedName("Gender")
        val gender: String?,
        @SerializedName("Category")
        val category: String?,
        @SerializedName("ProductCode")
        val productCode: String?
    ) : Parcelable

    @Parcelize
    data class Root(
        @SerializedName("d")
        val d: D?
    ) : Parcelable

    @Parcelize
    data class OutboundPreviewNavArgs(
        val allOutboundItems: List<OutBoundStockListModel>,
        val sohResult: Root,
        val toStoreCode: String,
        val locationId: Int
    ) : Parcelable
}
