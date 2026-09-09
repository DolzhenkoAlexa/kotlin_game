package main.kotlin.ui

import main.kotlin.entities.*
import main.kotlin.history.repository.PlayersStatistics

class UserConsole : UserInterface {
    private lateinit var router: MenuRouter

    fun setRouter(router: MenuRouter) {
        this.router = router
    }

    fun showMainMenu() {
        showMessage("=== Добро пожаловать в Битву Героев! ===")

        while (true) {
            val choice = readInt("""
                
                1 - Новая игра
                2 - Показать статистику
                3 - Воспроизвести игру
                4 - Выход
            """.trimIndent())

            when (choice) {
                1 -> router.startNewGame()
                2 -> router.showStatistics()
                3 -> router.replayGame()
                4 -> {
                    showMessage("Спасибо за игру")
                    return
                }
                else -> showMessage("Неверный выбор! Введите число от 1 до 4")
            }
        }
    }

    override fun showMessage(msg: String) {
        println(msg)
    }

    override fun readCommand(): String {
        return readln().trim()
    }

    override fun readInt(prompt: String): Int? {
        println(prompt)
        return readln().toIntOrNull()
    }

    override fun readString(prompt: String): String {
        println(prompt)
        return readln().trim()
    }

    override fun showHeroes(heroes: List<Character>) {
        if (heroes.isEmpty()) {
            showMessage("Нет доступных героев")
            return
        }
        heroes.forEachIndexed { index, hero ->
            showMessage("${index + 1}. ${hero.type} (HP: ${hero.health}, Атака: ${hero.power}, Защита: ${hero.defence})")
        }
    }

    override fun showBattleStatus(player1: Player, player2: Player, round: Int, currentPlayer: Player) {
        val aliveHeroes1 = player1.getAliveHeroes()
        val aliveHeroes2 = player2.getAliveHeroes()

        if (aliveHeroes1.isEmpty() && aliveHeroes2.isEmpty()) return

        println("")
        println("═══════════════════════════════════════")
        println("    Раунд $round  |   ХОД: ${currentPlayer.name}")

        if (aliveHeroes1.isEmpty()) {
            println("    ${player1.name}: Все герои пали")
        } else {
            println("    ${player1.name}:")
            aliveHeroes1.forEach { hero ->
                val defence = if (hero.isDefending) hero.defence + hero.defenceBonus else hero.defence
                val left = "      ${hero.type}".padEnd(18)
                val right = "${hero.health} HP, $defence DEF".padStart(16)
                println("    $left$right")
            }
        }

        if (aliveHeroes2.isEmpty()) {
            println("    ${player2.name}: Все герои пали")
        } else {
            println("    ${player2.name}:")
            aliveHeroes2.forEach { hero ->
                val defence = if (hero.isDefending) hero.defence + hero.defenceBonus else hero.defence
                val left = "      ${hero.type}".padEnd(18)
                val right = "${hero.health} HP, $defence DEF".padStart(16)
                println("    $left$right")
            }
        }

        println("═══════════════════════════════════════")
    }

    override fun showGameStatistics(stats: PlayersStatistics) {
        showMessage("\n=== СТАТИСТИКА ИГРОКОВ ===")
        showMessage("Всего игр: ${stats.totalGames}")
        showMessage("")

        if (stats.players.isEmpty()) {
            showMessage("Нет данных об игроках")
            return
        }

        val sortedPlayers = stats.players.entries.sortedByDescending { it.value.gamesPlayed }

        showMessage("Игрок          | Игр | Побед | % побед")
        showMessage("-".repeat(40))

        sortedPlayers.forEach { (name, stat) ->
            val winRate = if (stat.gamesPlayed > 0) {
                (stat.wins.toDouble() / stat.gamesPlayed * 100).toInt()
            } else 0
            showMessage("${name.padEnd(14)} ${stat.gamesPlayed.toString().padStart(4)}   ${stat.wins.toString().padStart(5)}    $winRate%")
        }
        showMessage("")
    }
}