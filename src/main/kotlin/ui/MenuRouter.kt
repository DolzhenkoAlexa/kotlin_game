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
        val modeType = selectGameMode()

        val (player1, player2) = getPlayerNames()

        val recorder = GameRecorder(repository)
        recorder.setGameInfo(modeType.displayName, player1.name, player2.name)

        val engine = GameEngine(recorder)
        val mode = createGameMode(modeType, engine)

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

    private fun selectGameMode(): GameModeType {
        while (true) {
            val choice = ui.readInt("""
                Выберите режим:
                1 - Классический
                2 - С маной
                3 - Командный
            """.trimIndent())
            if (choice == null) {
                ui.showMessage("Неверный ввод! Введите число от 1 до 3")
                continue
            }
            val modeType = GameModeType.fromChoice(choice)
            if (modeType != null) return modeType
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

    private fun createGameMode(modeType: GameModeType, engine: GameEngine): GameMode {
        return when (modeType) {
            GameModeType.CLASSIC -> ClassicMode(ui, engine)
            GameModeType.MANA -> ManaMode(ui, engine)
            GameModeType.TEAM -> TeamMode(ui, engine)
        }
    }
}