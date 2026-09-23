package main.kotlin.ui

import main.kotlin.entities.Character
import main.kotlin.entities.Player
import main.kotlin.history.repository.PlayersStatistics

interface UserInterface {
    fun showMessage(msg: String)
    fun readCommand(): String
    fun readInt(prompt: String): Int?
    fun readString(prompt: String): String
    fun showHeroes(heroes: List<Character>)
    fun showBattleStatus(player1: Player, player2: Player, round: Int, currentPlayer: Player)
    fun showGameStatistics(stats: PlayersStatistics)
}