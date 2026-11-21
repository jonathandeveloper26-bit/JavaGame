# Ripple Pond - UML class diagram

Version 1, 19 November 2026. Matches Draft 2 of the Game Design Document.

This file is the editable source of the class diagram. The diagrams are written in
Mermaid, which GitHub, IntelliJ IDEA and VS Code (with a Mermaid extension) render
directly. Edit the text between the code fences and the picture follows.

The design is split into four views, because one picture of every class would be
unreadable. A class can appear in more than one view; its members are listed only in
the view where it matters most.

How to read the arrows:

| Arrow | Meaning |
| --- | --- |
| `A <\|-- B` (solid line, hollow triangle) | B extends A |
| `A <\|.. B` (dashed line, hollow triangle) | B implements interface A |
| `A *-- B` (filled diamond) | A owns B; B does not exist without A |
| `A o-- B` (hollow diamond) | A holds B, but B can exist without A |
| `A --> B` (solid arrow) | A keeps a reference to B |
| `A ..> B` (dashed arrow) | A uses B for a moment (parameter, creates, throws) |

Members: `+` public, `#` protected, `-` private. Italic (written with a trailing `*`)
means abstract. Underlined (trailing `$`) means static.

## 1. Elements: everything that sits in the river

Every thing in the river is an `Element` with a position and a size. Subclasses exist
where behaviour differs. Thrown objects differ only in size, so they are one class with
a `ThrowableKind` value rather than three subclasses.

```mermaid
classDiagram
direction LR

class Element {
  <<interface>>
  +getPosition() Vec2
  +getSize() double
  +touches(other: Element) boolean
  +distanceTo(other: Element) double
  +reactTo(ripple: Ripple, pond: Pond)
}
class AbstractElement {
  <<abstract>>
  -position: Vec2
  -size: double
  +reactTo(ripple: Ripple, pond: Pond)
  #onRipple(ripple: Ripple, pond: Pond)
  #moveTo(target: Vec2)
}
class Vec2 {
  -x: double
  -y: double
  +plus(other: Vec2) Vec2
  +distanceTo(other: Vec2) double
  +stepTowards(target: Vec2, maxDistance: double) Vec2
}
class Frog
class FloatingObject {
  <<abstract>>
  -rider: Frog
  #onRipple(ripple: Ripple, pond: Pond)
  +push(direction: Vec2, distance: double, pond: Pond) boolean
  +covers(point: Vec2) boolean
}
class ThrownObject {
  -kind: ThrowableKind
}
class LilyPad
class ThrowableKind {
  <<enumeration>>
  TWIG
  PLANK
  LOG
  +size() double
}
class Turtle {
  -awake: boolean
  #onRipple(ripple: Ripple, pond: Pond)
}
class Predator {
  <<abstract>>
  -reach: double
  -chase: ChaseStrategy
  +getDamage() int*
  +canEnter(area: AreaType) boolean*
  +moveTowards(frog: Frog, pond: Pond)
  +canAttack(frog: Frog) boolean
  +attack(frog: Frog)
}
class Snake
class Bird
class Fish
class Pickup {
  <<abstract>>
  +collect(frog: Frog, level: Level)*
}
class Food {
  <<abstract>>
  +collect(frog: Frog, level: Level)
  #applyTo(frog: Frog)*
}
class Fly
class Minnow
class Breadcrumb
class ThrowableIcon {
  -kind: ThrowableKind
  +collect(frog: Frog, level: Level)
}

Element <|.. AbstractElement
AbstractElement *-- Vec2 : position
AbstractElement <|-- Frog
AbstractElement <|-- FloatingObject
AbstractElement <|-- Turtle
AbstractElement <|-- Predator
AbstractElement <|-- Pickup
FloatingObject <|-- ThrownObject
FloatingObject <|-- LilyPad
FloatingObject --> Frog : carries 0..1
ThrownObject --> ThrowableKind
ThrowableIcon --> ThrowableKind
Predator <|-- Snake
Predator <|-- Bird
Predator <|-- Fish
Pickup <|-- Food
Pickup <|-- ThrowableIcon
Food <|-- Fly
Food <|-- Minnow
Food <|-- Breadcrumb
```

