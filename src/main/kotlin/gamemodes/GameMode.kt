package main.kotlin.gamemodes

import main.kotlin.entities.*


interface GameMode {
    fun getAvailableHeroes(): List<Character>
    fun canUseUltimate(): Boolean
    fun isTeamMode(): Boolean
    fun startGame(player1: Player, player2: Player)
}