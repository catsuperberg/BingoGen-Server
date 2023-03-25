package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.common.FloatRange
import dev.catsuperberg.bingogen.server.repository.CsvTaskReader.TaskColumn
import org.joda.time.Period
import org.junit.Test
import kotlin.test.assertEquals


class CsvTaskReaderTest {
    private val testData = CSVTaskReaderTestData
    private val taskReader = CsvTaskReader()

    @Test
    fun testInvalidCSV() {
        val expectedResult = listOf<Task>()
        assertEquals(expectedResult, taskReader.read(""))
        assertEquals(readRow(""), expectedResult)
        assertEquals(expectedResult, readRow(testData.nonTaskCsv))
    }

    @Test
    fun testValidCSV() {
        val expectedResult = listOf(testData.baseResultEntity)
        assertEquals(expectedResult, readRow(testData.baseCSVRow))
    }

    @Test
    fun testBlankAsNullable() {
        val expectedResult = listOf(testData.blankResultEntity)
        assertEquals(expectedResult, readRow(testData.blankCSVRow))
    }

    @Test
    fun testBlankGameAndSheetName() {
        val expectedResultGame = listOf(testData.blankResultEntity.copy().apply { game = "uncategorized" })
        val expectedResultSheet = listOf(testData.blankResultEntity.copy().apply { taskSheet = "uncategorized" })
        assertEquals(expectedResultGame, readRow(testData.noGameCSVRow))
        assertEquals(expectedResultSheet, readRow(testData.noSheetNameCSVRow))
    }

    @Test
    fun testBlankShortName() {
        val expectedResult = listOf<Task>()
        assertEquals(expectedResult, readRow(testData.noShortTextCSVRow))
    }

    @Test
    fun testVariants() {
        val valOne = "test"
        val valTwo = "bonjour"
        val expectedResultValOne = listOf(testData.blankResultEntity.copy().apply { firstVariant = valOne })
        val expectedResultValTwo = listOf(testData.blankResultEntity.copy().apply { secondVariant = valTwo })
        val expectedResultValBoth = listOf(testData.blankResultEntity.copy().apply {
            firstVariant = valOne
            secondVariant = valTwo
        })
        val oneRow = changeColumnValue(testData.blankCSVRow, TaskColumn.VARIANT_ONE.number, valOne)
        val twoRow = changeColumnValue(testData.blankCSVRow, TaskColumn.VARIANT_TWO.number, valTwo)
        val bothRow = changeColumnValue(oneRow, TaskColumn.VARIANT_TWO.number, valTwo)
        assertEquals(expectedResultValOne, readRow(oneRow))
        assertEquals(expectedResultValTwo, readRow(twoRow))
        assertEquals(expectedResultValBoth, readRow(bothRow))
    }

    @Test
    fun testRange() {
        val intRangeRow = changeColumnValue(testData.blankCSVRow, TaskColumn.RANGE.number, "1..5")
        val floatRangeRow = changeColumnValue(testData.blankCSVRow, TaskColumn.RANGE.number, "1.5..5.6")
        val mixedRangeRow = changeColumnValue(testData.blankCSVRow, TaskColumn.RANGE.number, "1..5.6")
        val garbageRangeRow = changeColumnValue(testData.baseCSVRow, TaskColumn.RANGE.number, testData.garbageValue)
        val expectedResultInt = listOf(testData.blankResultEntity.copy().apply { range = FloatRange(1.0f.rangeTo(5.0f)) })
        val expectedResultFloat = listOf(testData.blankResultEntity.copy().apply { range = FloatRange(1.5f.rangeTo(5.6f)) })
        val expectedResultMixed = listOf(testData.blankResultEntity.copy().apply { range = FloatRange(1.0f.rangeTo(5.6f)) })
        val expectedResultGarbage = listOf(testData.baseResultEntity.copy().apply { range = null })
        assertEquals(expectedResultInt, readRow(intRangeRow))
        assertEquals(expectedResultFloat, readRow(floatRangeRow))
        assertEquals(expectedResultMixed, readRow(mixedRangeRow))
        assertEquals(expectedResultGarbage, readRow(garbageRangeRow))
    }

