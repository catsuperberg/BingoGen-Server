package dev.catsuperberg.bingogen.server.common

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
        )
    )

    private val defaultColumns = listOf(
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(3, 6, 9)
    )

    private val nonSquareColumns = listOf(
        listOf(1, 4, 7),
        listOf(2, 5, 8, 7),
        listOf(2, 5),
        listOf(3, 6, 9)
    )

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
        assertThrows<IllegalArgumentException> { Grid(nonSquareColumns) }
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
        val propertyResult = defaultGrid.count
        val flattenResult = defaultGrid.rows.flatten().size
        val results = listOf(propertyResult, flattenResult)
        results.forEach { assertEquals(expectedCount, it) }
    }

    @Test
    fun testEquals() {
        val createdGrid = Grid(defaultGrid.rows)
        assertNotEquals(defaultGrid.hashCode(), createdGrid.hashCode())
        assertNotSame(defaultGrid, createdGrid)
        assertEquals(defaultGrid, createdGrid)
    }
}
