# Tasks - Stephan

Ripple Pond, Saturday 21 to Saturday 28 November 2026.
Load: full. Role: team lead and integrator.

Read first: GDD (`docs/RIPPLE_POND_GDD.md`) sections 2.1, 3, 8 and 9, and the UML views 3 and 4. Task IDs
starting with J are Jonathan's (`TASKS_JONATHAN.md`) and X are Xaryab's
(`TASKS_XARYAB.md`).

## How we work

- **Due** means merged into `main` by that time. "End of day" means 22:00.
- One branch and one pull request per task, named like `stephan/s3-pond`.
- A pull request is the classes, their unit tests and their Javadoc together, with CI
  green. It is checked against the principles in GDD section 8.2.
- Every pull request is reviewed by one teammate before it is merged. Reviews are done
  within 12 hours [proposed].
- Every tunable number (distances, damage, energy, XP) goes in the `Balance` class
  (task S2), never inline, so the balancing pass on 25 November changes one file.
- If a task will be late, or you are blocked, tell the team the same day.

## Team calendar

| Date | Team milestone |
| --- | --- |
| Sat 21 Nov | Maven project, CI and shared types in `main` by 12:00. Level file format written. |
| Sun 22 Nov | Foundations done: geometry, ripples, element hierarchy, frog health and energy, level states. |
| Mon 23 Nov | Turn engine, level loader, movement and food. The JavaFX window switches screens. |
| Tue 24 Nov | Full turn loop runs end to end in tests. JavaFX level screen shows the river and accepts clicks. |
| Wed 25 Nov | Five levels by 12:00. Playtest in the afternoon. Balancing session at 19:00. |
| Thu 26 Nov | Feature freeze at 18:00. Javadoc, coverage, UML and GDD final. |
| Fri 27 Nov | Demo rehearsal and presentation preparation. |
| Sat 28 Nov | Delivery. |

## Your classes

| Package [proposed] | Classes |
| --- | --- |
| `ripplepond.model` | `Balance` |
| `ripplepond.model.geometry` | `Vec2` |
| `ripplepond.model.event` | `PondListener`, `GameEvent`, `EventType` |
| `ripplepond.model.level` | `Level`, `Pond`, `Area`, `AreaType`, `TurnAction`, `TurnEngine` |
| `ripplepond.model.ripple` | `Ripple`, `RippleSpread`, `CircularSpread` |
| `ripplepond.model.element` | the `Element` interface |
| `ripplepond.model.element.predator` | `Predator`, `Snake`, `Bird`, `Fish`, `ChaseStrategy`, `StraightLineChase` |
| `ripplepond.view` | `FxApp`, `FxLevelView`, `FxInputHandler`, `Launcher` |
| `src/main/resources/levels` | `level3.txt`, `level4.txt` |

## Tasks

### S1. Maven project, CI and package layout

**Due:** Sat 21 Nov, 12:00. **Needs:** nothing. **Blocks:** every other task in the
team. **Reviewer:** Jonathan.

1. Create `pom.xml` for Java 21 with JavaFX (`javafx-controls`), JUnit 5, JaCoCo, the
   Javadoc plugin and the JavaFX Maven plugin, so that `mvn verify`, `mvn javafx:run`
   and `mvn javadoc:javadoc` all work.
2. Configure JaCoCo to report line coverage on `ripplepond.model` and
   `ripplepond.controller` only, leaving out `ripplepond.view`.
3. Create every package in the table above and in the other two task lists, each with a
   `package-info.java` holding a one-line Javadoc description.
4. Add a `Launcher` class with a `main` method that starts `FxApp`, and an `FxApp` that
   opens an empty 3:2 window. A separate launcher lets JavaFX run without Java modules.
5. Add one placeholder test so that `mvn verify` has something to run.
6. Add `.github/workflows/ci.yml` that runs `mvn -B verify` on Java 21 for every push and
   pull request.
7. Add a `.gitignore` for `target/` and IDE files.
8. If the repository settings allow it, protect `main`: pull request and green CI
   required.
9. Check the commands in the README's "Build, run and test" section and correct any
   that differ.

**Done when:** a fresh clone passes `mvn verify` locally and in GitHub Actions, and
`mvn javafx:run` opens an empty window. Tell the team as soon as it is merged.

### S2. Shared types

**Due:** Sat 21 Nov, end of day. **Needs:** S1. **Blocks:** J2, J3, J4, X1.
**Reviewer:** Jonathan.

