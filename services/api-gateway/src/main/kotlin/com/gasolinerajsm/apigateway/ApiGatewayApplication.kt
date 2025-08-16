
package com.gasolinerajsm.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiGatewayApplication

fun main(args: Array<String>) {
    try {
        runApplication<ApiGatewayApplication>(*args)
    } catch (e: Exception) {
        System.err.println("API Gateway application failed to start: ${e.message}")
        e.printStackTrace()
        // In a real app, you might want to log this to a proper logging system
        // and potentially trigger alerts.
    }
}
