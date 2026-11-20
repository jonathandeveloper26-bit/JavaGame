# Banner & Blade — Game Design Document

| | |
|---|---|
| **Status** | Draft 3, roles added; for Stephan's and Xaryab's review |
| **Date** | 18 November 2026 |
| **Delivery** | 28 November 2026 |
| **Team** | Jonathan, Stephan, Xaryab |
| **Platform** | Desktop, Java + JavaFX |

**How to read this draft.** Design choices come from the team interview. Anything marked **[proposed]** is a default filled in to make the document complete; the team has not decided it yet. All numbers are starting values for balancing, not tested ones.

---

## 1. Overview

### 1.1 Pitch

Banner & Blade is a single-player fantasy auto-battler. You are a warlord raising a warband: each round you draft a new unit or item, arrange your troops on your half of the battlefield, and then watch the fight play out on its own. Three rival warlords are doing the same. Lose a fight and your health drops; the last warlord standing wins.

### 1.2 Design pillars

1. **Decide before the fight, not during it.** All player skill is in drafting, positioning and equipping. Combat is automatic.
2. **Banners matter.** Support units that carry banners or rally the team are the identity of the game: protecting your banner and cutting down the enemy's is the central tactical question.
3. **Gameplay over graphics.** Simple sprites, readable board, clear numbers.
4. **Deterministic rules.** Given the same boards and the same random seed, a fight always ends the same way. This makes the game fair, debuggable and unit-testable.

### 1.3 Genre and perspective

- Genre: auto-battler (strategy), in a classic fantasy setting.
- View: 2D top-down on a square grid.
- Players: one human against three AI rivals.
- Session length: roughly 10 to 15 minutes per match **[proposed]**.

### 1.4 Originality

The game uses the mechanics of the auto-battler genre. All units, items, names, rules text and visuals are our own; nothing is taken from an existing game.

---

## 2. Match structure

### 2.1 Players and health

- Four warlords per match: the player and three AI rivals.
- Each starts with **40 health** **[proposed]**.
- A warlord at 0 health or less is eliminated.
- **Victory:** all three rivals are eliminated.
- **Defeat:** the player is eliminated. The match ends immediately.

### 2.2 Round loop

Every round has four phases:

| Phase | What happens |
|---|---|
| 1. Draft | Each warlord picks one card from an offer of three. |
| 2. Planning | The player places units and equips items. Untimed; ends when the player presses **Ready**. |
| 3. Combat | Warlords are paired and their boards fight automatically. |
| 4. Resolution | Losers take damage, eliminations are checked, results are shown. |

### 2.3 Pairing

- Living warlords are paired at random each round.
- A warlord cannot face the same opponent two rounds in a row, unless that opponent is the only one left.
- The player's fight is shown on screen. Other fights are resolved instantly by the same combat engine and only their results are shown.

**Ghost armies.** When three warlords are alive, one of them has no opponent. That warlord fights the *ghost* of the eliminated rival:

- When a rival is eliminated, their warband stays in the match as a ghost.
- A ghost keeps advancing as if its warlord were still alive: every round it drafts, grows with the team cap and equips items using its former owner's strategy. It is never weaker than a living rival at the same round.
- A ghost has no health. It cannot be eliminated and cannot win the match.
- Losing to a ghost costs health as normal (section 2.4). Beating it has no effect on the ghost.
- The ghost counts as an opponent for the no-repeat rule, so no warlord faces it two rounds in a row.
- When only two warlords are alive they fight each other every round; ghosts are not used.

### 2.4 Damage on losing a round

Damage to the loser = **stage damage + number of surviving enemy units** **[proposed]**.

| Rounds | Stage damage |
|---|---|
| 1–3 | 2 |
| 4–6 | 4 |
| 7 and later | 6 |

If a fight reaches the time limit (section 4.6), it is a draw and both warlords take the stage damage only.

---

## 3. Building a warband

### 3.1 The draft

There is no gold and no shop. Units and items come from drafts: three cards are shown, the warlord keeps one, the other two are discarded.

Schedule:

- **Round 1:** two unit drafts in a row, so every warlord starts with two units.
- **Every later round:** one unit draft.
- **Every even round (2, 4, 6…):** one additional item draft.

Offers are drawn at random from the full roster or item list. Every warlord gets their own independent offers. A warlord may skip a draft.

### 3.2 Team cap, board and bench

