# Tasks - Jonathan

Ripple Pond, Saturday 21 to Saturday 28 November 2026.
Load: full. Role: the frog and everything it uses, level files, and the design
documents.

Read first: GDD Draft 3 sections 2, 3.2, 4, 8 and 9, and UML views 1, 2 and 4. Task IDs
starting with S are Stephan's (`TASKS_STEPHAN.md`) and Z are Zaryab's
(`TASKS_ZARYAB.md`).

## How we work

- **Due** means merged into `main` by that time. "End of day" means 22:00.
- One branch and one pull request per task, named like `jonathan/j2-elements`.
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

Your heaviest days are Monday 23 (J5 and J6) and Tuesday 24 (J7 at noon, then J8). If
either slips, say so on Sunday evening so the team can move work.

## Your classes

| Package [proposed] | Classes |
| --- | --- |
| `ripplepond.model.element` | `AbstractElement`, `Frog`, `FloatingObject`, `ThrownObject`, `ThrowableKind`, `LilyPad`, `Turtle`, `Reed` |
| `ripplepond.model.element.pickup` | `Pickup`, `ThrowableIcon` |
| `ripplepond.model.movement` | `MoveMode`, `Walk`, `Hop`, `Swim` |
| `ripplepond.model.level` | `LevelState`, `InProgress`, `Won`, `Lost`, `LoseReason`, `EndRules`, `ThrowBag` |
| `ripplepond.model.progress` | `Rank`, `FrogProgress`, `ExperienceRules`, `GameSession` |
| `ripplepond.model.io` | `LevelLoader`, `ElementFactory`, `LevelFormatException` |
| `ripplepond.controller` | `GameController` |
| `src/main/resources/levels` | `level1.txt`, `level2.txt` |
| `docs/` | `LEVEL_FORMAT.md`, the GDD, the UML and their PDFs, `CREDITS.md` |

## Tasks

### J1. Level file format

**Due:** Sat 21 Nov, 12:00. **Needs:** nothing (no code). **Blocks:** J5, S9, Z4.
**Reviewers:** Stephan and Zaryab, who both write levels.

1. Write `docs/LEVEL_FORMAT.md`: one line per item, a keyword first, then numbers in
   play-area widths (x from 0 to 1, y from 0 to 0.667). For example `frog 0.50 0.62`,
   `lilypad 0.30 0.40 0.08`, `bag twig 2`, `turns 40`.
2. Cover everything GDD section 2.6 lists: both bank depths, reed clumps, every element
   type with position and diameter, the frog's start, throwable icons, the starting bag
   and the turn limit.
3. Say how comments (`#`) and blank lines are handled, and what counts as an error.
4. Include one complete example level.

**Done when:** the other two can write a level file from the document alone.

### J2. Element hierarchy and frog health and energy

**Due:** `Reed` Sun 22 Nov, 12:00; everything else Sun 22 Nov, end of day.
**Needs:** S2. **Blocks:** S3 (needs `Reed` first), J3, J4, J6, J8, Z1.
**GDD:** 2.1 to 2.3, 4.1. **Reviewer:** Stephan.

1. `Reed` first: a solid circle that never moves, with `liesBetween(from, to)` true when
   the line segment between the points passes through the clump. Merge it by 12:00.
2. `AbstractElement`: position, size (the diameter), `getRadius`, `touches` (centres no
   further apart than the sum of the radii), `distanceTo`, and the template method
   `reactTo`, which calls `ripple.passes` and then `onRipple`. The default `onRipple`
   does nothing.
3. `Frog`: health and energy with maximums taken from its rank; `takeDamage` never
   below 0; `restoreHealth` and `restoreEnergy` never above the maximum; `isAlive`,
   `hasEnergy` and `standingOn`. Diameter 0.04 [proposed].
4. `FloatingObject`: `covers(point)`, a `rider`, and `push(direction, distance, pond)`
   that moves the rider with it. Leave blocking for J8.
5. `ThrownObject` with `ThrowableKind` (twig 0.05, plank 0.10, log 0.18), and `LilyPad`.
6. `Turtle` with an `awake` flag; its ripple reaction comes in J8.
7. `Pickup`: abstract, with `collect(frog, level)`. Zaryab's food builds on it.
8. Tests for every rule above, especially the clamps and the riding rule.

**Done when:** every class in UML view 1 except predators and food exists and is
tested.

### J3. Level states and end rules

**Due:** Sun 22 Nov, end of day. **Needs:** S2, J2. **Blocks:** S5. **GDD:** 3, 3.2.
**Reviewer:** Stephan.

1. `LevelState` with `InProgress`, `Won` and `Lost`; `Lost` carries a `LoseReason`
   (`NO_HEALTH`, `NO_ENERGY`, `TURN_LIMIT`).
2. `InProgress.playTurn` runs the `TurnEngine` (S5) and then asks `EndRules`; `Won` and
   `Lost` refuse turns.
3. `EndRules.evaluate`: a win is checked right after the frog moves, and then predators
   do not act; health, energy and the turn limit are checked after predators act.
4. Tests: each lose reason; reaching the far bank with the last energy wins; an empty
   throw bag alone does not lose; a finished level refuses a turn.

**Done when:** every rule in GDD section 3.2 is tested.

### J4. Throw bag

**Due:** Sun 22 Nov, end of day. **Needs:** J2. **Blocks:** S5, J8.
**GDD:** 4.4. **Reviewer:** Stephan.

1. `ThrowBag`: `add`, `has`, `take` (refuses when none is left), `isEmpty`, and a count
   per kind.
2. Tests for each method, including taking from an empty bag.

