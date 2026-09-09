package main.kotlin

import main.kotlin.history.repository.InMemoryRepository
import main.kotlin.ui.MenuRouter
import main.kotlin.ui.UserConsole

fun main() {
    val repository = InMemoryRepository()
    val console = UserConsole()
    val router = MenuRouter(console, repository)

    console.setRouter(router)
    console.showMainMenu()
}
