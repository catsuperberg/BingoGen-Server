package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.common.FloatRange
import org.joda.time.Duration

object CSVTaskReaderTestData {
    val nonTaskCsv: String = """
            |Name, Age, Gender
            |Alex, 25, Male
            |Sarah, 32, Female
        """.trimMargin()

    private val heads = enumValues<CsvTaskReader.TaskColumn>().toList().map { it.head }
    private val headerString = heads.joinToString(",")
    val csvTemplate = """
            |$headerString
            |%s
        """.trimMargin()

    const val baseCSVRow = "test,test,short,description,subject,var1,var2,1..2,3%,unit,00:05:00,TRUE"
    const val blankCSVRow = "test,test,short,,,,,,,,,"
    const val noGameCSVRow = ",test,short,,,,,,,,,"
    const val noSheetNameCSVRow = "test,,short,,,,,,,,,"
    const val noShortTextCSVRow = "test,test,,,,,,,,,,"

    val baseResultEntity = Task {
        game = "test"
        taskSheet = "test"
        shortText = "short"
        description = "description"
        subject = "subject"
        firstVariant = "var1"
        secondVariant = "var2"
        range = FloatRange(1f, 2f)
        distribution = 3f
        unit = "unit"
        timeToKeep = Duration.standardMinutes(5)
        fromStart = true
    }

    val blankResultEntity = baseResultEntity.copy().also {
        it.description = null
        it.subject = null
        it.firstVariant = null
        it.secondVariant = null
        it.range = null
        it.distribution = null
        it.unit = null
        it.timeToKeep = null
        it.fromStart = false
    }

    const val garbageValue = "22f23f..435.35%"
}
