package no.tordly.libs.ktorstarter

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

data class PostgresConfig(val url: String, val user: String, val password: String) {
    companion object {
        fun create(env: ApplicationConfig) = PostgresConfig(
            env.property("ktor.db.url").getString(),
            env.property("ktor.db.user").getString(),
            env.property("ktor.db.password").getString(),
        )
    }
}

class Postgres {
    companion object {
        fun init(config: PostgresConfig) = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = config.url
            username = config.user
            password = config.password
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }.let(::HikariDataSource)
    }
}

fun HikariDataSource.connect() {
    Database.connect(this)
}

fun HikariDataSource.flywayMigration() {
    Flyway.configure()
        .dataSource(this)
        .load()
        .migrate()
}
