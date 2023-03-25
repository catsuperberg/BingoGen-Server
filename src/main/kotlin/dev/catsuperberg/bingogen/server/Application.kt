package dev.catsuperberg.bingogen.server

import dev.catsuperberg.bingogen.server.plugins.configureRouting
import dev.catsuperberg.bingogen.server.plugins.configureSerialization
import dev.catsuperberg.bingogen.server.plugins.configureSockets
import dev.catsuperberg.bingogen.server.repository.CsvTaskReader
import dev.catsuperberg.bingogen.server.repository.TaskIngest
import dev.catsuperberg.bingogen.server.repository.transform
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val ingestFolder = environment.config.property("ktor.server.asset_ingest_folder").getString()
    val taskReader = CsvTaskReader()
    val taskIngest = TaskIngest(ingestFolder, taskReader)
    taskIngest.load().map { it.transform() }.forEach { println("${it.shortText} | ${it.description}") }
    configureSerialization()
    configureSockets()
    configureRouting()
}
