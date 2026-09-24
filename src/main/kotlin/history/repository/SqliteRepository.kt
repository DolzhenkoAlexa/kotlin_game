package main.kotlin.history.repository

import main.kotlin.engine.GameEvent
import java.sql.Connection
import java.sql.DriverManager
import java.time.LocalDate

class SqliteRepository(
    private val dbPath: String = "sqlHistory/BattleEvents.sqlite"
) : Repository {

    init {
        java.io.File(dbPath).parentFile?.mkdirs()
    }

    private val connection: Connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").also { initDatabase(it) }
    }

    private fun initDatabase(conn: Connection) {
        conn.createStatement().use { st ->
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS games (
                    id      INTEGER PRIMARY KEY AUTOINCREMENT,
                    date    TEXT NOT NULL,
                    mode    TEXT NOT NULL,
                    player1 TEXT NOT NULL,
                    player2 TEXT NOT NULL,
                    winner  TEXT NOT NULL
                )
            """.trimIndent())

            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game_events (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_id       INTEGER NOT NULL,
                    turn_number   INTEGER NOT NULL,
                    player_name   TEXT NOT NULL,
                    actor_type    TEXT NOT NULL,
                    action_type   TEXT NOT NULL,
                    target_type   TEXT,
                    damage        INTEGER,
                    health_before INTEGER,
                    health_after  INTEGER,
                    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
                )
            """.trimIndent())
        }
    }

    override fun saveGame(
        mode: String,
        player1: String,
        player2: String,
        winner: String,
        events: List<GameEvent>
    ) {
        connection.autoCommit = false
        try {
            val gameId = insertGame(mode, player1, player2, winner)
            insertEvents(gameId, events)
            connection.commit()
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }

    private fun insertGame(mode: String, p1: String, p2: String, winner: String): Int {
        connection.prepareStatement("""
            INSERT INTO games (date, mode, player1, player2, winner)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()).use { ps ->
            ps.setString(1, LocalDate.now().toString())
            ps.setString(2, mode)
            ps.setString(3, p1)
            ps.setString(4, p2)
            ps.setString(5, winner)
            ps.executeUpdate()
        }

        connection.createStatement().use { st ->
            st.executeQuery("SELECT last_insert_rowid()").use { rs ->
                rs.next()
                return rs.getInt(1)
            }
        }
    }

    private fun insertEvents(gameId: Int, events: List<GameEvent>) {
        connection.prepareStatement("""
            INSERT INTO game_events
                (game_id, turn_number, player_name, actor_type, action_type,
                 target_type, damage, health_before, health_after)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()).use { ps ->
            for (e in events) {
                ps.setInt(1, gameId)
                ps.setInt(2, e.turnNumber)
                ps.setString(3, e.playerName)
                ps.setString(4, e.actorType)
                ps.setString(5, e.actionType)
                ps.setString(6, e.targetType)
                ps.setObject(7, e.damage)
                ps.setObject(8, e.healthBefore)
                ps.setObject(9, e.healthAfter)
                ps.addBatch()
            }
            ps.executeBatch()
        }
    }

    override fun getAllGames(): List<GameSummary> {
        val result = mutableListOf<GameSummary>()
        connection.createStatement().use { st ->
            st.executeQuery("""
                SELECT id, date, mode, player1, player2, winner
                FROM games ORDER BY id
            """.trimIndent()).use { rs ->
                while (rs.next()) {
                    val id = rs.getInt("id")
                    result.add(
                        GameSummary(
                            id = id,
                            date = rs.getString("date"),
                            mode = rs.getString("mode"),
                            player1 = rs.getString("player1"),
                            player2 = rs.getString("player2"),
                            winner = rs.getString("winner"),
                            events = loadEvents(id)
                        )
                    )
                }
            }
        }
        return result
    }

    override fun getGame(id: Int): GameSummary? {
        connection.prepareStatement("""
            SELECT id, date, mode, player1, player2, winner
            FROM games WHERE id = ?
        """.trimIndent()).use { ps ->
            ps.setInt(1, id)
            ps.executeQuery().use { rs ->
                if (!rs.next()) return null
                return GameSummary(
                    id = rs.getInt("id"),
                    date = rs.getString("date"),
                    mode = rs.getString("mode"),
                    player1 = rs.getString("player1"),
                    player2 = rs.getString("player2"),
                    winner = rs.getString("winner"),
                    events = loadEvents(id)
                )
            }
        }
    }

    private fun loadEvents(gameId: Int): List<GameEvent> {
        val events = mutableListOf<GameEvent>()
        connection.prepareStatement("""
            SELECT turn_number, player_name, actor_type, action_type,
                   target_type, damage, health_before, health_after
            FROM game_events WHERE game_id = ? ORDER BY id
        """.trimIndent()).use { ps ->
            ps.setInt(1, gameId)
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    events.add(
                        GameEvent(
                            turnNumber = rs.getInt("turn_number"),
                            playerName = rs.getString("player_name"),
                            actorType = rs.getString("actor_type"),
                            actionType = rs.getString("action_type"),
                            targetType = rs.getString("target_type"),
                            damage = rs.getObject("damage") as? Int,
                            healthBefore = rs.getObject("health_before") as? Int,
                            healthAfter = rs.getObject("health_after") as? Int
                        )
                    )
                }
            }
        }
        return events
    }

    override fun getStatistics(): PlayersStatistics {
        val players = mutableMapOf<String, PlayerStats>()
        var totalGames = 0

        connection.createStatement().use { st ->
            st.executeQuery("""
                SELECT player, COUNT(*) AS games, SUM(win) AS wins FROM (
                    SELECT player1 AS player, CASE WHEN winner = player1 THEN 1 ELSE 0 END AS win FROM games
                    UNION ALL
                    SELECT player2 AS player, CASE WHEN winner = player2 THEN 1 ELSE 0 END AS win FROM games
                ) GROUP BY player
            """.trimIndent()).use { rs ->
                while (rs.next()) {
                    players[rs.getString("player")] =
                        PlayerStats(rs.getInt("games"), rs.getInt("wins"))
                }
            }

            st.executeQuery("SELECT COUNT(*) FROM games").use { rs ->
                if (rs.next()) totalGames = rs.getInt(1)
            }
        }

        return PlayersStatistics(totalGames, players)
    }

    override fun getPlayerStats(playerName: String): PlayerStats? =
        getStatistics().players[playerName]
}