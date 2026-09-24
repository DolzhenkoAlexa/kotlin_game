package main.kotlin.ui.gui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

object GuiUtils {

    private val cache = mutableMapOf<String, Image?>()

    private val fileNames = mapOf(
        "Рыцарь" to "knight", "Маг" to "mage", "Лучник" to "archer",
        "Варвар" to "barbarian", "Паладин" to "paladin", "Некромант" to "necromancer"
    )

    fun loadImage(type: String): Image? = cache.getOrPut(type) {
        val name = fileNames[type] ?: "knight"
        GuiUtils::class.java.getResourceAsStream("/cards/$name.jpg")?.let { Image(it) }
    }

    fun label(text: String, size: Int = 14, bold: Boolean = false, color: String = "#2c3e50"): Label =
        Label(text).apply {
            font = Font.font("Segoe UI", if (bold) FontWeight.BOLD else FontWeight.NORMAL, size.toDouble())
            style = "-fx-text-fill: $color;"
        }

    fun button(text: String, style: String? = null, action: () -> Unit): Button =
        Button(text).apply {
            style?.let { styleClass.add(it) }
            setOnAction { action() }
        }

    fun logArea(): TextArea = TextArea().apply {
        isEditable = false
        isWrapText = true
        prefHeight = 150.0
        styleClass.add("log")
    }

    fun heroCard(
        type: String,
        hp: Int? = null,
        def: Int? = null,
        mana: Int? = null,
        selected: Boolean = false,
        dead: Boolean = false,
        tip: String? = null,
        onClick: (() -> Unit)? = null
    ): VBox {
        val card = VBox(5.0).apply {
            alignment = Pos.CENTER
            padding = Insets(10.0)
            prefWidth = 145.0
            styleClass.add("hero-card")
            if (selected) styleClass.add("selected")
            if (dead) styleClass.add("dead")
        }

        loadImage(type)?.let {
            card.children.add(ImageView(it).apply {
                fitWidth = 105.0; fitHeight = 105.0; isPreserveRatio = true
            })
        }

        card.children.add(label(type, 14, bold = true))
        hp?.let { card.children.add(label("HP: $it", 12, color = "#27ae60")) }
        def?.let { card.children.add(label("DEF: $it", 12, color = "#2980b9")) }
        mana?.let { card.children.add(label("MP: $it", 12, color = "#8e44ad")) }

        tip?.let { Tooltip.install(card, Tooltip(it)) }

        onClick?.let {
            card.cursor = Cursor.HAND
            card.setOnMouseClicked { it() }
        }

        return card
    }
}