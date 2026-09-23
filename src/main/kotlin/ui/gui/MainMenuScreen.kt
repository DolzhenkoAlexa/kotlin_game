package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File

class MainMenuScreen(private val stage: Stage, private val app: BattlegroundApp) {

    fun show() {
        val root = VBox(25.0)
        root.padding = Insets(50.0)
        root.alignment = Pos.CENTER
        root.style = "-fx-background-color: linear-gradient(to bottom, #2c3e50, #3498db);"

        val title = Label("⚔ БИТВА ГЕРОЕВ ⚔")
        title.style = "-fx-font-size: 42px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 3);"

        val subtitle = Label("Выберите режим игры")
        subtitle.style = "-fx-font-size: 18px; -fx-text-fill: #ecf0f1;"

        val newGameBtn = createMenuButton("🎮 Новая игра")
        newGameBtn.setOnAction {
            GameModeSelectionScreen(stage, app).show()
        }

        val replayBtn = createMenuButton("📼 Просмотр реплеев")
        replayBtn.setOnAction {
            ReplayListScreen(stage, app).show()
        }

        val statsBtn = createMenuButton("📊 Статистика")
        statsBtn.setOnAction {
            StatisticsScreen(stage, app).show()
        }

        val exitBtn = createMenuButton("🚪 Выход")
        exitBtn.setOnAction {
            stage.close()
        }

        root.children.addAll(title, subtitle, newGameBtn, replayBtn, statsBtn, exitBtn)

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun createMenuButton(text: String): Button {
        val button = Button(text)
        button.styleClass.addAll("button", "primary")
        button.prefWidth = 300.0
        button.prefHeight = 60.0
        button.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        return button
    }
}
