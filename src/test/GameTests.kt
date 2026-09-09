package test

import main.kotlin.actions.*
import main.kotlin.engine.GameEngine
import main.kotlin.engine.GameEvent
import main.kotlin.entities.*
import main.kotlin.history.GameRecorder
import main.kotlin.history.GameReplayer
import main.kotlin.history.repository.InMemoryRepository
import main.kotlin.history.repository.PlayersStatistics
import main.kotlin.ui.UserInterface
import kotlin.math.roundToInt

fun main() {
    println("=== ЗАПУСК ТЕСТОВ ===\n")

    testDamageCalculation()
    testDefendAction()
    testUltimateKnight()
    testUltimateMage()
    testUltimateArcher()
    testUltimateBarbarian()
    testUltimatePaladin()
    testUltimateNecromancer()
    testGameEngine()
    testGameRecorder()
    testGameReplayer()
    testInMemoryRepository()

    println("\n=== ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ ===")
}

fun testDamageCalculation() {
    println("1. Тест расчета урона и защиты:")

    val knight = Knight("Артур")

    // Атака без защиты: 60 - (10 - 7) = 57
    knight.takeDamage(10)
    assert(knight.health == 57) {
        "Ошибка: урон прошел без учета защиты. Ожидалось 57 HP, получено ${knight.health}"
    }

    // Атака с бонусом защиты: защита 7 + бонус 7 = 14, урон 20 - 14 = 6, 57 - 6 = 51
    knight.isDefending = true
    knight.defenceBonus = 7
    knight.takeDamage(20)
    assert(knight.health == 51) {
        "Ошибка: урон был рассчитан без учета бонуса защиты. Ожидалось 51 HP, получено ${knight.health}"
    }

    // Минимальный урон всегда равен 1, даже если урон меньше защиты
    val weakAttack = CharacterTest("Слабак", 10, 1, 10)
    weakAttack.takeDamage(9)
    assert(weakAttack.health == 9) {
        "Ошибка: минимальный урон должен быть 1. Ожидалось 9 HP, получено ${weakAttack.health}"
    }

    // Проверка, что после получения урона защита снимается
    knight.isDefending = true
    knight.defenceBonus = 10
    knight.takeDamage(5)
    assert(!knight.isDefending) {
        "Ошибка: после получения урона защита должна быть снята, но isDefending осталось true"
    }
    assert(knight.defenceBonus == 0) {
        "Ошибка: после получения урона бонус защиты должен быть сброшен, но defenceBonus равен ${knight.defenceBonus}"
    }

    println("Success: Расчет урона и защиты работает корректно")
}

fun testDefendAction() {
    println("2. Тест действия защититься:")

    val knight = Knight("Артур")
    val defendAction = DefendAction()

    // Обычный режим: защищается только один герой
    defendAction.execute(knight, null, false, null, null)
    assert(knight.isDefending) {
        "Ошибка: после выполнения DefendAction защита не активирована для героя"
    }
    assert(knight.defenceBonus == (knight.defence / 2.0).roundToInt()) {
        "Ошибка: бонус защиты рассчитан неверно. Ожидалось ${(knight.defence / 2.0).roundToInt()}, получено ${knight.defenceBonus}"
    }

    // Командный режим: защищаться должны все живые герои в команде
    val player = Player("Командир")
    player.heroes.add(Knight("Артур"))
    player.heroes.add(Mage("Мерлин"))

    defendAction.execute(knight, null, true, player, null)
    player.heroes.forEach { hero ->
        assert(hero.isDefending) {
            "Ошибка: в командном режиме герой ${hero.type} должен защищаться, но isDefending = false"
        }
    }

    println("Success: Действие защититься работает корректно")
}

