# Banner & Blade — UML Design

| | |
|---|---|
| **Status** | Draft 2, written before any code; owners updated |
| **Date** | 18 November 2026 |
| **Source** | [Game Design Document](GDD.md), draft 3 |

The diagrams are written in Mermaid, which GitHub renders directly on this page. They describe the intended design. They must be updated to match the real code at the feature freeze (26 November).

A class that appears as an empty box in a diagram is detailed in another section.

**Contents**

1. [Package overview](#1-package-overview)
2. [Units, board and targeting](#2-units-board-and-targeting)
3. [Bonuses: modifiers, status effects, banners and items](#3-bonuses-modifiers-status-effects-banners-and-items)
4. [Combat and events](#4-combat-and-events)
5. [Match, warlords, draft and pairing](#5-match-warlords-draft-and-pairing)
6. [User interface](#6-user-interface)
7. [Match phases (state diagram)](#7-match-phases-state-diagram)
8. [One unit's turn in a tick (sequence diagram)](#8-one-units-turn-in-a-tick-sequence-diagram)
9. [Design patterns summary](#9-design-patterns-summary)

---

## 1. Package overview

The one rule that matters most: **nothing under `model` imports JavaFX.** The model can therefore run in unit tests and resolve off-screen fights instantly. The interface learns what happened through listener interfaces that the model defines.

```mermaid
flowchart TD
    subgraph ui["User interface (JavaFX)"]
        view["view"]
        controller["controller"]
    end
    subgraph model["model (no JavaFX)"]
        match["match"]
        ai["ai"]
        draft["draft"]
        combat["combat"]
        unit["unit"]
        item["item"]
        board["board"]
    end
    view --> controller
    controller --> match
    view -. "listens to events" .-> match
    view -. "listens to events" .-> combat
    match --> draft
    match --> ai
    match --> combat
    ai --> draft
    draft --> unit
    draft --> item
    combat --> unit
    combat --> board
    unit --> item
    unit --> board
```

| Package | Contents | Foundation owner | Shared content |
|---|---|---|---|
| `model.board` | Grid, positions, pathfinding | Jonathan | None |
| `model.unit` | Unit hierarchy, stats, abilities, targeting, status effects, banners | Jonathan | The ten unit classes, status effects, targeting rules |
| `model.combat` | The fight loop, teams, results, combat events | Jonathan | None |
| `model.item` | Item hierarchy and item factory | Jonathan | The six effect items |
| `model.draft` | Draft offers and cards | Stephan | None |
| `model.match` | Match loop, phases, warlords, pairing, rules | Stephan (pairing: Jonathan) | None |
| `model.ai` | Rival strategies | Stephan | One strategy per member |
| `controller`, `view` | JavaFX screens and input handling | Stephan | Views split between all three |

Who writes each shared class is listed in GDD section 11.1.

---

## 2. Units, board and targeting

### 2.1 The unit hierarchy

`Unit.act()` is the fixed sequence every unit follows each tick (GDD section 4.3). It is `final`: this is the Template Method. Each subclass supplies only `castAbility()`. The two banner units share an intermediate class that owns the banner.

```mermaid
classDiagram
    direction LR

    class Unit {
        <<abstract>>
        -UnitType type
        -Stats baseStats
        -int health
        -int mana
        -Position position
        -Team team
        -TargetingStrategy targeting
        -List~Item~ items
        -List~StatusEffect~ effects
        +act(CombatContext ctx)
        +getStat(StatType stat) int
        +takeDamage(int rawDamage, Unit source, CombatContext ctx)
        +heal(int amount)
        +equip(Item item)
        +unequip(Item item)
        +addEffect(StatusEffect effect)
        +prepareForCombat(Team team)
        +isAlive() boolean
        #castAbility(CombatContext ctx)*
    }

    class BannerUnit {
        <<abstract>>
        -Banner banner
        +getBanner() Banner
    }

    Unit <|-- Shieldbearer
    Unit <|-- Swordsman
    Unit <|-- Pikeman
    Unit <|-- Archer
    Unit <|-- FireMage
    Unit <|-- Cleric
    Unit <|-- Rogue
    Unit <|-- WarDrummer
    Unit <|-- BannerUnit
    BannerUnit <|-- StandardBearer
    BannerUnit <|-- BannerWarden
```

### 2.2 What a unit is built from

A unit is created by the factory from its type, carries its base stats, and delegates target selection to a strategy object. During a fight it sees the world only through the `CombatContext` interface.

```mermaid
classDiagram
    direction TB

    class Unit {
        <<abstract>>
    }

    class Stats {
        <<record>>
        +int maxHealth
        +int attack
        +int attackInterval
        +int range
        +int armour
        +int moveInterval
        +int maxMana
    }

    class UnitType {
        <<enumeration>>
        SHIELDBEARER
        SWORDSMAN
        PIKEMAN
        ARCHER
        FIRE_MAGE
        CLERIC
        ROGUE
        STANDARD_BEARER
        BANNER_WARDEN
        WAR_DRUMMER
    }

    class UnitFactory {
        +create(UnitType type) Unit
    }

    class TargetingStrategy {
        <<interface>>
        +selectTarget(Unit self, CombatContext ctx) Optional~Unit~
    }
    class NearestEnemyTargeting
    class WeakestEnemyTargeting
    class WoundedAllyTargeting

    class CombatContext {
        <<interface>>
        +board() Board
        +alliesOf(Unit unit) List~Unit~
        +enemiesOf(Unit unit) List~Unit~
        +pathfinder() Pathfinder
        +random() Random
        +currentTick() int
        +publish(CombatEvent event)
    }

    UnitFactory ..> Unit : creates
    UnitFactory ..> UnitType
    Unit *-- Stats : base stats
    Unit --> TargetingStrategy : chooses targets with
    Unit ..> CombatContext : acts through
    TargetingStrategy <|.. NearestEnemyTargeting
    TargetingStrategy <|.. WeakestEnemyTargeting
    TargetingStrategy <|.. WoundedAllyTargeting
```

### 2.3 Board and pathfinding

`Position.distanceTo()` counts in 8 directions and is used for range. `Position.orthogonalNeighbours()` returns the 4 cells a unit may step to. Keeping both on `Position` puts the game's key spatial rule (GDD section 4.2) in one place.

```mermaid
classDiagram
    direction TB

    class Board {
        +int ROWS$
        +int COLUMNS$
        +unitAt(Position position) Optional~Unit~
        +isFree(Position position) boolean
        +place(Unit unit, Position position)
        +move(Unit unit, Position destination)
        +remove(Unit unit)
    }

    class Position {
        <<record>>
        +int row
        +int column
        +distanceTo(Position other) int
        +orthogonalNeighbours() List~Position~
        +mirrored() Position
    }

    class Pathfinder {
        <<interface>>
        +nextStep(Board board, Position from, Position goal, int range) Optional~Position~
    }
    class BfsPathfinder

    class Unit {
        <<abstract>>
    }

    Board "1" o-- "0..*" Unit : holds
    Unit --> Position : stands on
    Board ..> Position
    Pathfinder ..> Board : reads
    Pathfinder <|.. BfsPathfinder
```

---

## 3. Bonuses: modifiers, status effects, banners and items

Everything that changes a stat — an item, a banner, a temporary buff — is a `ModifierSource`. `Unit.getStat()` starts from the base value and applies every modifier from the unit's items, its status effects and its team's banners. Because each source is counted separately, "everything stacks" (GDD section 6.1) needs no special code. Stretch-goal traits would be one more `ModifierSource`.

### 3.1 Modifiers, status effects and banners

`Banner.stackable` is false for the banners carried by banner units (only one per type counts for a team) and true for the Tattered Pennant.

```mermaid
classDiagram
    direction TB

    class ModifierSource {
        <<interface>>
        +modifiers() List~StatModifier~
    }

    class StatModifier {
        <<record>>
        +StatType stat
        +ModifierKind kind
        +int amount
    }

    class StatType {
        <<enumeration>>
        MAX_HEALTH
        ATTACK
        ATTACK_SPEED
        RANGE
        ARMOUR
        STARTING_MANA
    }

    class ModifierKind {
        <<enumeration>>
        FLAT
        PERCENT
    }

    class StatusEffect {
        <<abstract>>
        #int remainingTicks
        +onTick(Unit owner, CombatContext ctx)
        +isExpired() boolean
    }
    class Shield {
        -int amount
        +absorb(int damage) int
    }
    class Burn {
        -int damagePerPulse
        -int pulseInterval
    }
    class Stun
    class StatBuff {
        -List~StatModifier~ bonuses
    }

    class Banner {
        -BannerType type
        -List~StatModifier~ bonuses
        -boolean stackable
        +amplify(int factor, int ticks)
    }

    class BannerType {
        <<enumeration>>
        WAR
        AEGIS
        PENNANT
    }

    class Item {
        <<abstract>>
    }

    ModifierSource <|.. StatusEffect
    ModifierSource <|.. Banner
    ModifierSource <|.. Item
    ModifierSource ..> StatModifier
    StatModifier --> StatType
    StatModifier --> ModifierKind
    StatusEffect <|-- Shield
    StatusEffect <|-- Burn
    StatusEffect <|-- Stun
    StatusEffect <|-- StatBuff
    Banner --> BannerType
```

### 3.2 Items

Items that do more than change stats override the hook methods on `Item`, which do nothing by default. The five stat items are all instances of `StatItem` with different bonuses.

```mermaid
classDiagram
    direction LR

    class Item {
        <<abstract>>
        -String name
        -String description
        +modifiers() List~StatModifier~
        +onCombatStart(Unit holder, CombatContext ctx)
        +onAttackHit(Unit holder, Unit target, int damage, CombatContext ctx)
        +onHitTaken(Unit holder, Unit attacker, CombatContext ctx)
        +onFatalDamage(Unit holder) boolean
        +carriedBanner() Optional~Banner~
    }
    class StatItem {
        -List~StatModifier~ bonuses
    }
    class PhoenixFeather {
        -boolean used
    }
    class TatteredPennant {
        -Banner lesserBanner
    }

    Item <|-- StatItem
    Item <|-- VampiricBlade
    Item <|-- EmberBrand
    Item <|-- BulwarkCrest
    Item <|-- SpikedPauldrons
    Item <|-- PhoenixFeather
    Item <|-- TatteredPennant
```

### 3.3 Who holds what

```mermaid
classDiagram
    direction TB

    class Unit {
        <<abstract>>
        +getStat(StatType stat) int
    }
    class BannerUnit {
        <<abstract>>
    }
    class Item {
        <<abstract>>
    }
    class StatusEffect {
        <<abstract>>
    }
    class Banner
    class TatteredPennant

    class ItemType {
        <<enumeration>>
        IRON_SWORD
        WHETSTONE
        CHAINMAIL
        LEATHER_JERKIN
        SIGIL_OF_FOCUS
        VAMPIRIC_BLADE
        EMBER_BRAND
        BULWARK_CREST
        SPIKED_PAULDRONS
        PHOENIX_FEATHER
        TATTERED_PENNANT
    }

    class ItemFactory {
        +create(ItemType type) Item
    }

    Unit <|-- BannerUnit
    Item <|-- TatteredPennant
    Unit "1" o-- "0..*" Item : holds
    Unit "1" *-- "0..*" StatusEffect : affected by
    BannerUnit *-- Banner : carries
    TatteredPennant *-- Banner : grants
    ItemFactory ..> Item : creates
    ItemFactory ..> ItemType
```

---

## 4. Combat and events

A `Combat` is one fight. The on-screen fight calls `tick()` once per animation step; off-screen fights call `runToEnd()`. Both use the same code, and the same seed always gives the same result. The away team's planned positions are mirrored onto the top half of the board.

```mermaid
classDiagram
    direction TB

    class CombatContext {
        <<interface>>
    }

    class Combat {
        -Board board
        -Team home
        -Team away
        -int tick
        -int tickLimit
        -Random random
        -List~CombatListener~ listeners
        +Combat(Warband home, Warband away, MatchRules rules, long seed)
        +tick()
        +runToEnd() CombatResult
        +isOver() boolean
        +result() CombatResult
        +addListener(CombatListener listener)
    }

    class Team {
        -List~Unit~ units
        +livingUnits() List~Unit~
        +activeBanners() List~Banner~
        +teamModifiers() List~StatModifier~
        +isDefeated() boolean
    }

    class CombatResult {
        <<record>>
        +Outcome outcome
        +int survivingUnits
        +int ticks
    }

    class Outcome {
        <<enumeration>>
        HOME_WIN
        AWAY_WIN
        DRAW
    }

    class CombatListener {
        <<interface>>
        +onEvent(CombatEvent event)
    }

    class CombatEvent {
        <<interface>>
        +tick() int
    }

    class Board
    class Unit {
        <<abstract>>
    }

    CombatContext <|.. Combat
    Combat *-- Board
    Combat "1" *-- "2" Team
    Combat --> CombatResult : produces
    Combat "1" o-- "0..*" CombatListener : notifies
    CombatListener ..> CombatEvent
    CombatResult --> Outcome
    Team "1" o-- "1..*" Unit
```

`CombatEvent` is implemented by small records, one per thing that can happen: `UnitMoved`, `UnitAttacked`, `UnitDamaged`, `UnitHealed`, `AbilityCast`, `UnitDied`, `BannerFell`, `CombatEnded`.

---

## 5. Match, warlords, draft and pairing

### 5.1 The match and its phases

`Match` holds the current `MatchPhase` and asks it what to do and what comes next. All balancing numbers live in `MatchRules`, so tests and balancing touch one class.

```mermaid
classDiagram
    direction TB

    class Match {
        -int round
        -List~Warlord~ warlords
        -MatchPhase phase
        -MatchRules rules
        -Random random
        -List~MatchListener~ listeners
        +start()
        +playerReady()
        +advance()
        +setPhase(MatchPhase phase)
        +livingWarlords() List~Warlord~
        +ghost() Optional~AiWarlord~
        +player() Warlord
        +addListener(MatchListener listener)
    }

    class MatchPhase {
        <<interface>>
        +enter(Match match)
        +next(Match match) MatchPhase
    }
    class DraftPhase
    class PlanningPhase
    class CombatPhase
    class ResolutionPhase
    class GameOverPhase

    class MatchRules {
        +startingHealth() int
        +teamCap(int round) int
        +benchSize() int
        +stageDamage(int round) int
        +lossDamage(int round, int survivingEnemies) int
        +tickLimit() int
        +unitDrafts(int round) int
        +itemDrafts(int round) int
    }

    class MatchListener {
        <<interface>>
        +onPhaseChanged(MatchPhase phase)
        +onRoundResolved(List~RoundResult~ results)
        +onWarlordEliminated(Warlord warlord)
        +onMatchEnded(boolean playerWon)
    }

    class Warlord

    Match "1" *-- "4" Warlord
    Match --> MatchPhase : current
    Match --> MatchRules
    Match "1" o-- "0..*" MatchListener : notifies
    MatchPhase <|.. DraftPhase
    MatchPhase <|.. PlanningPhase
    MatchPhase <|.. CombatPhase
    MatchPhase <|.. ResolutionPhase
    MatchPhase <|.. GameOverPhase
```

### 5.2 Warlords, warbands and AI rivals

The human player and the rivals are all `Warlord`s with a `Warband`. A rival adds a `RivalStrategy` that makes its choices. When a rival is eliminated its strategy keeps running each round, which is exactly what a ghost army is (GDD section 2.3): no separate ghost class is needed.

```mermaid
classDiagram
    direction TB

    class Warlord {
        -String name
        -int health
        -Warband warband
        +takeDamage(int amount)
        +isEliminated() boolean
        +pick(DraftCard card)
    }

    class AiWarlord {
        -RivalStrategy strategy
        +playDraft(DraftOffer offer)
        +planRound(int teamCap)
    }

    class RivalStrategy {
        <<interface>>
        +chooseCard(DraftOffer offer, Warband warband) Optional~DraftCard~
        +arrange(Warband warband, int teamCap)
        +equip(Warband warband)
    }
    class VanguardStrategy
    class BulwarkStrategy
    class GamblerStrategy

    class Warband {
        -List~Unit~ fielded
        -List~Unit~ bench
        -List~Item~ stash
        +place(Unit unit, Position position, int teamCap) boolean
        +toBench(Unit unit) boolean
        +dismiss(Unit unit)
        +equip(Item item, Unit unit)
        +unequip(Item item, Unit unit)
        +fieldedUnits() List~Unit~
    }

    Warlord <|-- AiWarlord
    Warlord "1" *-- "1" Warband
    AiWarlord --> RivalStrategy : decides with
    RivalStrategy <|.. VanguardStrategy
    RivalStrategy <|.. BulwarkStrategy
    RivalStrategy <|.. GamblerStrategy
    RivalStrategy ..> Warband : arranges
```

### 5.3 Draft and pairing

`PairingService` enforces the rule that no warlord meets the same opponent twice in a row unless it is the only one left, and brings in the ghost when three warlords are alive.

```mermaid
classDiagram
    direction TB

    class DraftService {
        +offersFor(int round, Random random) List~DraftOffer~
    }

    class DraftOffer {
        -List~DraftCard~ cards
        +cards() List~DraftCard~
    }

    class DraftCard {
        <<interface>>
        +name() String
        +description() String
        +applyTo(Warband warband)
    }
    class UnitCard
    class ItemCard

    class PairingService {
        -List~Pairing~ previousRound
        -Random random
        +pair(List~Warlord~ living, Optional~AiWarlord~ ghost) List~Pairing~
    }

    class Pairing {
        <<record>>
        +Warlord home
        +Warlord away
        +boolean awayIsGhost
    }

    class RoundResult {
        <<record>>
        +Pairing pairing
        +CombatResult result
        +int damageDealt
    }

    class Match
    class Combat
    class UnitFactory
    class ItemFactory

    Match ..> DraftService : asks for offers
    Match ..> PairingService : asks for pairs
    Match ..> Combat : runs one per pairing
    DraftService ..> DraftOffer : builds
    DraftService ..> UnitFactory
    DraftService ..> ItemFactory
    DraftOffer "1" *-- "3" DraftCard
    DraftCard <|.. UnitCard
    DraftCard <|.. ItemCard
    PairingService ..> Pairing : produces
    RoundResult --> Pairing
```

---

## 6. User interface

This part is indicative; Stephan, who owns the app shell, will shape it, and each view has its own author (GDD section 11.1). What is fixed is the direction of the arrows: the interface depends on the model, and hears about changes by implementing the model's listener interfaces.

```mermaid
classDiagram
    direction TB

    class GameApp {
        +start(Stage stage)
    }
    class MainMenuScreen
    class MatchScreen {
        +onPhaseChanged(MatchPhase phase)
        +onRoundResolved(List~RoundResult~ results)
        +onWarlordEliminated(Warlord warlord)
        +onMatchEnded(boolean playerWon)
    }
    class EndScreen

    class MatchController {
        -Match match
        +pickCard(DraftCard card)
        +skipDraft()
        +moveUnit(Unit unit, Position position)
        +benchUnit(Unit unit)
        +dismissUnit(Unit unit)
        +equipItem(Item item, Unit unit)
        +unequipItem(Item item, Unit unit)
        +ready()
    }

    class BoardView
    class BenchView
    class StashView
    class DraftOverlay
    class UnitSheetView
    class WarlordPanel
    class CombatAnimator {
        +onEvent(CombatEvent event)
    }

    class MatchListener {
        <<interface>>
    }
    class CombatListener {
        <<interface>>
    }
    class Match

    GameApp --> MainMenuScreen
    GameApp --> MatchScreen
    GameApp --> EndScreen
    MatchScreen --> MatchController : sends input to
    MatchController --> Match : calls
    MatchScreen *-- BoardView
    MatchScreen *-- BenchView
    MatchScreen *-- StashView
    MatchScreen *-- DraftOverlay
    MatchScreen *-- UnitSheetView
    MatchScreen *-- WarlordPanel
    MatchScreen *-- CombatAnimator
    MatchListener <|.. MatchScreen
    CombatListener <|.. CombatAnimator
    CombatAnimator --> BoardView : animates
```

---

## 7. Match phases (state diagram)

Each state is one `MatchPhase` class from section 5.1.

```mermaid
stateDiagram-v2
    [*] --> Draft : New Match
    Draft --> Planning : all drafts for this round done
    Planning --> Combat : player presses Ready
    Combat --> Resolution : every fight has ended
    Resolution --> Draft : player alive and a rival alive
    Resolution --> GameOver : player eliminated, or all rivals eliminated
    GameOver --> [*] : back to menu

    note right of Draft
        Round 1: two unit drafts.
        Later rounds: one unit draft,
        plus one item draft on even rounds.
        Rivals and the ghost draft too.
    end note

    note right of Combat
        Pairs are drawn, the player's fight is shown,
        other fights are resolved instantly.
    end note

    note right of Resolution
        Losers take damage, eliminations are checked,
        a newly eliminated rival becomes the ghost.
    end note
```

---

## 8. One unit's turn in a tick (sequence diagram)

`Combat.tick()` calls `act()` on every living unit in a fixed order. This is what happens inside one call (GDD section 4.3).

```mermaid
sequenceDiagram
    participant C as Combat
    participant U as Unit
    participant T as TargetingStrategy
    participant P as Pathfinder
    participant E as Target unit
    participant L as CombatListener

    C->>U: act(ctx)
    U->>U: update status effects, stop here if stunned
    opt no living target
        U->>T: selectTarget(self, ctx)
        T-->>U: target
    end
    alt mana is full
        U->>U: castAbility(ctx)
        U->>C: publish(AbilityCast)
    else target in range and attack ready
        U->>E: takeDamage(attack, self, ctx)
        E->>E: shield absorbs, armour reduces, item hooks run
        E->>C: publish(UnitDamaged)
        U->>U: gain mana, run own item hooks
    else move ready
        U->>P: nextStep(board, position, goal, range)
        P-->>U: next cell, or none
        U->>C: publish(UnitMoved)
    end
    C->>L: onEvent(event)
```

---

## 9. Design patterns summary

| Pattern | Where | Why |
|---|---|---|
| **Template Method** | `Unit.act()` with abstract `castAbility()` | Every unit follows the same tick sequence; only the ability differs. |
| **Strategy** | `TargetingStrategy`; `RivalStrategy`; `Pathfinder` | Swap targeting rules, AI behaviour or the pathfinding algorithm without touching the classes that use them. |
| **State** | `MatchPhase` and its five phases | Each phase knows what it does and which phase comes next, instead of one large `switch` in `Match`. |
| **Factory** | `UnitFactory`, `ItemFactory` | One place that knows how to build each unit and item; the draft and the tests ask for them by type. |
| **Observer** | `CombatListener`, `MatchListener` | The model announces what happened; the interface reacts. Keeps JavaFX out of the model. |
| **Modifier chain** (not a classic catalogue pattern) | `ModifierSource` implemented by `Item`, `Banner`, `StatusEffect` | One uniform way to change stats, so stacking and future traits need no special cases. |

**Object-oriented principles on show**

- **Inheritance and polymorphism:** `Unit` → ten unit classes; `Item` → stat and effect items; `StatusEffect` → shield, burn, stun, buff.
- **Abstraction:** units act through the `CombatContext` interface and never see the `Combat` class.
- **Encapsulation:** a unit's health changes only through `takeDamage()` and `heal()`; a warband enforces the team cap and bench size itself.
- **Composition over inheritance:** targeting, AI behaviour and banners are objects a class *has*, not things it *is*.
