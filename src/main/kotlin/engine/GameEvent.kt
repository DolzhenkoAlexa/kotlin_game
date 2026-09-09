package main.kotlin.engine

data class GameEvent(
    val turnNumber: Int,
    val playerName: String,
    val actorType: String,
    val actionType: String,
    val targetType: String?,
    val damage: Int?,
    val healthBefore: Int?,
    val healthAfter: Int?
)

