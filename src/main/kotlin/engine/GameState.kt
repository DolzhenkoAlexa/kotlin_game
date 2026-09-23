package main.kotlin.engine

import main.kotlin.entities.Player

data class GameState(
    val player1: Player,
    val player2: Player,
    var turn: Int = 1,
    var winner: String? = null
)