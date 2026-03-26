package com.example.giorgioarmaniapp.service

import com.example.giorgioarmaniapp.models.AppConstants
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RestClientService private constructor() {

    private val gson = Gson()
    private val defaultClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    companion object {
        @Volatile
        private var instance: RestClientService? = null

        fun getInstance(): RestClientService {
            return instance ?: synchronized(this) {
                instance ?: RestClientService().also { instance = it }
            }
        }

        // Keep legacy static methods for RestService.kt
        suspend fun executePostRequestAsync(url: String, body: Any, headers: Map<String, String>? = null): String? = withContext(Dispatchers.IO) {
            val client = getInstance().defaultClient
            val jsonBody = if (body is String) body else Gson().toJson(body)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
            
            val builder = Request.Builder()
                .url(url)
                .post(requestBody)
            
            headers?.forEach { (k, v) -> builder.addHeader(k, v) }
            
            val response = client.newCall(builder.build()).await()
            if (response.isSuccessful) response.body?.string() else null
        }

        suspend fun executePostRequestSTAsync(url: String, bodyBytes: ByteArray, timeoutMinutes: Long): String? = withContext(Dispatchers.IO) {
            val client = getInstance().defaultClient.newBuilder()
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .build()
            
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), bodyBytes)
            val request = Request.Builder().url(url).post(requestBody).build()
            
            val response = client.newCall(request).await()
            if (response.isSuccessful) response.body?.string() else null
        }

        suspend fun executeGetRequestAsyncForSAP(url: String, headers: Map<String, String>): String? = withContext(Dispatchers.IO) {
            val client = getInstance().defaultClient
            val builder = Request.Builder().url(url).get()
            headers.forEach { (k, v) -> builder.addHeader(k, v) }
            
            val response = client.newCall(builder.build()).await()
            if (response.isSuccessful) response.body?.string() else null
        }

        private suspend fun Call.await(): Response {
            return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        cont.resume(response)
                    }
                    override fun onFailure(call: Call, e: IOException) {
                        cont.resumeWithException(e)
                    }
                })
                cont.invokeOnCancellation { cancel() }
            }
        }
    }

    suspend fun <T> post(url: String, body: Any, responseType: Class<T>): T? = withContext(Dispatchers.IO) {
        val jsonBody = gson.toJson(body)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url(ServiceConfiguration.URL + url)
            .post(requestBody)
            .build()

        val response = defaultClient.newCall(request).await()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            gson.fromJson(responseBody, responseType)
        } else {
            null
        }
    }

    suspend fun <T> get(url: String, responseType: Class<T>): T? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(ServiceConfiguration.URL + url)
            .get()
            .build()

        val response = defaultClient.newCall(request).await()
        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            gson.fromJson(responseBody, responseType)
        } else {
            null
        }
    }

    fun cancelPendingRequests() {
        defaultClient.dispatcher.cancelAll()
    }
}