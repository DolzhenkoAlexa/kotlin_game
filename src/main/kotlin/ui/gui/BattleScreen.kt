package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Stage
import main.kotlin.actions.*
import main.kotlin.engine.GameEngine
import main.kotlin.engine.GameEvent
import main.kotlin.entities.*
import main.kotlin.gamemodes.GameModeType
import main.kotlin.history.GameRecorder
import java.io.File

class BattleScreen(
    private val stage: Stage,
    private val app: BattlegroundApp,
    private val gameModeType: GameModeType,
    private val player1Name: String,
    private val player2Name: String,
    private val player1Heroes: List<Character>,
    private val player2Heroes: List<Character>
) {

    private val repository = GameRepository.instance
    private val gameRecorder = GameRecorder(repository)
    private val engine = GameEngine(gameRecorder)
    private lateinit var player1: Player
    private lateinit var player2: Player

    private lateinit var player1HeroesBox: HBox
    private lateinit var player2HeroesBox: HBox
    private lateinit var logArea: TextArea
    private lateinit var turnLabel: Label
    private lateinit var actionButtons: VBox

    private var selectedHero: Character? = null
    private var selectedTarget: Character? = null
    private var currentPlayerIndex = 0

    // ── НОВОЕ: собственный список событий и счётчик ходов ──
    private val recordedEvents = mutableListOf<GameEvent>()
    private var recordedTurn = 1

    fun show() {
        player1 = Player(player1Name)
        player1.heroes.addAll(player1Heroes)

        player2 = Player(player2Name)
        player2.heroes.addAll(player2Heroes)

        gameRecorder.setGameInfo(gameModeType.displayName, player1Name, player2Name)

        engine.startGame(player1, player2)

        val battleScene = createBattleScene()
        stage.scene = battleScene
        updateUI()
    }

    private fun createBattleScene(): Scene {
        val root = BorderPane()
        root.padding = Insets(20.0)

        val player2Box = VBox(10.0)
        player2Box.alignment = Pos.CENTER
        val p2Label = Label(player2Name)
        p2Label.style = "-fx-font-size: 18px; -fx-font-weight: bold;"
        player2HeroesBox = HBox(15.0)
        player2HeroesBox.alignment = Pos.CENTER
        player2Box.children.addAll(p2Label, player2HeroesBox)

        val centerBox = VBox(10.0)
        centerBox.alignment = Pos.CENTER

        turnLabel = Label("Ход: $player1Name")
        turnLabel.style = "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2980b9;"

        val modeLabel = Label("Режим: ${getModeName()}")
        modeLabel.style = "-fx-font-size: 14px; -fx-text-fill: #7f8c8d;"

        logArea = TextArea()
        logArea.isEditable = false
        logArea.prefHeight = 220.0
        logArea.styleClass.add("log")
        logArea.text = "⚔ Битва началась!\n\n"

        centerBox.children.addAll(turnLabel, modeLabel, logArea)

        val player1Box = VBox(15.0)
        player1Box.alignment = Pos.CENTER

        player1HeroesBox = HBox(15.0)
        player1HeroesBox.alignment = Pos.CENTER

        actionButtons = createActionButtons()

        val p1Label = Label(player1Name)
        p1Label.style = "-fx-font-size: 18px; -fx-font-weight: bold;"

        player1Box.children.addAll(player1HeroesBox, actionButtons, p1Label)

        root.top = player2Box
        root.center = centerBox
        root.bottom = player1Box

        BorderPane.setMargin(player2Box, Insets(0.0, 0.0, 20.0, 0.0))
        BorderPane.setMargin(centerBox, Insets(20.0, 0.0, 20.0, 0.0))

        val scene = Scene(root, 950.0, 800.0)
        scene.stylesheets.add(File("src/main/resources/styles.css").toURI().toString())
        return scene
    }

    private fun getModeName(): String = gameModeType.displayName

    private fun createActionButtons(): VBox {
        val container = VBox(10.0)
        container.alignment = Pos.CENTER

        val buttonsRow = HBox(10.0)
        buttonsRow.alignment = Pos.CENTER

        val attackBtn = Button("⚔ Атаковать")
        attackBtn.styleClass.addAll("button", "primary")
        attackBtn.setOnAction { performAction(AttackAction()) }

        val defendBtn = Button("🛡 Защита")
        defendBtn.styleClass.add("button")
        defendBtn.setOnAction { performAction(DefendAction()) }

        buttonsRow.children.addAll(attackBtn, defendBtn)

        when (gameModeType) {
            GameModeType.MANA -> {
                val rollBtn = Button("🎲 Кубик")
                rollBtn.styleClass.add("button")
                rollBtn.setOnAction { performAction(RollDiceAction()) }

                val ultimateBtn = Button("💥 Ультимейт (10 маны)")
                ultimateBtn.styleClass.addAll("button", "danger")
                ultimateBtn.setOnAction { performAction(UltimateAction()) }

                buttonsRow.children.addAll(rollBtn, ultimateBtn)
            }
            GameModeType.TEAM -> {
                val ultimateBtn = Button("💥 Сверхспособность (1 раз)")
                ultimateBtn.styleClass.addAll("button", "danger")
                ultimateBtn.setOnAction { performAction(UltimateAction()) }

                buttonsRow.children.add(ultimateBtn)
            }
            else -> {}
        }

        container.children.add(buttonsRow)
        return container
    }

    private fun updateUI() {
        player1HeroesBox.children.clear()
        player1.heroes.forEach { hero ->
            player1HeroesBox.children.add(createHeroCard(hero, true))
        }

        player2HeroesBox.children.clear()
        player2.heroes.forEach { hero ->
            player2HeroesBox.children.add(createHeroCard(hero, false))
        }

        val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
        turnLabel.text = "Ход: ${currentPlayer.name}"
    }

    private fun createHeroCard(hero: Character, isPlayer1: Boolean): VBox {
        val card = VBox(8.0)
        card.alignment = Pos.CENTER
        card.styleClass.add("hero-card")
        card.prefWidth = 140.0

        if (!hero.isAlive()) {
            card.styleClass.add("dead")
        }

        val imageName = when (hero.type) {
            "Рыцарь" -> "knight.JPG"
            "Маг" -> "mage.JPG"
            "Лучник" -> "archer.JPG"
            "Варвар" -> "barbarian.JPG"
            "Паладин" -> "paladin.JPG"
            "Некромант" -> "necromancer.JPG"
            else -> "knight.JPG"
        }

        val imageView = createHeroImage(imageName, 80.0)

        val nameLabel = Label(hero.type)
        nameLabel.style = "-fx-font-weight: bold; -fx-font-size: 13px;"

        val hpLabel = Label("❤ HP: ${hero.health}")
        hpLabel.style = "-fx-font-size: 12px;"

        val statsLabel = Label("⚔${hero.power} | 🛡${hero.defence}")
        statsLabel.style = "-fx-font-size: 11px; -fx-text-fill: #7f8c8d;"

        card.children.addAll(imageView, nameLabel, hpLabel, statsLabel)

        if (gameModeType == GameModeType.MANA) {
            val manaLabel = Label("💎 Мана: ${hero.mana}/10")
            manaLabel.style = "-fx-font-size: 11px; -fx-text-fill: #8e44ad;"
            card.children.add(manaLabel)
        }

        if (gameModeType == GameModeType.TEAM) {
            val ultStatus = if (hero.specialAbilityUsed) "Ульта использована" else "Ульта готова"
            val ultLabel = Label("💥 $ultStatus")
            ultLabel.style = "-fx-font-size: 10px; -fx-text-fill: ${if (hero.specialAbilityUsed) "#e74c3c" else "#27ae60"};"
            card.children.add(ultLabel)
        }

        card.setOnMouseClicked {
            if (hero.isAlive()) {
                val currentPlayer = if (currentPlayerIndex == 0) player1 else player2

                if (gameModeType == GameModeType.TEAM) {
                    if (isPlayer1 && currentPlayer == player1) {
                        selectedHero = hero
                        logArea.appendText("✅ Выбран атакующий: ${hero.type}\n")
                        updateUI()
                    } else if (!isPlayer1 && currentPlayer == player2) {
                        selectedHero = hero
                        logArea.appendText("✅ Выбран атакующий: ${hero.type}\n")
                        updateUI()
                    } else {
                        selectedTarget = hero
                        logArea.appendText("🎯 Выбрана цель: ${hero.type}\n")
                        updateUI()
                    }
                } else {
                    if (isPlayer1 && currentPlayer == player1) {
                        selectedHero = hero
                        logArea.appendText("Выбран герой: ${hero.type}\n")
                    } else if (!isPlayer1 && currentPlayer == player2) {
                        selectedHero = hero
                        logArea.appendText("Выбран герой: ${hero.type}\n")
                    } else {
                        selectedTarget = hero
                        logArea.appendText("Выбрана цель: ${hero.type}\n")
                    }
                }
            }
        }

        if (gameModeType == GameModeType.TEAM) {
            val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
            if ((isPlayer1 && currentPlayer == player1) || (!isPlayer1 && currentPlayer == player2)) {
                if (selectedHero == hero) card.styleClass.add("selected")
            } else {
                if (selectedTarget == hero) {
                    card.style = "-fx-border-color: #e74c3c; -fx-border-width: 3; -fx-background-color: #fadbd8;"
                }
            }
        }

        return card
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

    private fun performAction(action: Action) {
        if (engine.isGameOver()) return

        val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
        val opponent = if (currentPlayerIndex == 0) player2 else player1

        val actor = if (gameModeType == GameModeType.TEAM) {
            if (currentPlayer.getAliveHeroes().size > 1 && selectedHero == null) {
                logArea.appendText("❌ Выберите героя для атаки или защиты! (кликните на карточку своего героя)\n")
                return
            }
            selectedHero ?: currentPlayer.getAliveHeroes().firstOrNull()
        } else {
            currentPlayer.getAliveHeroes().firstOrNull()
        }

        if (actor == null) {
            logArea.appendText("❌ Нет живых героев!\n")
            return
        }

        val needsTarget = when (action) {
            is AttackAction -> true
            is DefendAction -> false
            is RollDiceAction -> false
            is UltimateAction -> when (actor.type) {
                "Паладин" -> false
                else -> true
            }
            else -> false
        }

        val target = if (needsTarget) {
            if (gameModeType == GameModeType.TEAM && opponent.getAliveHeroes().size > 1 && selectedTarget == null) {
                logArea.appendText("❌ Выберите цель для атаки! (кликните на карточку врага)\n")
                return
            }
            selectedTarget ?: opponent.getAliveHeroes().firstOrNull()
        } else {
            null
        }

        if (needsTarget && target == null) {
            logArea.appendText("❌ Нет цели для атаки!\n")
            return
        }

        if (gameModeType == GameModeType.MANA && action is UltimateAction) {
            if (actor.mana < 10) {
                logArea.appendText("❌ Недостаточно маны! (нужно 10, есть ${actor.mana})\n")
                return
            }
            actor.mana -= 10
        }

        if (gameModeType == GameModeType.TEAM && action is UltimateAction) {
            if (actor.specialAbilityUsed) {
                logArea.appendText("❌ Сверхспособность уже использована!\n")
                return
            }
            actor.specialAbilityUsed = true
        }

        // хп до действия
        val hpBefore = opponent.heroes.associateWith { it.health }
        val targetHpBefore = target?.health

        val oldOut = System.out
        val logStream = java.io.ByteArrayOutputStream()
        System.setOut(java.io.PrintStream(logStream))

        action.execute(
            character = actor,
            target = target,
            isTeamMode = gameModeType == GameModeType.TEAM,
            player = currentPlayer,
            targetPlayer = opponent
        )

        System.setOut(oldOut)
        val actionLog = logStream.toString("UTF-8")
        logArea.appendText(actionLog)
        logArea.scrollTop = Double.MAX_VALUE

        // Считаеь урон (суммарный по всем врагам) и пишет событие
        val targetHpAfter = target?.health
        val totalDamage = opponent.heroes.sumOf { (hpBefore[it] ?: it.health) - it.health }

        val event = GameEvent(
            turnNumber = recordedTurn,
            playerName = currentPlayer.name,
            actorType = actor.type,
            actionType = action.javaClass.simpleName,
            targetType = target?.type,
            damage = totalDamage.takeIf { it > 0 },
            healthBefore = targetHpBefore,
            healthAfter = targetHpAfter
        )
        recordedEvents.add(event)
        recordedTurn++

        selectedHero = null
        selectedTarget = null

        // проверка победы для всех режимов
        if (!opponent.hasAliveHeroes()) {
            logArea.appendText("\nПОБЕДА: ${currentPlayer.name}! \n")
            gameRecorder.saveGame(recordedEvents.toList(), currentPlayer.name)

            actionButtons.children.clear()

            val backBtn = Button("← Вернуться в главное меню")
            backBtn.styleClass.addAll("button", "primary")
            backBtn.prefWidth = 300.0
            backBtn.prefHeight = 50.0
            backBtn.style = "-fx-font-size: 16px;"
            backBtn.setOnAction { MainMenuScreen(stage, app).show() }
            actionButtons.children.add(backBtn)

            updateUI()
            return
        }

        currentPlayerIndex = 1 - currentPlayerIndex
        updateUI()
    }
}