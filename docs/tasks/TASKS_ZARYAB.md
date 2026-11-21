# Tasks - Zaryab

Ripple Pond, Saturday 21 to Saturday 28 November 2026.
Load: light, about 10 hours in total. Each task shows its estimated time.

Read first: GDD Draft 3 sections 2.5, 5, 8.2 and 9.3. Task IDs starting with S are
Stephan's (`TASKS_STEPHAN.md`) and J are Jonathan's (`TASKS_JONATHAN.md`).

None of your tasks blocks anyone else, so a late task does not stop the team. Still,
say so the same day if you will be late.

## How we work

- **Due** means merged into `main` by that time. "End of day" means 22:00.
- One branch and one pull request per task, named like `zaryab/z1-food`.
- A pull request is the classes, their unit tests and their Javadoc together, with CI
  green. It is checked against the principles in GDD section 8.2.
- Every pull request is reviewed by one teammate before it is merged. Reviews are done
  within 12 hours [proposed].
- Every tunable number (food amounts) goes in the `Balance` class (task S2), never
  inline.

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

## Your classes and files

| Package or folder [proposed] | Classes or files |
| --- | --- |
| `ripplepond.model.element.pickup` | `Food`, `Fly`, `Minnow`, `Breadcrumb` |
| `ripplepond.view` | `TitleScreen`, `LevelCompleteScreen`, `GameOverScreen` |
| `src/main/resources` | `levels/level5.txt`, `how-to-play.txt` |
| `docs/` | `PLAYTEST_NOTES.md`, and the README at the repository root |

## Tasks

### Z0. Get set up (0.5 hour)

**Due:** Sun 22 Nov, 12:00. **Needs:** S1 (Sat 21, 12:00) and J1 (Sat 21, 12:00).

1. Clone the repository and run `mvn verify` once S1 is merged.
2. Read the GDD sections listed at the top, and `docs/LEVEL_FORMAT.md` (J1).
3. Review J1: comment on the level format pull request with anything unclear to you as
   a level writer.

### Z1. Food (2.5 hours)

**Due:** Mon 23 Nov, end of day. **Needs:** J2 (`Pickup` and `Frog`, Sun 22).
**GDD:** 2.5. **Reviewer:** Jonathan.

1. `Food`, abstract, extending `Pickup`. Its `collect(frog, level)` is the fixed part:
   call `applyTo(frog)`, remove the food from the level, publish `FOOD_EATEN`. Make
   `collect` `final` and `applyTo` abstract: this is the Template method pattern.
2. `Fly` (+2 HP and +4 energy), `Minnow` (+5 energy) and `Breadcrumb` (+3 HP), each only
   implementing `applyTo`. Put the amounts in `Balance`.
3. Add `fly`, `minnow` and `breadcrumb` to the `ElementFactory` once J5 is merged
   (Mon 23).
4. Tests: each food restores the right stats by the right amount, never above the
   maximum, and is gone from the level after it is eaten.

**Done when:** the three foods work in a test level.

### Z2. Title, level complete and game over screens (3 hours)

**Due:** Wed 25 Nov, 12:00. **Needs:** S7 (screen switching, Mon 23, 12:00); J7
(`GameSession` data, Tue 24, 12:00). **GDD:** 5. **Reviewer:** Stephan.

1. `TitleScreen`: **Play**, **How to Play** (shows the text from Z3) and **Quit**.
2. `LevelCompleteScreen`: turns used, health and energy left, XP gained, the new rank
   if there is one, and **Next level**.
3. `GameOverScreen`: why the level was lost (health, energy or turn limit), **Retry**
   and **Back to title**.
4. Replace Stephan's placeholders from S7. Until J7 is merged, show made-up numbers;
   then read the real ones from the `Level` and `GameSession`.
5. Buttons call `GameController` (J7) or `FxApp`; the screens hold no game rules.

**Done when:** you can go from the title screen to a level and back through both end
screens.

### Z3. How to Play text (0.5 hour)

**Due:** Tue 24 Nov, end of day. **Needs:** nothing. **GDD:** 3, 4.2, 5.

1. Write `src/main/resources/how-to-play.txt`: the goal, one turn's five steps, walk,
   hop and swim and their costs, and how to win and lose. At most 15 short lines.
2. Use the same text for the How to Play section of the README if it helps.

### Z4. Level 5 (1 hour)

**Due:** Wed 25 Nov, 12:00. **Needs:** J1, J5 (loader, Mon 23). **GDD:** 2.6.
**Reviewer:** Jonathan.

1. Write `src/main/resources/levels/level5.txt` following `docs/LEVEL_FORMAT.md`: the
   hardest level, with all three predators and at least one food of each kind
   [proposed].
2. By level 5 the frog will usually be rank 3 or 4, so swimming is available.
3. Win it yourself with its starting bag before merging. If the level screen (S8) is
   not ready, ask Jonathan for a test that loads it.

### Z5. Playtest all five levels (1.5 hours)

**When:** Wed 25 Nov, 14:00 to 18:00. **Needs:** S8, J9, S9, Z4.

1. Play levels 1 to 5 in order from the title screen.
2. For each level, note in `docs/PLAYTEST_NOTES.md`: won or lost, turns used, health
   and energy left, where you got stuck, anything too easy or too hard, and every bug
   with the steps to reproduce it.
3. Merge the notes by 18:00. They are the agenda for the balancing session.

### Z6. Balancing session (0.5 hour)

**When:** Wed 25 Nov, 19:00, all three.

1. Walk the team through your notes, level by level.

### Z7. Final README (0.5 hour)

**Due:** Thu 26 Nov, 18:00. **Reviewer:** Jonathan.

1. Update the status table, the build commands (checked against `pom.xml`), the team
   table and the milestones.
2. Replace the mock-up with a real screenshot of the level screen, or add one next to it.

### Z8. Rehearsal (0.5 hour)

**When:** Fri 27 Nov.

1. Prepare your part of the presentation, about 2 minutes: food (Template method) with
   one of its tests, and the screens.
2. Take part in one full run of the demo with the team.
