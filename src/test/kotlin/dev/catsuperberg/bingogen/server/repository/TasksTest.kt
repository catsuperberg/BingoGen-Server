package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.common.FloatRange
import dev.catsuperberg.bingogen.server.test.data.common.TestTaskEntities
import org.joda.time.Duration
import org.junit.Test
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.logging.NoOpLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalTime
import kotlin.test.assertEquals

class TasksTest {
    private val testData = TestTaskEntities

    @Test
    fun testRangeSerialization() {
        val range = FloatRange(1f.rangeTo(2f))
        val outputStream = ByteArrayOutputStream()
        ObjectOutputStream(outputStream).use { it.writeObject(range) }
        val serializedBytes = outputStream.toByteArray()
        val inputStream = ByteArrayInputStream(serializedBytes)
        val deserialized = ObjectInputStream(inputStream).let { it.readObject() as FloatRange }
        assertEquals(range, deserialized)
    }

    @Test
    fun testDurationConversion() {
        val duration = Duration.standardSeconds(23)
        val time = LocalTime.ofNanoOfDay(duration.millis * 1_000_000)
        val millis = time.toNanoOfDay() / 1_000_000
        val newDuration = Duration(millis)
        println("duration $duration")
        println("time $time")
        println("millis $millis")
        println("new duration $newDuration")
    }

    @Test
    fun testInOutSame() {
        val database = Database.connect(
            url = "jdbc:h2:mem:${::testInOutSame.name};DB_CLOSE_DELAY=-1;MODE=Postgresql;DATABASE_TO_LOWER=TRUE",
            driver = "org.h2.Driver",
            user = "root",
            password = "",
            logger = NoOpLogger
        ).also { database ->
            database.useConnection { conn ->
                conn.createStatement().execute(Tasks.tasksSchema)
            }

            database.batchInsert(Tasks) {
                testData.multiGameAndSheetEntitiesBlank.forEach { taskEntity ->
                    item {
                        set(it.game, taskEntity.game)
                        set(it.taskSheet, taskEntity.taskSheet)
                        set(it.shortText, taskEntity.shortText)
                        set(it.description, taskEntity.description)
                        set(it.subject, taskEntity.subject)
                        set(it.firstVariant, taskEntity.firstVariant)
                        set(it.secondVariant, taskEntity.secondVariant)
                        set(it.range, taskEntity.range)
                        set(it.distribution, taskEntity.distribution)
                        set(it.unit, taskEntity.unit)
                        set(it.timeToKeep, taskEntity.timeToKeep)
                        set(it.fromStart, taskEntity.fromStart)
                    }
                }
            }
        }

        val result = database.sequenceOf(Tasks).toList().map(Task::stripId)

        assertEquals(testData.multiGameAndSheetEntitiesBlank, result)
    }
}
