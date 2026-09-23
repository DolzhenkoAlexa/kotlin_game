package main.kotlin.ui.gui

import javafx.application.Application
import javafx.stage.Stage

class BattlegroundApp : Application() {

    override fun start(stage: Stage) {
        stage.title = "Битва Героев"
        stage.width = 900.0
        stage.height = 700.0

        // Показываем главное меню
        MainMenuScreen(stage, this).show()

        stage.show()
    }
}

fun main() {
    Application.launch(BattlegroundApp::class.java)
}
