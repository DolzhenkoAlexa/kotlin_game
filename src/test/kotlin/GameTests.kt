package test.kotlin

import main.kotlin.actions.*
import main.kotlin.engine.GameEngine
import main.kotlin.engine.GameEvent
import main.kotlin.entities.*
import main.kotlin.history.GameRecorder
import main.kotlin.history.GameReplayer
import main.kotlin.history.repository.InMemoryRepository
import main.kotlin.history.repository.PlayersStatistics
import main.kotlin.ui.UserInterface
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class GameTests {

    @Test
    fun testDamageCalculation() {
        val knight = Knight("Артур")

        // Атака без защиты: 60 - (10 - 7) = 57
        knight.takeDamage(10)
        assertEquals(57, knight.health) {
            "Урон прошел без учета защиты. Ожидалось 57 HP, получено ${knight.health}"
        }

        // Атака с бонусом защиты: защита 7 + бонус 7 = 14, урон 20 - 14 = 6, 57 - 6 = 51
        knight.isDefending = true
        knight.defenceBonus = 7
        knight.takeDamage(20)
        assertEquals(51, knight.health) {
            "Урон был рассчитан без учета бонуса защиты. Ожидалось 51 HP, получено ${knight.health}"
        }

        // Минимальный урон всегда равен 1, даже если урон меньше защиты
        val weakAttack = CharacterTest("Слабак", 10, 1, 10)
        weakAttack.takeDamage(9)
        assertEquals(9, weakAttack.health) {
            "Минимальный урон должен быть 1. Ожидалось 9 HP, получено ${weakAttack.health}"
        }

        // Проверка, что после получения урона защита снимается
        knight.isDefending = true
        knight.defenceBonus = 10
        knight.takeDamage(5)
        assertFalse(knight.isDefending) {
            "После получения урона защита должна быть снята, но isDefending осталось true"
        }
        assertEquals(0, knight.defenceBonus) {
            "После получения урона бонус защиты должен быть сброшен, но defenceBonus равен ${knight.defenceBonus}"
        }
    }

    @Test
    fun testDefendAction() {
        val knight = Knight("Артур")
        val defendAction = DefendAction()

        // Обычный режим: защищается только один герой
        defendAction.execute(knight, null, false, null, null)
        assertTrue(knight.isDefending) {
            "После выполнения DefendAction защита не активирована для героя"
        }
        assertEquals((knight.defence / 2.0).roundToInt(), knight.defenceBonus) {
            "Бонус защиты рассчитан неверно. Ожидалось ${(knight.defence / 2.0).roundToInt()}, получено ${knight.defenceBonus}"
        }

        // Командный режим: защищаться должны все живые герои в команде
        val player = Player("Командир")
        player.heroes.add(Knight("Артур"))
        player.heroes.add(Mage("Мерлин"))

        defendAction.execute(knight, null, true, player, null)
        player.heroes.forEach { hero ->
            assertTrue(hero.isDefending) {
                "В командном режиме герой ${hero.type} должен защищаться, но isDefending = false"
            }
        }
    }

    @Test
    fun testUltimateKnight() {
        val knight = Knight("Артур")
        val target = Mage("Мерлин")
        val initialHealth = target.health
        val action = UltimateAction()

        action.execute(knight, target, false, null, null)

        assertEquals(initialHealth - 10, target.health) {
            "Сверхспособность Рыцаря должна наносить 10 урона цели. Ожидалось ${initialHealth - 10}, получено ${target.health}"
        }
        assertTrue(knight.isDefending) {
            "Сверхспособность Рыцаря должна активировать защиту, но isDefending = false"
        }
        assertEquals(knight.defence * 3, knight.defenceBonus) {
            "Сверхспособность Рыцаря должна давать бонус защиты +200% (в 3 раза). Ожидалось ${knight.defence * 3}, получено ${knight.defenceBonus}"
        }
    }

    @Test
    fun testUltimateMage() {
        val mage = Mage("Мерлин")
        val target = Knight("Артур")
        val initialHealth = target.health
        val action = UltimateAction()

        // Обычный режим: урон по одной цели
        action.execute(mage, target, false, null, null)
        assertEquals(initialHealth - 10, target.health) {
            "Сверхспособность Мага должна наносить 10 урона цели. Ожидалось ${initialHealth - 10}, получено ${target.health}"
        }

        // Командный режим: урон по всем врагам
        val player1 = Player("Командир")
        player1.heroes.add(mage)
        val player2 = Player("Враг")
        player2.heroes.add(Knight("Артур"))
        player2.heroes.add(Archer("Леголас"))

        val healthBefore1 = player2.heroes[0].health
        val healthBefore2 = player2.heroes[1].health

        action.execute(mage, null, true, player1, player2)

        assertEquals(healthBefore1 - 10, player2.heroes[0].health) {
            "В командном режиме сверхспособность Мага должна наносить 10 урона первому врагу. Ожидалось ${healthBefore1 - 10}, получено ${player2.heroes[0].health}"
        }
        assertEquals(healthBefore2 - 10, player2.heroes[1].health) {
            "В командном режиме сверхспособность Мага должна наносить 10 урона второму врагу. Ожидалось ${healthBefore2 - 10}, получено ${player2.heroes[1].health}"
        }
    }

    @Test
    fun testUltimateArcher() {
        val archer = Archer("Леголас")
        val target = Mage("Мерлин")
        val initialHealth = target.health
        val action = UltimateAction()

        action.execute(archer, target, false, null, null)

        // Лучник не игнорирует защиту, поэтому урон = 35 - защита цели
        val expectedDamage = 35 - target.defence
        assertEquals(initialHealth - expectedDamage, target.health) {
            "Сверхспособность Лучника должна наносить ${expectedDamage} урона с учетом защиты цели (защита = ${target.defence}). " +
                    "Ожидалось ${initialHealth - expectedDamage}, получено ${target.health}"
        }
    }

    @Test
    fun testUltimateBarbarian() {
        val barbarian = Barbarian("Конан")
        val target = Mage("Мерлин")
        val initialHealth = target.health
        val initialBarbarianHealth = barbarian.health
        val action = UltimateAction()

        action.execute(barbarian, target, false, null, null)

        assertEquals(initialHealth - 35, target.health) {
            "Сверхспособность Варвара должна наносить 35 урона цели. Ожидалось ${initialHealth - 35}, получено ${target.health}"
        }
        assertEquals(initialBarbarianHealth - 20, barbarian.health) {
            "Сверхспособность Варвара должна наносить 20 урона себе. Ожидалось ${initialBarbarianHealth - 20}, получено ${barbarian.health}"
        }

        // Проверка, что сверхспособность недоступна при HP <= 20
        barbarian.health = 20
        val healthBeforeInvalid = barbarian.health
        action.execute(barbarian, target, false, null, null)
        assertEquals(healthBeforeInvalid, barbarian.health) {
            "Сверхспособность Варвара не должна срабатывать при HP <= 20. Ожидалось $healthBeforeInvalid, получено ${barbarian.health}"
        }
    }

    @Test
    fun testUltimatePaladin() {
        // Паладин доступен только в командном режиме
        val player = Player("Командир")
        val paladin = Paladin("Утер")
        val knight = Knight("Артур")
        val mage = Mage("Мерлин")

        player.heroes.add(paladin)
        player.heroes.add(knight)
        player.heroes.add(mage)

        // Наносим урон через takeDamage() и запоминаем результат
        paladin.takeDamage(20)  // У Паладина защита 10, урон = 10
        knight.takeDamage(20)   // У Рыцаря защита 7, урон = 13
        mage.takeDamage(20)     // У Мага защита 1, урон = 19

        val healthBeforePaladin = paladin.health  // 55 - 10 = 45
        val healthBeforeKnight = knight.health    // 60 - 13 = 47
        val healthBeforeMage = mage.health        // 55 - 19 = 36

        val action = UltimateAction()
        action.execute(paladin, null, true, player, null)

        // Проверяем, что лечение сработало
        assertTrue(paladin.health > healthBeforePaladin) {
            "Паладин должен вылечиться. Было $healthBeforePaladin, стало ${paladin.health}"
        }
        assertTrue(knight.health > healthBeforeKnight) {
            "Рыцарь должен вылечиться. Было $healthBeforeKnight, стало ${knight.health}"
        }
        assertTrue(mage.health > healthBeforeMage) {
            "Маг должен вылечиться. Было $healthBeforeMage, стало ${mage.health}"
        }

        // Проверяем конкретные значения с учетом защиты и максимумов
        assertEquals(55, paladin.health) {
            "Паладин должен восстановиться до 55 HP (максимум). " +
                    "Было 45, ожидалось 55, получено ${paladin.health}"
        }
        assertEquals(60, knight.health) {
            "Рыцарь должен восстановиться до 60 HP (максимум). " +
                    "Было 47, ожидалось 60, получено ${knight.health}"
        }
        assertEquals(51, mage.health) {
            "Маг должен восстановиться до 51 HP. " +
                    "Было 36, ожидалось 51, получено ${mage.health}"
        }

        // Проверяем, что здоровье не превышает максимум
        assertTrue(paladin.health <= 55) {
            "Здоровье Паладина не должно превышать максимум (55). Получено ${paladin.health}"
        }
        assertTrue(knight.health <= 60) {
            "Здоровье Рыцаря не должно превышать максимум (60). Получено ${knight.health}"
        }
        assertTrue(mage.health <= 55) {
            "Здоровье Мага не должно превышать максимум (55). Получено ${mage.health}"
        }
    }

    @Test
    fun testUltimateNecromancer() {
        val necromancer = Necromancer("Мордред")
        val target = Knight("Артур")
        val action = UltimateAction()

        // Наносим урон Некроманту, чтобы он мог себя лечить
        necromancer.takeDamage(20)
        val initialHealth = target.health
        val initialNecromancerHealth = necromancer.health

        action.execute(necromancer, target, false, null, null)

        assertTrue(target.health < initialHealth) {
            "Сверхспособность Некроманта должна наносить урон цели. Было $initialHealth, стало ${target.health}"
        }

        assertEquals(initialNecromancerHealth + 10, necromancer.health) {
            "Сверхспособность Некроманта должна восстанавливать 10 HP себе. " +
                    "Было $initialNecromancerHealth, ожидалось ${initialNecromancerHealth + 10}, получено ${necromancer.health}"
        }

        assertTrue(necromancer.health <= 60) {
            "Здоровье Некроманта не должно превышать максимум (60). Получено ${necromancer.health}"
        }
    }

    @Test
    fun testGameEngine() {
        val player1 = Player("Игрок1")
        player1.heroes.add(Knight("Артур"))

        val player2 = Player("Игрок2")
        player2.heroes.add(Mage("Мерлин"))

        val engine = GameEngine()
        engine.startGame(player1, player2)

        assertFalse(engine.isGameOver()) {
            "Сразу после старта игра не должна быть завершена, но isGameOver = true"
        }
        assertEquals(1, engine.getCurrentState().turn) {
            "После старта номер хода должен быть 1, получено ${engine.getCurrentState().turn}"
        }

        val action = AttackAction()
        engine.processTurn(player1, action)

        assertEquals(2, engine.getCurrentState().turn) {
            "После выполнения хода номер хода должен увеличиться до 2, получено ${engine.getCurrentState().turn}"
        }

        // Проверка завершения игры, когда у одного игрока не осталось героев
        player2.heroes.clear()
        engine.processTurn(player1, action)

        assertTrue(engine.isGameOver()) {
            "Когда у игрока нет живых героев, игра должна завершиться, но isGameOver = false"
        }
        assertEquals("Игрок1", engine.getCurrentState().winner) {
            "Победителем должен быть Игрок1, получено ${engine.getCurrentState().winner}"
        }
    }

    @Test
    fun testGameRecorder() {
        val repository = InMemoryRepository()
        val recorder = GameRecorder(repository)
        recorder.setGameInfo("Тестовый режим", "Игрок1", "Игрок2")

        val event = GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48)
        recorder.recordEvent(event)

        recorder.saveGame(listOf(event), "Игрок1")

        val games = repository.getAllGames()
        assertEquals(1, games.size) {
            "Игра не сохранилась в репозитории. Ожидалось 1 игра, получено ${games.size}"
        }
        assertEquals("Игрок1", games[0].winner) {
            "Победитель должен быть Игрок1, получено ${games[0].winner}"
        }
        assertEquals(1, games[0].events.size) {
            "Должно быть сохранено 1 событие, получено ${games[0].events.size}"
        }
    }

    @Test
    fun testGameReplayer() {
        val repository = InMemoryRepository()
        val ui = TestUI()
        val replayer = GameReplayer(ui, repository)

        val event = GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48)
        repository.saveGame("Тест", "Игрок1", "Игрок2", "Игрок1", listOf(event))

        replayer.replayGame(1)

        assertTrue(ui.messages.isNotEmpty()) {
            "Реплей не вывел ни одного сообщения"
        }
        assertTrue(ui.messages.any { it.contains("Игрок1") }) {
            "Реплей должен содержать имя игрока, но сообщения: ${ui.messages}"
        }
    }

    @Test
    fun testInMemoryRepository() {
        val repository = InMemoryRepository()

        val events = listOf(
            GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48),
            GameEvent(2, "Игрок2", "Маг", "DefendAction", null, null, null, null)
        )

        repository.saveGame("Классический", "Игрок1", "Игрок2", "Игрок1", events)

        val games = repository.getAllGames()
        assertEquals(1, games.size) {
            "В репозитории должна быть 1 игра, получено ${games.size}"
        }

        val game = repository.getGame(1)
        assertNotNull(game) {
            "Игра с ID 1 не найдена в репозитории"
        }
        assertEquals(2, game!!.events.size) {
            "В игре должно быть 2 события, получено ${game.events.size}"
        }

        val stats = repository.getStatistics()
        assertEquals(1, stats.totalGames) {
            "Статистика должна показывать 1 игру, получено ${stats.totalGames}"
        }
        assertEquals(1, stats.players["Игрок1"]?.wins) {
            "У Игрок1 должна быть 1 победа, получено ${stats.players["Игрок1"]?.wins}"
        }
    }

    class CharacterTest(
        type: String,
        health: Int,
        power: Int,
        defence: Int
    ) : Character(type, health, power, defence) {
        override fun getUltimateDescription(): String = "Тестовое описание сверхспособности"
    }

    class TestUI : UserInterface {
        val messages = mutableListOf<String>()

        override fun showMessage(msg: String) {
            messages.add(msg)
        }

        override fun readCommand(): String = ""
        override fun readInt(prompt: String): Int? = null
        override fun readString(prompt: String): String = ""
        override fun showHeroes(heroes: List<Character>) {}
        override fun showBattleStatus(player1: Player, player2: Player, round: Int, currentPlayer: Player) {}
        override fun showGameStatistics(stats: PlayersStatistics) {}
    }
}