package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.test.data.common.TestTaskEntities

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

    val baseResultEntity = TestTaskEntities.baseResultEntity

    val blankResultEntity = TestTaskEntities.blankResultEntity

    const val garbageValue = "22f23f..435.35%"
}
