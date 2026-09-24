package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File

class StatisticsScreen(private val stage: Stage, private val app: BattlegroundApp) {

    private val repository = GameRepository.instance

    fun show() {
        val root = VBox(20.0)
        root.padding = Insets(30.0)
        root.alignment = Pos.TOP_CENTER

        val title = Label("📊 Статистика игроков")
        title.styleClass.add("title")

        val stats = repository.getStatistics()

        val totalGamesLabel = Label("Всего игр: ${stats.totalGames}")
        totalGamesLabel.style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2980b9;"

        val statsGrid = GridPane()
        statsGrid.hgap = 20.0
        statsGrid.vgap = 15.0
        statsGrid.alignment = Pos.CENTER
        statsGrid.padding = Insets(20.0)

        // Заголовки таблицы
        val headerPlayer = Label("Игрок")
        headerPlayer.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

        val headerGames = Label("Игр")
        headerGames.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

        val headerWins = Label("Побед")
        headerWins.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

        val headerWinRate = Label("% побед")
        headerWinRate.style = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"

        statsGrid.add(headerPlayer, 0, 0)
        statsGrid.add(headerGames, 1, 0)
        statsGrid.add(headerWins, 2, 0)
        statsGrid.add(headerWinRate, 3, 0)

        if (stats.players.isEmpty()) {
            val emptyLabel = Label("Нет данных. Сыграйте хотя бы одну игру!")
            emptyLabel.style = "-fx-font-size: 16px; -fx-text-fill: #7f8c8d;"
            root.children.addAll(title, emptyLabel)
        } else {
            // Сортировка игроков по проценту побед (от большего к меньшему)
            val sortedPlayers = stats.players.entries.sortedByDescending { (_, playerStats) ->
                if (playerStats.gamesPlayed > 0) {
                    (playerStats.wins.toDouble() / playerStats.gamesPlayed.toDouble()) * 100
                } else {
                    0.0
                }
            }

            sortedPlayers.forEachIndexed { index, (playerName, playerStats) ->
                val winRate = if (playerStats.gamesPlayed > 0) {
                    (playerStats.wins.toDouble() / playerStats.gamesPlayed.toDouble()) * 100
                } else {
                    0.0
                }

                val nameLabel = Label(playerName)
                nameLabel.style = "-fx-font-size: 13px;"

                val gamesLabel = Label("${playerStats.gamesPlayed}")
                gamesLabel.style = "-fx-font-size: 13px;"

                val winsLabel = Label("${playerStats.wins}")
                winsLabel.style = "-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;"

                val winRateLabel = Label(String.format("%.1f%%", winRate))
                winRateLabel.style = "-fx-font-size: 13px; -fx-text-fill: ${getWinRateColor(winRate)}; -fx-font-weight: bold;"

                statsGrid.add(nameLabel, 0, index + 1)
                statsGrid.add(gamesLabel, 1, index + 1)
                statsGrid.add(winsLabel, 2, index + 1)
                statsGrid.add(winRateLabel, 3, index + 1)
            }

            val scrollPane = ScrollPane(statsGrid)
            scrollPane.isFitToWidth = true
            scrollPane.prefHeight = 400.0
            scrollPane.style = "-fx-background-color: transparent; -fx-border-color: #d0d7de; -fx-border-radius: 8; -fx-background-radius: 8;"

            root.children.addAll(title, totalGamesLabel, scrollPane)
        }

        val backBtn = Button("← Назад")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            MainMenuScreen(stage, app).show()
        }

        root.children.add(backBtn)

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun getWinRateColor(winRate: Double): String {
        return when {
            winRate >= 70.0 -> "#27ae60" // зеленый для 70+ процентов
            winRate >= 40.0 -> "#f39c12" // оранжевый для 40+ процентов
            else -> "#e74c3c" // красный для оставшихся
        }
    }
}
