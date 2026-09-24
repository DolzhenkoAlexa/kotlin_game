package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import main.kotlin.gamemodes.GameModeType
import java.io.File

class GameModeSelectionScreen(private val stage: Stage, private val app: BattlegroundApp) {

    fun show() {
        val root = VBox(20.0)
        root.padding = Insets(40.0)
        root.alignment = Pos.CENTER

        val title = Label("Выбор режима игры")
        title.styleClass.add("title")

        val subtitle = Label("Каждый режим имеет свои правила и героев")
        subtitle.styleClass.add("subtitle")

        val classicBtn = createModeButton(
            "⚔ Классический режим",
            "Только 3 базовых героя\nПростая механика: атака и защита"
        )
        classicBtn.setOnMouseClicked {
            PlayerNamesScreen(stage, app, GameModeType.CLASSIC).show()
        }

        val manaBtn = createModeButton(
            "💎 Режим с маной",
            "5 героев, кубик для получения маны\nМощные ультимейты за 10 маны"
        )
        manaBtn.setOnMouseClicked {
            PlayerNamesScreen(stage, app, GameModeType.MANA).show()
        }

        val teamBtn = createModeButton(
            "👥 Командный режим",
            "Каждый игрок выбирает 3 героев\nСверхспособности 1 раз за игру"
        )
        teamBtn.setOnMouseClicked {
            PlayerNamesScreen(stage, app, GameModeType.TEAM).show()
        }

        val backBtn = Button("← Назад")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            MainMenuScreen(stage, app).show()
        }

        root.children.addAll(title, subtitle, classicBtn, manaBtn, teamBtn, backBtn)

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun createModeButton(title: String, description: String): VBox {
        val container = VBox(5.0)
        container.alignment = Pos.CENTER
        container.padding = Insets(15.0)
        container.styleClass.add("hero-card")
        container.prefWidth = 400.0
        container.style = "-fx-cursor: hand;"

        val titleLabel = Label(title)
        titleLabel.style = "-fx-font-size: 20px; -fx-font-weight: bold;"

        val descLabel = Label(description)
        descLabel.style = "-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;"
        descLabel.isWrapText = true

        container.children.addAll(titleLabel, descLabel)

        return container
    }
}
