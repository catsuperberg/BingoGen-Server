package dev.catsuperberg.bingogen.server.common

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

class FloatRange(private var range: ClosedRange<Float>) : Serializable, ClosedRange<Float> by range {
    companion object {
        private val exception = NumberFormatException("Wrong string format to parse by ${FloatRange::javaClass.name}")

        fun parseFromString(value: String) = try { FloatRange(parseRange(value)) } catch (e: NumberFormatException) { null }

        private fun parseRange(value: String): ClosedRange<Float> {
            val values = value.split("..")
            if (values.size != 2) throw exception
            return try { values.map { it.toFloat() }.let { it.first().rangeTo(it.last()) } } catch (e: Exception) {
                throw exception
            }
        }
    }

    override val start: Float
        get() = range.start

    override val endInclusive: Float
        get() = range.endInclusive

    constructor(start: Float, endInclusive: Float) : this(start.rangeTo(endInclusive))

    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(range.toString())
    }

    private fun readObject(inp: ObjectInputStream) {
        range = parseRange((inp.readObject() as String))
    }

    override fun toString(): String = range.toString()

    override fun equals(other: Any?) = this === other || other is FloatRange && range == other.range

    override fun hashCode() = range.hashCode()
}
