package main.kotlin.ui.gui

import main.kotlin.history.repository.Repository
import main.kotlin.history.repository.SqliteRepository

object GameRepository {
    val instance: Repository = SqliteRepository()
}