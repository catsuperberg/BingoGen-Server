package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.presentation.TaskDTO
import dev.catsuperberg.bingogen.server.repository.Task

interface ITaskMapper {
    fun map(task: Task): TaskDTO
}
