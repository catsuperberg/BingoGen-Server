package dev.catsuperberg.bingogen.server.repository

import dev.catsuperberg.bingogen.server.common.TestConfiguration
import dev.catsuperberg.bingogen.server.repository.Tasks.item
import dev.catsuperberg.bingogen.server.test.data.common.TestTaskEntities
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.ktorm.database.Database
import org.ktorm.dsl.batchInsert
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.logging.NoOpLogger
import org.postgresql.util.PSQLException
import kotlin.test.assertEquals

@RunWith(value = Parameterized::class)
class TaskRepositoryTest(private val dbInfo: DatabaseInfo) {
    companion object {
        val testData = TestTaskEntities
        lateinit var referenceDatabase: Database
        lateinit var databaseUnderTest: Database

        @JvmStatic
        @BeforeClass
        fun initializeOnce(): Unit {
            referenceDatabase = Database.connect(
                url = "jdbc:h2:mem:reference;DB_CLOSE_DELAY=-1;MODE=Postgresql;DATABASE_TO_LOWER=TRUE",
                driver = "org.h2.Driver",
                user = "root",
                password = "",
                logger = NoOpLogger
            ).also { database ->
                database.useConnection { conn ->
                    conn.createStatement().execute(Tasks.tasksSchema)
                }

                database.batchInsert(Tasks) {
                    testData.multiGameAndSheetEntities.forEach { item(it) }
                }
            }
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<DatabaseInfo> = TestConfiguration.databasesToTest
    }

    @Before
    fun setUp() {
        databaseUnderTest = Database.connect(
            url = dbInfo.url,
            driver = dbInfo.driver,
            user = dbInfo.user,
            password = dbInfo.password,
            logger = dbInfo.logger
        )
    }

    @After
    fun tearDown() {
        val dbName = databaseUnderTest.useConnection { conn -> conn.catalog }
        try {
            databaseUnderTest.useConnection { conn ->
                conn.createStatement().execute("DROP ALL OBJECTS")
            }
        } catch (_: PSQLException) {}
        try {
            databaseUnderTest.useConnection { conn ->
                conn.createStatement().execute("DROP SCHEMA public CASCADE")
                conn.createStatement().execute("CREATE SCHEMA public")
            }
        } catch (_: JdbcSQLSyntaxErrorException) {}
    }

    @Test
    fun testInitialization() {
        TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities)

        val expectedTable = referenceDatabase.sequenceOf(Tasks).toList()
        val actualTable = databaseUnderTest.sequenceOf(Tasks).toList()
        assertEquals(expectedTable, actualTable)
    }

    @Test
    fun testTableDroppedOnReinit() {
        TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities)
        assertDoesNotThrow { TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities) }

        val expectedTable = referenceDatabase.sequenceOf(Tasks).toList()
        val actualTable = databaseUnderTest.sequenceOf(Tasks).toList()
        assertEquals(expectedTable, actualTable)
    }

    @Test
    fun testBlankInitiation() {
        assertThrows<ArgumentNullOrEmptyException> { TaskRepository(databaseUnderTest, listOf()) }
    }

    @Test
    fun testFindGames() {
        val expectedGames = testData.multiGameAndSheetEntities.map { it.game }.distinct().toSet()
        val repository = TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities)
        assertEquals(expectedGames, repository.findAllGames())
    }

    @Test
    fun testFindTaskSheets() {
        val games = testData.multiGameAndSheetEntities.map { it.game }.distinct().toSet()
        val repository = TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities)
        games.forEach { game ->
            val expectedTaskSheets = testData.multiGameAndSheetEntities.filter { it.game == game }.map { it.taskSheet }.distinct().toSet()
            assertEquals(expectedTaskSheets, repository.findAllTaskSheets(game))
        }
    }

    @Test
    fun testFindTasks() {
        val games = testData.multiGameAndSheetEntities.map { it.game }.distinct().toSet()
        val repository = TaskRepository(databaseUnderTest, testData.multiGameAndSheetEntities)
        val taskSheets = games.flatMap { game ->
            testData.multiGameAndSheetEntities.filter { it.game == game }.map { it.taskSheet }.distinct().map { game to it }
        }
        taskSheets.forEach { sheet ->
            val expectedTasks = testData.multiGameAndSheetEntities.filter { it.game == sheet.first && it.taskSheet == sheet.second }
            assertEquals(expectedTasks, repository.findAllTasks(sheet.first, sheet.second).map(Task::stripId))
        }
    }
}
