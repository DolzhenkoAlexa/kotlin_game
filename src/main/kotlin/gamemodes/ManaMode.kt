package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface
import main.kotlin.actions.*

class ManaMode(
    private val ui: UserInterface,
    private val engine: GameInterface
) : GameMode {

    // Доступные герои для режима с маной (все, кроме Паладина)
    private val availableHeroes = listOf(
        Knight(""),
        Mage(""),
        Archer(""),
        Barbarian(""),
        Necromancer("")
    )

    private lateinit var player1: Player
    private lateinit var player2: Player

    override fun getAvailableHeroes(): List<Character> = availableHeroes
    override fun canUseUltimate(): Boolean = true
    override fun isTeamMode(): Boolean = false

    override fun startGame(p1: Player, p2: Player) {
        player1 = p1
        player2 = p2

        ui.showMessage("\n=== Режим с маной ===")
        ui.showMessage("Кидайте кубик, чтобы получить ману (0-6)")
        ui.showMessage("Сверхспособность стоит 10 маны, она уникальная для каждого героя и очень сильная")

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
                is Barbarian -> Barbarian("${player.name}'s герой")
                is Necromancer -> Necromancer("${player.name}'s герой")
                else -> Knight("${player.name}'s герой")
            }

            usedHeroes.add(heroName)
        }

        return selectedHero
    }

    private fun showUltimateInfo(hero: Character, owner: String) {
        ui.showMessage("\nИнформация о сверхспособности ${owner} героя:")
        ui.showMessage("   Герой: ${hero.type}")
        ui.showMessage(hero.getUltimateDescription())  // ← вызываем метод героя
        ui.showMessage("")
    }

    private fun processTurn(actor: Player, target: Player) {
        val state = engine.getCurrentState()
        val hero = actor.getAliveHeroes().firstOrNull()
        val enemyHero = target.getAliveHeroes().firstOrNull()

        ui.showBattleStatus(player1, player2, state.turn, actor)

        if (hero == null) {
            ui.showMessage("${actor.name}: нет живых героев!")
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