    @Test
    fun testDistribution() {
        val digitOnlyIntRow = changeColumnValue(testData.blankCSVRow, TaskColumn.DISTRIBUTION.number, "5")
        val digitOnlyFloatRow = changeColumnValue(testData.blankCSVRow, TaskColumn.DISTRIBUTION.number, "5.74")
        val withPercentRow = changeColumnValue(testData.blankCSVRow, TaskColumn.DISTRIBUTION.number, "5.74%")
        val expectedResultInt = listOf(testData.blankResultEntity.copy().apply { distribution = 5f })
        val expectedResultFloat = listOf(testData.blankResultEntity.copy().apply { distribution = 5.74f })
        val expectedResultGarbage = listOf(testData.baseResultEntity.copy().apply { distribution = null })
        val garbageRow = changeColumnValue(testData.baseCSVRow, TaskColumn.DISTRIBUTION.number, testData.garbageValue)
        assertEquals(expectedResultInt, readRow(digitOnlyIntRow))
        assertEquals(expectedResultFloat, readRow(digitOnlyFloatRow))
        assertEquals(expectedResultFloat, readRow(withPercentRow))
        assertEquals(expectedResultGarbage, readRow(garbageRow))
    }

    @Test
    fun testDuration() {
        val hoursMinutesSecondsRow = changeColumnValue(testData.blankCSVRow, TaskColumn.TIME.number, "10:05:12")
        val oneDigitRow = changeColumnValue(testData.blankCSVRow, TaskColumn.TIME.number, "1:5:2")
        val bigHoursRow = changeColumnValue(testData.blankCSVRow, TaskColumn.TIME.number, "555:05:12")
        val bigMinutesRow = changeColumnValue(testData.blankCSVRow, TaskColumn.TIME.number, "10:85:12")
        val garbageRow = changeColumnValue(testData.baseCSVRow, TaskColumn.TIME.number, testData.garbageValue)
        val expectedResultHoursMinutesSeconds = listOf(testData.blankResultEntity.copy().apply {
            timeToKeep = Period(10, 5, 12, 0).toStandardDuration()
        })
        val expectedResultOneDigit = listOf(testData.blankResultEntity.copy().apply {
            timeToKeep = Period(1, 5, 2, 0).toStandardDuration()
            })
        val expectedResultBigHours = listOf(testData.blankResultEntity.copy().apply {
            timeToKeep = Period(555, 5, 12, 0).toStandardDuration()
        })
        val expectedResultBigMinutes = listOf(testData.blankResultEntity.copy().apply {
            timeToKeep = Period(10, 85, 12, 0).toStandardDuration()
        })
        val expectedResultGarbage = listOf(testData.baseResultEntity.copy().apply { timeToKeep = null })


        assertEquals(expectedResultHoursMinutesSeconds, readRow(hoursMinutesSecondsRow), "failed on: $hoursMinutesSecondsRow")
        assertEquals(expectedResultOneDigit, readRow(oneDigitRow), "failed on: $oneDigitRow")
        assertEquals(expectedResultBigHours, readRow(bigHoursRow), "failed on: $bigHoursRow")
        assertEquals(expectedResultBigMinutes, readRow(bigMinutesRow), "failed on: $bigMinutesRow")
        assertEquals(expectedResultGarbage, readRow(garbageRow), "failed on: $garbageRow")
    }

    @Test
    fun testFromStart() {
        val capsRow = changeColumnValue(testData.blankCSVRow, TaskColumn.FROM_START.number, "TRUE")
        val lowRow = changeColumnValue(testData.blankCSVRow, TaskColumn.FROM_START.number, "true")
        val sentenceRow = changeColumnValue(testData.blankCSVRow, TaskColumn.FROM_START.number, "True")
        val falseRow = changeColumnValue(testData.blankCSVRow, TaskColumn.FROM_START.number, "FALSE")
        val garbageRow = changeColumnValue(testData.baseCSVRow, TaskColumn.FROM_START.number, testData.garbageValue)
        val expectedResultFalse = listOf(testData.blankResultEntity.copy().apply { fromStart = false })
        val expectedGarbageResultFalse = listOf(testData.baseResultEntity.copy().apply { fromStart = false })
        val expectedResultTrue = listOf(testData.blankResultEntity.copy().apply { fromStart = true })
        assertEquals(expectedResultTrue, readRow(capsRow))
        assertEquals(expectedResultTrue, readRow(lowRow))
        assertEquals(expectedResultTrue, readRow(sentenceRow))
        assertEquals(expectedResultFalse, readRow(testData.blankCSVRow))
        assertEquals(expectedResultFalse, readRow(falseRow))
        assertEquals(expectedGarbageResultFalse, readRow(garbageRow))
    }

    private fun readRow(row: String) = taskReader.read(testData.csvTemplate.format(row))

    private fun changeColumnValue(row: String, columnNumber: Int, value: String) = row.split(",")
        .toMutableList().also { it[columnNumber] = value }.joinToString(",")
}
