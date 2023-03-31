package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.common.Grid
import dev.catsuperberg.bingogen.server.presentation.TaskDTO
import dev.catsuperberg.bingogen.server.repository.Task
import dev.catsuperberg.bingogen.server.repository.TaskRepository
import kotlin.random.Random

class BoardCompiler(private val repository: TaskRepository) {
    class NotEnoughEntriesException(message: String) : Exception(message)

    fun compile(sideCount: Int, game: String, taskSheet: String): Grid<TaskDTO> {
        val taskCount = sideCount * sideCount
        val rawTasks = repository.findAllTasks(game, taskSheet)
        if(rawTasks.size < taskCount)
            throw NotEnoughEntriesException("Not enough tasks in database for board with side of $sideCount")
        val tasks = rawTasks.shuffled().take(taskCount)
        val dtoList = tasks.map { it.transform() }
        return Grid(dtoList.chunked(sideCount))
    }


    // TODO simple implementation, should apply sentence case and be done in separate class to enable testability
    private fun Task.transform(): TaskDTO {
        val taskId = id ?: throw IllegalStateException("Task transformation only works on entity from db that has it's id")
        val subject = this.subject
        val variants = listOf(this.firstVariant, this.secondVariant).ifEmpty { null }
        val variant = variants?.random()
        val midRange = this.range?.let { Random.nextDouble(it.start.toDouble(), it.endInclusive.toDouble()) }
        val (offsetPlus, offsetMinus) = ((this.distribution ?: 0f) / 100).let { Pair(1 + it, 1 - it) }
        val range = midRange?.let { it * offsetMinus..it * offsetPlus }

        var shortText = this.shortText
        var description = this.description ?: shortText
        subject?.also {
            shortText = shortText.replace("\$s", it)
            description = description.replace("\$s", it)
        }
        variant?.also {
            shortText = shortText.replace("\$v", it)
            description = description.replace("\$v", it)
        }
        midRange?.also {
            shortText = shortText.replace("\$mr", "$it")
            description = description.replace("\$mr", "$it")
        }
        range?.also {
            shortText = shortText.replace("\$r", "$it")
            description = description.replace("\$r", "$it")
        }
        this.unit?.also {
            shortText = shortText.replace("\$u", it)
            description = description.replace("\$u", it)
        }
        return TaskDTO(taskId, shortText, description, this.timeToKeep?.millis, this.fromStart)
    }
}
