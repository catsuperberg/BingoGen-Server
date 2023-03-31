package dev.catsuperberg.bingogen.server.common

import kotlinx.serialization.Serializable

@Serializable
class Grid<T>(val rows: List<List<T>>) : List<List<T>> by rows {
    val columns: List<List<T>>
        get() = List(size) { column -> List(size) { row -> this[row][column] } }

    fun column(index: Int): List<T> = List(this.size) { row -> this[row][index] }
    fun row(index: Int): List<T> = this[index]

    override fun toString(): String = rows.toString()

    override fun equals(other: Any?) = this === other || other is Grid<*> && rows == other.rows

    override fun hashCode() = rows.hashCode()
}
