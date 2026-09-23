package main.kotlin.ui

import main.kotlin.engine.GameEngine
import main.kotlin.entities.Player
import main.kotlin.gamemodes.*
import main.kotlin.history.GameRecorder
import main.kotlin.history.GameReplayer
import main.kotlin.history.repository.Repository

class MenuRouter(
    private val ui: UserInterface,
    private val repository: Repository
) {
    private val replayer = GameReplayer(ui, repository)

    fun startNewGame() {
        val modeChoice = selectGameMode()
        val modeName = modeName(modeChoice)

        val (player1, player2) = getPlayerNames()

        val recorder = GameRecorder(repository)
        recorder.setGameInfo(modeName, player1.name, player2.name)

        val engine = GameEngine(recorder)
        val mode = createGameMode(modeChoice, engine)

        mode.startGame(player1, player2)

        val winner = engine.getCurrentState().winner
            ?: error("Победитель не определён")
        recorder.saveGame(engine.getEvents(), winner)
        ui.showMessage("\nИгра сохранена в истории!")
    }

    fun showStatistics() {
        val stats = repository.getStatistics()
        ui.showGameStatistics(stats)
    }

    fun replayGame() {
        val games = repository.getAllGames()

        if (games.isEmpty()) {
            ui.showMessage("Нет сохранённых игр")
            return
        }

        ui.showMessage("\n=== СОХРАНЁННЫЕ ИГРЫ ===")
        games.forEachIndexed { index, game ->
            ui.showMessage("${index + 1}. ${game.date} | ${game.player1} vs ${game.player2} | Победитель: ${game.winner} | ${game.mode}")
        }

        var choice: Int? = null
        while (choice == null || choice !in 0..games.size) {
            choice = ui.readInt("Выберите номер игры для воспроизведения (0 - отмена):")
            if (choice == null || choice !in 0..games.size) {
                ui.showMessage("Неверный номер! Введите число от 0 до ${games.size}")
            }
        }

        if (choice == 0) {
            ui.showMessage("Воспроизведение отменено")
            return
        }

        val game = games[choice - 1]
        replayer.replayGame(game.id)
    }

    private fun selectGameMode(): Int? {
        while (true) {
            val choice = ui.readInt("""
                Выберите режим:
                1 - Классический
                2 - С маной
                3 - Командный
            """.trimIndent())
            if (choice in 1..3) return choice
            ui.showMessage("Неверный выбор! Введите число от 1 до 3")
        }
    }

    private fun getPlayerNames(): Pair<Player, Player> {
        while (true) {
            val name1 = ui.readString("Введите имя игрока 1:")
            val name2 = ui.readString("Введите имя игрока 2:")
            if (name1 != name2) {
                return Pair(Player(name1), Player(name2))
            }
            ui.showMessage("Имена игроков не могут совпадать! Попробуйте снова.")
        }
    }

    private fun createGameMode(choice: Int?, engine: GameEngine): GameMode {
        return when (choice) {
            1 -> ClassicMode(ui, engine)
            2 -> ManaMode(ui, engine)
            3 -> TeamMode(ui, engine)
            else -> error("Неизвестный режим")
        }
    }

    private fun modeName(choice: Int?): String = when (choice) {
        1 -> "Классический"
        2 -> "С маной"
        3 -> "Командный"
        else -> "Классический"
    }
}