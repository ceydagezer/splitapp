package com.splitapp.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

// An entity is a Kotlin class that mirrors a database table: one object = one row.
// This one matches the `users` table created in db/migration/V1__create_users.sql.
// It stays inside the server: it contains the password hash, so it is never sent as JSON.
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, unique = true)
    var email: String,

    // Kotlin uses camelCase, the table uses snake_case, so we give the column name explicitly.
    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,
) {
    // PostgreSQL generates the id (GENERATED ALWAYS AS IDENTITY), so it is null until the user is saved.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
}
