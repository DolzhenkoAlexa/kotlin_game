package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface

abstract class GameMode(
    protected val ui: UserInterface,
    protected val engine: GameInterface
) {
    protected lateinit var player1: Player
    protected lateinit var player2: Player

    abstract fun getAvailableHeroes(): List<Character>
    abstract fun canUseUltimate(): Boolean
    abstract fun isTeamMode(): Boolean

    open fun startGame(p1: Player, p2: Player) {
        player1 = p1
        player2 = p2

        showModeInfo()
        selectHeroesForPlayers(p1, p2)
        showGameStart()

        engine.startGame(p1, p2)
        runGameLoop()

        ui.showMessage("\nПобедитель: ${engine.getCurrentState().winner ?: "никто"}!")
    }

    protected abstract fun showModeInfo()
    protected abstract fun selectHeroesForPlayers(p1: Player, p2: Player)
    protected abstract fun processTurn(actor: Player, target: Player)

    // Общий метод показа начала игры
    protected open fun showGameStart() {
        ui.showMessage("\nБой начинается!")
        ui.showMessage("${player1.name}: ${player1.heroes.joinToString { "${it.type} (HP: ${it.health})" }}")
        ui.showMessage("${player2.name}: ${player2.heroes.joinToString { "${it.type} (HP: ${it.health})" }}")
    }

    // Игровой цикл
    protected open fun runGameLoop() {
        while (!engine.isGameOver()) {
            if (!player1.hasAliveHeroes()) {
                ui.showMessage("\n${player2.name} победил! У ${player1.name} нет живых героев!")
                break
            }
            processTurn(player1, player2)
            if (engine.isGameOver()) break

            if (!player2.hasAliveHeroes()) {
                ui.showMessage("\n${player1.name} победил! У ${player2.name} нет живых героев!")
                break
            }
            processTurn(player2, player1)
        }
    }

    // Выбор героев из набора доступных (в разных режимах для баланса наборы разные)
    protected fun selectHeroFromList(
        player: Player,
        availableHeroes: List<Character>,
        usedHeroes: List<String>
    ): Character? {
        ui.showHeroes(availableHeroes)

        var selectedHero: Character? = null

        while (selectedHero == null) {
            val choice = ui.readInt("Выберите номер героя:")

            if (choice == null || choice !in 1..availableHeroes.size) {
                ui.showMessage("Неверный номер! Выберите от 1 до ${availableHeroes.size}")
                continue
            }

            val template = availableHeroes[choice - 1]
            val heroName = template.type

            if (heroName in usedHeroes) {
                ui.showMessage("Герой '$heroName' уже выбран! Выберите другого.")
                continue
            }

            selectedHero = createHeroInstance(template, player)
        }

        return selectedHero
    }

    // Создание экземпляра героя по шаблону
    protected fun createHeroInstance(template: Character, player: Player): Character {
        return when (template) {
            is Knight -> Knight("${player.name}'s герой")
            is Mage -> Mage("${player.name}'s герой")
            is Archer -> Archer("${player.name}'s герой")
            is Barbarian -> Barbarian("${player.name}'s герой")
            is Paladin -> Paladin("${player.name}'s герой")
            is Necromancer -> Necromancer("${player.name}'s герой")
            else -> Knight("${player.name}'s герой")
        }
    }
}