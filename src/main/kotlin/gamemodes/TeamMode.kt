package main.kotlin.gamemodes

import main.kotlin.entities.*
import main.kotlin.engine.GameInterface
import main.kotlin.ui.UserInterface
import main.kotlin.actions.*

class TeamMode(
    private val ui: UserInterface,
    private val engine: GameInterface
) : GameMode {

    // Доступные герои для командного режима
    private val allHeroes = listOf(
        Knight(""),
        Mage(""),
        Archer(""),
        Barbarian(""),
        Paladin(""),
        Necromancer("")
    )

    private lateinit var player1: Player
    private lateinit var player2: Player
    private var roundNumber = 1

    override fun getAvailableHeroes(): List<Character> = allHeroes
    override fun canUseUltimate(): Boolean = true
    override fun isTeamMode(): Boolean = true

    override fun startGame(p1: Player, p2: Player) {
        player1 = p1
        player2 = p2

        ui.showMessage("\n=== Командный режим ===")
        ui.showMessage("Каждый игрок выбирает 3 героев по очереди")
        ui.showMessage("Сверхспособности доступны 1 раз за игру в любой момент")

        draftHeroes(p1, p2)

        ui.showMessage("\nКоманды сформированы!")
        showTeams()

        engine.startGame(p1, p2)

        while (!engine.isGameOver()) {
            processTurn(p1, p2)
            if (engine.isGameOver()) break
            processTurn(p2, p1)
            roundNumber++
        }

        ui.showMessage("\nПобедитель: ${engine.getCurrentState().winner ?: "никто"}!")
    }

    private fun draftHeroes(p1: Player, p2: Player) {
        val usedHeroes = mutableListOf<String>()
        val availableHeroes = allHeroes.toMutableList()

        ui.showMessage("\n--- Драфт героев ---")
        ui.showMessage("Игроки выбирают по очереди:")

        for (pick in 1..6) {
            val currentPlayer = if (pick % 2 == 1) p1 else p2
            val playerName = currentPlayer.name

            ui.showMessage("\n${playerName} выбирает героя (осталось ${availableHeroes.size}):")
            val hero = selectHero(currentPlayer, availableHeroes, usedHeroes)
            currentPlayer.heroes.add(hero)
            ui.showMessage("${playerName} выбрал: ${hero.type}")
        }
    }

    private fun selectHero(
        player: Player,
        availableHeroes: MutableList<Character>,
        usedHeroes: MutableList<String>
    ): Character {
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
                is Paladin -> Paladin("${player.name}'s герой")
                is Necromancer -> Necromancer("${player.name}'s герой")
                else -> Knight("${player.name}'s герой")
            }

            usedHeroes.add(heroName)
            availableHeroes.remove(template)
        }

        return selectedHero
    }

    private fun showTeams() {
        ui.showMessage("\n--- Команды ---")
        ui.showMessage("${player1.name}:")
        player1.heroes.forEach { hero ->
            ui.showMessage("  - ${hero.type} (HP: ${hero.health})")
        }
        ui.showMessage("${player2.name}:")
        player2.heroes.forEach { hero ->
            ui.showMessage("  - ${hero.type} (HP: ${hero.health})")
        }
    }

    private fun processTurn(actor: Player, target: Player) {
        val state = engine.getCurrentState()
        val aliveHeroes = actor.getAliveHeroes()
        val enemyHeroes = target.getAliveHeroes()

        ui.showBattleStatus(player1, player2, state.turn, actor)

        if (aliveHeroes.isEmpty()) {
            ui.showMessage("${actor.name}: нет живых героев!")
            return
        }

        if (enemyHeroes.isEmpty()) {
            ui.showMessage("У ${target.name} нет живых героев! Победа!")
            return
        }

        ui.showMessage("\n--- Ход ${actor.name} ---")
        ui.showMessage("Живые герои: ${aliveHeroes.size}")

        val hero = selectAliveHero(actor)
        if (hero == null) {
            ui.showMessage("Нет доступных героев!")
            return
        }

        val action = selectAction(actor, hero)
        // Определяем нужна ли цель
        val needsTarget = when {
            action is AttackAction -> true
            action is UltimateAction -> when (hero.type) {
                // В командном режиме цель не нужна Магу и Некроманту, так как AOE атаки
                "Маг" -> !isTeamMode()
                "Некромант" -> !isTeamMode()
                // Паладину цель не нужна никогда
                "Паладин" -> false
                // Остальным нужна
                else -> true
            }
            else -> false
        }

        val targetHero = if (needsTarget) selectTarget(target) else null

        if (needsTarget && targetHero == null) {
            ui.showMessage("Нет цели для атаки!")
            return
        }

        action.execute(
            character = hero,
            target = targetHero,
            isTeamMode = true,
            player = actor,
            targetPlayer = target
        )
    }

    private fun selectAliveHero(player: Player): Character? {
        val aliveHeroes = player.getAliveHeroes()

        if (aliveHeroes.isEmpty()) return null
        if (aliveHeroes.size == 1) {
            ui.showMessage("Выбран: ${aliveHeroes.first().type}")
            return aliveHeroes.first()
        }

        ui.showMessage("\nВыберите героя:")
        aliveHeroes.forEachIndexed { index, hero ->
            val hasUltimate = if (!hero.specialAbilityUsed) " (сверхспособность готова)" else ""
            ui.showMessage("${index + 1}. ${hero.type} (HP: ${hero.health})${hasUltimate}")
        }

        val choice = ui.readInt("Выберите номер героя:") ?: 1
        return aliveHeroes.getOrElse(choice - 1) { aliveHeroes.first() }
    }

    private fun selectAction(player: Player, hero: Character): Action {
        ui.showMessage("\n--- Действия для ${hero.type} ---")
        ui.showMessage("HP: ${hero.health}")

        var action: Action? = null
        val ultimateAvailable = !hero.specialAbilityUsed

        while (action == null) {
            var choice: Int? = null
            while (choice == null || choice !in 1..3) {
                choice = ui.readInt("""
                1 - Атаковать
                2 - Защититься
                ${if (ultimateAvailable) "3 - Сверхспособность (1 раз)" else "3 - Ульта уже использована"}
            """.trimIndent())
                if (choice == null || choice !in 1..3) {
                    ui.showMessage("Неверный выбор! Введите 1, 2 или 3")
                }
            }

            when (choice) {
                1 -> action = AttackAction()
                2 -> action = DefendAction()
                3 -> {
                    if (ultimateAvailable) {
                        ui.showMessage("${hero.type} использует сверхспособность!")
                        hero.specialAbilityUsed = true
                        action = UltimateAction()
                    } else {
                        ui.showMessage("Сверхспособность уже использована!")
                    }
                }
            }
        }

        return action
    }

    private fun selectTarget(target: Player): Character? {
        val aliveHeroes = target.getAliveHeroes()

        if (aliveHeroes.isEmpty()) return null
        if (aliveHeroes.size == 1) {
            ui.showMessage("Цель: ${aliveHeroes.first().type}")
            return aliveHeroes.first()
        }

        ui.showMessage("\nВыберите цель:")
        aliveHeroes.forEachIndexed { index, hero ->
            ui.showMessage("${index + 1}. ${hero.type} (HP: ${hero.health})")
        }

        val choice = ui.readInt("Выберите номер цели:") ?: 1
        return aliveHeroes.getOrElse(choice - 1) { aliveHeroes.first() }
    }
}