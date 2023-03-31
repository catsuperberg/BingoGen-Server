package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.common.FloatRange
import org.joda.time.Duration
import org.ktorm.dsl.BatchInsertStatementBuilder
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalTime

interface Task: Entity<Task> {
    companion object : Entity.Factory<Task>()

    val id: Long?
    var game: String
    var taskSheet: String
    var shortText: String
    var description: String?
    var subject: String?
    var firstVariant: String?
    var secondVariant: String?
    var range: FloatRange?
    var distribution: Float?
    var unit: String?
    var timeToKeep: Duration?
    var fromStart: Boolean

    fun stripId(): Task = this.let { source ->
        Task {
            game = source.game
            taskSheet = source.taskSheet
            shortText = source.shortText
            description = source.description
            subject = source.subject
            firstVariant = source.firstVariant
            secondVariant = source.secondVariant
            range = source.range
            distribution = source.distribution
            unit = source.unit
            timeToKeep = source.timeToKeep
            fromStart = source.fromStart
        }
    }
}

object Tasks: Table<Task>("task") {
    const val tasksSchema = """
    CREATE TABLE task(
        id SERIAL NOT NULL PRIMARY KEY,
        game VARCHAR(128) NOT NULL,
        task_sheet VARCHAR(128) NOT NULL,
        short_text VARCHAR(128) NOT NULL,
        description VARCHAR(128),
        subject VARCHAR(128),
        first_variant VARCHAR(128),
        second_variant VARCHAR(128),
        range VARCHAR(128),
        distribution FLOAT,
        unit VARCHAR(128),
        time_to_keep TIME,
        from_start BOOLEAN NOT NULL
    );
    """

    val id = long("id").primaryKey().bindTo { it.id }
    val game = varchar("game").bindTo { it.game }
    val taskSheet = varchar("task_sheet").bindTo { it.taskSheet }
    val shortText = varchar("short_text").bindTo { it.shortText }
    val description = varchar("description").bindTo { it.description }
    val subject = varchar("subject").bindTo { it.subject }
    val firstVariant = varchar("first_variant").bindTo { it.firstVariant }
    val secondVariant = varchar("second_variant").bindTo { it.secondVariant }
    val range = varchar("range").transform(
        { FloatRange.parseFromString(it) ?: FloatRange(0f, 0f) }, { it.toString() }
    ).bindTo { it.range }
    val distribution = float("distribution").bindTo { it.distribution }
    val unit = varchar("unit").bindTo { it.unit }
    val timeToKeep = time("time_to_keep").transform(
        { Duration(it.toNanoOfDay() / 1_000_000) },
        { LocalTime.ofNanoOfDay(it.millis * 1_000_000) }
    ).bindTo {  it.timeToKeep }
    val fromStart = boolean("from_start").bindTo { it.fromStart }

    fun BatchInsertStatementBuilder<Tasks>.item(taskEntity: Task) {
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
