package dev.catsuperberg.bingogen.server.common

import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.assertEquals

class FloatRangeTest {
    @Test
    fun testRangeSerialization() {
        val base = 1f.rangeTo(2f)
        val range = FloatRange(base)
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use { it.writeObject(range) }
        val serializedBytes = outputStream.toByteArray()
        val inputStream = ByteArrayInputStream(serializedBytes)
        val deserialized = ObjectInputStream(inputStream).let { it.readObject() as FloatRange }
        assertEquals(range, deserialized)
        assertValuesAsBase(base, deserialized)
    }

    @Test
    fun testBaseClassAccess() {
        val base = 1f.rangeTo(2f)
        val range = FloatRange(base)
        assertValuesAsBase(base, range)
    }

    private fun assertValuesAsBase(
        base: ClosedFloatingPointRange<Float>,
        range: FloatRange
    ) {
        assertEquals(base.start, range.start)
        assertEquals(base.endInclusive, range.endInclusive)
    }
}