fun testUltimateKnight() {
    println("3. Тест сверхспособности Рыцаря:")

    val knight = Knight("Артур")
    val target = Mage("Мерлин")
    val initialHealth = target.health
    val action = UltimateAction()

    action.execute(knight, target, false, null, null)

    assert(target.health == initialHealth - 10) {
        "Ошибка: сверхспособность Рыцаря должна наносить 10 урона цели. Ожидалось ${initialHealth - 10}, получено ${target.health}"
    }
    assert(knight.isDefending) {
        "Ошибка: сверхспособность Рыцаря должна активировать защиту, но isDefending = false"
    }
    assert(knight.defenceBonus == knight.defence * 3) {
        "Ошибка: сверхспособность Рыцаря должна давать бонус защиты +200% (в 3 раза). Ожидалось ${knight.defence * 3}, получено ${knight.defenceBonus}"
    }

    println("Success: Сверхспособность Рыцаря работает корректно")
}

fun testUltimateMage() {
    println("4. Тест сверхспособности Мага:")

    val mage = Mage("Мерлин")
    val target = Knight("Артур")
    val initialHealth = target.health
    val action = UltimateAction()

    // Обычный режим: урон по одной цели
    action.execute(mage, target, false, null, null)

    assert(target.health == initialHealth - 10) {
        "Ошибка: сверхспособность Мага должна наносить 10 урона цели. Ожидалось ${initialHealth - 10}, получено ${target.health}"
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

    assert(player2.heroes[0].health == healthBefore1 - 10) {
        "Ошибка: в командном режиме сверхспособность Мага должна наносить 10 урона первому врагу. Ожидалось ${healthBefore1 - 10}, получено ${player2.heroes[0].health}"
    }
    assert(player2.heroes[1].health == healthBefore2 - 10) {
        "Ошибка: в командном режиме сверхспособность Мага должна наносить 10 урона второму врагу. Ожидалось ${healthBefore2 - 10}, получено ${player2.heroes[1].health}"
    }

    println("Success: Сверхспособность Мага работает корректно")
}

fun testUltimateArcher() {
    println("5. Тест сверхспособности Лучника:")

    val archer = Archer("Леголас")
    val target = Mage("Мерлин")
    val initialHealth = target.health
    val action = UltimateAction()

    action.execute(archer, target, false, null, null)

    // Лучник не игнорирует защиту, поэтому урон = 35 - защита цели
    val expectedDamage = 35 - target.defence
    assert(target.health == initialHealth - expectedDamage) {
        "Ошибка: сверхспособность Лучника должна наносить ${expectedDamage} урона с учетом защиты цели (защита = ${target.defence}). " +
                "Ожидалось ${initialHealth - expectedDamage}, получено ${target.health}"
    }

    println("Success: Сверхспособность Лучника работает корректно")
}

fun testUltimateBarbarian() {
    println("6. Тест сверхспособности Варвара:")

    val barbarian = Barbarian("Конан")
    val target = Mage("Мерлин")
    val initialHealth = target.health
    val initialBarbarianHealth = barbarian.health
    val action = UltimateAction()

    action.execute(barbarian, target, false, null, null)

    assert(target.health == initialHealth - 35) {
        "Ошибка: сверхспособность Варвара должна наносить 35 урона цели. Ожидалось ${initialHealth - 35}, получено ${target.health}"
    }
    assert(barbarian.health == initialBarbarianHealth - 20) {
        "Ошибка: сверхспособность Варвара должна наносить 20 урона себе. Ожидалось ${initialBarbarianHealth - 20}, получено ${barbarian.health}"
    }

    // Проверка, что сверхспособность недоступна при HP <= 20
    barbarian.health = 20
    val healthBeforeInvalid = barbarian.health
    action.execute(barbarian, target, false, null, null)
    assert(barbarian.health == healthBeforeInvalid) {
        "Ошибка: сверхспособность Варвара не должна срабатывать при HP <= 20. Ожидалось $healthBeforeInvalid, получено ${barbarian.health}"
    }

    println("Success: Сверхспособность Варвара работает корректно")
}

fun testUltimatePaladin() {
    println("7. Тест сверхспособности Паладина:")

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
    assert(paladin.health > healthBeforePaladin) {
        "Ошибка: Паладин должен вылечиться. Было $healthBeforePaladin, стало ${paladin.health}"
    }
    assert(knight.health > healthBeforeKnight) {
        "Ошибка: Рыцарь должен вылечиться. Было $healthBeforeKnight, стало ${knight.health}"
    }
    assert(mage.health > healthBeforeMage) {
        "Ошибка: Маг должен вылечиться. Было $healthBeforeMage, стало ${mage.health}"
    }

    // Проверяем, что здоровье не превышает максимум
    assert(paladin.health <= 55) {
        "Ошибка: здоровье Паладина не должно превышать максимум (55). Получено ${paladin.health}"
    }
    assert(knight.health <= 60) {
        "Ошибка: здоровье Рыцаря не должно превышать максимум (60). Получено ${knight.health}"
    }
    assert(mage.health <= 55) {
        "Ошибка: здоровье Мага не должно превышать максимум (55). Получено ${mage.health}"
    }

    // Проверяем конкретные значения с учетом защиты и максимумов
    // Паладин: 45 + 15 = 60, ограничение 55, должно быть 55
    assert(paladin.health == 55) {
        "Ошибка: Паладин должен восстановиться до 55 HP (максимум). " +
                "Было 45, ожидалось 55, получено ${paladin.health}"
    }
    // Рыцарь: 47 + 15 = 62, ограничение 60, должно быть 60
    assert(knight.health == 60) {
        "Ошибка: Рыцарь должен восстановиться до 60 HP (максимум). " +
                "Было 47, ожидалось 60, получено ${knight.health}"
    }
    // Маг: 36 + 15 = 51, ограничение 55, должно быть 51
    assert(mage.health == 51) {
        "Ошибка: Маг должен восстановиться до 51 HP. " +
                "Было 36, ожидалось 51, получено ${mage.health}"
    }

    println("Success: Сверхспособность Паладина работает корректно")
}

fun testUltimateNecromancer() {
    println("8. Тест сверхспособности Некроманта:")

    val necromancer = Necromancer("Мордред")
    val target = Knight("Артур")
    val action = UltimateAction()

    // Наносим урон Некроманту, чтобы он мог себя лечить
    necromancer.takeDamage(20)
    val initialHealth = target.health
    val initialNecromancerHealth = necromancer.health

    action.execute(necromancer, target, false, null, null)

    assert(target.health < initialHealth) {
        "Ошибка: сверхспособность Некроманта должна наносить урон цели. Было $initialHealth, стало ${target.health}"
    }

    // Проверяем, что Некромант вылечился на 10 HP
    assert(necromancer.health == initialNecromancerHealth + 10) {
        "Ошибка: сверхспособность Некроманта должна восстанавливать 10 HP себе. " +
                "Было $initialNecromancerHealth, ожидалось ${initialNecromancerHealth + 10}, получено ${necromancer.health}"
    }

    // Проверяем, что здоровье Некроманта не превышает максимум (60)
    assert(necromancer.health <= 60) {
        "Ошибка: здоровье Некроманта не должно превышать максимум (60). Получено ${necromancer.health}"
    }

    println("Success: Сверхспособность Некроманта работает корректно")
}

fun testGameEngine() {
    println("9. Тест GameEngine:")

    val player1 = Player("Игрок1")
    player1.heroes.add(Knight("Артур"))

    val player2 = Player("Игрок2")
    player2.heroes.add(Mage("Мерлин"))

    val engine = GameEngine()
    engine.startGame(player1, player2)

    assert(!engine.isGameOver()) {
        "Ошибка: сразу после старта игра не должна быть завершена, но isGameOver = true"
    }
    assert(engine.getCurrentState().turn == 1) {
        "Ошибка: после старта номер хода должен быть 1, получено ${engine.getCurrentState().turn}"
    }

    // Проверка, что ход увеличивается после выполнения действия
    val action = AttackAction()
    engine.processTurn(player1, action)

    assert(engine.getCurrentState().turn == 2) {
        "Ошибка: после выполнения хода номер хода должен увеличиться до 2, получено ${engine.getCurrentState().turn}"
    }

    // Проверка завершения игры, когда у одного игрока не осталось героев
    player2.heroes.clear()
    engine.processTurn(player1, action)

    assert(engine.isGameOver()) {
        "Ошибка: когда у игрока нет живых героев, игра должна завершиться, но isGameOver = false"
    }
    assert(engine.getCurrentState().winner == "Игрок1") {
        "Ошибка: победителем должен быть Игрок1, получено ${engine.getCurrentState().winner}"
    }

    println("Success: GameEngine работает корректно")
}

fun testGameRecorder() {
    println("10. Тест GameRecorder:")

    val repository = InMemoryRepository()
    val recorder = GameRecorder(repository)
    recorder.setGameInfo("Тестовый режим", "Игрок1", "Игрок2")

    val event = GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48)
    recorder.recordEvent(event)

    recorder.saveGame(listOf(event), "Игрок1")

    val games = repository.getAllGames()
    assert(games.size == 1) {
        "Ошибка: игра не сохранилась в репозитории. Ожидалось 1 игра, получено ${games.size}"
    }
    assert(games[0].winner == "Игрок1") {
        "Ошибка: победитель должен быть Игрок1, получено ${games[0].winner}"
    }
    assert(games[0].events.size == 1) {
        "Ошибка: должно быть сохранено 1 событие, получено ${games[0].events.size}"
    }

    println("Success: GameRecorder работает корректно")
}

