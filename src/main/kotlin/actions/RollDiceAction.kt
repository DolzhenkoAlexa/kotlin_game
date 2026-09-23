package main.kotlin.actions

import main.kotlin.entities.Character
import main.kotlin.entities.Player
import kotlin.random.Random

class RollDiceAction : Action {
    override fun execute(
        character: Character,
        target: Character?,
        isTeamMode: Boolean,
        player: Player?,
        targetPlayer: Player?
    ) {
        val dice = Random.nextInt(0, 6)
        character.mana += dice
        if (character.mana > 10) character.mana = 10
        println("${character.type} бросает кубик... Выпало: $dice")
        println("Мана: ${character.mana - dice} → ${character.mana} / 10")
    }
}