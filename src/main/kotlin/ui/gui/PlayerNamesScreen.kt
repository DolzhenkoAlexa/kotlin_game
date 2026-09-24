package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.Stage
import main.kotlin.gamemodes.GameModeType
import java.io.File

class PlayerNamesScreen(
    private val stage: Stage,
    private val app: BattlegroundApp,
    private val gameModeType: GameModeType
) {

    fun show() {
        val root = VBox(20.0)
        root.padding = Insets(40.0)
        root.alignment = Pos.CENTER

        val title = Label("Введите имена игроков")
        title.styleClass.add("title")

        val player1Label = Label("Игрок 1:")
        player1Label.style = "-fx-font-size: 16px; -fx-font-weight: bold;"

        val player1Field = TextField()
        player1Field.promptText = "Введите имя первого игрока"
        player1Field.prefWidth = 300.0
        player1Field.style = "-fx-font-size: 14px;"

        val player2Label = Label("Игрок 2:")
        player2Label.style = "-fx-font-size: 16px; -fx-font-weight: bold;"

        val player2Field = TextField()
        player2Field.promptText = "Введите имя второго игрока"
        player2Field.prefWidth = 300.0
        player2Field.style = "-fx-font-size: 14px;"

        val continueBtn = Button("Продолжить →")
        continueBtn.styleClass.addAll("button", "primary")
        continueBtn.prefWidth = 200.0
        continueBtn.setOnAction {
            val name1 = player1Field.text.trim()
            val name2 = player2Field.text.trim()

            if (name1.isEmpty() || name2.isEmpty()) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "Внимание"
                alert.headerText = null
                alert.contentText = "Пожалуйста, введите имена обоих игроков!"
                alert.showAndWait()
                return@setOnAction
            }

            if (name1 == name2) {
                val alert = Alert(Alert.AlertType.WARNING)
                alert.title = "Внимание"
                alert.headerText = null
                alert.contentText = "Имена игроков должны быть разными!"
                alert.showAndWait()
                return@setOnAction
            }

            HeroSelectionScreen(stage, app, gameModeType, name1, name2).show()
        }

        val backBtn = Button("← Назад")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            GameModeSelectionScreen(stage, app).show()
        }

        root.children.addAll(
            title,
            player1Label, player1Field,
            player2Label, player2Field,
            continueBtn,
            backBtn
        )

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }
}
