package main.kotlin.ui.gui

import javafx.animation.PauseTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration
import main.kotlin.history.repository.InMemoryRepository
import java.io.File

class ReplayViewerScreen(
    private val stage: Stage,
    private val app: BattlegroundApp,
    private val gameId: Int
) {

    private val repository = GameRepository.instance
    private lateinit var logArea: TextArea
    private lateinit var turnLabel: Label
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var speedLabel: Label

    private var currentEventIndex = 0
    private var isPlaying = false
    private var playbackSpeed = 1.0 // время между ходами

    fun show() {
        val game = repository.getGame(gameId)

        if (game == null) {
            val alert = javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR)
            alert.title = "Ошибка"
            alert.headerText = null
            alert.contentText = "Игра с ID $gameId не найдена"
            alert.showAndWait()
            ReplayListScreen(stage, app).show()
            return
        }

        val root = BorderPane()
        root.padding = Insets(20.0)

        // инфо об игре
        val topBox = VBox(10.0)
        topBox.alignment = Pos.CENTER

        val title = Label("📼 Просмотр реплея #$gameId")
        title.styleClass.add("title")

        val infoLabel = Label("${game.player1} vs ${game.player2} | Режим: ${game.mode} | Победитель: ${game.winner}")
        infoLabel.style = "-fx-font-size: 14px;"

        val dateLabel = Label("Дата: ${game.date}")
        dateLabel.style = "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"

        turnLabel = Label("Ход: 0 / ${game.events.size}")
        turnLabel.style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2980b9;"

        topBox.children.addAll(title, infoLabel, dateLabel, turnLabel)

        // лог событий
        logArea = TextArea()
        logArea.isEditable = false
        logArea.styleClass.add("log")
        logArea.prefHeight = 400.0
        logArea.text = "⏸ Реплей готов к воспроизведению\n\n"

        // управление воспроизведением
        val controlsBox = VBox(15.0)
        controlsBox.alignment = Pos.CENTER

        val buttonsRow = HBox(10.0)
        buttonsRow.alignment = Pos.CENTER

        playButton = Button("▶ Воспроизвести")
        playButton.styleClass.addAll("button", "primary")
        playButton.setOnAction {
            startPlayback(game.events)
        }

        pauseButton = Button("⏸ Пауза")
        pauseButton.styleClass.add("button")
        pauseButton.isDisable = true
        pauseButton.setOnAction {
            pausePlayback()
        }

        val restartButton = Button("🔄 Сначала")
        restartButton.styleClass.add("button")
        restartButton.setOnAction {
            restartPlayback()
        }

        buttonsRow.children.addAll(playButton, pauseButton, restartButton)

        val backBtn = Button("← Назад к списку")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            ReplayListScreen(stage, app).show()
        }

        controlsBox.children.addAll(buttonsRow, backBtn)

        root.top = topBox
        root.center = logArea
        root.bottom = controlsBox

        BorderPane.setMargin(topBox, Insets(0.0, 0.0, 20.0, 0.0))
        BorderPane.setMargin(logArea, Insets(20.0, 0.0, 20.0, 0.0))

        val scene = Scene(root, 900.0, 700.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun startPlayback(events: List<main.kotlin.engine.GameEvent>) {
        if (currentEventIndex >= events.size) {
            currentEventIndex = 0
            logArea.clear()
            logArea.appendText("⏸ Реплей готов к воспроизведению\n\n")
        }

        isPlaying = true
        playButton.isDisable = true
        pauseButton.isDisable = false

        playNextEvent(events)
    }

    private fun playNextEvent(events: List<main.kotlin.engine.GameEvent>) {
        if (!isPlaying || currentEventIndex >= events.size) {
            isPlaying = false
            playButton.isDisable = false
            pauseButton.isDisable = true

            if (currentEventIndex >= events.size) {
                logArea.appendText("\n✅ Конец реплея\n")
            }
            return
        }

        val event = events[currentEventIndex]

        logArea.appendText("⚔ Ход ${event.turnNumber}: ${event.playerName} (${event.actorType})\n")
        logArea.appendText("   Действие: ${getActionName(event.actionType)}\n")

        if (event.targetType != null) {
            logArea.appendText("   Цель: ${event.targetType}\n")
        }

        if (event.damage != null && event.damage > 0) {
            logArea.appendText("   💥 Урон: ${event.damage}\n")
            if (event.healthAfter != null) {
                logArea.appendText("   ❤ HP цели: ${event.healthBefore} → ${event.healthAfter}\n")
            }
        }

        logArea.appendText("\n")
        logArea.scrollTop = Double.MAX_VALUE

        currentEventIndex++
        turnLabel.text = "Ход: $currentEventIndex / ${events.size}"

        val pause = PauseTransition(Duration.seconds(playbackSpeed))
        pause.setOnFinished {
            playNextEvent(events)
        }
        pause.play()
    }

    private fun pausePlayback() {
        isPlaying = false
        playButton.isDisable = false
        pauseButton.isDisable = true
        logArea.appendText("⏸ Пауза\n\n")
    }

    private fun restartPlayback() {
        isPlaying = false
        currentEventIndex = 0
        playButton.isDisable = false
        pauseButton.isDisable = true
        logArea.clear()
        logArea.appendText("🔄 Реплей перезапущен\n\n")
        turnLabel.text = "Ход: 0 / ${turnLabel.text.split("/").lastOrNull()?.trim() ?: "0"}"
    }

    private fun getActionName(actionType: String): String {
        return when (actionType) {
            "AttackAction" -> "⚔ Атака"
            "DefendAction" -> "🛡 Защита"
            "UltimateAction" -> "💥 Ультимейт"
            "RollDiceAction" -> "🎲 Бросок кубика"
            else -> actionType
        }
    }
}
