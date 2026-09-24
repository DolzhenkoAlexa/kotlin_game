package main.kotlin.ui

import javafx.application.Application
import main.kotlin.history.repository.SqliteRepository
import main.kotlin.ui.gui.BattlegroundApp
import main.kotlin.ui.gui.GameRepository

object AppLauncher {

    fun run(args: Array<String>) {
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

    private fun askUser(): Boolean {
        println("Выберите формат игры:")
        println("  1 — Графический интерфейс (GUI)")
        println("  2 — Консоль")
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
        val console = UserConsole()
        val router = MenuRouter(console, GameRepository.instance)
        console.setRouter(router)
        console.showMainMenu()
    }
}