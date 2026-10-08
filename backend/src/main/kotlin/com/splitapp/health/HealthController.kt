package com.splitapp.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// A controller is the "front door" of the API: it receives HTTP requests and returns responses.
// @RestController = whatever a function returns is turned into JSON automatically.
@RestController
class HealthController {

    // GET /health -> {"status":"ok"}
    @GetMapping("/health")
    fun health(): Map<String, String> = mapOf("status" to "ok")
}