- The battlefield is **8 columns × 8 rows**. Each warlord owns the 8 × 4 half nearest to them and may only place units there.
- One unit per cell.
- The number of units a warlord may field grows with the round:

| Round | 1 | 2–3 | 4–5 | 6–7 | 8+ |
|---|---|---|---|---|---|
| Team cap | 2 | 3 | 4 | 5 | 6 |

- Units not on the board wait on the **bench** (8 slots **[proposed]**). If the bench is full, the player must dismiss a unit or skip the draft.
- Dismissing a unit removes it permanently and returns its items to the stash.

### 3.3 Planning phase controls

- Drag a unit between bench and board, or between board cells.
- Drag an item from the stash onto a unit to equip it; drag it off to unequip **[proposed: items can be moved freely during planning]**.
- Hover or click a unit to see its full sheet: stats, ability, items, active bonuses.
- Press **Ready** to start combat.

---

## 4. Combat rules

### 4.1 Time

Combat advances in fixed **ticks**. On screen, 1 tick is shown as 100 ms **[proposed]**, but the rules only ever count ticks, never real time.

### 4.2 Movement and reach

This is the key spatial rule of the game:

- **Movement is 4-directional.** A unit steps up, down, left or right, never diagonally.
- **Actions are 8-directional.** Attacks and abilities reach diagonally. Distance for range is the larger of the row and column differences (a range-1 unit can hit all 8 neighbours).

A unit that has a target but is out of range walks toward it along the shortest free path (pathfinding on the grid, treating occupied cells as blocked). If no path exists, it waits.

### 4.3 What a unit does each tick

1. If it has no living target, choose one using its targeting rule (section 5.2).
2. If its mana is full, cast its ability.
3. Otherwise, if the target is in range and its attack is ready, attack.
4. Otherwise, if its move is ready, step toward the target.

### 4.4 Damage, armour and shields

- Damage taken = raw damage × 100 / (100 + armour).
- There is a single damage type; ability damage is reduced by armour like attacks **[proposed]**.
- A **shield** is temporary extra health that absorbs damage first and expires after its duration.

### 4.5 Mana and abilities

- Every unit has exactly one ability and a mana bar that starts empty.
- A unit gains **10 mana per attack it makes** and **5 mana each time it is hit** **[proposed]**.
- At full mana the unit casts its ability in place of its next attack, and its mana returns to 0.

### 4.6 End of combat

- A side with no living units loses.
- If both sides still have units after **600 ticks**, the fight is a draw **[proposed]**.
- After combat, all units are restored to full health and returned to their planned positions.

### 4.7 Determinism

Units act in a fixed, documented order within a tick, ties (for example two equally near targets) are broken by a fixed rule, and every random choice comes from a seeded generator. The same inputs always give the same result.

---

## 5. Units

### 5.1 Stats

| Stat | Meaning |
|---|---|
| Health | Damage a unit can take before it dies. |
| Attack | Raw damage of a basic attack. |
| Attack interval | Ticks between basic attacks (lower is faster). |
| Range | Reach in cells, counted 8-directionally. |
| Armour | Reduces damage taken (section 4.4). |
| Move interval | Ticks between steps. |
| Max mana | Mana needed to cast the ability. |

### 5.2 Targeting rules

| Rule | Picks |
|---|---|
| Nearest | The closest enemy. Default. |
| Weakest | The enemy with the lowest current health. |
| Wounded ally | The ally with the lowest health percentage (used by healing). |

### 5.3 Roster (10 units)

The roster and abilities below are the agreed baseline. Numbers remain open for balancing.

Move interval is 4 ticks for every unit except the Rogue (3).

| Unit | Role | Health | Attack | Interval | Range | Armour | Mana | Targets |
|---|---|---|---|---|---|---|---|---|
| Shieldbearer | Tank | 900 | 40 | 12 | 1 | 40 | 80 | Nearest |
| Swordsman | Fighter | 650 | 65 | 9 | 1 | 25 | 60 | Nearest |
| Pikeman | Fighter | 600 | 60 | 10 | 2 | 20 | 60 | Nearest |
| Archer | Ranged | 450 | 55 | 8 | 4 | 10 | 70 | Nearest |
| Fire Mage | Caster | 400 | 40 | 12 | 4 | 5 | 90 | Nearest |
| Cleric | Healer | 450 | 30 | 12 | 3 | 10 | 70 | Nearest |
| Rogue | Assassin | 450 | 70 | 7 | 1 | 10 | 50 | Weakest |
| Standard Bearer | Banner | 550 | 35 | 12 | 1 | 20 | 100 | Nearest |
| Banner Warden | Banner | 700 | 30 | 12 | 1 | 30 | 100 | Nearest |
| War Drummer | Support | 500 | 35 | 12 | 3 | 15 | 80 | Nearest |