### J5. Level loader and element factory

**Due:** Mon 23 Nov, end of day. **Needs:** J1, J2, S3. **Blocks:** J7, J9, S9, Z4.
**GDD:** 2.6. **Reviewer:** Stephan.

1. `ElementFactory.create(typeName, position, size)`: a map from names such as `log` or
   `lilypad` to the right class. An unknown name throws `LevelFormatException`.
2. Each author adds their own names when their classes land: Stephan adds `snake`,
   `bird` and `fish` (S6); Zaryab adds `fly`, `minnow` and `breadcrumb` (Z1).
3. `LevelLoader.load(path, progress)`: read the file, build the `Pond`, the frog with
   full health and energy for its rank, the `ThrowBag` and the turn limit, and return a
   `Level`.
4. Error messages give the line number and what is wrong.
5. Tests with small level files in `src/test/resources`: a valid level loads; a missing
   frog, an unknown name, a bad number and a position outside the play area are each
   rejected with a clear message.

**Done when:** the example level from J1 loads into a playable `Level`.

### J6. Movement

**Due:** Mon 23 Nov, end of day. **Needs:** J2, S3. **Blocks:** the full turn loop
(S5 uses a stub until this is merged). **GDD:** 4.2. **Reviewer:** Stephan.

1. `MoveMode` with `Walk`, `Hop` and `Swim`: 1, 2 and 4 energy per 0.10 travelled.
2. `maxDistance`: walk 0.10, hop the rank's hop range, swim 0.10.
3. `isLegal`: walk only within the bank or object the frog stands on; hop only to a
   surface within range; swim only through open water and only once the rank allows it.
4. `Frog.move` and `canAfford`: a move the frog cannot pay for is refused; the frog's
   `standingOn` is updated after the move.
5. After a move, the frog collects every `Pickup` it touches.
6. Tests: the cost grows with distance; the 1, 2 and 4 ratio; out of range refused;
   swim refused before rank 3; unaffordable move refused.

**Done when:** every movement rule in GDD section 4.2 is tested.

### J7. Experience, game session and controller

**Due:** Tue 24 Nov, 12:00. **Needs:** J5. **Blocks:** S8, Z2.
**GDD:** 4.3, 4.5, 4.6. **Reviewer:** Stephan.

1. `Rank` holding the table in GDD section 4.3 as data, with `forXp`.
2. `FrogProgress` and `ExperienceRules.xpFor`: 50 + 2 x HP left + 1 x energy left.
3. `GameSession`: the five level files in order, unlock the next level when one is won,
   `startLevel` builds a fresh frog for the current rank, `completeLevel` awards XP and
   publishes `RANK_GAINED` when the rank goes up.
4. `GameController`: `submit`, `reset` (reloads the level file and keeps XP) and
   `nextLevel`. Stephan's input handler and Zaryab's screens call it.
5. Tests: the XP formula, every rank boundary, unlocking in order, reset restores the
   level as loaded and keeps XP.

**Done when:** a test can win level 1 through `GameController` and see the XP and rank
change.

### J8. Turtle, push rules and throwable icons

**Due:** Tue 24 Nov, end of day. **Needs:** S4, J4. **GDD:** 2.2, 2.3, 3.1.
**Reviewer:** Stephan.

1. `Turtle.onRipple`: it wakes and moves away from the ripple's source, once per wake
   until the team decides (GDD section 10.1).
2. `FloatingObject.push` is blocked (the object does not move) when a reed clump, a bank
   or another object is in the way.
3. `ThrowableIcon.collect`: adds its kind to the `ThrowBag`, is removed from the level,
   and publishes `ICON_GATHERED`.
4. Tests for each rule, including a frog riding an object that is blocked.

### J9. Levels 1 and 2

**Due:** Wed 25 Nov, 12:00. **Needs:** J5, S8. **Blocks:** Z5.
**Reviewer:** Zaryab plays them.

1. Level 1 teaches throwing and hopping: no predators, a generous bag [proposed].
2. Level 2 adds lily pads, a turtle and a fly [proposed].
3. Win each level yourself with its starting bag before merging.

### J10. Balancing session

**When:** Wed 25 Nov, 19:00, all three. **Needs:** Z5's playtest notes.

1. Change numbers only in `Balance` and the level files.
2. Afterwards, update the numbers in the GDD to match.

### J11. Feature freeze and documents

**Due:** Thu 26 Nov, 18:00. **Reviewer:** Stephan.

1. Fix the bugs from the playtest notes that are in your classes.
2. Complete the Javadoc on every public class and method you wrote, then run
   `mvn javadoc:javadoc` for the whole project and ask owners to fix any warnings.
3. Add tests where your classes are below 80% line coverage.
4. Update the UML to match the code and the GDD to match the final rules and numbers.
   Export new PDF revisions with `tools\make-doc-pdfs.ps1`; keep the old ones.
5. Write `CREDITS.md` listing every third-party asset and its licence (or saying there
   are none).

### J12. Rehearsal

**When:** Fri 27 Nov.

1. Prepare your part of the presentation, about 3 minutes: the element hierarchy
   (Template method), moving (Strategy), the element factory (Factory) and level states
   (State), with one test of each.
2. Who assembles the slides is still to be decided.
3. Run the full demo once as a team, from a fresh clone.

### J13. Delivery

**When:** Sat 28 Nov.

1. Check that the final GDD and UML PDFs are in `docs/` and listed in the README.
2. Help Stephan with the final checks before the `v1.0` tag.

## Every day

- Review Stephan's pull requests and Zaryab's food (Z1) within 12 hours.
