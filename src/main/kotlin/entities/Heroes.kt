package main.kotlin.entities

class Knight(name: String) : Character("Рыцарь", 60, 8, 7) {
    override fun getUltimateDescription(): String {
        return """
            Священный щит:
            • Наносит 10 урона цели
            • Игнорирует защиту
            • Активируется действие "защититься" у всех живых союзников и себя, но защита вместо +50% становится +200% 
        """.trimIndent()
    }
}

class Mage(name: String) : Character("Маг", 55, 14, 1) {
    override fun getUltimateDescription(): String {
        return """
            Огненная буря:
            • Наносит 10 урона всем живым целям противника
            • Игнорирует защиту
        """.trimIndent()
    }
}

class Archer(name: String) : Character("Лучник", 60, 10, 5) {
    override fun getUltimateDescription(): String {
        return """
            Точный выстрел:
            • Наносит 35 урона выбранной цели
            • Не игнорирует защиту
        """.trimIndent()
    }
}

class Barbarian(name: String) : Character("Варвар", 60, 13, 2) {
    override fun getUltimateDescription(): String {
        return """
            Ярость берсерка:
            • Наносит 35 урона цели
            • Игнорирует защиту
            • Наносит 20 урона себе
            • Доступно только при HP > 20
        """.trimIndent()
    }
}

class Paladin(name: String) : Character("Паладин", 55, 6, 10) {
    override fun getUltimateDescription(): String {
        return """
            Божественное исцеление:
            • Восстанавливает по 15 HP всей команде и себе
        """.trimIndent()
    }
}

class Necromancer(name: String) : Character("Некромант", 60, 11, 3) {
    override fun getUltimateDescription(): String {
        return """
            Чумной ветер:
            • Наносит 12 урона всем живым персонажам противника
            • Не игнорирует защиту
            • Восстанавливает 10 HP себе
        """.trimIndent()
    }
}