Notes:

- `AbstractElement.reactTo` is the same for every element: it checks whether the ripple
  reaches the element and, if so, calls `onRipple`. Subclasses only override `onRipple`.
  A floating object is pushed, a turtle wakes and moves, everything else ignores it.
- `FloatingObject.push` moves its `rider` with it. This is the riding rule.
- `Snake`, `Bird` and `Fish` each answer `getDamage` and `canEnter` differently.
- `Fly`, `Minnow` and `Breadcrumb` each implement `applyTo` (health, energy or both).

## 2. The frog: movement, energy and experience

The three ways of moving are separate classes behind one interface. The frog asks the
chosen `MoveMode` whether a move is legal and what it costs.

```mermaid
classDiagram
direction TB

class Frog {
  -health: int
  -energy: double
  -rank: Rank
  -standingOn: FloatingObject
  +move(mode: MoveMode, target: Vec2, pond: Pond)
  +canAfford(mode: MoveMode, target: Vec2) boolean
  +takeDamage(amount: int)
  +restoreHealth(amount: int)
  +restoreEnergy(amount: double)
  +isAlive() boolean
  +hasEnergy() boolean
}
class MoveMode {
  <<interface>>
  +energyPerUnit() double
  +maxDistance(frog: Frog) double
  +isLegal(frog: Frog, target: Vec2, pond: Pond) boolean
  +costFor(distance: double) double
}
class Walk
class Hop
class Swim
class Rank {
  <<enumeration>>
  RANK_1
  RANK_2
  RANK_3
  RANK_4
  RANK_5
  +xpNeeded() int
  +hopRange() double
  +maxHealth() int
  +maxEnergy() int
  +canSwim() boolean
  +forXp(xp: int) Rank$
}
class FrogProgress {
  -xp: int
  +addXp(amount: int)
  +getRank() Rank
}
class ExperienceRules {
  +xpFor(frog: Frog) int
}
class GameSession {
  -levelFiles: List~String~
  -unlockedLevels: int
  +startLevel(index: int) Level
  +completeLevel(level: Level)
  +isUnlocked(index: int) boolean
}
class ThrowBag {
  -counts: Map
  +add(kind: ThrowableKind)
  +take(kind: ThrowableKind) ThrownObject
  +has(kind: ThrowableKind) boolean
  +isEmpty() boolean
}
class Level

MoveMode <|.. Walk
MoveMode <|.. Hop
MoveMode <|.. Swim
Frog ..> MoveMode : moves with
Frog --> Rank
FrogProgress --> Rank
GameSession *-- FrogProgress
GameSession ..> ExperienceRules : awards XP
ExperienceRules ..> Frog : reads health and energy
GameSession ..> Level : starts
Level *-- ThrowBag
```

Notes:

- `Walk`, `Hop` and `Swim` return 1, 2 and 4 from `energyPerUnit`. `Hop.maxDistance`
  reads the frog's rank; `Swim.isLegal` checks `Rank.canSwim`.
- `Rank` holds the table from section 4.3 of the GDD as data.
- `FrogProgress` lives in the `GameSession`, so experience survives from level to level.
  A `Frog` is created fresh for each level with full health and energy for its rank.

## 3. The turn engine, ripples and level state

`Level.playTurn` hands the action to the current `LevelState`. Only `InProgress` runs
the `TurnEngine`; `Won` and `Lost` refuse further turns.

