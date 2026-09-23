package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface
import main.kotlin.actions.AttackAction
import main.kotlin.actions.DefendAction

class ClassicMode(
    private val ui: UserInterface,
    private val engine: GameInterface
) : GameMode {

    // доступны только 3 базовых героя
    private val availableHeroes = listOf(
        Knight(""),
        Mage(""),
        Archer("")
    )

    private lateinit var player1: Player
    private lateinit var player2: Player

    override fun getAvailableHeroes(): List<Character> = availableHeroes
    override fun canUseUltimate(): Boolean = false
    override fun isTeamMode(): Boolean = false

    override fun startGame(p1: Player, p2: Player) {
        player1 = p1
        player2 = p2

        ui.showMessage("\n=== Классический режим ===")

        val usedHeroes = mutableListOf<String>()

        ui.showMessage("\n${p1.name}, выберите героя:")
        val hero1 = selectHero(p1, usedHeroes)
        p1.heroes.add(hero1)

        ui.showMessage("\n${p2.name}, выберите героя:")
        val hero2 = selectHero(p2, usedHeroes)
        p2.heroes.add(hero2)

        ui.showMessage("\nБой начинается!")
        ui.showMessage("${p1.name}: ${hero1.type} (HP: ${hero1.health})")
        ui.showMessage("${p2.name}: ${hero2.type} (HP: ${hero2.health})")

        engine.startGame(p1, p2)
        // поочередные ходы до победы одного из игроков
        while (!engine.isGameOver()) {
            processTurn(p1, p2)
            if (engine.isGameOver()) break
            processTurn(p2, p1)
        }

        ui.showMessage("\nПобедитель: ${engine.getCurrentState().winner ?: "никто"}!")
    }

    private fun selectHero(player: Player, usedHeroes: MutableList<String>): Character {
        ui.showHeroes(availableHeroes)

        var choice: Int? = null
        var selectedHero: Character? = null

        while (selectedHero == null) {
            choice = ui.readInt("Выберите номер героя:")

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

            selectedHero = when (template) {
                is Knight -> Knight("${player.name}'s герой")
                is Mage -> Mage("${player.name}'s герой")
                is Archer -> Archer("${player.name}'s герой")
                else -> Knight("${player.name}'s герой")
            }

            usedHeroes.add(heroName)
        }

        return selectedHero
    }

    private fun processTurn(actor: Player, target: Player) {
        val state = engine.getCurrentState()
        ui.showBattleStatus(player1, player2, state.turn, actor)

        ui.showMessage("\n--- Ход ${actor.name} ---")

        var actionChoice: Int? = null
        while (actionChoice == null || actionChoice !in 1..2) {
            actionChoice = ui.readInt("1 - Атаковать\n2 - Защититься")
            if (actionChoice == null || actionChoice !in 1..2) {
                ui.showMessage("Неверный выбор! Введите 1 или 2")
            }
        }

        val action = when (actionChoice) {
            1 -> AttackAction()
            2 -> DefendAction()
            else -> AttackAction()
        }

        engine.processTurn(actor, action)
    }
}