1. `Vec2`, fully implemented and tested: immutable, with `plus`, `minus`, `scale`,
   `length`, `normalised`, `distanceTo` and `stepTowards`.
2. `Balance`: a final class of named constants, starting with the play-area size
   (width 1, height 0.667) and the ripple numbers from GDD section 3.1. Everyone adds
   their own numbers here.
3. The `Element` interface exactly as in UML view 1, with Javadoc on every method.
4. `PondListener`, `GameEvent` and `EventType` as in UML view 4.
5. `TurnAction` as in UML view 3.
6. Shells for `Level`, `Pond`, `Area`, `AreaType` and `Ripple`: every public method from
   the UML with its signature and Javadoc, and a body that throws
   `UnsupportedOperationException` until S3 and S4.

**Done when:** everything in UML views 1 and 3 that the others build on compiles, and
`Vec2` has tests for every method.

### S3. Pond and geometry

**Due:** Sun 22 Nov, end of day. **Needs:** S2; J2's `Reed` (due Sun 22, 12:00).
**Blocks:** S4, J5, J6. **GDD:** 2.1. **Reviewer:** Jonathan.

1. `Area` and `AreaType` (start bank, water, far bank), each bank a strip across the
   full width.
2. `Pond`: `areaAt`, `add`, `remove`, `isFree` (no overlap with any element or reed
   clump, and inside the play area), `surfaceAt` and `reedBetween`, which asks each
   `Reed` whether it lies between two points.
3. `Pond.elementsInOrder`: top to bottom, then left to right, so every turn processes
   elements in the same order.
4. Tests: a distance is the same in every direction; `isFree` against banks, reed clumps
   and other objects; `surfaceAt` on a floating object and on open water; ordering.

**Done when:** `Pond` has no `UnsupportedOperationException` left and every method is
tested.

### S4. Ripples

**Due:** Sun 22 Nov, end of day. **Needs:** S3. **Blocks:** S5, J8. **GDD:** 3.1.
**Reviewer:** Jonathan.

1. `RippleSpread` and `CircularSpread`: the radius grows by the `Balance` amount (0.08)
   each turn.
2. `Ripple.advance` keeps `previousRadius` and `radius`, and counts down `turnsLeft`
   (3 turns).
3. `Ripple.passes(element, pond)` is true only in the turn the ring first reaches the
   element's nearest edge, only once per element (record it in `alreadyPassed`), never
   for `origin`, and never when `Pond.reedBetween` finds a reed clump on the line from
   the source.
4. `Ripple.pushDirection`: the unit vector from the source to the element's centre.
5. Tests, one per rule in GDD section 8.4 about ripples: growth, fade after 3 turns,
   pushes once, never the origin, reed shadow.

**Done when:** all ripple rules in GDD section 3.1 except blocking a push (J8) are
tested.

### S5. Turn engine and level wiring

**Due:** Mon 23 Nov, end of day. **Needs:** S4, J3, J4. **Blocks:** S6, S8, J7.
**GDD:** 3. **Reviewer:** Jonathan.

1. `TurnEngine.runTurn` calls the five steps in the order of GDD section 3: throw,
   ripples spread, objects react, frog moves, predators act.
2. `throwStep`: take the object from the `ThrowBag` (J4), place it if `Pond.isFree`,
   start a `Ripple` with that object as its `origin`.
3. `objectsReact`: for each element in `elementsInOrder`, call `reactTo` for each active
   ripple; when two ripples pass the same object in one turn, add their directions and
   push at most one push distance.
4. `frogMoves`: hand the action's move to `Frog.move` (J6). In tests, use a stub
   `MoveMode` until J6 is merged.
5. `predatorsAct`: leave an empty hook for S6.
6. `Level.playTurn` delegates to the current `LevelState` (J3); `Level.addListener` and
   `publish` send `GameEvent`s.
7. Tests: the step order, determinism (same level and same actions give the same
   river), and the events a turn publishes, using a test `PondListener`.

**Done when:** a level built in a test can play several turns in a row.

### S6. Predators

**Due:** Tue 24 Nov, 12:00. **Needs:** S5. **Blocks:** J5 can register `snake`,
`bird` and `fish`. **GDD:** 2.4. **Reviewer:** Jonathan.

