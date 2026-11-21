# Ripple Pond - Game Design Document

| Field | Value |
| --- | --- |
| Status | Draft 5, for review by the whole team |
| Date | 20 November 2026 |
| Delivery | 28 November 2026 |
| Team | Stephan (lead), Jonathan, Xaryab |
| Platform | Desktop, Java 21 with JavaFX |
| Core technology | Maven, JUnit 5, JaCoCo, Javadoc, GitHub Actions |

**How to read this draft.** This file is the editable source of the Game Design Document. Design choices come from the team's discussions. Anything marked **[proposed]** is a default filled in to make the document complete; the team has not decided it yet. All numbers are starting values for balancing, not tested ones.

**What changed in Draft 5.** The third team member's name is corrected to Xaryab, and his task list is now `docs/tasks/TASKS_XARYAB.md`. Nothing else changed.

**What changed in Draft 4.** Work starts on 21 November, so the milestones are re-planned from that date (section 9.4), and each member has a step-by-step task list with due dates in `docs/tasks/`. Section 9.2 gives an owner to a few classes the task lists needed: `Pickup`, `LevelState`, `GameController`, `Food` and a new `Balance` class that holds every tunable number (section 8.1). The game rules are unchanged.

**What changed in Draft 3.** The pedago has approved the concept. Every object is now a circle, and all distances are measured in play-area widths on a play area that always keeps its 3:2 shape, so resizing the window changes nothing in the game. A ripple pushes each object once, when its ring passes. The frog loses only on health, energy or the turn limit. Reset is core and undo is a stretch goal. The console version is dropped. Reeds are round clumps placed in the river. Section 8.2 lists the object-oriented principles every class must follow. Section 10.3 lists every decision taken since Draft 2.

When this file changes, export a new PDF revision (`RIPPLE_POND_GDD_REV_n.pdf`) and keep the old ones.

## 1. Overview

### 1.1 Pitch

Ripple Pond is a single-player, turn-based game. A young frog must cross a river from one bank to the other. The player throws objects into the water. Each one floats where it lands, gives the frog something to walk on, and sends out a ripple that pushes other floating things. The frog has health and energy: snakes, birds and fish hurt it, every movement costs energy, and food found along the way restores both. Each completed level earns experience, so the frog grows tougher, hops further and learns to swim.

### 1.2 Design pillars

1. **Every throw changes the river.** The core question is always "where will this land, and where will the ripple push things?". Experimenting and watching the result is the main fun.
2. **Short levels, clear goals.** One frog, two banks, a handful of things to throw. A player should understand the goal in five seconds and finish a level in two to five minutes.
3. **Readable over pretty.** Plain shapes and colours. The state of every object, and the frog's health and energy, must be obvious at a glance.
4. **Deterministic rules.** The same level and the same player actions always give the same result. Predators follow fixed rules with no random numbers [proposed]. This makes the game fair, easy to debug and easy to test.
5. **Spend the frog wisely [proposed].** Health and energy are limited, so the shortest route is not always the best one.

### 1.3 Genre and perspective

Genre: turn-based puzzle with light role-playing elements (health, energy, experience). View: top-down. World: free 2D positions, no grid. Players: one. Session length: 2 to 5 minutes per level [proposed]. Total levels in the core game: 5.

The game's object-oriented heart is the family of things in the river: floating objects, a turtle, reeds, three predators and four kinds of pickup, all sharing a position and a size and each reacting in its own way. The pedago approved the concept on this basis.

### 1.4 Originality

The game uses general ideas (push mechanics, limited resources, health and energy, experience). The ripple, throwing and frog concepts, the rules text and all art are the team's own. No existing game is copied.

## 2. World and objects

### 2.1 The play area and its units

There is no grid. Every distance in the game, including positions, sizes, ripple growth, push distances, hop range and predator reach, is measured in **play-area widths**.

