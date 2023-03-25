package dev.catsuperberg.bingogen.server.repository

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import dev.catsuperberg.bingogen.server.common.FloatRange
import org.joda.time.format.PeriodFormatterBuilder

typealias TaskRow = Map<String, String>

class CsvTaskReader() : IFileTaskReader {
    enum class TaskColumn(val number: Int, val head: String) {
        GAME(0, "Game"),
        SHEET(1, "Task sheet"),
        SHORT_TEXT(2, "Short text"),
        DESCRIPTION(3, "Description"),
        SUBJECT(4, "Subject"),
        VARIANT_ONE(5, "Variant 1"),
        VARIANT_TWO(6, "Variant 2"),
        RANGE(7, "Range"),
        DISTRIBUTION(8, "Distribution"),
        UNIT(9, "Unit"),
        TIME(10, "Time to keep"),
        FROM_START(11, "From the beginning")
    }

    private val distributionRegex = Regex("^([+-]?\\d+(\\.\\d+)?|\\d+(\\.\\d+)?)(%?)$")

    private val reader = CsvReader()
    override val fileExtension: String = "csv"
    private val durationFormatter = PeriodFormatterBuilder()
        .appendHours()
        .appendLiteral(":")
        .appendMinutes()
        .appendLiteral(":")
        .appendSeconds()
        .toFormatter()

    override fun read(data: String) = try {
            val csvData = reader.readAllWithHeader(data)
            csvData.mapNotNull(::readCsvTask)
        } catch (e: Exception) {
            listOf()
        }

    private fun readCsvTask(row: TaskRow): Task? {
        val gameValue = row.readString(TaskColumn.GAME.head) ?: "uncategorized"
        val taskSheetValue = row.readString(TaskColumn.SHEET.head) ?: "uncategorized"
        val shortTextValue = row.readString(TaskColumn.SHORT_TEXT.head) ?: return null

        return Task {
            game = gameValue
            taskSheet = taskSheetValue
            shortText = shortTextValue
            description = row.readString(TaskColumn.DESCRIPTION.head)
            subject = row.readString(TaskColumn.SUBJECT.head)
            firstVariant = row.readString(TaskColumn.VARIANT_ONE.head)
            secondVariant = row.readString(TaskColumn.VARIANT_TWO.head)
            range = row.readString(TaskColumn.RANGE.head)?.let(FloatRange::parseFromString)
            distribution = row.readString(TaskColumn.DISTRIBUTION.head)?.let(::parseDistribution)
            unit = row.readString(TaskColumn.UNIT.head)
            timeToKeep = row.readString(TaskColumn.TIME.head)
                ?.let {
                    try {
                        durationFormatter.parsePeriod(it).toStandardDuration()
                    } catch (e: Exception) {
                        null
                    }
                }
            fromStart = row.readString(TaskColumn.FROM_START.head)?.let { it.equals("true", true) } ?: false
        }
    }

    private fun parseDistribution(value: String) = distributionRegex.find(value)?.groups?.get(1)?.value?.toFloatOrNull()

    private fun TaskRow.readString(columnName: String) = this[columnName]?.takeIf { it.isNotEmpty() }
}
