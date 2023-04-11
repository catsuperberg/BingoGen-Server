package dev.catsuperberg.bingogen.server.repository

import org.ktorm.logging.Logger
import org.ktorm.logging.NoOpLogger

data class DatabaseCredentials(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val logger: Logger = NoOpLogger
) {
    companion object {
        val H2TEST = DatabaseCredentials(
            "jdbc:h2:mem:under_test;DB_CLOSE_DELAY=-1;MODE=Postgresql;DATABASE_TO_LOWER=TRUE",
            "org.h2.Driver",
            "root",
            ""
        )

        fun fromConfigStrings(strings: List<String>) = DatabaseCredentials(strings[0], strings[1], strings[2], strings[3])
    }
}
