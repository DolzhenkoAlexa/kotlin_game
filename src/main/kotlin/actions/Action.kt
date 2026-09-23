package main.kotlin.actions
import main.kotlin.entities.Character
import main.kotlin.entities.Player


interface Action {
    fun execute(
        character: Character,
        target: Character?,
        isTeamMode: Boolean = false,
        player: Player? = null,
        targetPlayer: Player? = null
    )
}