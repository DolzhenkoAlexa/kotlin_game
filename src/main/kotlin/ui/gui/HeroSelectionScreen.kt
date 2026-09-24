package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import main.kotlin.entities.*
import main.kotlin.gamemodes.GameModeType
import java.io.File

class HeroSelectionScreen(
    private val stage: Stage,
    private val app: BattlegroundApp,
    private val gameModeType: GameModeType,
    private val player1Name: String,
    private val player2Name: String
) {

    private val selectedHeroes = mutableMapOf<String, MutableList<String>>()
    private var currentPlayer = player1Name
    private var selectionPhase = 0 // для командного режима

    private val availableHeroes = when (gameModeType) {
        GameModeType.CLASSIC -> listOf("Рыцарь", "Маг", "Лучник")
        GameModeType.MANA -> listOf("Рыцарь", "Маг", "Лучник", "Варвар", "Некромант")
        GameModeType.TEAM -> listOf("Рыцарь", "Маг", "Лучник", "Варвар", "Паладин", "Некромант")
    }

    init {
        selectedHeroes[player1Name] = mutableListOf()
        selectedHeroes[player2Name] = mutableListOf()
    }

    fun show() {
        val root = VBox(20.0)
        root.padding = Insets(30.0)
        root.alignment = Pos.TOP_CENTER

        val title = Label("Выбор героев")
        title.styleClass.add("title")

        val modeDescription = when (gameModeType) {
            GameModeType.CLASSIC -> "Классический режим: каждый выбирает 1 героя из 3"
            GameModeType.MANA -> "Режим с маной: каждый выбирает 1 героя из 5"
            GameModeType.TEAM -> "Командный режим: каждый выбирает 3 героев по очереди"
        }

        val subtitle = Label(modeDescription)
        subtitle.styleClass.add("subtitle")

        val statusLabel = Label(getStatusText())
        statusLabel.style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2980b9;"

        val infoLabel = Label(getSelectionInfo())
        infoLabel.style = "-fx-font-size: 14px; -fx-text-fill: #7f8c8d;"

        val heroGrid = GridPane()
        heroGrid.hgap = 15.0
        heroGrid.vgap = 15.0
        heroGrid.alignment = Pos.CENTER

        availableHeroes.forEachIndexed { index, heroName ->
            val card = createHeroCard(heroName, statusLabel, infoLabel)
            heroGrid.add(card, index % 3, index / 3)
        }

        val backBtn = Button("← Назад")
        backBtn.styleClass.add("button")
        backBtn.setOnAction {
            PlayerNamesScreen(stage, app, gameModeType).show()
        }

        root.children.addAll(title, subtitle, statusLabel, infoLabel, heroGrid, backBtn)

        val scene = Scene(root, 950.0, 750.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        stage.scene = scene
    }

    private fun getStatusText(): String {
        return when (gameModeType) {
            GameModeType.TEAM -> {
                val p1Count = selectedHeroes[player1Name]?.size ?: 0
                val p2Count = selectedHeroes[player2Name]?.size ?: 0
                "Сейчас выбирает: $currentPlayer | $player1Name: $p1Count/3 | $player2Name: $p2Count/3"
            }
            else -> "Сейчас выбирает: $currentPlayer"
        }
    }

    private fun getSelectionInfo(): String {
        val p1Heroes = selectedHeroes[player1Name]?.joinToString(", ") ?: "не выбрано"
        val p2Heroes = selectedHeroes[player2Name]?.joinToString(", ") ?: "не выбрано"
        return "$player1Name: $p1Heroes\n$player2Name: $p2Heroes"
    }

    private fun createHeroCard(heroName: String, statusLabel: Label, infoLabel: Label): VBox {
        val card = VBox(10.0)
        card.alignment = Pos.CENTER
        card.styleClass.add("hero-card")
        card.prefWidth = 150.0
        card.prefHeight = 220.0

        val imageName = when (heroName) {
            "Рыцарь" -> "knight.JPG"
            "Маг" -> "mage.JPG"
            "Лучник" -> "archer.JPG"
            "Варвар" -> "barbarian.JPG"
            "Паладин" -> "paladin.JPG"
            "Некромант" -> "necromancer.JPG"
            else -> "knight.JPG"
        }

        val imageView = createHeroImage(imageName, 100.0)

        val nameLabel = Label(heroName)
        nameLabel.style = "-fx-font-size: 14px; -fx-font-weight: bold;"

        val statsLabel = Label(getHeroStats(heroName))
        statsLabel.style = "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"

        val isAlreadySelected = selectedHeroes.values.any { it.contains(heroName) }

        if (isAlreadySelected) {
            card.styleClass.add("dead")
            val takenLabel = Label("Занят")
            takenLabel.style = "-fx-font-size: 12px; -fx-text-fill: #e74c3c;"
            card.children.addAll(imageView, nameLabel, statsLabel, takenLabel)
        } else {
            card.children.addAll(imageView, nameLabel, statsLabel)
            card.style = "-fx-cursor: hand;"
            card.setOnMouseClicked {
                selectHero(heroName, statusLabel, infoLabel)
            }
        }

        return card
    }

    private fun selectHero(heroName: String, statusLabel: Label, infoLabel: Label) {
        val currentPlayerHeroes = selectedHeroes[currentPlayer] ?: return

        val maxHeroes = when (gameModeType) {
            GameModeType.TEAM -> 3
            else -> 1
        }

        if (currentPlayerHeroes.size >= maxHeroes) {
            return
        }

        val isAlreadySelected = selectedHeroes.values.any { it.contains(heroName) }
        if (isAlreadySelected) {
            return
        }

        currentPlayerHeroes.add(heroName)

        // Проверяем, завершен ли выбор
        val player1Heroes = selectedHeroes[player1Name] ?: return
        val player2Heroes = selectedHeroes[player2Name] ?: return

        val allSelected = when (gameModeType) {
            GameModeType.TEAM -> {
                player1Heroes.size == 3 && player2Heroes.size == 3
            }
            else -> {
                player1Heroes.size == 1 && player2Heroes.size == 1
            }
        }

        if (allSelected) {
            startBattle()
        } else {
            // Переключаем игрока
            currentPlayer = if (currentPlayer == player1Name) player2Name else player1Name
            statusLabel.text = getStatusText()
            infoLabel.text = getSelectionInfo()
            show()
        }
    }

    private fun startBattle() {
        val player1Heroes = selectedHeroes[player1Name]?.map { createHero(it) } ?: return
        val player2Heroes = selectedHeroes[player2Name]?.map { createHero(it) } ?: return

        BattleScreen(stage, app, gameModeType, player1Name, player2Name, player1Heroes, player2Heroes).show()
    }

    private fun createHero(type: String): Character {
        return when (type) {
            "Рыцарь" -> Knight(type)
            "Маг" -> Mage(type)
            "Лучник" -> Archer(type)
            "Варвар" -> Barbarian(type)
            "Паладин" -> Paladin(type)
            "Некромант" -> Necromancer(type)
            else -> Knight(type)
        }
    }

    private fun createHeroImage(imageName: String, size: Double): ImageView {
        return try {
            val imageFile = File("src/main/resources/$imageName")
            val image = Image(imageFile.toURI().toString())
            val imageView = ImageView(image)
            imageView.fitWidth = size
            imageView.fitHeight = size
            imageView.isPreserveRatio = true
            imageView
        } catch (e: Exception) {
            val placeholder = ImageView()
            placeholder.fitWidth = size
            placeholder.fitHeight = size
            placeholder
        }
    }

    private fun getHeroStats(heroName: String): String {
        return when (heroName) {
            "Рыцарь" -> "HP: 60 | ATK: 8 | DEF: 7"
            "Маг" -> "HP: 55 | ATK: 14 | DEF: 1"
            "Лучник" -> "HP: 60 | ATK: 10 | DEF: 5"
            "Варвар" -> "HP: 60 | ATK: 13 | DEF: 2"
            "Паладин" -> "HP: 55 | ATK: 6 | DEF: 10"
            "Некромант" -> "HP: 60 | ATK: 11 | DEF: 3"
            else -> ""
        }
    }
}
