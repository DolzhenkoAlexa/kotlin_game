package test.kotlin

import main.kotlin.engine.GameEvent
import main.kotlin.history.repository.SqliteRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SqliteRepositoryTests {
    @TempDir
    lateinit var tempDir: Path

    private lateinit var repo: SqliteRepository

    @BeforeEach
    fun setUp() {
        repo = SqliteRepository(tempDir.resolve("test.sqlite").toString())
    }

    @Test
    fun newRepositoryIsEmpty() {
        assertTrue(repo.getAllGames().isEmpty()) {
            "Только созданный репозиторий не должен содержать игр"
        }

        val stats = repo.getStatistics()
        assertEquals(0, stats.totalGames) {
            "totalGames у пустого репозитория должен быть 0, получено ${stats.totalGames}"
        }
        assertTrue(stats.players.isEmpty()) {
            "players у пустого репозитория должен быть пустым, получено ${stats.players}"
        }
    }

    @Test
    fun getGameWithUnknownIdReturnsNull() {
        assertNull(repo.getGame(999)) {
            "getGame для отсутствующего id должен вернуть null"
        }
    }

    @Test
    fun getPlayerStatsForUnknownPlayerReturnsNull() {
        assertNull(repo.getPlayerStats("Призрак")) {
            "getPlayerStats для неизвестного игрока должен вернуть null"
        }
    }

    @Test
    fun saveGameStoresAllFieldsCorrectly() {
        val event = GameEvent(1, "A", "Рыцарь", "AttackAction", "Маг", 7, 55, 48)
        repo.saveGame("Классический", "A", "B", "A", listOf(event))

        val game = repo.getGame(1)
        assertNotNull(game) { "Игра с id=1 должна существовать" }
        game!!

        assertEquals(1, game.id)
        assertEquals("Классический", game.mode)
        assertEquals("A", game.player1)
        assertEquals("B", game.player2)
        assertEquals("A", game.winner)
        assertEquals(1, game.events.size)
        assertEquals(7, game.events[0].damage)
        assertEquals(55, game.events[0].healthBefore)
        assertEquals(48, game.events[0].healthAfter)
    }

    @Test
    fun saveAndLoadSingleGameRoundTrip() {
        val events = listOf(
            GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48),
            GameEvent(2, "Игрок2", "Маг", "AttackAction", "Рыцарь", 10, 60, 50),
            GameEvent(3, "Игрок1", "Рыцарь", "UltimateAction", "Маг", 35, 48, 13)
        )
        repo.saveGame("С маной", "Игрок1", "Игрок2", "Игрок1", events)

        val loaded = repo.getGame(1)
        assertNotNull(loaded)
        loaded!!

        assertEquals("С маной", loaded.mode)
        assertEquals("Игрок1", loaded.winner)
        assertEquals(3, loaded.events.size)
        assertEquals(events.map { it.turnNumber }, loaded.events.map { it.turnNumber })
        assertEquals(events.map { it.damage }, loaded.events.map { it.damage })
    }

    @Test
    fun saveAndLoadMultipleGames() {
        repo.saveGame("Классический", "A", "B", "A",
            listOf(GameEvent(1, "A", "Рыцарь", "AttackAction", "Маг", 8, 55, 47)))

        repo.saveGame("Командный", "C", "D", "D",
            listOf(GameEvent(1, "C", "Маг", "AttackAction", "Лучник", 13, 60, 47)))

        val all = repo.getAllGames()
        assertEquals(2, all.size)
        assertEquals(listOf("A", "C"), all.map { it.player1 })
        assertEquals(listOf("A", "D"), all.map { it.winner })
    }


    @Test
    fun statisticsCountGamesAndWinsCorrectly() {
        // A wins 2, B wins 1, C wins 0
        repo.saveGame("Классический", "A", "B", "A", emptyList())
        repo.saveGame("Классический", "A", "C", "A", emptyList())
        repo.saveGame("Классический", "B", "C", "B", emptyList())

        val stats = repo.getStatistics()
        assertEquals(3, stats.totalGames)

        assertEquals(2, stats.players["A"]?.gamesPlayed)
        assertEquals(2, stats.players["A"]?.wins)

        assertEquals(2, stats.players["B"]?.gamesPlayed)
        assertEquals(1, stats.players["B"]?.wins)

        assertEquals(2, stats.players["C"]?.gamesPlayed)
        assertEquals(0, stats.players["C"]?.wins)
    }

    @Test
    fun getPlayerStatsReturnsDataForSpecificPlayer() {
        repo.saveGame("Классический", "A", "B", "A", emptyList())
        repo.saveGame("Классический", "A", "C", "C", emptyList())

        val statsA = repo.getPlayerStats("A")
        assertNotNull(statsA)
        assertEquals(2, statsA!!.gamesPlayed)
        assertEquals(1, statsA.wins)

        val statsC = repo.getPlayerStats("C")!!
        assertEquals(1, statsC.gamesPlayed)
        assertEquals(1, statsC.wins)
    }

    @Test
    fun reopeningRepositoryReadsSameData() {
        val dbPath = tempDir.resolve("persistent.sqlite").toString()

        // First connection — write
        SqliteRepository(dbPath).saveGame(
            "Классический", "A", "B", "A",
            listOf(GameEvent(1, "A", "Рыцарь", "AttackAction", "Маг", 5, 55, 50))
        )

        // Second connection — read
        val repo2 = SqliteRepository(dbPath)
        val game = repo2.getGame(1)
        assertNotNull(game) {
            "Данные должны сохраниться между запусками приложения"
        }
        assertEquals("A", game!!.winner)
        assertEquals(1, game.events.size)
    }

    @Test
    fun fullGameCycleWithStatisticsAndReplay() {
        // Players play a match
        val events = listOf(
            GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 8, 55, 47),
            GameEvent(2, "Игрок2", "Маг", "AttackAction", "Рыцарь", 7, 60, 53),
            GameEvent(3, "Игрок1", "Рыцарь", "UltimateAction", "Маг", 35, 47, 12),
            GameEvent(4, "Игрок2", "Маг", "UltimateAction", "Рыцарь", 10, 53, 43),
            GameEvent(5, "Игрок1", "Рыцарь", "AttackAction", "Маг", 12, 12, 0)
        )

        // save match
        repo.saveGame("Классический", "Игрок1", "Игрок2", "Игрок1", events)

        // open saved games
        val games = repo.getAllGames()
        assertEquals(1, games.size)
        val saved = games.first()
        assertEquals("Классический", saved.mode)
        assertEquals("Игрок1", saved.winner)
        assertEquals(5, saved.events.size)

        // open statistics
        val stats = repo.getStatistics()
        assertEquals(1, stats.totalGames)
        assertEquals(1, stats.players["Игрок1"]?.wins)
        assertEquals(1, stats.players["Игрок2"]?.gamesPlayed)
        assertEquals(0, stats.players["Игрок2"]?.wins)

        // Open replay
        val replay = repo.getGame(saved.id)
        assertNotNull(replay)
        assertEquals(events.size, replay!!.events.size)

        // Check that all events are present and in the right order
        replay.events.forEachIndexed { idx, event ->
            assertEquals(events[idx].turnNumber, event.turnNumber) {
                "Событие #$idx: turnNumber не совпадает"
            }
            assertEquals(events[idx].playerName, event.playerName) {
                "Событие #$idx: playerName не совпадает"
            }
            assertEquals(events[idx].actionType, event.actionType) {
                "Событие #$idx: actionType не совпадает"
            }
        }
    }
}