```mermaid
classDiagram
direction TB

class Level {
  -turn: int
  -turnLimit: int
  -state: LevelState
  +playTurn(action: TurnAction)
  +getState() LevelState
  +getPond() Pond
  +getBag() ThrowBag
  +addListener(listener: PondListener)
  +publish(event: GameEvent)
}
class LevelState {
  <<interface>>
  +playTurn(level: Level, action: TurnAction) LevelState
  +isOver() boolean
}
class InProgress
class Won
class Lost {
  -reason: LoseReason
}
class LoseReason {
  <<enumeration>>
  NO_HEALTH
  NO_ENERGY
  NO_MOVES
  TURN_LIMIT
}
class TurnAction {
  -throwKind: ThrowableKind
  -throwTarget: Vec2
  -moveMode: MoveMode
  -moveTarget: Vec2
  +hasThrow() boolean
  +hasMove() boolean
}
class TurnEngine {
  +runTurn(level: Level, action: TurnAction)
  #throwStep(level: Level, action: TurnAction)
  #spreadRipples(pond: Pond)
  #objectsReact(pond: Pond)
  #frogMoves(level: Level, action: TurnAction)
  #predatorsAct(level: Level)
}
class EndRules {
  +evaluate(level: Level) LevelState
}
class Pond {
  -areas: List~Area~
  -elements: List~Element~
  -ripples: List~Ripple~
  -frog: Frog
  +areaAt(point: Vec2) AreaType
  +isFree(point: Vec2, size: double) boolean
  +surfaceAt(point: Vec2) FloatingObject
  +elementsInOrder() List~Element~
  +add(element: Element)
  +remove(element: Element)
}
class Area {
  -type: AreaType
  -bounds: Rect
  +contains(point: Vec2) boolean
}
class AreaType {
  <<enumeration>>
  START_BANK
  WATER
  REEDS
  FAR_BANK
}
class Ripple {
  -source: Vec2
  -radius: double
  -turnsLeft: int
  +advance(pond: Pond)
  +reaches(element: Element, pond: Pond) boolean
  +pushDirection(element: Element) Vec2
  +isFaded() boolean
}
class RippleSpread {
  <<interface>>
  +nextRadius(ripple: Ripple) double
  +reaches(ripple: Ripple, element: Element, pond: Pond) boolean
}
class CircularSpread
class ChaseStrategy {
  <<interface>>
  +nextPosition(predator: Predator, frog: Frog, pond: Pond) Vec2
}
class StraightLineChase
class Predator
<<abstract>> Predator
class Element
<<interface>> Element

Level --> LevelState : current
Level *-- Pond
LevelState <|.. InProgress
LevelState <|.. Won
LevelState <|.. Lost
Lost --> LoseReason
InProgress ..> TurnEngine : runs
InProgress ..> EndRules : asks
TurnEngine ..> TurnAction : reads
TurnEngine ..> Pond : changes
Pond *-- Area
Area --> AreaType
Pond o-- Element
Pond *-- Ripple
Ripple --> RippleSpread
RippleSpread <|.. CircularSpread
Predator --> ChaseStrategy
ChaseStrategy <|.. StraightLineChase
```

Notes:

- `TurnEngine.runTurn` always calls the five steps in the order of section 3 of the GDD:
  throw, ripples spread, objects react, frog moves, predators act.
- `predatorsAct` moves predators on every second turn only, but lets them attack on
  every turn.
- `Pond.elementsInOrder` returns elements top to bottom, then left to right, so the
  result is always the same for the same input.
- `EndRules.evaluate` returns `Won`, `Lost` (with a reason) or `InProgress`.

## 4. Loading levels, events, views and controllers

The model never imports JavaFX. Views learn about changes by listening for events.

