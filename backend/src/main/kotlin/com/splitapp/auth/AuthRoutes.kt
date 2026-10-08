package com.splitapp.auth

import com.splitapp.common.ErrorResponse
import com.splitapp.common.ValidationException
import com.splitapp.users.UserRepository
import com.splitapp.users.UserResponse
import com.splitapp.users.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: UserResponse)

fun Route.authRoutes(users: UserRepository, jwtConfig: JwtConfig) {
    route("/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val name = request.name.trim()
            val email = request.email.trim().lowercase()

            if (name.isEmpty() || name.length > 100) throw ValidationException("Name must be 1-100 characters")
            if (!EMAIL_REGEX.matches(email)) throw ValidationException("Email is not valid")
            // bcrypt only looks at the first 72 bytes, so we cap the length.
            if (request.password.length !in 8..72) throw ValidationException("Password must be 8-72 characters")

            val user = users.create(name, email, Passwords.hash(request.password))
                ?: return@post call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("An account with this email already exists"),
                )

            call.respond(HttpStatusCode.Created, AuthResponse(jwtConfig.createToken(user.id), user.toResponse()))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = users.findByEmail(request.email.trim().lowercase())

            // Same message for "no such email" and "wrong password", so attackers
            // can't find out which emails are registered.
            if (user == null || !Passwords.verify(request.password, user.passwordHash)) {
                return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Email or password is incorrect"))
            }

            call.respond(AuthResponse(jwtConfig.createToken(user.id), user.toResponse()))
        }
    }
}

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
