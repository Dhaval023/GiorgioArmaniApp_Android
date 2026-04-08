
package com.example.giorgioarmaniapp.service

import android.util.Log
import com.example.giorgioarmaniapp.BuildConfig
import com.example.giorgioarmaniapp.models.*
import com.google.gson.Gson
import kotlinx.coroutines.delay

class RestService {

    private val gson = Gson()

    // Login
    suspend fun login(username: String, password: String, deviceType: String): GetLoginDetailsResponse? {
        return try {
            val uri = String.format(ServiceConfiguration.postLoginURL, username, password, deviceType)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, GetLoginDetailsResponse::class.java) }
        } catch (e: Exception) { null }
    }

    // Inbound
    suspend fun getInboundPendingList(storeCode: String): InboundPendingListModel.ResponseInboundPendingList? {
        return try {
            val uri = String.format(ServiceConfiguration.getInboundPendingListURL, storeCode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, InboundPendingListModel.ResponseInboundPendingList::class.java) }
        } catch (e: Exception) { null }
    }

    suspend fun submitInboundList(userID: String, listModel: InboundPendingListModel.InboundPendingListResult): InboundPendingListModel.ResponseSubmitInboundPendingList? {
        return try {
            val uri = String.format(ServiceConfiguration.postInboundSubmitListURL, userID)
            val resource = "${ServiceConfiguration.URL}$uri"
            val obj = gson.toJson(listModel)
            val response = RestClientService.executePostRequestAsync(resource, obj)
            response?.let { gson.fromJson(it, InboundPendingListModel.ResponseSubmitInboundPendingList::class.java) }
        } catch (e: Exception) { null }
    }

    // Outbound
    suspend fun getOutboundList(storeCode: String): OutBoundStockModel.ResponseOutBoundStockListModel? {
        return try {
            val uri = String.format(ServiceConfiguration.getOutboundListURL, storeCode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, OutBoundStockModel.ResponseOutBoundStockListModel::class.java) }
        } catch (e: Exception) { null }
    }

    suspend fun submitSOHOutboundPreviewList(
        fromStoreCode: String, toStoreCode: String, userID: String,
        transferType: String, id: Int, locationId: Int,
        listModel: OutBoundStockModel.OutboundPendingListResult
    ): OutBoundStockModel.ResponseSubmitOutBoundListModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postOutboundSubmitListURL, fromStoreCode, toStoreCode, userID, transferType, id, locationId)
            val resource = "${ServiceConfiguration.URL}$uri"
            val obj = gson.toJson(listModel)
            val response = RestClientService.executePostRequestAsync(resource, obj)
            response?.let { gson.fromJson(it, OutBoundStockModel.ResponseSubmitOutBoundListModel::class.java) }
        } catch (e: Exception) { null }
    }

    suspend fun submitOutboundList(
        fromStoreCode: String, toStoreCode: String, userID: String,
        transferType: String, id: Int,
        listModel: OutBoundStockModel.OutboundPendingListResult
    ): OutBoundStockModel.ResponseSubmitOutBoundListModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postOutboundConsolidatedSTSubmitListURL, fromStoreCode, toStoreCode, userID, transferType, id)
            val resource = "${ServiceConfiguration.URL}$uri"
            val obj = gson.toJson(listModel)
            val response = RestClientService.executePostRequestAsync(resource, obj)
            response?.let { gson.fromJson(it, OutBoundStockModel.ResponseSubmitOutBoundListModel::class.java) }
        } catch (e: Exception) { null }
    }

    suspend fun getPendingConsolidatedOutboundList(storeCode: String): OutBoundStockModel.ResponseforPendingOutBoundStockListModel? {
        return try {
            val uri = String.format(ServiceConfiguration.getOutboundPendingListURL, storeCode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, OutBoundStockModel.ResponseforPendingOutBoundStockListModel::class.java) }
        } catch (e: Exception) { null }
    }

    // StockTake
    suspend fun getSOHPendingList(storeCode: String, category: String, brand: String, gender: String): StockTakeModel.ResponseStockTakeListModel? {
        return try {
            val uri = String.format(ServiceConfiguration.getStockTakePendingListURL, storeCode, category, brand, gender)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, StockTakeModel.ResponseStockTakeListModel::class.java) }
        } catch (e: Exception) { null }
    }

    suspend fun submitStockTakeList(userID: String, listModel: StockTakeModel.StockTakeListResult): StockTakeModel.ResponseStockTakeListModel? {
        val maxRetries = 3
        var result: StockTakeModel.ResponseStockTakeListModel? = null
        for (attempt in 1..maxRetries) {
            try {
                val uri = String.format(ServiceConfiguration.postSubmitStockTakeListURL, userID)
                val resource = "${ServiceConfiguration.URL}$uri"
                val bodyBytes = gson.toJson(listModel).toByteArray(Charsets.UTF_8)
                val response = RestClientService.executePostRequestSTAsync(resource, bodyBytes, timeoutMinutes = 5)
                if (response != null) {
                    result = gson.fromJson(response, StockTakeModel.ResponseStockTakeListModel::class.java)
                    if (result?.success == "Successfully Submitted") return result
                }
            } catch (e: Exception) {
                Log.e("RestService", "Attempt $attempt failed: ${e.message}")
                if (attempt == maxRetries) {
                    return StockTakeModel.ResponseStockTakeListModel(
                        results = emptyList(),
                        error = "Failed after $maxRetries attempts: ${e.message}",
                        success = null,
                        fileName = null
                    )
                }
                delay(1000L * attempt)
            }
        }
        return result
    }

    suspend fun submitStockTakeFile(filename: String): StockTakeModel.ResponseStockTakeFilenameModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postSubmitStockTakeFileURL, filename)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, StockTakeModel.ResponseStockTakeFilenameModel::class.java) }
        } catch (e: Exception) { null }
    }

    // GTIN
    suspend fun gtinPatternPost(): GTINPatternModel.GTINPatternResponse? {
        return try {
            val uri = ServiceConfiguration.postGTINPatternListURL
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, GTINPatternModel.GTINPatternResponse::class.java) }
        } catch (e: Exception) { null }
    }

    // Passcode
    suspend fun passcodePost(passcode: String): PasscodeModel.ResponsePasscodeModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postPasscodeURL, passcode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, PasscodeModel.ResponsePasscodeModel::class.java) }
        } catch (e: Exception) { null }
    }

    // SAP StockTake (SOH)