- The play area always has a 3:2 shape. Positions run from 0 to 1 across (x) and from 0 to 0.667 down (y).
- Because both axes use the same unit, a distance means the same thing in every direction: a hop of 0.10 covers the same ground going up the screen as going across it.
- When the window is resized, the play area is scaled evenly to fit, with empty margins if the window has a different shape. The model never knows about pixels, so a bigger window cannot make the frog hop further or change any other rule.
- Every object in the river is a **circle**. Its size is its diameter, in play-area widths. Two objects touch when the distance between their centres is no more than the sum of their radii. Art may draw an object in any shape (a log can look long), but the rules always use the circle.
- The frog is a circle of diameter 0.04 [proposed]. It stands on a surface when its centre is inside that surface [proposed].

The frog starts on one bank and must reach the opposite bank. The world is a river; the game keeps the name Ripple Pond.

| Area | Meaning | Can the frog be here? |
| --- | --- | --- |
| Start bank | A strip across the full width on one side. Where the frog begins each level. | Yes |
| Water | Everything between the banks. Ripples travel across it. | Only on a floating object, or by swimming once the frog can swim |
| Far bank | A strip across the full width on the other side. The goal. | Yes |

Each level file sets how deep each bank is.

### 2.2 Thrown objects

The player throws objects into the water. Each one floats where it lands, starts a ripple, and can be stood on. Objects come in different sizes: a larger one gives the frog more room to walk and is easier to hop onto.

Throwable objects are gathered during the game. Each one appears in a level as an icon, and the frog adds it to the throw bag by touching the icon [proposed: by touch]. A level also gives a small starting bag, so the frog can leave the first bank [proposed].

The three types below are [proposed]; the team has only decided that there are several sizes.

| Object [proposed] | Diameter |
| --- | --- |
| Twig | 0.05 |
| Plank | 0.10 |
| Log | 0.18 |

### 2.3 Other objects in the river

These are placed by the level file, not thrown.

| Object | Behaviour |
| --- | --- |
| Lily pad | Floats on water and can be stood on. Pushed by a ripple (section 3.1). |
| Turtle | Sleeps on the bottom and blocks the space it occupies. When a ripple's ring passes it, it wakes and moves away from the ripple's source. Not an enemy. |
| Reed clump | A solid circle. It blocks ripples, floating objects and the frog. It never moves. |

### 2.4 Predators

Snakes, birds and fish damage the frog. The frog cannot attack them [proposed].

Predators chase the frog. Each one moves towards it once every two turns, but can attack on any turn. A predator attacks when the frog is inside its reach at the end of the turn [proposed]. It moves in a straight line towards the frog, up to 0.06 per move, and stays in its own kind of place [proposed]. Whether ripples affect predators is open (section 10).

| Predator | Where it is found [proposed] | Damage [proposed] |
| --- | --- | --- |
| Snake | On the banks and in reed clumps | 3 HP |
| Bird | In the air, over any area | 2 HP |
| Fish | In open water | 2 HP |

### 2.5 Food

Food lies along the way. The frog eats an item by touching it [proposed]. Health and energy cannot go above their maximum [proposed].

| Item | Restores health | Restores energy | Amount [proposed] |
| --- | --- | --- | --- |
| Fly | Yes | Yes | +2 HP, +4 energy |
| Small minnow | No | Yes | +5 energy |
| Breadcrumb | Yes | No | +3 HP |

### 2.6 Levels

Each level is a text file. It lists the depth of each bank, every reed clump, and every object with its type, position and diameter, plus the frog's start position, the icons to gather, the starting throw bag and the turn limit. Levels can be written and tested without changing Java code.

## 3. Turn rules

The game is turn-based. Each turn runs the five steps below in a fixed order, and nothing moves while the player thinks, so there is no timer.

