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
    private val events = mutableListOf<GameEvent>()  // ← храним события

    override fun startGame(player1: Player, player2: Player) {
        state = GameState(player1, player2, turn = 1)
        gameOver = false
        events.clear()
    }

    override fun processTurn(player: Player, action: Action?) {
        val actor = player.getAliveHeroes().firstOrNull()
        if (actor == null) {
            println("${player.name}: нет живых героев!")
            return
        }

        val opponent = getOpponent(player)
        val target = opponent.getAliveHeroes().firstOrNull()

        if (action == null) {
            println("${player.name}: действие не выбрано!")
            return
        }

        action.execute(actor, target)

        val event = GameEvent(
            turnNumber = state!!.turn,
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

        state!!.turn++

        if (!opponent.hasAliveHeroes()) {
            gameOver = true
            state = state!!.copy(winner = player.name)
            println("\n ${player.name} победил! Все враги повержены!")
        }
    }

    override fun processTurnWithTarget(player: Player, action: Action?, actor: Character, target: Character?) {
        if (action == null) {
            println("${player.name}: действие не выбрано!")
            return
        }

        val healthBefore = target?.health ?: 0  // запоминаем HP до атаки
        action.execute(actor, target)
        val healthAfter = target?.health ?: 0  // запоминаем HP после атаки

        val event = GameEvent(
            turnNumber = state!!.turn,
            playerName = player.name,
            actorType = actor.type,
            actionType = action.javaClass.simpleName,
            targetType = target?.type,
            damage = healthBefore - healthAfter,
            healthBefore = healthBefore,
            healthAfter = healthAfter
        )
        events.add(event)
        history?.recordEvent(event)

        state!!.turn++

        val opponent = getOpponent(player)
        if (!opponent.hasAliveHeroes()) {
            gameOver = true
            state = state!!.copy(winner = player.name)
            println("\n ${player.name} победил! Все враги повержены!")
        }
    }

    override fun getCurrentState(): GameState = state ?: throw IllegalStateException("Игра не запущена")

    override fun isGameOver(): Boolean = gameOver

    override fun getEvents(): List<GameEvent> = events.toList()

    private fun getOpponent(player: Player): Player {
        return if (state!!.player1 == player) state!!.player2 else state!!.player1
    }
}