package com.splitapp.auth

import com.splitapp.common.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(jwtConfig.verifier)
            validate { credential ->
                // Accept the token only if it contains a valid user id.
                if (credential.payload.subject?.toLongOrNull() != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Token is missing or invalid"))
            }
        }
    }
}

/** The id of the logged-in user. Only use inside `authenticate { }` routes. */
fun ApplicationCall.userId(): Long =
    principal<JWTPrincipal>()!!.payload.subject.toLong()
