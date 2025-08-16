package com.gasolinerajsm.redemptionservice.config

import com.gasolinerajsm.sdk.adengine.api.AdApi
import com.gasolinerajsm.sdk.adengine.ApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

 @Configuration
class ApiClientConfig {

    @Bean
    fun adApi(): AdApi {
        val apiClient = ApiClient()
        apiClient.basePath = "http://ad-engine:8080" // URL de servicio interna en Docker
        return AdApi(apiClient)
    }
}
