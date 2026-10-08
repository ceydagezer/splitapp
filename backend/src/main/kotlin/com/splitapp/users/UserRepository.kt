package com.splitapp.users

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.ResultSet
import javax.sql.DataSource

// How a user looks inside the server (includes the password hash).
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val passwordHash: String,
)

// How a user looks when we send it to the app (never send the hash!).
@Serializable
data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
)

fun User.toResponse() = UserResponse(id = id, name = name, email = email)

// All SQL for the users table lives here. The `?` placeholders are filled in
// with setString/setLong, which protects us from SQL injection attacks.
class UserRepository(private val dataSource: DataSource) {

    /** Returns null if a user with this email already exists. */
    suspend fun create(name: String, email: String, passwordHash: String): User? = query { connection ->
        connection.prepareStatement(
            """
            INSERT INTO users (name, email, password_hash)
            VALUES (?, ?, ?)
            ON CONFLICT (email) DO NOTHING
            RETURNING id
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, name)
            statement.setString(2, email)
            statement.setString(3, passwordHash)
            statement.executeQuery().use { rows ->
                if (rows.next()) User(rows.getLong("id"), name, email, passwordHash) else null
            }
        }
    }

    suspend fun findByEmail(email: String): User? = query { connection ->
        connection.prepareStatement("SELECT id, name, email, password_hash FROM users WHERE email = ?").use { statement ->
            statement.setString(1, email)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toUser() else null }
        }
    }

    suspend fun findById(id: Long): User? = query { connection ->
        connection.prepareStatement("SELECT id, name, email, password_hash FROM users WHERE id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { rows -> if (rows.next()) rows.toUser() else null }
        }
    }

    private fun ResultSet.toUser() = User(
        id = getLong("id"),
        name = getString("name"),
        email = getString("email"),
        passwordHash = getString("password_hash"),
    )

    // Database calls block the thread while waiting, so we run them on the IO dispatcher.
    private suspend fun <T> query(block: (java.sql.Connection) -> T): T = withContext(Dispatchers.IO) {
        dataSource.connection.use(block)
    }
}
