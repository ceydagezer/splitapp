package com.splitapp

import com.splitapp.auth.JwtConfig
import com.splitapp.auth.authRoutes
import com.splitapp.auth.configureSecurity
import com.splitapp.auth.userId
import com.splitapp.common.ErrorResponse
import com.splitapp.common.configureErrorHandling
import com.splitapp.database.DatabaseFactory
import com.splitapp.users.UserRepository
import com.splitapp.users.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main(args: Array<String>) = EngineMain.main(args)

// Ktor calls this when the server starts (see `modules` in application.conf).
fun Application.module() {
    val dataSource = DatabaseFactory.create(environment.config)
    val jwtConfig = JwtConfig.from(environment.config)
    val userRepository = UserRepository(dataSource)

    install(CallLogging)
    install(ContentNegotiation) { json() }
    configureErrorHandling()
    configureSecurity(jwtConfig)

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        authRoutes(userRepository, jwtConfig)

        // Everything inside here requires a valid token in the "Authorization: Bearer ..." header.
        authenticate("auth-jwt") {
            get("/me") {
                val user = userRepository.findById(call.userId())
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                call.respond(user.toResponse())
            }
        }
    }

    monitor.subscribe(ApplicationStopped) { dataSource.close() }
}
