package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.common.Grid
import dev.catsuperberg.bingogen.server.presentation.GridDTO
import dev.catsuperberg.bingogen.server.repository.TaskRepository

class BoardCompiler(private val repository: TaskRepository, private val taskMapper: ITaskMapper) {
    class NoTaskSheetFound(message: String) : Exception(message)
    class NotEnoughEntriesException(message: String) : Exception(message)

    fun compile(sideCount: Int, game: String, taskSheet: String): GridDTO {
        val taskCount = sideCount * sideCount
        val rawTasks = repository.findAllTasks(game, taskSheet)
        if(rawTasks.isEmpty()) {
            val otherTaskSheets = repository.findAllTaskSheets(game)
            val otherString = if
                    (otherTaskSheets.isNotEmpty()) ". But task sheets found for $game: $otherTaskSheets"
            else
                ". Available games: ${repository.findAllGames()}"
            throw NoTaskSheetFound("No tasks found for combination of $game - $taskSheet$otherString")
        }
        if(rawTasks.size < taskCount)
            throw NotEnoughEntriesException("Not enough tasks in database for board with side of $sideCount")
        val tasks = rawTasks.shuffled().take(taskCount)
        val dtoList = tasks.map(taskMapper::map)
        val grid = Grid(dtoList.chunked(sideCount))
        return GridDTO(grid.rows)
    }
}
