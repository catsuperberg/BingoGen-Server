package dev.catsuperberg.bingogen.server.presentation

import dev.catsuperberg.bingogen.server.repository.TaskRepository
import dev.catsuperberg.bingogen.server.usecases.BoardCompiler
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: TaskRepository, boardCompiler: BoardCompiler) {
    routing {
        getAllGames(repository)
        getAllSheetsByGame(repository)
        getBoardByGameAndSheet(boardCompiler)
    }
}

private fun Routing.getAllGames(repository: TaskRepository) {
    get("/game") {
        call.respond(repository.findAllGames())
    }
}

private fun Routing.getAllSheetsByGame(repository: TaskRepository) {
    get("/game/{game_name?}") {
        val game = call.parameters["game_name"] ?: return@get call.respondBadRequest("Missing game_name")
        val taskSheet = repository.findAllTaskSheets(game).ifEmpty {
            return@get call.respondText(
                "No task sheets for $game",
                status = HttpStatusCode.NotFound
            )
        }
        call.respond(taskSheet)
    }
}

private fun Routing.getBoardByGameAndSheet(boardCompiler: BoardCompiler) {
    get("/board/") {
        val sideCount =
            call.parameters["side_count"]?.toInt() ?: return@get call.respondBadRequest("Missing side_count")
        val game = call.parameters["game_name"] ?: return@get call.respondBadRequest("Missing game_name")
        val taskSheet = call.parameters["task_sheet"] ?: return@get call.respondBadRequest("Missing task_sheet")

        try {
            val taskBoard = boardCompiler.compile(sideCount, game, taskSheet)
            call.respond(taskBoard)
        } catch (e: BoardCompiler.NoTaskSheetFound) {
            return@get call.respondBadRequest("Compiler exception: ${e.message}")
        } catch (e: BoardCompiler.NotEnoughEntriesException) {
            return@get call.respondBadRequest("Task sheet too small for the board size")
        }
    }
}

suspend fun ApplicationCall.respondBadRequest(message: String) = respondText(message, status = HttpStatusCode.BadRequest)
