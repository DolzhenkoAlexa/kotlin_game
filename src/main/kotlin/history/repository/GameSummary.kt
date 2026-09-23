package main.kotlin.history.repository

import main.kotlin.engine.GameEvent

data class GameSummary(
    val id: Int,
    val date: String,
    val mode: String,
    val player1: String,
    val player2: String,
    val winner: String,
    val events: List<GameEvent>
)