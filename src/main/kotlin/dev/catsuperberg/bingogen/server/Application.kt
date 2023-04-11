package dev.catsuperberg.bingogen.server

import dev.catsuperberg.bingogen.server.common.config.Configuration
import dev.catsuperberg.bingogen.server.presentation.configureRouting
import dev.catsuperberg.bingogen.server.presentation.configureSerialization
import dev.catsuperberg.bingogen.server.presentation.configureSockets
import dev.catsuperberg.bingogen.server.repository.CsvTaskReader
import dev.catsuperberg.bingogen.server.repository.TaskIngest
import dev.catsuperberg.bingogen.server.repository.TaskRepository
import dev.catsuperberg.bingogen.server.usecases.BoardCompiler
import dev.catsuperberg.bingogen.server.usecases.TaskComposer
import io.ktor.server.application.*
import org.ktorm.database.Database
import org.ktorm.logging.NoOpLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val config = Configuration.create(environment)
    val ingestFolder = config.assetIngestFolder
    val taskReader = CsvTaskReader()
    val tasks = TaskIngest(ingestFolder, taskReader).load()
    val database = config.databaseCredentials.let { credentials ->
        Database.connect(
            url = credentials.url,
            driver = credentials.driver,
            user = credentials.user,
            password = credentials.password,
            logger = NoOpLogger
        )
    }

    val repository = TaskRepository(database, tasks)
    val boardCompiler = BoardCompiler(repository, TaskComposer())

    configureSerialization()
    configureSockets()
    configureRouting(repository, boardCompiler)
}