### 5.4 Abilities

| Unit | Ability | Effect |
|---|---|---|
| Shieldbearer | Shield Wall | Gains a 300 shield for 40 ticks. |
| Swordsman | Cleave | Deals 150% attack damage to every enemy adjacent to the Swordsman. |
| Pikeman | Impale | Deals 200% attack damage to its target and stuns it for 10 ticks. |
| Archer | Volley | Deals 100% attack damage to the three nearest enemies. |
| Fire Mage | Fireball | Deals 220 damage to its target and to all enemies in the 8 cells around it. |
| Cleric | Mend | Heals the most wounded ally for 250. |
| Rogue | Shadowstep | Teleports to a free cell next to the weakest enemy and deals 250% attack damage. |
| Standard Bearer | Rally | Doubles its banner's bonus for 40 ticks. |
| Banner Warden | Hold the Line | Every ally gains a 120 shield for 30 ticks. |
| War Drummer | Battle March | Every ally attacks 30% faster for 40 ticks. |

### 5.5 Banners

A banner is a team-wide bonus that lasts for as long as its bearer is alive. When the bearer dies the banner falls and the bonus is lost at once.

| Banner | Carried by | Bonus to every ally |
|---|---|---|
| War Banner | Standard Bearer | +15% attack |
| Aegis Banner | Banner Warden | +15 armour |

- Two copies of the same banner do not stack. (This rule is for banners carried by banner units; the Tattered Pennant item follows the item stacking rule in section 6.1.)
- Different banners do stack.
- The bearer benefits from its own banner.

---

## 6. Items

### 6.1 Rules

- Items come only from item drafts (section 3.1).
- A unit may hold **any number of items**. There is no slot limit.
- **Everything stacks.** Stat bonuses add together, and every copy of an effect item applies its own effect. Two Vampiric Blades give 40% lifesteal; two Phoenix Feathers give two revives; two Ember Brands apply two separate burns.
- Known risk: piling effect items onto one unit can create extreme units. This is accepted as part of the game's fun and is controlled through the item numbers during balancing, not through a rule.

The item list below is the agreed baseline. Numbers remain open for balancing.

### 6.2 Stat items

| Item | Bonus |
|---|---|
| Iron Sword | +15 attack |
| Whetstone | 15% faster attacks |
| Chainmail | +20 armour |
| Leather Jerkin | +150 health |
| Sigil of Focus | Starts combat with 20 mana |

### 6.3 Effect items

| Item | Effect |
|---|---|
| Vampiric Blade | The holder heals for 20% of the damage it deals. |
| Ember Brand | The holder's attacks burn the target: 8 damage every 10 ticks for 30 ticks. |
| Bulwark Crest | The holder starts combat with a 200 shield. |
| Spiked Pauldrons | Each time the holder is hit by an attack, the attacker takes 15 damage. |
| Phoenix Feather | The first time the holder would die in a fight, it returns with 30% health instead. |
| Tattered Pennant | The holder carries a lesser banner: every ally gets +5% attack and +5 armour while the holder lives. Stacks with real banners and with other Pennants. |

---

## 7. AI rivals

Each rival drafts, positions and equips by the same rules as the player, using its own strategy. All three run on the same code path as the player's warband; only the decision-making differs.

| Rival | Style | Drafting | Positioning | Items |
|---|---|---|---|---|
| **The Vanguard** | Aggressive | Prefers fighters, ranged units and the Rogue. | Pushes units to the front rows. | Stacks everything on its highest-attack unit. |
| **The Bulwark** | Defensive | Prefers tanks, banner bearers and the Cleric. | Tanks in front, banner bearers and ranged units in the back corners. | Spreads items across the front line. |
| **The Gambler** | Random | Picks at random. | Places at random. | Equips at random. |

An eliminated rival's strategy keeps running for its ghost army (section 2.3), so the same code drives living rivals and ghosts.

Rival names and exact preferences are **[proposed]**.

---

## 8. Interface

### 8.1 Screens

1. **Main menu** — New Match, How to Play, Quit.
2. **Match screen** — used for draft, planning and combat (layout below).
3. **Round result** — win, loss or draw, damage taken, results of the other fights.
4. **End screen** — Victory or Defeat, with a button back to the menu.