| Step | What happens |
| --- | --- |
| 1. Throw (optional) | The player picks one object from the bag and a point on open water with room for it. The object lands there, floats, and starts a ripple. |
| 2. Ripples spread | Every active ripple grows outward from its source. Ripples fade after 3 turns and then disappear. |
| 3. Objects react | Floating objects whose ripple ring passed them this turn are pushed away from the ripple's source; turtles are woken. A frog standing on a pushed object rides along with it. Changes are applied in a fixed order: top to bottom, then left to right [proposed]. |
| 4. Frog moves (optional) | The player may make one move: walk, hop or swim (section 4.2). The move costs energy. The frog eats any food and gathers any throwable icon it touches. |
| 5. Predators act | On every second turn, each predator moves towards the frog. Then, on every turn, each predator with the frog inside its reach deals its damage. |

End-of-turn checks [proposed]: if the frog reaches the far bank in step 4, the level is won at once and step 5 does not run. Otherwise the lose conditions (section 3.2) are checked after step 5.

### 3.1 Ripple rules

- A ripple has a source point, a radius that starts at 0, and a strength of 3 turns [proposed].
- Each throw starts one ripple at the landing point. Its radius grows by 0.08 each turn [proposed distance], starting in step 2 of the same turn.
- **A ripple pushes an object once, when its ring passes it.** That is the turn in which the radius first reaches the object's nearest edge. A ripple never pushes the same object twice, even if the object is pushed back inside the circle. The object that started a ripple is not pushed by it [proposed].
- A ripple is blocked by reeds: it does not reach an object if a reed clump lies on the straight line between the ripple's source and the object's centre [proposed]. Ripples have no effect on the banks.
- A floating object passed by a ripple moves 0.08 directly away from the source, unless a reed clump, a bank or another object is in the way [proposed distance].
- Ripples from two sources can pass the same object in the same turn. Their push directions are added, but an object moves at most one push distance per turn.

### 3.2 Win and lose

- **Win:** the frog reaches the far bank.
- **Lose:** the frog's health reaches 0 [proposed].
- **Lose:** the frog's energy reaches 0. A move that reaches the far bank with the last of its energy still wins [proposed].
- **Lose:** the turn limit is reached [proposed: 40 turns]. This guarantees every level ends.
- An empty throw bag is not a loss: the frog may still be able to walk or hop to the far bank.
- A lost level can be retried at once with Reset (section 4.5).

## 4. The frog and progress

### 4.1 Health and energy

The frog has health (HP) and energy. Predators take away health (section 2.4). Every movement costs energy (section 4.2). Food restores them (section 2.5). The frog starts with 10 HP and 20 energy [proposed], and both are refilled to their maximum at the start of each level. If either reaches 0, the level is lost (section 3.2).

### 4.2 Movement

The frog makes at most one move per turn. Energy is charged by distance: the further the move, the more it costs. Over the same distance, hopping costs 2 times the energy of walking and swimming costs 4 times. The base rate of 1 energy per 0.10 walked and the furthest distances are [proposed]. The split between walking and hopping is this draft's reading of the team's ideas (section 10).

| Move | What it is | Furthest per turn [proposed] | Energy per 0.10 travelled | Available |
| --- | --- | --- | --- | --- |
| Walk | Across a bank, or within the object the frog is standing on | 0.10 | 1 | Always |
| Hop | A jump over water from one surface to another | The frog's hop range | 2 | Always; the range grows with experience |
| Swim | Through open water | 0.10 | 4 | Unlocked by experience |

Example: a hop of 0.10 costs 2 energy, and swimming the same distance costs 4. A move the frog cannot pay for is refused.

### 4.3 Experience

The frog gains experience (XP) by completing a level. The award is a fixed amount plus a variable amount based on the health and energy it has left.

XP for a level = 50 + (2 x HP left) + (1 x energy left) [proposed numbers].

As its XP grows, the frog reaches a new rank. Each rank lets it hop further and raises its maximum health and energy, and a later rank lets it swim. This document says "rank" for the frog so that "level" always means a river to cross.

