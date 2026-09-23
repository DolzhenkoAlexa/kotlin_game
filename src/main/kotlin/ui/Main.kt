package main.kotlin.ui

import javafx.application.Application
import main.kotlin.history.repository.InMemoryRepository
import main.kotlin.ui.gui.BattlegroundApp

fun main(args: Array<String>) {
    val useConsole = when {
        args.contains("--console") -> true
        args.contains("--gui") -> false
        else -> askUser()
    }

    if (useConsole) {
        runConsole()
    } else {
        Application.launch(BattlegroundApp::class.java)
    }
}

// Спрашивает формат игры, пока пользователь не введёт 1 или 2
private fun askUser(): Boolean {
    println("╔════════════════════════════════════════╗")
    println("║            БИТВА ГЕРОЕВ                ║")
    println("╚════════════════════════════════════════╝")
    println()
    println("Выберите формат игры:")
    println("  1 — Графический интерфейс (GUI)")
    println("  2 — Консоль")
    println()

    while (true) {
        print("Ваш выбор: ")
        System.out.flush()

        when (readlnOrNull()?.trim()) {
            "1" -> return false
            "2" -> return true
            else -> println("Ошибка: введите 1 (GUI) или 2 (Консоль).\n")
        }
    }
}

private fun runConsole() {
    val repository = InMemoryRepository()
    val console = UserConsole()
    val router = MenuRouter(console, repository)
    console.setRouter(router)
    console.showMainMenu()
}