### 8.2 Match screen layout

- **Centre:** the 8 × 8 battlefield, enemy half at the top, player half at the bottom.
- **Bottom:** the bench and the item stash.
- **Left:** the four warlords with health, and who the player faces this round.
- **Right:** the selected unit's sheet (stats, ability, items, active bonuses).
- **Top:** round number, current phase, team cap (for example "4 / 5 units").
- **Overlay during the draft:** three cards with full details and a Skip button.

### 8.3 In combat

- Each unit shows a health bar, a mana bar and a shield overlay.
- Banner bearers show a banner icon; a falling banner is signalled visibly.
- Simple move, attack and ability animations; floating damage numbers **[proposed]**.

### 8.4 Art and audio

- JavaFX with a free-licensed fantasy sprite pack. The pack and its licence are recorded in a `CREDITS.md` file.
- Audio is out of scope for the core game.

---

## 9. Scope

### 9.1 Core (must ship)

- Full match loop: draft, planning, combat, resolution, victory and defeat.
- 8 × 8 board, bench, drag and drop.
- 10 units with one ability each, banners included.
- 11 items with unlimited slots.
- Tick-based automatic combat with pathfinding.
- 3 AI rivals with distinct strategies, pairing rule, off-screen fights, ghost armies.

### 9.2 Stretch goals (only if the core is done and tested)

1. **Traits:** each unit gets one or two traits (for example *Order*, *Arcane*, *Shadow*); fielding enough units of a trait grants a team bonus.
2. **Merging:** three copies of a unit combine into a stronger version.
3. Sound effects and music.
4. Save and resume a match.

The core design leaves room for the first two: units carry a (currently empty) list of traits, and banners, items and ability buffs all go through one shared bonus system that traits can reuse.

### 9.3 Out of scope

Gold, shop and interest economy; online or hot-seat multiplayer; hex grid; timed planning; multiple damage types.

---

## 10. Technical outline

This section is a summary. The [UML diagrams](UML.md) are a separate deliverable and are the authority on class design.

### 10.1 Tools

Java 21, JavaFX, Maven, JUnit 5, JaCoCo for coverage, Javadoc, GitHub Actions for automated test runs on every push.

### 10.2 Architecture

- **Game model and engine:** all rules. No JavaFX imports, so it runs in unit tests and powers the instant off-screen fights.
- **View:** JavaFX screens that draw the model's state.
- **Controller:** turns player input into model actions.

### 10.3 Where the design patterns appear

| Pattern | Use in the game |
|---|---|
| Strategy | Targeting rules; AI rival behaviour. |
| State | Match phases (draft, planning, combat, resolution, game over). |
| Factory | Creating units and items from their definitions; building draft offers. |
| Observer | The view listens to combat and match events (damage dealt, unit died, banner fell). |
| Decorator or modifier chain | Stat bonuses from items, banners and temporary buffs. |
| Template method | The shared "act this tick" sequence, with abilities as the varying step. |

### 10.4 What the unit tests must cover

- Damage, armour and shield arithmetic.
- Every ability and every item effect.
- Item stacking: several copies of the same stat or effect item on one unit each apply.
- Banner rules: applies team-wide, does not stack with itself, ends on the bearer's death.
- Movement is 4-directional; range is 8-directional; pathfinding around obstacles.
- Combat end conditions, including the tick limit draw.
- Draft schedule, team cap and bench limits.
- Pairing: no repeat opponent, the last-opponent exception.
- Ghost armies: created on elimination, keep drafting each round, deal damage but take none, unused when two warlords remain.
- Loss damage, elimination, victory and defeat.
- Determinism: the same seed and boards give the same result.

---

## 11. Plan

### 11.1 Roles

**Status:** written with Jonathan on 18 November; Stephan and Xaryab have not confirmed it yet.

The team's two goals are to gain programming experience and to ship a good game. So the work is organised in three layers: every critical piece has one owner, and everyone writes some of each kind of content.

| Member | Load | Summary |
|---|---|---|
| Jonathan | Full | Combat core, plus a full share of content. |
| Stephan | Full | Match core and app shell, plus a full share of content. |
| Xaryab | Light, about 10 hours in total | Content only, chosen so that nothing else waits on it. |

#### Layer 1 — Foundations (one owner each, due 21 November)

These are the frameworks that all content plugs into.

