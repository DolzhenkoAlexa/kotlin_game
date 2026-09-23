package main.kotlin.history.repository

data class PlayersStatistics(
    val totalGames: Int,
    val players: Map<String, PlayerStats>
)

data class PlayerStats(
    val gamesPlayed: Int,
    val wins: Int
)