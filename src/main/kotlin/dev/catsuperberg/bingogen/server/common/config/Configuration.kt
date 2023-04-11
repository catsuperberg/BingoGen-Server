package dev.catsuperberg.bingogen.server.common.config

import dev.catsuperberg.bingogen.server.repository.DatabaseCredentials
import io.ktor.server.application.*

enum class ConfigSource(val type: String) {
    ENV("ENV variable"),
    CONFIG("config file entry"),
    DEFAULT("default value"),
    NONE("nowhere")
}

data class Configuration(
    val databaseCredentials: DatabaseCredentials,
    val assetIngestFolder: String
) {
    companion object {
        fun create(appEnv: ApplicationEnvironment): Configuration {
            var dbFrom = ConfigSource.NONE
            val dbCredentials = (
                    System.getenv("DB_CREDENTIALS")?.split(",")
                        .also { dbFrom = ConfigSource.ENV } ?:
                    appEnv.config.propertyOrNull("repository.database_credentials")?.getList()
                        .also { dbFrom = ConfigSource.CONFIG}
                    )?.let(DatabaseCredentials::fromConfigStrings) ?: DatabaseCredentials.H2TEST
                        .also { dbFrom = ConfigSource.DEFAULT }

            var ingestFrom = ConfigSource.NONE
            val ingestFolder = System.getenv("ASSET_INGEST")
                    ?.also { ingestFrom = ConfigSource.ENV } ?:
                appEnv.config.property("ingest.asset_folder").getString()
                    .also { ingestFrom = ConfigSource.CONFIG }

            val result =  Configuration(
                databaseCredentials = dbCredentials,
                assetIngestFolder = ingestFolder
            )
            println("Database credentials retrieved from ${dbFrom.type}")
            println("Ingest folder retrieved from ${ingestFrom.type}")
            return result
        }
    }
}
