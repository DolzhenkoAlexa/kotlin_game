package main.kotlin.engine

import main.kotlin.entities.Player
import main.kotlin.actions.Action
import main.kotlin.history.GameHistory
import main.kotlin.entities.Character

class GameEngine(
    private val history: GameHistory? = null
) : GameInterface {

    private var state: GameState? = null
    private var gameOver = false
    private val events = mutableListOf<GameEvent>()

    override fun startGame(player1: Player, player2: Player) {
        state = GameState(player1, player2, turn = 1)
        gameOver = false
        events.clear()
    }

    override fun processTurn(player: Player, action: Action?) {
        val currentState = state ?: throw IllegalStateException("Игра не запущена")

        val opponent = getOpponent(player)

        // Проверяем победу в начале хода
        if (!player.hasAliveHeroes()) {
            gameOver = true
            state = currentState.copy(winner = opponent.name)
            println("\n${opponent.name} победил! У ${player.name} нет живых героев!")
            return
        }

        val actor = player.getAliveHeroes().firstOrNull()
        if (actor == null) {
            println("${player.name}: нет живых героев!")
            return
        }

        val target = opponent.getAliveHeroes().firstOrNull()

        if (action == null) {
            println("${player.name}: действие не выбрано!")
            return
        }

        action.execute(actor, target)

        val event = GameEvent(
            turnNumber = currentState.turn,
            playerName = player.name,
            actorType = actor.type,
            actionType = action.javaClass.simpleName,
            targetType = target?.type,
            damage = null,
            healthBefore = null,
            healthAfter = null
        )
        events.add(event)
        history?.recordEvent(event)

        state = currentState.copy(turn = currentState.turn + 1)

        if (!opponent.hasAliveHeroes()) {
            gameOver = true
            state = currentState.copy(winner = player.name)
            println("\n ${player.name} победил! Все враги повержены!")
        }
    }

    override fun getCurrentState(): GameState = state ?: throw IllegalStateException("Игра не запущена")

    override fun isGameOver(): Boolean = gameOver

    override fun getEvents(): List<GameEvent> = events.toList()

    private fun getOpponent(player: Player): Player {
        val currentState = state ?: throw IllegalStateException("Игра не запущена")
        return if (currentState.player1 == player) currentState.player2 else currentState.player1
    }
}