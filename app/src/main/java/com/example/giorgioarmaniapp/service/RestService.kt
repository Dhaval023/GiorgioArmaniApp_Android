package com.example.giorgioarmaniapp.service

import android.util.Log
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
    suspend fun gtinPatternPost(): GTINPatternModel.GTINPatternResponse? {
        return try {
            val uri = ServiceConfiguration.postGTINPatternListURL
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, GTINPatternModel.GTINPatternResponse::class.java) }
        } catch (e: Exception) { null }
    }
    suspend fun passcodePost(passcode: String): PasscodeModel.ResponsePasscodeModel? {
        return try {
            val uri = String.format(ServiceConfiguration.postPasscodeURL, passcode)
            val resource = "${ServiceConfiguration.URL}$uri"
            val response = RestClientService.executePostRequestAsync(resource, "")
            response?.let { gson.fromJson(it, PasscodeModel.ResponsePasscodeModel::class.java) }
        } catch (e: Exception) { null }
    }

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

}
