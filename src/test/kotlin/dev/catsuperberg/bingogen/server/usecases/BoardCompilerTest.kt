package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.common.Grid
import dev.catsuperberg.bingogen.server.presentation.TaskDTO
import dev.catsuperberg.bingogen.server.repository.Task
import dev.catsuperberg.bingogen.server.repository.TaskRepository
import dev.catsuperberg.bingogen.server.repository.Tasks
import dev.catsuperberg.bingogen.server.test.data.common.TestTaskEntities
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BoardCompilerTest {
    private val testData = TestTaskEntities

    private val entitiesWithIds = testData.singleSheetEntities.mapIndexed {
            index, task -> task.copy().apply { set(Tasks.id.name, index.toLong()) }
    }.toSet()

    private object TaskMapperFake : ITaskMapper {
        override fun map(task: Task): TaskDTO = TaskDTO(
            task.id ?: throw Exception("Id is null"),
            task.shortText,
            task.description ?: "",
            task.timeToKeep?.millis,
            task.fromStart
        )
    }

    private val repositoryMock = mock<TaskRepository> {
        entitiesWithIds.first().let { task ->
            on { findAllTasks(task.game, task.taskSheet) } doReturn entitiesWithIds
        }
    }

    private val compiler = BoardCompiler(repositoryMock, TaskMapperFake)

    @Test
    fun testNoGame() {
        assertThrows<BoardCompiler.NoTaskSheetFound> {
            entitiesWithIds.first().let { task -> compiler.compile(
                2,
                task.game+"non existent",
                task.taskSheet) }
        }
    }

    fun testNoSheet() {
        assertThrows<BoardCompiler.NoTaskSheetFound> {
            entitiesWithIds.first().let { task -> compiler.compile(
                2,
                task.game,
                task.taskSheet+"non existent") }
        }
    }

    fun testNoGameAndSheet() {
        assertThrows<BoardCompiler.NoTaskSheetFound> {
            entitiesWithIds.first().let { task -> compiler.compile(
                2,
                task.game+"non existent",
                task.taskSheet+"non existent") }
        }
    }

    @Test
    fun testNotEnoughTasks() {
        assertThrows<BoardCompiler.NotEnoughEntriesException> {
            entitiesWithIds.first().let { task -> compiler.compile(
                entitiesWithIds.size+1,
                task.game,
                task.taskSheet) }
        }
    }

    @Test
    fun testEachLineBeatable() {
        val board = Grid(entitiesWithIds.first().let { task -> compiler.compile(5, task.game, task.taskSheet) }.rows)
        val lines = board.rows + board.columns
        lines.forEach { line ->
            assertEquals(line.size, line.distinctBy { it.dbid }.size)
        }
    }

    @Test
    fun testIsRandom() {
        fun <T> List<T>.containsSublist(subList: List<T>) = windowed(subList.size, 1).any { it == subList }

        repeat(2000) {
            val sourceIds = entitiesWithIds.mapNotNull { it.id }
            val sortedSourceIds = sourceIds.sorted()
            val board = Grid(entitiesWithIds.first().let { task -> compiler.compile(5, task.game, task.taskSheet) }.rows)
            val rowSequence = board.rows.flatten().map { it.dbid }
            val columnSequence = board.columns.flatten().map { it.dbid }
            val ordered = rowSequence.sorted()

            val hasRowWindow = sourceIds.containsSublist(rowSequence) || sortedSourceIds.containsSublist(rowSequence)
            val hasColumnWindow = sourceIds.containsSublist(columnSequence) || sortedSourceIds.containsSublist(columnSequence)
            val hasOrderedWindow = sourceIds.containsSublist(ordered) || sortedSourceIds.containsSublist(ordered)

            assertFalse(hasRowWindow)
            assertFalse(hasColumnWindow)
            assertFalse(hasOrderedWindow)
        }
    }

    @Test
    fun testCount() {
        (2..6).forEach(::testCountAsExpected)
    }

    private fun testCountAsExpected(sideCount: Int) {
        val expectedCount = sideCount * sideCount
        val board = Grid(entitiesWithIds.first().let { task -> compiler.compile(sideCount, task.game, task.taskSheet) }.rows)
        val count = board.count
        assertEquals(expectedCount, count)
    }
}