1. `Predator` (abstract) with `getDamage` and `canEnter` abstract; `Snake`, `Bird` and
   `Fish` each answer them differently (GDD section 2.4 table).
2. `ChaseStrategy` and `StraightLineChase`: up to 0.06 per move towards the frog,
   staying in the areas the predator can enter.
3. In `predatorsAct`: predators move on every second turn only; on every turn, each
   predator with the frog in reach deals its damage.
4. Add `snake`, `bird` and `fish` to the `ElementFactory` (J5).
5. Tests: moves only on even turns, attacks on any turn, each predator's damage, health
   never below 0.

**Done when:** predators chase and hurt the frog in a test level.

### S7. JavaFX screen switching

**Due:** Mon 23 Nov, 12:00. **Needs:** S1. **Blocks:** X2. **Reviewer:** Jonathan.

1. `FxApp` with `showTitle`, `showLevel`, `showLevelComplete` and `showGameOver`, each
   switching the window's scene.
2. Placeholder `TitleScreen`, `LevelCompleteScreen` and `GameOverScreen` that Xaryab
   replaces in X2, and a placeholder level screen.
3. Esc quits from any screen.

**Done when:** the four placeholder screens can be reached with buttons. Tell Xaryab
when it is merged.

### S8. Level screen and input

**Due:** Tue 24 Nov, end of day. **Needs:** S5, S7, J7 (`GameController`, due Tue 24,
12:00). **Blocks:** J9, X5, S9. **GDD:** 5. **Reviewer:** Jonathan.

1. `FxLevelView.render`: draw the banks, water and every element as simple coloured
   circles at their positions and sizes.
2. `toPixels` and `toFraction` with one scale factor for both axes. The play area keeps
   its 3:2 shape, centred, with margins when the window has another shape.
3. The side panel: health, energy, rank and XP, turn number out of the limit, the throw
   bag, the walk, hop and swim choice, **End turn** and **Reset**.
4. `FxInputHandler`: pick an object or a move, click a point, build a `TurnAction`, send
   it with `GameController.submit`. **Reset** calls `GameController.reset`.
5. `FxLevelView.onEvent` shows a one-line message for each event ("A snake bit the frog:
   3 HP").
6. A test for `toPixels` and `toFraction`: a point survives the round trip, and both
   axes use the same scale.

**Done when:** level 1 can be played with the mouse from the first throw to winning.

### S9. Levels 3 and 4, and the animated river

**Due:** levels Wed 25 Nov, 12:00; river animation Wed 25 Nov, end of day.
**Needs:** J1, J5, S8. **Blocks:** X5. **Reviewer:** Jonathan for the river, Xaryab
plays the levels.

1. Level 3 introduces predators: one snake on the far bank and one fish [proposed].
2. Level 4 needs swimming, which the frog unlocks at rank 3 [proposed].
3. Win each level yourself with its starting bag before merging.
4. The flowing river background: an animation behind the elements, with no effect on
   the rules.

**Done when:** both levels load, can be won, and are merged by 12:00 for playtesting.

### S10. Balancing session

**When:** Wed 25 Nov, 19:00, all three. **Needs:** X5's playtest notes.

1. Go through Xaryab's notes level by level.
2. Change numbers only in `Balance` and the level files; merge the changes that evening.

### S11. Feature freeze

**Due:** Thu 26 Nov, 18:00. **Reviewer:** Jonathan.

1. Fix the bugs from the playtest notes that are in your classes.
2. Complete the Javadoc on every public class and method you wrote.
3. Turn on JaCoCo's 80% line-coverage check for the model, and add tests where you are
   short.
4. Merge everything that is ready. Nothing new is added after 18:00, only fixes.
5. Tag `main` as `v1.0-rc`.

### S12. Rehearsal

**When:** Fri 27 Nov.

1. Prepare your part of the presentation, about 3 minutes: the turn engine (Template
   method), ripples and chasing (Strategy), and events (Observer), with one test of each.
2. Who assembles the slides is still to be decided.
3. Run the full demo once as a team, from a fresh clone.

### S13. Delivery

**When:** Sat 28 Nov.

1. From a fresh clone: `mvn verify` passes, the coverage report is produced, and
   `mvn javafx:run` starts the game.
2. Tag `main` as `v1.0` and deliver.

## Every day

- Merge reviewed pull requests and keep `main` building and green.
- Review Jonathan's pull requests and Xaryab's screens (X2) within 12 hours.
