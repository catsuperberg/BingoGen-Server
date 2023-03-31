package dev.catsuperberg.bingogen.server

import dev.catsuperberg.bingogen.server.presentation.configureRouting
import dev.catsuperberg.bingogen.server.presentation.configureSerialization
import dev.catsuperberg.bingogen.server.presentation.configureSockets
import dev.catsuperberg.bingogen.server.repository.CsvTaskReader
import dev.catsuperberg.bingogen.server.repository.TaskIngest
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val ingestFolder = environment.config.property("ktor.server.asset_ingest_folder").getString()
    val taskReader = CsvTaskReader()
    val taskIngest = TaskIngest(ingestFolder, taskReader)
    configureSerialization()
    configureSockets()
    configureRouting()
}
