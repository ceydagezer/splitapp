package com.splitapp.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway

object DatabaseFactory {

    fun create(config: ApplicationConfig): HikariDataSource {
        // Hikari keeps a "pool" of open connections so each request
        // doesn't pay the cost of connecting to PostgreSQL from scratch.
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.property("database.url").getString()
            username = config.property("database.user").getString()
            password = config.property("database.password").getString()
            maximumPoolSize = 10
        }
        val dataSource = HikariDataSource(hikariConfig)

        // Creates/updates tables using the SQL files in resources/db/migration.
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()

        return dataSource
    }
}
