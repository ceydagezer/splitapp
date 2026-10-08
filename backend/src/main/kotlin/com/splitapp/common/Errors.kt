package com.splitapp.common

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

// Every error the API returns has the same shape: {"error": "..."}
// so the Android app can always read it the same way.
@Serializable
data class ErrorResponse(val error: String)

// Throw this when the user sent something invalid (e.g. a too-short password).
class ValidationException(message: String) : Exception(message)

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Invalid request"))
        }
        // Thrown by Ktor when the JSON body is missing or malformed.
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Request body is invalid"))
        }
        // Anything unexpected: log the details for us, but don't leak them to the user.
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Something went wrong"))
        }
    }
}
