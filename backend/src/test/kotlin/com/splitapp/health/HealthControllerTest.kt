package com.splitapp.health

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

// @WebMvcTest starts only the web layer (controllers + JSON), not the whole app, so it's fast.
// MockMvc sends fake HTTP requests to our controllers without opening a real network port.
@WebMvcTest(HealthController::class)
class HealthControllerTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `GET health returns status ok`() {
        mockMvc.get("/health").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("ok") }
        }
    }
}
