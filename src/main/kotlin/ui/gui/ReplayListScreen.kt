package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import main.kotlin.history.repository.InMemoryRepository
import java.io.File

class ReplayListScreen(private val stage: Stage, private val app: BattlegroundApp) {

    private val repository = GameRepository.instance

    fun show() {
        val root = VBox(20.0)
        root.padding = Insets(30.0)
        root.alignment = Pos.TOP_CENTER

        val title = Label("📼 Сохраненные игры")
        title.styleClass.add("title")

        val games = repository.getAllGames()

        val gamesContainer = VBox(15.0)
        gamesContainer.alignment = Pos.CENTER

        if (games.isEmpty()) {
            val emptyLabel = Label("Нет сохраненных игр")
            emptyLabel.style = "-fx-font-size: 16px; -fx-text-fill: #7f8c8d;"
            gamesContainer.children.add(emptyLabel)
        } else {
            games.forEach { game ->
                val gameCard = createGameCard(game.id, game.mode, game.player1, game.player2, game.winner, game.date)
                gamesContainer.children.add(gameCard)
            }
        }

        val scrollPane = ScrollPane(gamesContainer)
        scrollPane.isFitToWidth = true
        scrollPane.prefHeight = 500.0
        scrollPane.style = "-fx-background-color: transparent;"

        val backBtn = Button("← Назад")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            MainMenuScreen(stage, app).show()
        }

        root.children.addAll(title, scrollPane, backBtn)

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun createGameCard(
        id: Int,
        mode: String,
        player1: String,
        player2: String,
        winner: String,
        date: String
    ): VBox {
        val card = VBox(8.0)
        card.alignment = Pos.CENTER_LEFT
        card.padding = Insets(15.0)
        card.styleClass.add("hero-card")
        card.prefWidth = 600.0
        card.style = "-fx-cursor: hand;"

        val titleLabel = Label("Игра #$id")
        titleLabel.style = "-fx-font-size: 18px; -fx-font-weight: bold;"

        val modeLabel = Label("Режим: $mode")
        modeLabel.style = "-fx-font-size: 14px;"

        val playersLabel = Label("$player1 vs $player2")
        playersLabel.style = "-fx-font-size: 14px;"

        val winnerLabel = Label("Победитель: $winner")
        winnerLabel.style = "-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;"

        val dateLabel = Label("Дата: $date")
        dateLabel.style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"

        val watchBtn = Button("▶ Смотреть")
        watchBtn.styleClass.addAll("button", "primary")
        watchBtn.setOnAction {
            ReplayViewerScreen(stage, app, id).show()
        }

        card.children.addAll(titleLabel, modeLabel, playersLabel, winnerLabel, dateLabel, watchBtn)

        return card
    }
}