| Rank [proposed] | XP needed | Hop range | Max HP | Max energy | Can swim |
| --- | --- | --- | --- | --- | --- |
| 1 | 0 | 0.10 | 10 | 20 | No |
| 2 | 50 | 0.12 | 12 | 24 | No |
| 3 | 120 | 0.14 | 14 | 28 | Yes |
| 4 | 200 | 0.16 | 16 | 32 | Yes |
| 5 | 290 | 0.18 | 18 | 36 | Yes |

### 4.4 The throw bag

Each level gives the player a small starting bag of objects to throw [proposed]. The frog gathers more by touching icons in the level (section 2.2). There is no shop; the only reward between levels is experience. Whether unused objects carry over to the next level is open (section 10).

### 4.5 Reset and undo

**Reset is core.** It restarts the current level from its file: the river, the starting bag, and the frog with full health and energy for its rank. Experience already earned is not affected.

**Undo is a stretch goal** (section 7.2). If built, it takes back the last turn, including the frog's health and energy, food eaten, icons gathered and every object's position. The mock-up shows an Undo button; it appears only if the stretch goal is done.

### 4.6 Progression

Levels are played in order. Each level is unlocked after the previous one is won. Progress and experience are kept in memory for the session (saving to disk is a stretch goal).

## 5. Controls and screens

The game is played with the mouse in a JavaFX window. With no grid, a throw or a move is aimed at a point.

| Action | How |
| --- | --- |
| Throw an object | Pick an object from the bag, then click a point on the water |
| Move the frog | Choose walk, hop or swim, then click a destination |
| Confirm a turn | Click "End turn" |
| Reset the level | Reset button |
| Undo (stretch goal) | Undo button |
| Quit | Esc |

Screens:

1. Title screen (Play, How to Play, Quit).
2. Level screen (river, health, energy, rank and XP, throw bag, move choice, turn counter, messages).
3. Level complete screen (turns used, health and energy left, XP gained, new rank if any, next level).
4. Game over screen for a lost level (reason, retry, back to title).

## 6. Art and audio

The river is a flowing animation drawn as the background. It is only a backdrop and has no effect on the rules. Every object is drawn over it at its own position and size.

JavaFX with simple coloured shapes first. A free-licensed pond or tile pack may replace them if the team has time. Every asset and its licence is recorded in `CREDITS.md`. Audio is out of scope for the core game.

## 7. Scope

Everything in 7.1 is core. The team decided on 19 November that none of the systems added in Draft 2 is a stretch goal.

### 7.1 Core (must ship)

- Full turn loop: throw, ripples, objects, frog move, predators, win and lose.
- Free positions and circle sizes in play-area widths, on a play area that keeps its shape.
- Thrown objects of different sizes that the frog can walk on, gathered as icons during play. Lily pads, turtles and reed clumps.
- Frog health and energy, with walking, hopping and swimming costs.
- Snakes, birds and fish that chase and damage the frog.
- Flies, small minnows and breadcrumbs.
- Experience and ranks: further hops, more health and energy, swimming.
- Five levels read from text files.
- Reset.
- JavaFX title, level, level complete and game over screens, with mouse clicks.
- Unit tests for every rule above.

### 7.2 Stretch goals (only if the core is done and tested)

- Undo.
- A level editor that writes the text file format.
- Sound effects and a simple background ambience.
- A sixth to tenth level with new objects, such as a moving turtle or a leaf that floats in one direction.
- Saving the lowest number of turns for each level.
- Saving progress to disk.

### 7.3 Out of scope

Attacking predators [proposed], a console version (dropped on 20 November), timed levels, online play, multiple frogs, and a story with dialogue.

## 8. Technical outline

This section is a summary. The UML class diagram (`RIPPLE_POND_UML.md`) is a separate deliverable and is the authority on class design.

### 8.1 Architecture

