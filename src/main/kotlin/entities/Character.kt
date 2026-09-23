package main.kotlin.entities

abstract class Character(
    var type: String,
    var health: Int,
    var power: Int,
    var defence: Int
) {
    var isDefending = false
    var defenceBonus = 0
    var mana: Int = 0
    var specialAbilityUsed: Boolean = false

    fun takeDamage(damage: Int) {
        val totalDefence = if (isDefending) defence + defenceBonus else defence
        val actualDamage = damage - totalDefence
        val finalDamage = if (actualDamage <= 0) 1 else actualDamage
        health -= finalDamage
        if (health < 0) health = 0

        isDefending = false
        defenceBonus = 0

        println("$type получил $finalDamage урона, осталось $health здоровья")
    }

    fun isAlive(): Boolean = health > 0

    abstract fun getUltimateDescription(): String
}