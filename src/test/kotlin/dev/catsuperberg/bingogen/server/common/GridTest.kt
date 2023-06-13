package dev.catsuperberg.bingogen.server.common

import dev.catsuperberg.bingogen.server.common.Grid.Companion.toGrid
import dev.catsuperberg.bingogen.server.presentation.TaskDTO
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class GridTest {
    private val defaultSideCount = 3
    private val defaultGrid = Grid(
        listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9)
        ).flatten()
    )

    private val defaultColumns = listOf(
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(3, 6, 9)
    )

    private val nonSquareSequence = listOf(
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(2, 5),
    ).flatten()

    @Test
    fun testColumnsProperty() {
        assertEquals(defaultColumns, defaultGrid.columns)
    }

    @Test
    fun testColumn() {
        repeat(defaultGrid.sideCount) {
            assertEquals(defaultColumns[it], defaultGrid.column(it))
        }
    }

    @Test
    fun testRow() {
        repeat(defaultGrid.sideCount) {
            assertEquals(defaultGrid.rows[it], defaultGrid.row(it))
        }
    }

    @Test
    fun testNonSquare() {
        assertThrows<IllegalArgumentException> { Grid(nonSquareSequence) }
    }

    @Test
    fun testSideCount() {
        val expectedCount = defaultSideCount
        val propertyResult = defaultGrid.sideCount
        val rowsResult = defaultGrid.rows.size
        val columnResult = defaultGrid.columns.size
        val results = listOf(propertyResult, rowsResult, columnResult)
        results.forEach { assertEquals(expectedCount, it) }
    }

    @Test
    fun testCount() {
        val expectedCount = defaultSideCount * defaultSideCount
        val propertyResult = defaultGrid.size
        val flattenResult = defaultGrid.rows.flatten().size
        val results = listOf(propertyResult, flattenResult)
        results.forEach { assertEquals(expectedCount, it) }
    }

    @Test
    fun testEquals() {
        val grid = Grid(defaultGrid.toList())
        assertNotSame(defaultGrid, grid)
        assertEquals(defaultGrid, grid)
    }

    @Test
    fun testEqualityAsList() {
        val taskOne = TaskDTO(1, "test", "test", 2L, true)
        val taskTwo = taskOne.copy()
        val taskThree = taskTwo.copy()
        val taskFour = taskThree.copy()
        assertTrue(taskOne !== taskTwo)
        assertTrue(taskTwo !== taskThree)
        assertEquals(taskOne, taskTwo)
        val set = setOf(taskOne, taskTwo)
        assertTrue(set.size == 1)

        val list1 = listOf(taskOne, taskTwo, taskThree)
        val list2 = listOf(taskOne.copy(), taskTwo.copy(), taskThree.copy())
        assertTrue(list1.first() !== list2.first())
        val listSet = setOf(list1, list2)
        assertTrue(listSet.size == 1)

        val listList1 = listOf(
            listOf(taskOne, taskTwo),
            listOf(taskThree, taskFour),
        )
        val listList2 = listOf(
            listOf(taskOne.copy(), taskTwo.copy()),
            listOf(taskThree.copy(), taskFour.copy()),
        )
        val listListSet = setOf(listList1, listList2)
        assertEquals(listList1, listList2)
        assertEquals(listList1.hashCode(), listList2.hashCode())
        assertTrue(listList1 !== listList2)
        assertTrue(listListSet.size == 1)

        val grid1 = Grid(listOf(taskOne, taskTwo, taskThree, taskFour))
        val grid2 = Grid(listOf(taskOne.copy(), taskTwo.copy(), taskThree.copy(), taskFour.copy()))
        assertEquals(grid1, grid2)
        assertEquals(grid1.hashCode(), grid2.hashCode())
        assertTrue(grid1 !== grid2)
        val gridSet = setOf(grid1, grid2)
        assertTrue(gridSet.size == 1)
    }

    @Test
    fun testGetOperatorMappedAsSequentialRows() {
        val grid = Grid(defaultGrid.toList())

        assertEquals(1, grid[0])
        assertEquals(2, grid[1])
        assertEquals(5, grid[4])
        assertEquals(9, grid[8])
    }

    @Test
    fun testToGrid() {
        val grid = defaultGrid.toList().toGrid()
        assertEquals(defaultGrid, grid)
    }
}