- **Game model:** all rules (river, ripples, objects, frog stats, turns). No JavaFX imports, so it runs in unit tests.
- **Elements:** everything in the river shares a position and a size. Thrown objects, lily pads, turtles, reed clumps, predators and pickups differ in how they behave. This family of classes is the main place the project shows object-oriented design.
- **Geometry:** positions and sizes are in play-area widths. The model decides what touches or reaches what; the view converts to pixels with one scale factor for both axes.
- **View:** JavaFX screens that only read the model and listen for its events. JavaFX draws the river as an animated background and every object over it.
- **Controller:** turns clicks into model actions.
- **Level loader:** reads text files and builds the river.
- **Balancing numbers:** every tunable number (distances, damage, energy, food, XP) is a named constant in one class, `Balance` [proposed], so the balancing pass changes one file and no number is written inline.

### 8.2 Object-oriented principles every class follows

The pedago's one condition is that the code follows object-oriented principles throughout. Reviewers check every merge against this list.

| Principle | What it means in this project |
| --- | --- |
| Encapsulation | Fields are private. State changes only through methods that enforce the rules: health never drops below 0, food never raises a stat above its maximum, an empty bag refuses to throw. |
| Abstraction | Code depends on interfaces where behaviour varies: `Element`, `MoveMode`, `RippleSpread`, `ChaseStrategy`, `LevelState`, `PondListener`. |
| Inheritance | Used only for a real "is a" relationship with shared behaviour, such as the element family. Differences that are only data, such as the size of a thrown object, are values, not subclasses. |
| Polymorphism | No `instanceof` checks or `switch` on a type to decide behaviour. Each class answers for itself: how it reacts to a ripple, what a food item restores, how much a predator hurts. |
| Single responsibility | One class, one job: a `Ripple` knows its circle, the `Pond` knows what is where, the `TurnEngine` knows the order of a turn, `EndRules` knows when a level ends. |
| Open for extension | A new predator, food, way of moving or chase rule is a new class. Existing classes do not change. |
| Separation of model and view | The model never imports JavaFX and never knows about pixels. |

### 8.3 Design patterns in use

| Pattern | Where it appears |
| --- | --- |
| Template method | Every element reacts to a ripple through one fixed method and fills in only its own step. Food is collected the same way and each item fills in what it restores. The turn engine always runs its five steps in order. |
| Strategy | The three ways of moving (walk, hop, swim), how a ripple spreads, and how a predator chases. |
| Factory | Creating objects from a type name in the level file (for example "log" creates a log). |
| Observer | The view listens for events such as "object pushed", "frog hurt", "food eaten" or "rank gained". Tests can listen the same way to check what happened. |
| State | Level states: in progress, won, lost. Only a level in progress accepts turns. |

### 8.4 Tests the team must write

- Geometry: two circles touch when their centres are no further apart than the sum of their radii; a distance is the same in every direction; whether the frog is inside a predator's reach.
- Ripple growth and fade after 3 turns.
- A ripple pushes an object once, in the turn its ring passes it, and never again; it does not push the object that started it.
- Reed shadow: an object behind a reed clump, as seen from the source, is not reached.
- Floating object pushed away from the source, blocked by a reed clump, another object or a bank; a frog standing on it rides along.
- Two ripples passing one object in the same turn move it at most one push distance.
- Turtle wakes and moves when a ripple's ring passes it.
- Frog movement rules: walking only within a surface, hopping only within hop range, swimming only once unlocked.
- Energy: the cost grows with distance; over the same distance hopping costs 2 times and swimming 4 times the walking cost; a move the frog cannot pay for is refused.
- Predators: each one moves towards the frog on every second turn only, attacks on any turn when the frog is in reach, and deals its own damage; health never drops below 0.
- Food: fly, small minnow and breadcrumb restore the right stats and never exceed the maximum.
- Experience: the level award (fixed part plus health and energy left) and each rank's hop range, maximum health, maximum energy and swimming.
- Throw bag: touching an icon adds the object to the bag; an object can only be thrown when one is left.
- Win and lose: health at 0, energy at 0, the turn limit; reaching the far bank with the last energy wins; an empty bag alone does not lose.
- Reset restores the level exactly as loaded and keeps experience.
- Determinism: the same level and moves always give the same final river.
- Level loader rejects a malformed level file with a clear error.

