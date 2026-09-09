package main.kotlin.history.repository

import main.kotlin.engine.GameEvent

interface Repository {
    fun saveGame(
        mode: String,
        player1: String,
        player2: String,
        winner: String,
        events: List<GameEvent>
    )

    fun getAllGames(): List<GameSummary>
    fun getGame(id: Int): GameSummary?
    fun getStatistics(): PlayersStatistics
    fun getPlayerStats(playerName: String): PlayerStats?
}