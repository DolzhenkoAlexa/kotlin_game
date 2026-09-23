package main.kotlin.ui.gui

import main.kotlin.history.repository.InMemoryRepository

object GameRepository {
    val instance: InMemoryRepository = InMemoryRepository()
}