```mermaid
classDiagram
direction TB

class LevelLoader {
  -factory: ElementFactory
  +load(path: String, progress: FrogProgress) Level
}
class ElementFactory {
  +create(typeName: String, position: Vec2, size: double) Element
}
class LevelFormatException
class PondListener {
  <<interface>>
  +onEvent(event: GameEvent)
}
class GameEvent {
  -type: EventType
  -subject: Element
  -amount: double
}
class EventType {
  <<enumeration>>
  OBJECT_THROWN
  OBJECT_PUSHED
  TURTLE_WOKE
  FROG_MOVED
  FROG_HURT
  FOOD_EATEN
  ICON_GATHERED
  PREDATOR_MOVED
  RANK_GAINED
  LEVEL_WON
  LEVEL_LOST
}
class GameController {
  -session: GameSession
  -level: Level
  +submit(action: TurnAction)
  +reset()
  +nextLevel()
}
class ConsoleApp {
  +run()
}
class ConsoleView {
  +render(level: Level)
  +onEvent(event: GameEvent)
}
class CommandParser {
  +parse(line: String) TurnAction
}
class FxApp {
  +start(stage: Stage)
}
class FxLevelView {
  +render(level: Level)
  +onEvent(event: GameEvent)
  +toPixels(point: Vec2) Point2D
  +toFraction(x: double, y: double) Vec2
}
class FxInputHandler {
  +onClick(x: double, y: double)
  +chooseThrow(kind: ThrowableKind)
  +chooseMove(mode: MoveMode)
  +endTurn()
}
class Level
class GameSession
class TurnAction
class Element
<<interface>> Element

LevelLoader --> ElementFactory
LevelLoader ..> LevelFormatException : throws
LevelLoader ..> Level : builds
ElementFactory ..> Element : creates
GameSession ..> LevelLoader : uses
Level o-- PondListener : notifies
PondListener ..> GameEvent
GameEvent --> EventType
PondListener <|.. ConsoleView
PondListener <|.. FxLevelView
GameController --> GameSession
GameController --> Level
ConsoleApp --> CommandParser
ConsoleApp --> ConsoleView
ConsoleApp --> GameController
CommandParser ..> TurnAction : builds
FxApp --> FxLevelView
FxApp --> FxInputHandler
FxInputHandler --> GameController
FxInputHandler ..> TurnAction : builds
```

Notes:

- `ElementFactory.create` turns a type name from the level file (`log`, `snake`, `fly`)
  into the right `Element` subclass. An unknown name becomes a `LevelFormatException`.
- `FxLevelView.toPixels` and `toFraction` are the only place where fractions of the play
  area become pixels and back.
- Both views receive the same events, so the model does not know which one is running.

## Design patterns and where to find them

| Pattern | Classes | What it buys |
| --- | --- | --- |
| Template method | `AbstractElement.reactTo` calling `onRipple`; `Food.collect` calling `applyTo`; `TurnEngine.runTurn` calling its five steps | The fixed part is written once; each subclass fills in only its own step |
| Strategy | `MoveMode` (Walk, Hop, Swim); `RippleSpread` (CircularSpread); `ChaseStrategy` (StraightLineChase) | A rule can be swapped or added without touching the class that uses it |
| Factory | `ElementFactory`, used by `LevelLoader` | Level files name types as text; one place maps names to classes |
| Observer | `PondListener`, `GameEvent`, `Level.addListener` and `publish` | The model stays free of any view code; console and JavaFX listen the same way |
| State | `LevelState` (InProgress, Won, Lost) | A finished level cannot accept turns, with no `if` chains in `Level` |

## Where this differs from the GDD, and what is still open

- The GDD lists `Level` among the shared interfaces. Here it is a class, because there
  is only one kind of level. `Element` and `PondListener` are interfaces as planned.
- `ThrowableKind` uses the placeholder names twig, plank and log (GDD section 10).
- `ConsoleApp`, `ConsoleView` and `CommandParser` only stay if the console version does
  (GDD section 10).
- `StraightLineChase` is the simple rule proposed in the GDD. A smarter rule that goes
  around reeds would be a second `ChaseStrategy`, with no change to `Predator`.
- Undo is not modelled, because the team has not decided whether it is core.
- Method lists show the operations that matter for the design. Getters, setters and
  constructors are left out.