//    companion object {
//        private const val USE_PRODUCTION = false
//        private const val API_KEY_PROD = "LcKMTT5UldIJhX8GGYNK4V41e6tAybH6pQ6FHtrg"
//        private const val API_KEY_TEST = "cvmaATSLPP3GpskBYIJ8D0jq7z8Bq6u3QGaWAEDj"
//        private val apiKey get() = if (USE_PRODUCTION) API_KEY_PROD else API_KEY_TEST
//    }

    companion object {
        private const val USE_PRODUCTION = false

        private val apiKey: String
            get() = if (USE_PRODUCTION)
                BuildConfig.API_KEY_PROD
            else
                BuildConfig.API_KEY_TEST
    }
    suspend fun getSOHStockTakePendingList(storeCode: String, listModel: String): OutBoundStockModel.Root? {
        return try {
            val baseUrl = if (USE_PRODUCTION)
                "https://api-gw-sap-prod.giorgioarmani.tech/fms-odata/ZRFID_IF004_SRV/ZA_RFID_IF004_CC"
            else
                "https://api-gw-sap-quality.giorgioarmani.tech/fms-odata/ZRFID_IF004_SRV/ZA_RFID_IF004_CC"

            val resource = "$baseUrl?\$format=json&\$filter=Site eq '$storeCode' and ($listModel)&sap-language=EN&sap-client=100"
            Log.d("RestService", ">>> FULL URL: $resource")

            val headers = mapOf("x-api-key" to apiKey)
            val response = RestClientService.executeGetRequestAsyncForSAP(resource, headers)
            if (response != null) {
                val result = gson.fromJson(response, OutBoundStockModel.Root::class.java)
                Log.d("RestService", ">>> RESULT COUNT: ${result?.d?.results?.size ?: "NULL"}")
                result
            } else null
        } catch (e: Exception) {
            Log.e("RestService", "getSOHStockTakePendingList ERROR: ${e.message}")
            null
        }
    }

    // StockTake Selection
    suspend fun stockTakeSelectionList(storeCode: String): StockTakeSelectionModel.ResponseStockTakeResultModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postStockTakeSelectionListURL, storeCode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val headers = mapOf("b-APIKey" to BuildConfig.B_API_KEY)
            val response = RestClientService.executePostRequestAsync(resource, "", headers)
            response?.let { gson.fromJson(it, StockTakeSelectionModel.ResponseStockTakeResultModel::class.java) }
        } catch (e: Exception) { null }
    }
}
