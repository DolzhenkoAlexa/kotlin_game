package main.kotlin.actions

import main.kotlin.entities.Character
import main.kotlin.entities.Player

class AttackAction : Action {
    override fun execute(
        character: Character,
        target: Character?,
        isTeamMode: Boolean,
        player: Player?,
        targetPlayer: Player?
    ) {
        if (target == null) {
            println("Нет цели для атаки!")
            return
        }
        println("${character.type} атакует с силой ${character.power}!")
        target.takeDamage(character.power)
    }
}