package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface
import main.kotlin.actions.AttackAction
import main.kotlin.actions.DefendAction

class ClassicMode(
    ui: UserInterface,
    engine: GameInterface
) : GameMode(ui, engine) {

    // доступны только 3 базовых героя
    private val availableHeroes = listOf(
        Knight(""),
        Mage(""),
        Archer("")
    )

    override fun getAvailableHeroes(): List<Character> = availableHeroes
    override fun canUseUltimate(): Boolean = false
    override fun isTeamMode(): Boolean = false

    override fun showModeInfo() {
        ui.showMessage("\n=== Классический режим ===")
    }

    override fun selectHeroesForPlayers(p1: Player, p2: Player) {
        val usedHeroes = mutableListOf<String>()

        ui.showMessage("\n${p1.name}, выберите героя:")
        val hero1 = selectHeroFromList(p1, availableHeroes, usedHeroes)
        if (hero1 != null) {
            p1.heroes.add(hero1)
            usedHeroes.add(hero1.type)
        }

        ui.showMessage("\n${p2.name}, выберите героя:")
        val hero2 = selectHeroFromList(p2, availableHeroes, usedHeroes)
        if (hero2 != null) {
            p2.heroes.add(hero2)
            usedHeroes.add(hero2.type)
        }
    }

    override fun processTurn(actor: Player, target: Player) {
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