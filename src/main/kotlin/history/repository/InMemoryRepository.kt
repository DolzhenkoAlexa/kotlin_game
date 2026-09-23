package main.kotlin.history.repository

import main.kotlin.engine.GameEvent
import java.time.LocalDate

class InMemoryRepository : Repository {

    private val games = mutableListOf<GameSummary>()
    private var nextId = 1

    override fun saveGame(
        mode: String,
        player1: String,
        player2: String,
        winner: String,
        events: List<GameEvent>
    ) {
        val summary = GameSummary(
            id = nextId,
            date = LocalDate.now().toString(),
            mode = mode,
            player1 = player1,
            player2 = player2,
            winner = winner,
            events = events
        )
        games.add(summary)
        nextId++
    }

    override fun getAllGames(): List<GameSummary> = games.toList()

    override fun getGame(id: Int): GameSummary? = games.find { it.id == id }

    override fun getStatistics(): PlayersStatistics {
        val stats = mutableMapOf<String, PlayerStats>()

        games.forEach { game ->
            val p1Stats = stats.getOrPut(game.player1) { PlayerStats(0, 0) }
            stats[game.player1] = p1Stats.copy(
                gamesPlayed = p1Stats.gamesPlayed + 1,
                wins = p1Stats.wins + (if (game.winner == game.player1) 1 else 0)
            )

            val p2Stats = stats.getOrPut(game.player2) { PlayerStats(0, 0) }
            stats[game.player2] = p2Stats.copy(
                gamesPlayed = p2Stats.gamesPlayed + 1,
                wins = p2Stats.wins + (if (game.winner == game.player2) 1 else 0)
            )
        }

        return PlayersStatistics(
            totalGames = games.size,
            players = stats
        )
    }

    override fun getPlayerStats(playerName: String): PlayerStats? {
        return getStatistics().players[playerName]
    }
}