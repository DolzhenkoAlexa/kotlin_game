package main.kotlin.history

import main.kotlin.engine.GameEvent

interface GameHistory {
    fun recordEvent(event: GameEvent)
    fun saveGame(events: List<GameEvent>, winner: String)
    fun loadGame(id: Int): List<GameEvent>?
}