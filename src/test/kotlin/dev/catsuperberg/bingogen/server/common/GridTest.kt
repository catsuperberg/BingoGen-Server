package dev.catsuperberg.bingogen.server.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class GridTest {
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

    @Test
    fun testColumnsProperty() {
        assertEquals(defaultColumns, defaultGrid.columns)
    }

    @Test
    fun testColumn() {
        repeat(defaultGrid.size) {
            assertEquals(defaultColumns[it], defaultGrid.column(it))
        }
    }

    @Test
    fun testRow() {
        repeat(defaultGrid.size) {
            assertEquals(defaultGrid[it], defaultGrid.row(it))
        }
    }

    @Test
    fun testSerialization() {
        val serializedGrid = Json.encodeToString(defaultGrid)
        val deserializedGrid = Json.decodeFromString<Grid<Int>>(serializedGrid)

        assertEquals(defaultGrid, deserializedGrid)
    }
}
