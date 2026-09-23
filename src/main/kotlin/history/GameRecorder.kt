package main.kotlin.history

import main.kotlin.engine.GameEvent
import main.kotlin.history.repository.Repository

class GameRecorder(
    private val repository: Repository
) : GameHistory {

    private var mode: String = ""
    private var player1: String = ""
    private var player2: String = ""

    override fun recordEvent(event: GameEvent) {
        // Метод оставлен для совместимости с интерфейсом, но не используется
        // Сохранение происходит целиком в конце игры через saveGame()
    }

    override fun saveGame(events: List<GameEvent>, winner: String) {
        repository.saveGame(
            mode = mode,
            player1 = player1,
            player2 = player2,
            winner = winner,
            events = events
        )
    }

    override fun loadGame(id: Int): List<GameEvent>? {
        return repository.getGame(id)?.events
    }

    fun setGameInfo(mode: String, player1: String, player2: String) {
        this.mode = mode
        this.player1 = player1
        this.player2 = player2
    }
}