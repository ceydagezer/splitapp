package com.splitapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// @SpringBootApplication tells Spring: "scan this package (com.splitapp) and everything
// below it, find the classes marked @RestController, @Service, @Repository, ...,
// create one object of each and connect them together."
@SpringBootApplication
class SplitAppApplication

fun main(args: Array<String>) {
    runApplication<SplitAppApplication>(*args)
}
