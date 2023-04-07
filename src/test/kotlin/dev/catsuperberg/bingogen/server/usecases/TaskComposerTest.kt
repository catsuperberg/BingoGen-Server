package dev.catsuperberg.bingogen.server.usecases

import dev.catsuperberg.bingogen.server.common.FloatRange
import dev.catsuperberg.bingogen.server.repository.Task
import dev.catsuperberg.bingogen.server.repository.Tasks
import org.joda.time.DateTime
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.math.sign
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class TaskComposerTest {
    private val composer = TaskComposer()
    private val noIdTask = Task {}
    private val blankWithId = Task {}.also { it[Tasks.id.name] = 1L }

    private val baseString = "Bonjour %s hello"
    private val charStartSting = "%s bonjour hello"
    private val subjectChar = "\$s"
    private val variantChar = "\$v"
    private val midRangeChar = "\$mr"
    private val rangeChar = "\$r"
    private val unitChar = "\$u"
    private val testString = "test"
    private val testStringVariant = "toast"

    private val testRanges = listOf(
        FloatRange(100f, 5000f),
        FloatRange(100f, 500f),
        FloatRange(10f, 50f),
        FloatRange(1f, 5f),
        FloatRange(0.1f, 0.5f),
        FloatRange(0.01f, 0.05f),
        FloatRange(0.05f, 0.051f),
        FloatRange(0.034f, 0.035f),
        FloatRange(-0.05f, -0.01f),
        FloatRange(1.4E-45f, 2.8E-45f),
    )

    @Test
    fun testNoIdIllegalState() {
        assertThrows<IllegalStateException> { composer.map(noIdTask) }
    }

    @Test
    fun testSubjectInsertion() {
        testSimpleStringInsertion(subjectChar, Tasks.subject.name)
    }

    @Test
    fun testVariantInsertion() {
        val string = baseString.format(variantChar)
        val expectedFirstVar = baseString.format(testString)
        val expectedSecondVar = baseString.format(testStringVariant)
        val firstVarTask = blankWithId.copy().apply {
            this.shortText = string
            this.firstVariant = testString
        }
        val secondVarTask = blankWithId.copy().apply {
            this.shortText = string
            this.secondVariant = testStringVariant
        }
        val bothVarTask = blankWithId.copy().apply {
            this.shortText = string
            this.firstVariant = testString
            this.secondVariant = testStringVariant
        }

        assertEquals(expectedFirstVar, composer.map(firstVarTask).shortText)
        assertEquals(expectedSecondVar, composer.map(secondVarTask).shortText)
        val randomResults = List(100) { composer.map(bothVarTask).shortText }
        assertTrue(randomResults.distinct().size > 1)
    }

    @Test
    fun testMidRangeInsertion() {
        val range = FloatRange(1f, 1.001f)
        val task = blankWithId.copy().apply {
            this.shortText = midRangeChar
            this.range = range
        }
        val randomResults = List(100) { composer.map(task).shortText.toFloat() }
        assertTrue(randomResults.all { range.contains(it) })
    }

    @Test
    fun testMidRangeRounding() {
        val tasks = testRanges.map { range ->
            blankWithId.copy().apply {
                this.shortText = midRangeChar
                this.range = range
            }
        }
        val stopwatch = DateTime.now().millis
        val results = tasks
            .mapNotNull { task -> task.range?.let{ it to List(100) { composer.map(task).shortText } } }
            .toMap()
        println("Time to execute: ${DateTime.now().millis - stopwatch}")
        results.forEach { range ->
            assertTrue(
                range.value.all { result -> numberOfSignificantDigitsInRange(result) },
                message = "$range: ${range.value}"
            )
        }
    }

    private fun numberOfSignificantDigitsInRange(number: String, range: IntRange = 1..1): Boolean {
        val string = number.replace(".", "")
        val firstSignificant = string.takeWhile { it !in '1'..'9' }.count()
        val firstAfterSignificant = (firstSignificant+range.last).coerceIn(0..string.length)
        val afterSignificant = string.substring(firstAfterSignificant)
        val significant = string.substring(firstSignificant, firstAfterSignificant)
        return significant.length in range && afterSignificant.none { it in '1'..'9'}
    }
    @Test
    fun testRangeInsertion() {
        val tasks = testRanges.map { range ->
            blankWithId.copy().apply {
                this.shortText = rangeChar
                this.range = range
                this.distribution = 10f
            }
        }
        val randomResults = tasks.map { task -> task to List(100) { composer.map(task).shortText } }.toMap()
        randomResults.forEach { taskResults ->
            taskResults.value.forEach { string ->
                val randomizedRange = FloatRange.parseRange(string)
                val sourceRange = taskResults.key.range ?: fail("Task with empty source range")
                val distribution = taskResults.key.distribution ?: fail("Task with empty distribution")
                val sourceRangeWithDistribution = FloatRange(
                    sourceRange.start * (1 - distribution * sourceRange.start.sign),
                    sourceRange.endInclusive * (1 + distribution * sourceRange.start.sign)
                )
                assertTrue(message = "Should be inside: $sourceRangeWithDistribution but is $randomizedRange") {
                    sourceRangeWithDistribution.contains(randomizedRange.start) &&
                            sourceRangeWithDistribution.contains(randomizedRange.endInclusive)
                }
            }
        }
    }

    @Test
    fun testUnitInsertion() {
        testSimpleStringInsertion(unitChar, Tasks.unit.name)
    }

    @Test
    fun testFloatFormatting() {
        val splitChar = "|"
        val tasks = testRanges.map { range ->
            blankWithId.copy().apply {
                this.shortText = "$midRangeChar$splitChar$rangeChar"
                this.range = range
                this.distribution = 8f
            }
        }

        val results = tasks
            .mapNotNull { task -> task.range?.let { it to List(100) { composer.map(task).shortText.split("|") } } }
            .toMap()
        val midRangeFloatStrings = results.map { taskResult -> taskResult.key to taskResult.value.map { it[0] } } .toMap()
        val rangeMinMaxFloatStrings = results.map { taskResult ->
            taskResult.key to taskResult.value.map { it[1] }.flatMap { it.split("..") }
        }.toMap()

        midRangeFloatStrings.forEach { range ->
            assertTrue(
                range.value.all { result -> numberOfSignificantDigitsInRange(result) },
                message = "$range: ${range.value}"
            )
        }
        rangeMinMaxFloatStrings.forEach { range ->
            range.value.forEach {
                assertTrue(
                    numberOfSignificantDigitsInRange(it, 1..3),
                    message = "Value: $it | $range: ${range.value}"
                )
            }

        }
    }

    @Test
    fun testSentenceCase() {
        testFirstCharCapital(baseString, "TEST")
        testFirstCharCapital(charStartSting, "TEST")
        testFirstCharCapital(charStartSting, "test")
    }

    private fun testFirstCharCapital(base: String, testString: String) {
        val text = base.format(subjectChar)
        val expectedString = base.format(testString)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val task = blankWithId.copy().apply { this.shortText = text; this.description = text }
            .also { it[Tasks.subject.name] = testString }
        val result = composer.map(task)
        assertEquals(expectedString, result.shortText)
        assertEquals(expectedString, result.description)
    }

    @Test
    fun testRemovePadding() {
        val insertion = "bonjour"
        val string = "   %stest    "
        val expectedSting = "Test"
        val expectedInsertion = "Bonjourtest"
        val task = blankWithId.copy().apply { this.shortText = string.format("") }
        val taskInsertion = blankWithId.copy().apply { this.shortText = string.format(subjectChar) }
            .also { it[Tasks.subject.name] = insertion }

        assertEquals(expectedSting, composer.map(task).shortText)
        assertEquals(expectedInsertion, composer.map(taskInsertion).shortText)
    }

    private fun testSimpleStringInsertion(characterToReplace: String, fieldToTest: String, text: String = testString) {
        val shortText = baseString.format(characterToReplace)
        val expectedShortText = baseString.format(text)
        val task = blankWithId.copy().apply { this.shortText = shortText }.also { it[fieldToTest] = text }
        val result = composer.map(task)
        assertEquals(expectedShortText, result.shortText)
    }
}