fun testGameReplayer() {
    println("11. Тест GameReplayer:")

    val repository = InMemoryRepository()
    val ui = TestUI()
    val replayer = GameReplayer(ui, repository)

    // Сохраняем тестовую игру в репозиторий
    val event = GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48)
    repository.saveGame("Тест", "Игрок1", "Игрок2", "Игрок1", listOf(event))

    // Воспроизводим игру по ID
    replayer.replayGame(1)

    assert(ui.messages.isNotEmpty()) {
        "Ошибка: реплей не вывел ни одного сообщения"
    }
    assert(ui.messages.any { it.contains("Игрок1") }) {
        "Ошибка: реплей должен содержать имя игрока, но сообщения: ${ui.messages}"
    }

    println("Success: GameReplayer работает корректно")
}

fun testInMemoryRepository() {
    println("15. Тест InMemoryRepository:")

    val repository = InMemoryRepository()

    val events = listOf(
        GameEvent(1, "Игрок1", "Рыцарь", "AttackAction", "Маг", 7, 55, 48),
        GameEvent(2, "Игрок2", "Маг", "DefendAction", null, null, null, null)
    )

    repository.saveGame("Классический", "Игрок1", "Игрок2", "Игрок1", events)

    val games = repository.getAllGames()
    assert(games.size == 1) {
        "Ошибка: в репозитории должна быть 1 игра, получено ${games.size}"
    }

    val game = repository.getGame(1)
    assert(game != null) {
        "Ошибка: игра с ID 1 не найдена в репозитории"
    }
    assert(game!!.events.size == 2) {
        "Ошибка: в игре должно быть 2 события, получено ${game.events.size}"
    }

    val stats = repository.getStatistics()
    assert(stats.totalGames == 1) {
        "Ошибка: статистика должна показывать 1 игру, получено ${stats.totalGames}"
    }
    assert(stats.players["Игрок1"]?.wins == 1) {
        "Ошибка: у Игрок1 должна быть 1 победа, получено ${stats.players["Игрок1"]?.wins}"
    }

    println("Success: InMemoryRepository работает корректно")
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