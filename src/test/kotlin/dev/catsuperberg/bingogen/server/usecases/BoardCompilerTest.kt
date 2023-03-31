package dev.catsuperberg.bingogen.server.usecases

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

    private val repositoryMock = mock<TaskRepository> {
        entitiesWithIds.first().let { task ->
            on { findAllTasks(task.game, task.taskSheet) } doReturn entitiesWithIds
        }
    }

    private val compiler = BoardCompiler(repositoryMock)

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
        val board = entitiesWithIds.first().let { task -> compiler.compile(5, task.game, task.taskSheet) }
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
            val board = entitiesWithIds.first().let { task -> compiler.compile(5, task.game, task.taskSheet) }
            val rowSequence = board.flatten().map { it.dbid }
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
        val board = entitiesWithIds.first().let { task -> compiler.compile(sideCount, task.game, task.taskSheet) }
        val count = board.flatten().size
        assertEquals(expectedCount, count)
    }
}
