package com.splitapp.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

// A JWT is a signed "ticket" we give the app after login. The app sends it with
// every request, and we can check the signature to know who the user is
// without looking anything up in the database.
class JwtConfig(
    secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    private val expiresIn: Duration,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun createToken(userId: Long): String = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withSubject(userId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn.inWholeMilliseconds))
        .sign(algorithm)

    companion object {
        fun from(config: ApplicationConfig) = JwtConfig(
            secret = config.property("jwt.secret").getString(),
            issuer = config.property("jwt.issuer").getString(),
            audience = config.property("jwt.audience").getString(),
            realm = config.property("jwt.realm").getString(),
            expiresIn = config.property("jwt.expiresInDays").getString().toInt().days,
        )
    }
}
