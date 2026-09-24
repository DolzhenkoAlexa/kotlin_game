package main.kotlin.gamemodes

enum class GameModeType(val id: String, val displayName: String) {
    CLASSIC("classic", "Классический"),
    MANA("mana", "С маной"),
    TEAM("team", "Командный");

    companion object {
        fun fromChoice(choice: Int): GameModeType? {
            return when (choice) {
                1 -> CLASSIC
                2 -> MANA
                3 -> TEAM
                else -> null
            }
        }
    }
}
