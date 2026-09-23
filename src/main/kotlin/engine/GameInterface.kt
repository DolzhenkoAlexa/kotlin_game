package main.kotlin.engine

import main.kotlin.entities.Player
import main.kotlin.actions.Action

interface GameInterface {
    fun startGame(player1: Player, player2: Player)
    fun processTurn(player: Player, action: Action?)
    fun getCurrentState(): GameState
    fun isGameOver(): Boolean
    fun getEvents(): List<GameEvent>
}