### 8.5 Coverage and automation

JaCoCo reports coverage on the game model; the team aims for 80% line coverage. GitHub Actions runs the tests on every push.

## 9. Plan

### 9.1 Roles

Status: first proposed by Stephan for Draft 1. The systems added in Draft 2 were assigned on 19 November by fit and workload, and adjusted on 20 November when the console was dropped and reed clumps were added [proposed]; all three to confirm. The team's two goals are to gain programming experience and to ship a good game. Every critical part has one owner, and everyone writes some of each kind of content.

| Member | Load | Summary |
| --- | --- | --- |
| Stephan | Full | Geometry, ripple engine and turn loop, predators, plus the application window and level screen. Team lead and integrator. |
| Jonathan | Full | The frog and what it uses: health, energy, movement, experience and ranks; thrown objects, lily pads, turtles and reed clumps; the level loader and the level file format. |
| Xaryab | Light, about 10 hours | The title, level complete and game over screens; the three food items; level 5; playtesting all five levels; the README and How to Play text. |

### 9.2 Ownership by layer

| Layer | Owner | Contents |
| --- | --- | --- |
| Foundations (due 22 November) | Stephan | River model and geometry (positions, sizes, touching and reach), ripple engine, turn loop, shared types (the `Element` and `PondListener` interfaces, the `Level` class, the `Balance` class). |
| Foundations (due 22 November) | Jonathan | Element hierarchy (Frog, thrown objects, LilyPad, Turtle, Reed, the `Pickup` base class), frog health and energy, the throw bag, level states and win and lose checks. |
| Content (shared) | Jonathan | Level file format, LevelLoader and ElementFactory, walking, hopping and swimming with energy by distance, riding, throwable icons, experience and ranks, the game session and `GameController` (reset, next level), turtle wake and move, pad push rules, levels 1 and 2. |
| Content (shared) | Stephan | Circular ripples, fade and reed shadow, predators (snake, bird, fish: chase and attack), JavaFX screen switching, level screen and input, the animated river, levels 3 and 4. |
| Content (shared) | Xaryab | Title, level complete and game over screens; the `Food` base class and food (fly, small minnow, breadcrumb); level 5; playtest notes on all five levels. |
| One-off jobs | Stephan | Maven project, CI and coverage, integration of daily merges, JavaFX window. |
| One-off jobs | Jonathan | GDD and UML revisions and their PDFs, Javadoc check, `CREDITS.md`. |
| One-off jobs | Xaryab | README, How to Play text, level design review. |
| Balancing | All three | Turn limit, throw bags, ripple strength, health, energy, damage, food and XP numbers, 25 November. |
| Presentation slides | To decide | Each member prepares their own part; who assembles the slides is open (section 10.1). |

Each member's tasks, step by step and with due dates, are in `docs/tasks/TASKS_STEPHAN.md`, `TASKS_JONATHAN.md` and `TASKS_XARYAB.md`.

### 9.3 Working rules

- A merge is the class, its unit tests and its Javadoc together.
- Every merge is reviewed by one teammate, who checks it against the principles in section 8.2. Each person reviews code they did not write.
- The team writes the code with AI assistance. Everyone must be able to explain every line they merge.
- The main branch always builds and its tests pass.
- A task's due date means merged into `main` by that time; "end of day" means 22:00. Reviews are done within 12 hours [proposed].

### 9.4 Milestones

Re-planned on 20 November, because work starts on 21 November; all three to confirm.

