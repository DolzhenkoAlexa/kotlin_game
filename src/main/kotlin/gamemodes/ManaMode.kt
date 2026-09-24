package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface
import main.kotlin.actions.*

class ManaMode(
    ui: UserInterface,
    engine: GameInterface
) : GameMode(ui, engine) {

    // Доступные герои для режима с маной (все, кроме Паладина)
    private val availableHeroes = listOf(
        Knight(""),
        Mage(""),
        Archer(""),
        Barbarian(""),
        Necromancer("")
    )

    override fun getAvailableHeroes(): List<Character> = availableHeroes
    override fun canUseUltimate(): Boolean = true
    override fun isTeamMode(): Boolean = false

    override fun showModeInfo() {
        ui.showMessage("\n=== Режим с маной ===")
        ui.showMessage("Кидайте кубик, чтобы получить ману (0-6)")
        ui.showMessage("Сверхспособность стоит 10 маны, она уникальная для каждого героя и очень сильная")
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

    private fun showUltimateInfo(hero: Character, owner: String) {
        ui.showMessage("\nИнформация о сверхспособности ${owner} героя:")
        ui.showMessage("   Герой: ${hero.type}")
        ui.showMessage(hero.getUltimateDescription())
        ui.showMessage("")
    }

    override fun processTurn(actor: Player, target: Player) {
        val state = engine.getCurrentState()
        val hero = actor.getAliveHeroes().firstOrNull()
        val enemyHero = target.getAliveHeroes().firstOrNull()

        ui.showBattleStatus(player1, player2, state.turn, actor)

        if (hero == null) {
            return
        }

        ui.showMessage("\n--- Ход ${actor.name} ---")
        ui.showMessage("Мана: ${hero.mana} / 10")

        var action: Action? = null

        while (action == null) {
            var actionChoice: Int? = null
            while (actionChoice == null || actionChoice !in 1..6) {
                actionChoice = ui.readInt("""
                1 - Атаковать (0 маны)
                2 - Защититься (0 маны)
                3 - Бросить кубик (0 маны)
                4 - Сверхспособность (10 маны)
                5 - Узнать о своей сверхспособности
                6 - Узнать о сверхспособности противника
            """.trimIndent())
                if (actionChoice == null || actionChoice !in 1..6) {
                    ui.showMessage("Неверный выбор! Введите число от 1 до 6")
                }
            }

            when (actionChoice) {
                1 -> action = AttackAction()
                2 -> action = DefendAction()
                3 -> action = RollDiceAction()
                4 -> {
                    if (hero.mana >= 10) {
                        hero.mana -= 10
                        ui.showMessage("Сверхспособность! -10 маны")
                        action = UltimateAction()
                    } else {
                        ui.showMessage("Недостаточно маны! (нужно 10, у вас ${hero.mana})")
                    }
                }
                5 -> {
                    showUltimateInfo(hero, "своей")
                }
                6 -> {
                    if (enemyHero != null) {
                        showUltimateInfo(enemyHero, "противника")
                    } else {
                        ui.showMessage("У противника нет живых героев!")
                    }
                }
            }
        }

        engine.processTurn(actor, action)
    }
}