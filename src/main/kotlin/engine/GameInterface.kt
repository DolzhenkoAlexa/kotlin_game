package main.kotlin.engine

import main.kotlin.entities.Player
import main.kotlin.actions.Action
import main.kotlin.entities.Character

interface GameInterface {
    fun startGame(player1: Player, player2: Player)
    fun processTurn(player: Player, action: Action?)
    fun processTurnWithTarget(player: Player, action: Action?, actor: Character, target: Character?)
    fun getCurrentState(): GameState
    fun isGameOver(): Boolean
    fun getEvents(): List<GameEvent>
}