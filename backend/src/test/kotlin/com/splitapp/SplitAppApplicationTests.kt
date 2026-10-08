package com.splitapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// Starts the whole application once. If any piece is misconfigured
// (a missing setting, two beans that can't be connected...), this test fails.
@SpringBootTest
class SplitAppApplicationTests {

    @Test
    fun contextLoads() {
    }
}
