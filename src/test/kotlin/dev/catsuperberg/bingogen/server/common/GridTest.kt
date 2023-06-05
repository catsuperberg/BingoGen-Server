package dev.catsuperberg.bingogen.server.common

import dev.catsuperberg.bingogen.server.common.Grid.Companion.toGrid
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

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
        assertNotEquals(defaultGrid.hashCode(), grid.hashCode())
        assertNotSame(defaultGrid, grid)
        assertEquals(defaultGrid, grid)
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
