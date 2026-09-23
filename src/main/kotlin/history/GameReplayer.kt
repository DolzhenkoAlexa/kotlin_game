package main.kotlin.history

import main.kotlin.engine.GameEvent
import main.kotlin.history.repository.Repository
import main.kotlin.ui.UserInterface

class GameReplayer(
    private val ui: UserInterface,
    private val repository: Repository
) {
    fun replayGame(id: Int) {
        val game = repository.getGame(id)
        if (game == null) {
            ui.showMessage("Игра с ID $id не найдена")
            return
        }
        replay(game.events)
    }

    private fun replay(events: List<GameEvent>) {
        if (events.isEmpty()) {
            ui.showMessage("Нет событий для воспроизведения")
            return
        }

        ui.showMessage("\n=== Воспроизведение игры ===")
        ui.showMessage("Всего ходов: ${events.size}")
        ui.showMessage("Начинаем...")
        ui.showMessage("")

        events.forEach { event ->
            ui.showMessage("Ход ${event.turnNumber}: ${event.playerName} (${event.actorType})")
            ui.showMessage("  Действие: ${event.actionType}")
            ui.showMessage("  Цель: ${event.targetType ?: "нет"}")
            if (event.damage != null) {
                ui.showMessage("  Урон: ${event.damage}")
            }
            ui.showMessage("")
        }

        ui.showMessage("=== Конец воспроизведения ===")
    }
}