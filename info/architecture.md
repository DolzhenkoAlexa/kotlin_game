# Архитектура приложения «Битва Героев»

## 1. Общая концепция
Приложение представляет собой консольную пошаговую игру, где два игрока сражаются друг с другом, используя героев с различными способностями.

**Основные функции:**
- Выбор героев для каждого игрока из доступного пула
- Три режима игры: классический (только защита и атака), с маной (ресурс для сверхспособности) и командный (3 героя в команде и есть сверхспособности)
- В классическом режиме можно выбрать только Knight, Mage, Archer.
- В режиме с маной добавляются Barbarian и Necromancer, а в командном режиме в пуле есть также Paladin
- Пошаговое выполнение действий
- Запись всех действий в историю и возможность вывести ходы
- Статистика игроков (количество игр и побед)

## 2. Архитектурные слои
Приложение построено по **многослойной архитектуре**:

- **Entities** – модели предметной области: `Character`, `Player`, `Knight`, `Mage`, `Archer`, `Barbarian`, `Paladin`, `Necromancer`
- **Actions** – логика действий: `Action`, `AttackAction`, `DefendAction`, `RollDiceAction`, `UltimateAction`
- **Engine** – управление игрой: `GameEngine`, `GameInterface`, `GameState`
- **Gamemodes** – реализации режимов игры: `GameMode`, `ClassicMode`, `ManaMode`, `TeamMode`
- **UI** – работа с пользователем: `UserInterface`, `UserConsole`, `MenuRouter`
- **History** – история и статистика: `GameHistory`, `GameRecorder`, `GameReplayer`, `Repository `, `InMemoryRepository`, `SqlRepository`, `GameSummary`, `GameEvent`, `PlayersStatistics`

## 3. Описание классов

### 3.1. Entities

#### Класс `Character` (abstract)
Базовый класс для всех героев, имеет поля-характеристики: type, health, power, defence и т.д.

**Методы:**
- `takeDamage(damage: Int)` — наносит урон с учётом защиты
- `isAlive(): Boolean` — проверяет, жив ли герой
- `getUltimateDescription(): String` — возвращает описание сверхспособности

#### Класс `Player`
Представляет игрока. Имеет поля name и heroes (список героев).

**Методы:**
- `hasAliveHeroes(): Boolean` — есть ли живые герои
- `getAliveHeroes(): List<Character>` — возвращает список живых героев

### 3.2. Actions

#### Интерфейс `Action`
Определяет контракт для всех действий.

**Методы:**
- `execute(character: Character, target: Character?)` — выполнить действие

#### Реализации `Action`
- `AttackAction` – атака силой героя
- `DefendAction` – защита (+50% к защите)
- `RollDiceAction` – бросок кубика (0 – 6), добавляет ману
- `UltimateAction` – сверхспособность (у каждого героя своя)

### 3.3. Engine

#### Интерфейс `GameInterface`
Движок игры.

**Методы:**
- `startGame(player1: Player, player2: Player)` — запускает игру
- `processTurn(player: Player, action: Action?)` — обрабатывает ход
- `processTurnWithTarget(player, action, actor, target)` — обрабатывает ход с выбором цели
- `getCurrentState(): GameState` — возвращает состояние игры
- `isGameOver(): Boolean` — завершена ли игра
- `getEvents(): List<GameEvent>` — возвращает все события игры

#### Класс `GameEngine`
Реализует `GameInterface`

#### Класс `GameState`
Состояние игры. Имеет поля player1, player2, turn, winner.

### 3.4. Game Modes

#### Интерфейс `GameMode`
Определяет контракт для всех режимов игры.

**Методы:**
- `getAvailableHeroes(): List<Character>` — доступные герои
- `canUseUltimate(): Boolean` — можно ли использовать сверхспособность
- `isTeamMode(): Boolean` — командный ли режим
- `startGame(player1: Player, player2: Player)` — запуск игры

#### Реализации `GameMode`
- `ClassicMode` – классический режим (без маны, 3 героя)
- `ManaMode` – режим с маной (5 героев, ульта за 10 маны)
- `TeamMode` – командный режим (3 героя на игрока, ульта 1 раз за игру)

### 3.5. UI

#### Интерфейс `UserInterface`
Базовый интерфейс для всех видов UI.

**Методы:**
- `showMessage(msg: String)` — показать сообщение
- `readCommand(): String` — прочитать команду
- `readInt(prompt: String): Int?` — прочитать число
- `readString(prompt: String): String` — прочитать строку
- `showHeroes(heroes: List<Character>)` — показать список героев
- `showBattleStatus(player1, player2, round, currentPlayer)` — показать статус боя
- `showGameStatistics(stats: PlayersStatistics)` — показать статистику

#### Класс `UserConsole`
Реализует `UserInterface`. Работа с консолью. Содержит ссылку на `MenuRouter`.

**Методы:**
- `showMainMenu()` — отображает главное меню
- `setRouter(router: MenuRouter)` — внедряет роутер

#### Класс `MenuRouter`
Маршрутизатор – обрабатывает команды из меню. Содержит ссылки на `UserInterface` и `Repository`.

**Методы:**
- `startNewGame()` — начинает новую игру
- `showStatistics()` — показывает статистику
- `replayGame()` — воспроизводит игру

### 3.6. History

#### Интерфейс `GameHistory`
Определяет контракт для работы с историей.

**Методы:**
- `recordEvent(event: GameEvent)` — записать событие
- `saveGame(events: List<GameEvent>, winner: String)` — сохранить игру
- `loadGame(id: Int): List<GameEvent>?` — загрузить игру по ID

#### Класс `GameRecorder`
Реализует `GameHistory`. Записывает события и сохраняет в БД.

#### Класс `GameReplayer`
Воспроизводит сохранённую игру по ID. Содержит ссылки на `UserInterface` и `Repository`.

**Методы:**
- `replayGame(id: Int)` — воспроизвести игру по ID

#### Интерфейс `Database`
Хранилище данных с двумя реализациями

**Методы:**
- `saveGame(mode, p1, p2, winner, events)` — сохранить игру
- `getAllGames(): List<GameSummary>` — получить все игры
- `getGame(id: Int): GameSummary?` — получить игру по ID
- `getStatistics(): PlayersStatistics` — получить статистику

#### Реализации `Database`
- `InMemoryRepository` – хранение в оперативной памяти
- `SqlRepository` – хранение в SQL 

#### Класс `GameEvent`
Запись об одном действии (ходе). Содержит turnNumber, playerName, actorType, actionType, targetType, damage, healthBefore, healthAfter.

#### Класс `GameSummary`
Краткая информация об игре. Содержит id, date, mode, player1, player2, winner, events.

#### Класс `PlayersStatistics`
Статистика игроков. Содержит totalGames и players (Map<String, PlayerStats>).

#### Класс `PlayerStats`
Статистика одного игрока. Содержит gamesPlayed и wins.
