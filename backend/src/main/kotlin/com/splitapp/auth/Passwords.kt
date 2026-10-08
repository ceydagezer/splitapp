package com.splitapp.auth

import at.favre.lib.crypto.bcrypt.BCrypt

// We never store real passwords. bcrypt turns "hunter2" into a one-way hash;
// at login we hash the typed password again and compare.
object Passwords {
    // Higher = slower to crack, but also slower to log in. 12 is a common choice.
    private const val COST = 12

    fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(COST, password.toCharArray())

    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}
