package main.kotlin.actions

import main.kotlin.entities.Character
import main.kotlin.entities.Player
import kotlin.math.roundToInt

class DefendAction : Action {
    override fun execute(
        character: Character,
        target: Character?,
        isTeamMode: Boolean,
        player: Player?,
        targetPlayer: Player?
    ) {
        if (isTeamMode && player != null) {
            // Выбор "защититься" в командном режиме активирует защиту на всех живых героях
            val aliveHeroes = player.getAliveHeroes()
            aliveHeroes.forEach { hero ->
                hero.isDefending = true
                hero.defenceBonus = (hero.defence / 2.0).roundToInt()
            }
            println("Все герои ${player.name} защищаются! Броня увеличена на 50% у каждого!")
        } else {
            // Только на одном герое
            character.isDefending = true
            character.defenceBonus = (character.defence / 2.0).roundToInt()
            println("${character.type} защищается! Броня увеличена до ${character.defence + character.defenceBonus}")
        }
    }
}