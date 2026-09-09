package main.kotlin.entities

class Player(val name: String) {
    val heroes: MutableList<Character> = mutableListOf()
    var mana: Int = 0

    fun hasAliveHeroes(): Boolean = heroes.any { it.isAlive() }

    fun getAliveHeroes(): List<Character> = heroes.filter { it.isAlive() }
}