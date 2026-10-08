package com.splitapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

// Starts the whole application once. If any piece is misconfigured
// (a missing setting, two beans that can't be connected, an entity that
// doesn't match its table...), this test fails.
@SpringBootTest
@ActiveProfiles("test")
class SplitAppApplicationTests {

    @Test
    fun contextLoads() {
    }
}