| Foundation | Owner | Contents |
|---|---|---|
| Combat core | Jonathan | Board and pathfinding; the `Unit` base class; damage, armour and stat modifiers; the status effect, banner and `Item` base classes; stat items and the item factory; the fight loop, teams and combat events. |
| Match core and app shell | Stephan | Match phases; warlords and warbands; draft offers; `MatchRules`; the rival framework (`AiWarlord`, `RivalStrategy`); the application window and match screen; board, bench and stash with drag and drop. |
| Pairing | Jonathan | `PairingService`, including the no-repeat rule and ghost armies. |

On 18–19 November the shared interfaces (`CombatContext`, `CombatListener`, `MatchListener`, `Warband`) are agreed and committed as empty shells, so that neither foundation waits for the other.

Each foundation owner also builds one worked example of the content that plugs into it, with tests and Javadoc, for the others to copy: the Shieldbearer and one effect item (Jonathan), one view (Stephan).

#### Layer 2 — Content (shared)

Each piece is one class in its own file.

| Content | Jonathan | Stephan | Xaryab |
|---|---|---|---|
| Units | Shieldbearer, Swordsman, Standard Bearer, War Drummer | Pikeman, Rogue, Cleric, Banner Warden | Archer, Fire Mage |
| Effect items | Phoenix Feather, Tattered Pennant | Vampiric Blade, Ember Brand | Bulwark Crest, Spiked Pauldrons |
| Supporting classes | Shield, stat buff, nearest-enemy targeting | Stun, Burn, weakest-enemy and wounded-ally targeting | None |
| AI rival | The Vanguard | The Bulwark | The Gambler |
| Interface views | Combat animation, unit sheet, warlord panel, round result | Draft overlay (with board, bench and stash from layer 1) | Main menu with How to Play, end screen |

Xaryab's pieces need no new supporting classes and none is on the critical path: the menu and end screen can start at once, and the units and items can start as soon as the worked examples exist.

With this split every member has units (Template Method), an AI rival (Strategy), items, a view (Observer) and tests of their own to present at the individual review.

#### Layer 3 — One-off jobs **[proposed]**

| Job | Owner |
|---|---|
| Maven project, CI and coverage pipeline (18–19 November) | Jonathan |
| Integrator: merges daily and keeps `main` building and passing | Jonathan |
| Documents: GDD, UML and their PDF revisions | Jonathan |
| Sprite pack and `CREDITS.md` | Stephan |
| Demo script and slides (each member prepares their own part) | Stephan |
| Balancing pass on 25 November | All three together |

#### Working rules

1. **A merge is the class, its unit tests and its Javadoc together.** Nothing is merged without all three.
2. **Every merge is reviewed by one teammate.** Reviewing is how each member learns the parts they did not write.
3. **You must be able to explain every line you merge.** The team uses AI assistance to write code; this rule protects both the learning goal and the individual grade.

### 11.2 Milestones **[proposed]**

| Dates | Goal |
|---|---|
| 18–19 Nov | This document agreed. UML class diagram. Maven project, CI and coverage running. Shared interfaces committed as empty shells. |
| 20–22 Nov | Foundations and worked examples done by the 21st. Two teams can fight to the end in tests. Board and drag and drop work on screen. Shared content starts. |
| 23–24 Nov | Draft, items, AI rivals and the full match loop playable from menu to end screen. |
| 25 Nov | Sprites and animations in. First balancing pass. |
| 26 Nov | **Feature freeze.** Bug fixing, Javadoc complete, coverage reviewed, UML updated to match the code. |
| 27 Nov | Demo rehearsal and presentation preparation. |
| 28 Nov | Delivery. |

---

## 12. Open decisions for the team

1. **Roles.** Stephan and Xaryab to confirm or change section 11.1, including the one-off jobs.
2. **Sprite pack.** Owner: Stephan. Due before the 25 November art milestone. It needs a licence that allows reuse (CC0 is simplest), a top-down view, and at least 10 distinguishable characters. It does not block the UML or the engine.
3. **Balancing numbers.** Every value still marked **[proposed]** (health, damage, bench size, tick limits, mana gain) is tuned during the 25 November balancing pass.

Decided since draft 1:

- Tools (10.1).
- Roles organised as foundations, shared content and one-off jobs, replacing the earlier three-area split (11.1).
- Ghost armies for an odd number of warlords (2.3).
- Draft schedule and team cap as written (3.1, 3.2).
- All item effects stack, with no limit (6.1).
- Roster, abilities and item list accepted as the baseline (sections 5 and 6).