| Date | Goal |
| --- | --- |
| Sat 21 Nov | Maven project, CI and shared types in `main` by 12:00 (planned for 18-19 November). Level file format written. |
| Sun 22 Nov | Foundations done: geometry, ripples, element hierarchy, frog health and energy, level states, all tested (planned for 21 November). |
| Mon 23 Nov | Turn engine, level loader, movement and food. The JavaFX window switches between screens. |
| Tue 24 Nov | Full turn loop runs end to end in tests, with predators, food, throwable icons and experience. JavaFX level screen shows the river and accepts clicks. |
| Wed 25 Nov | Five levels by 12:00. Playtest in the afternoon. Balancing session at 19:00. Game playable from title to last level. |
| Thu 26 Nov | Feature freeze at 18:00. Bug fixing, Javadoc complete, coverage reviewed, UML and GDD updated to match the code. |
| Fri 27 Nov | Demo rehearsal and presentation preparation. |
| Sat 28 Nov | Delivery. |

Without a console version, the turn loop can only be played once JavaFX accepts clicks, so that is due on 24 November, leaving a playable game for the playtest and balancing on 25 November.

## 10. Open decisions for the team

### 10.1 Still open

- **Roles and dates.** All three to confirm or change the assignments in section 9 and the task lists in `docs/tasks/`.
- **Presentation slides.** Who assembles them? Each member prepares their own part.
- **Art pack.** Owner: Stephan. Needs a licence that allows reuse (CC0 is simplest), a top-down view, and enough distinguishable shapes for about 13 kinds of object.
- **Turtle behaviour.** Does a turtle move once when woken, or keep moving each turn until it reaches a reed clump?
- **Thrown objects.** Which objects and what sizes? Twig, plank and log are placeholders. How many are in each starting bag, and do unused ones carry over to the next level?
- **Predators and ripples.** Can ripples push or scare predators? Can a predator pass reed clumps or floating objects on its way to the frog?
- **Walk versus hop.** Is section 4.2's reading right: walking within a surface, hopping across a gap? Is stepping onto a touching object a walk or a hop?
- **Replays.** Does replaying a level that is already won give XP again?
- **Nearly empty energy.** Because a move the frog cannot pay for is refused, a frog with a little energy left but too little for any useful move does not lose until the turn limit. Should it lose sooner?
- **Balancing numbers.** Throw bag per level, ripple strength, turn limit, health, energy, damage, food amounts, XP award and rank thresholds are tuned on 25 November.

### 10.2 Raised by Draft 3

- **Reed clump owner.** Draft 3 gives the `Reed` class to Jonathan, with the element family, and the reed shadow rule to Stephan, with the ripples [proposed].
- **Frog size.** The frog's diameter of 0.04, and the rule that it stands where its centre is, are new in this draft [proposed].

### 10.3 Decided since Draft 2

- **Pedago approval (20 November).** The concept is approved, on condition that the code follows object-oriented principles throughout (section 8.2).
- **Units and scaling.** Every distance is in play-area widths on a play area that keeps its 3:2 shape, so resizing the window cannot change any rule (section 2.1).
- **Shapes.** Every object is a circle; its size is its diameter (section 2.1).
- **Ripple pushes.** A ripple pushes an object once, when its ring passes it (section 3.1).
- **Losing.** Only health at 0, energy at 0 or the turn limit lose a level. Having nothing left to throw does not (section 3.2).
- **Turn limit.** Kept, so every level ends (section 3.2).
- **Reset and undo.** Reset is core; undo is a stretch goal (section 4.5).
- **Console.** Dropped. JavaFX is the only interface. Xaryab takes the title, level complete and game over screens instead (sections 7.3 and 9).
- **Levels.** Jonathan makes levels 1 and 2, Stephan 3 and 4, Xaryab 5; Xaryab playtests all five (section 9.2).
- **Reeds.** Round clumps placed in the river as elements, not rectangular areas (section 2.3).
- **AI assistance.** The team writes the code with AI assistance, and every member can explain every line they merge (section 9.3).
- **`Level` is a class.** There is only one kind of level, so it is a class, not one of the shared interfaces (section 9.2).
