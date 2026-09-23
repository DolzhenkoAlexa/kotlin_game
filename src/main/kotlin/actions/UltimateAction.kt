package main.kotlin.actions

import main.kotlin.entities.Character
import main.kotlin.entities.Player
import kotlin.math.roundToInt

class UltimateAction : Action {
    override fun execute(
        character: Character,
        target: Character?,
        isTeamMode: Boolean,
        player: Player?,
        targetPlayer: Player?
    ) {
        when (character.type) {
            "Рыцарь" -> {
                // урон 10 по цели с игнором защиты + защита +200% у всех союзников
                if (target == null) {
                    println("Нет цели для атаки!")
                    return
                }
                println("${character.type} использует Священный щит!")
                println("Наносит 10 урона!")
                val targetDefence = target.defence
                target.defence = 0
                target.takeDamage(10)
                target.defence = targetDefence
                val allies = if (isTeamMode && player != null) {
                    player.getAliveHeroes()
                } else {
                    listOf(character)
                }

                if (allies.isNotEmpty()) {
                    println("Защита +200% у всех союзников!")
                    allies.forEach { hero ->
                        hero.isDefending = true
                        hero.defenceBonus = hero.defence * 3
                        println("  ${hero.type}: защита увеличена до ${hero.defence + hero.defenceBonus}")
                    }
                }
            }

            "Маг" -> {
                // 10 урона всем врагам с игнором защиты
                if (isTeamMode && targetPlayer != null) {
                    println("${character.type} использует Огненную бурю по всей вражеской команде!")
                    val enemies = targetPlayer.getAliveHeroes()
                    if (enemies.isEmpty()) {
                        println("Нет живых врагов!")
                        return
                    }
                    enemies.forEach { enemy ->
                        val enemyDefence = enemy.defence
                        enemy.defence = 0
                        enemy.takeDamage(10)
                        enemy.defence = enemyDefence
                    }
                    println("Все враги получили по 10 урона!")
                } else {
                    if (target == null) {
                        println("Нет цели для атаки!")
                        return
                    }
                    println("${character.type} использует Огненную бурю и наносит 10 урона всем!")
                    val targetDefence = target.defence
                    target.defence = 0
                    target.takeDamage(10)
                    target.defence = targetDefence
                }
            }

            "Лучник" -> {
                // 35 урона одной цели (НЕ игнорирует защиту)
                if (target == null) {
                    println("Нет цели для атаки!")
                    return
                }
                println("${character.type} использует Точный выстрел и наносит 35 урона цели!")
                target.takeDamage(35)
            }

            "Варвар" -> {
                // 35 урона одной цели с игнором защиты, но получает 20 урона сам
                if (target == null) {
                    println("Нет цели для атаки!")
                    return
                }
                if (character.health > 20) {
                    println("${character.type} использует Ярость берсерка!")
                    println("Наносит 35 урона, но получает 20!")

                    val targetDefence = target.defence
                    target.defence = 0
                    target.takeDamage(35)
                    target.defence = targetDefence
                    character.health -= 20
                    if (character.health < 0) character.health = 0
                    println("${character.type} получил 20 урона, осталось ${character.health} здоровья")
                } else {
                    println("Слишком мало здоровья! Ярость берсерка недоступна (нужно > 20 HP)")
                }
            }

            "Паладин" -> {
                // лечит 15 HP всей команде
                if (isTeamMode && player != null) {
                    println("${character.type} использует Божественное исцеление для всей команды!")
                    val aliveHeroes = player.getAliveHeroes()
                    if (aliveHeroes.isEmpty()) {
                        println("Нет живых героев для исцеления!")
                        return
                    }
                    aliveHeroes.forEach { hero ->
                        val maxHp = when (hero.type) {
                            "Рыцарь" -> 60
                            "Маг" -> 55
                            "Лучник" -> 60
                            "Варвар" -> 60
                            "Паладин" -> 55
                            "Некромант" -> 60
                            else -> 60
                        }
                        val healed = minOf(15, maxHp - hero.health)
                        hero.health += healed
                        println("${hero.type} восстановил $healed HP! Текущее HP: ${hero.health}")
                    }
                    println("Вся команда ${player.name} исцелена!")
                } else {
                    println("${character.type} использует Божественное исцеление!")
                    val healed = minOf(15, 55 - character.health)
                    character.health += healed
                    println("${character.type} восстанавливает $healed HP! Текущее HP: ${character.health}")
                }
            }

            "Некромант" -> {
                // 12 урона всем врагам (не игнорирует защиту) + 10 HP себе
                if (isTeamMode && targetPlayer != null) {
                    println("${character.type} использует Чумной ветер по всей вражеской команде!")
                    val enemies = targetPlayer.getAliveHeroes()
                    if (enemies.isEmpty()) {
                        println("Нет живых врагов!")
                        return
                    }
                    enemies.forEach { enemy ->
                        enemy.takeDamage(12)
                    }
                    println("Все враги получили по 12 урона!")

                    val healed = minOf(10, 60 - character.health)
                    character.health += healed
                    println("${character.type} восстанавливает $healed HP! Текущее HP: ${character.health}")
                } else {
                    if (target == null) {
                        println("Нет цели для атаки!")
                        return
                    }
                    println("${character.type} использует Чумной ветер!")
                    target.takeDamage(12)
                    val healed = minOf(10, 60 - character.health)
                    character.health += healed
                    println("${character.type} восстанавливает $healed HP! Текущее HP: ${character.health}")
                }
            }

            else -> {
                if (target == null) {
                    println("Нет цели для атаки!")
                    return
                }
                println("${character.type} пытается использовать сверхспособность, но ему не хватает сил!")
                target.takeDamage(character.power)
            }
        }
    }
}