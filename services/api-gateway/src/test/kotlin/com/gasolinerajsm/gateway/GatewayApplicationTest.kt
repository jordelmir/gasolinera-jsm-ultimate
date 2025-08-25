package com.gasolinerajsm.gateway

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class GatewayApplicationTest {

    @Test
    fun contextLoads() {
        // Test que verifica que el contexto de Spring se carga correctamente
    }
}
