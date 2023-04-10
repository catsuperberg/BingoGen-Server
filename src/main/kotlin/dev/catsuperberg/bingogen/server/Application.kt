package dev.catsuperberg.bingogen.server

import dev.catsuperberg.bingogen.server.presentation.configureRouting
import dev.catsuperberg.bingogen.server.presentation.configureSerialization
import dev.catsuperberg.bingogen.server.presentation.configureSockets
import dev.catsuperberg.bingogen.server.repository.CsvTaskReader
import dev.catsuperberg.bingogen.server.repository.TaskIngest
import dev.catsuperberg.bingogen.server.repository.TaskRepository
import dev.catsuperberg.bingogen.server.repository.Tasks
import dev.catsuperberg.bingogen.server.usecases.BoardCompiler
import dev.catsuperberg.bingogen.server.usecases.TaskComposer
import io.ktor.server.application.*
import org.ktorm.database.Database
import org.ktorm.logging.NoOpLogger

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val ingestFolder = environment.config.property("ktor.server.asset_ingest_folder").getString()
    val taskReader = CsvTaskReader()
    val tasks = TaskIngest(ingestFolder, taskReader).load()
    val database = Database.connect(
        url = "jdbc:h2:mem:reference;DB_CLOSE_DELAY=-1;MODE=Postgresql;DATABASE_TO_LOWER=TRUE",
        driver = "org.h2.Driver",
        user = "root",
        password = "",
        logger = NoOpLogger
    ).also { database ->
        database.useConnection { conn ->
            conn.createStatement().execute(Tasks.tasksSchema)
        }
    }

    val repository = TaskRepository(database, tasks)
    val boardCompiler = BoardCompiler(repository, TaskComposer())

    configureSerialization()
    configureSockets()
    configureRouting(repository, boardCompiler)
}
