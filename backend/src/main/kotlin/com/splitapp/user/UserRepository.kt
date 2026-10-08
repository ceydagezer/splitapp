package com.splitapp.user

import org.springframework.data.jpa.repository.JpaRepository

// A repository is the only place that talks to the database for one entity.
// We only write an interface; Spring Data JPA writes the implementation for us when the app starts.
//
// JpaRepository<User, Long> ("entity type", "id type") already gives us:
//   save(user), findById(id), findAll(), count(), deleteById(id), ...
interface UserRepository : JpaRepository<User, Long> {

    // Spring Data reads the function name and builds the SQL itself:
    //   findByEmail   -> SELECT ... FROM users WHERE email = ?
    //   existsByEmail -> SELECT 1 FROM users WHERE email = ? LIMIT 1
    // Returning `User?` means "null if not found".
    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean
}
