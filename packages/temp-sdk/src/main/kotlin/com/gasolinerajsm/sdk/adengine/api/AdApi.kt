package com.gasolinerajsm.sdk.adengine.api

import com.gasolinerajsm.sdk.adengine.ApiClient
import com.gasolinerajsm.sdk.adengine.model.AdCreative
import com.gasolinerajsm.sdk.adengine.model.AdSelectionRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AdApiContract {
    @POST("ad/select")
    fun selectAd(@Body request: AdSelectionRequest): Call<AdCreative>

    @POST
    fun recordImpression(@Url url: String): Call<ResponseBody>
}

class AdApi(private val apiClient: ApiClient) {
    private val service by lazy {
        apiClient.createService<AdApiContract>()
    }

    fun selectAd(adSelectionRequest: AdSelectionRequest): AdCreative? {
        val response = service.selectAd(adSelectionRequest).execute()
        if (response.isSuccessful) {
            return response.body()
        }
        // Naive error handling
        throw RuntimeException("Failed to select ad: ${response.errorBody()?.string()}")
    }

    fun recordImpression(impressionUrl: String) {
        val response = service.recordImpression(impressionUrl).execute()
        if (!response.isSuccessful) {
            // Naive error handling
            throw RuntimeException("Failed to record impression: ${response.errorBody()?.string()}")
        }
    }
}