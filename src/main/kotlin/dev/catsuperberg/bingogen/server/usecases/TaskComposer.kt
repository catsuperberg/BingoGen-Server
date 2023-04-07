package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.common.RandomBoolCache
import dev.catsuperberg.bingogen.server.common.RandomFloatCache
import dev.catsuperberg.bingogen.server.presentation.TaskDTO
import dev.catsuperberg.bingogen.server.repository.Task
import java.text.DecimalFormat
import java.util.*
import kotlin.math.*

class TaskComposer : ITaskMapper {
    private val randomBoolCache = RandomBoolCache(333)
    private val randomFloatCache = RandomFloatCache(444)

    override fun map(task: Task): TaskDTO {
        val taskId = task.id ?: throw IllegalStateException("Task transformation only works on entity from db that has it's id")
        val subject = task.subject
        val variant = listOfNotNull(task.firstVariant, task.secondVariant)
            .let { if(randomBoolCache.next()) it.firstOrNull() else it.lastOrNull() }
        val midRange = task.range?.let { randomFloatCache.nextInRange(it.start, it.endInclusive).roundToFirst() } // rounding to first only so that int values are produced for when you need only them
        val range = midRange?.let { mr -> task.distribution?.let { distribution ->
                val offset = mr * (distribution / 100)
                "${(mr - offset).toRoundedString(3)}..${(mr + offset).toRoundedString(3)}"
            }
        }
        var shortText = task.shortText
        var description = task.description ?: shortText

        val interpolateField = { charToChange: String, value: String ->
            shortText = shortText.replace(charToChange, value)
            description = description.replace(charToChange, value)
        }

        subject?.also { interpolateField("\$s", it) }
        variant?.also { interpolateField("\$v", it) }
        midRange?.also { interpolateField("\$mr", it.toRoundedString()) }
        range?.also { interpolateField("\$r", it) }
        task.unit?.also { interpolateField("\$u", it) }

        return TaskDTO(
            taskId,
            shortText.trim().sentenceCase(),
            description.trim().sentenceCase(),
            task.timeToKeep?.millis,
            task.fromStart
        )
    }

    private fun String.sentenceCase() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    private fun Float.roundToFirst(): Float {
        val powerOf10 = 10.0f.pow(floor(log10(abs(this))))
        return (this / powerOf10).roundToInt() * powerOf10
    }

    private val intFormat = DecimalFormat("#")
    private val formatLookup = (0 until 48).associateWith { index ->
        DecimalFormat("#.${"#".repeat(index)}")
    }

    private fun Float.toRoundedString(significantCount: Int = 1): String {
        log10(abs(this)).toInt().also { exponent ->
            if(exponent <= 0) formatLookup[-(exponent-significantCount)]?.also { return it.format(this) }
            return intFormat.format(this)
        }
    }
}
