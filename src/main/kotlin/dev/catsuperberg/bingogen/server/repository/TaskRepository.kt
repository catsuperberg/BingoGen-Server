package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.repository.Tasks.item
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toSet

class ArgumentNullOrEmptyException(message: String) : Exception(message)

class TaskRepository(
    private val database: Database,
    tasks: List<Task>
) {
    init {
        if(tasks.isEmpty())
            throw ArgumentNullOrEmptyException("${TaskRepository::javaClass.name} was provided with empty tasks, database can't be filled")
        dropTable()
        createTable()
        fillTable(tasks)
    }

    private fun dropTable() {
        database.useConnection { conn ->
            conn.createStatement().execute("DROP TABLE IF EXISTS ${Tasks.tableName};")
        }
    }

    private fun createTable() {
        database.useConnection { conn ->
            conn.createStatement().execute(Tasks.tasksSchema)
        }
    }

    private fun fillTable(tasks: List<Task>) {
        database.batchInsert(Tasks) {
            tasks.forEach { item(it) }
        }
    }

    fun findAllGames() = database.from(Tasks).selectDistinct(Tasks.game).mapNotNull { it[Tasks.game] }.toSet()

    fun findAllTaskSheets(game: String): Set<String>  = database.from(Tasks)
        .selectDistinct(Tasks.game, Tasks.taskSheet)
        .where { Tasks.game eq game}
        .mapNotNull { it[Tasks.taskSheet] }
        .toSet()

    fun findAllTasks(game: String, sheet: String): Set<Task> = database.sequenceOf(Tasks)
        .filter { it.game eq game }
        .filter { it.taskSheet eq sheet }
        .toSet()
}
