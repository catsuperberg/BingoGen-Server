package dev.catsuperberg.bingogen.server.repository

import org.ktorm.logging.Logger
import org.ktorm.logging.NoOpLogger

data class DatabaseInfo(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val logger: Logger = NoOpLogger
) {
    companion object {
        val H2TEST = DatabaseInfo(
            "jdbc:h2:mem:under_test;DB_CLOSE_DELAY=-1;MODE=Postgresql;DATABASE_TO_LOWER=TRUE",
            "org.h2.Driver",
            "root",
            ""
        )
    }
}
