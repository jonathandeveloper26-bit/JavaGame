# Ripple Pond - Level file format

Version 1, 21 November 2026. Owner: Jonathan. Read by `LevelLoader` (task J5).

Each level is a plain text file in `src/main/resources/levels/`, named `level1.txt` to
`level5.txt`. This document is everything you need to write one.

## 1. General rules

- Plain text, UTF-8, one item per line.
- A line starts with a **keyword**, followed by **values** separated by one or more
  spaces.
- `#` starts a comment, which runs to the end of the line. Blank lines and comment-only
  lines are ignored.
- Keywords and type names are lower case.
- Numbers use a dot for decimals (`0.25`), never a comma.
- Lines can be in any order, but the convention is: settings first, then elements, then
  the bag.

## 2. Units

These are the units of GDD section 2.1.

- Every position and size is in **play-area widths**.
- x runs from 0 (left edge) to 1 (right edge).
- y runs from 0 (top edge) to 0.667 (bottom edge). The play area is always 3:2.
- A size is a **diameter**: every element is a circle.

## 3. Level settings

Each of these appears **exactly once**, except `bag`.

| Line | Meaning |
| --- | --- |
| `turns 40` | The turn limit. A whole number above 0. |
| `bank far 0.10` | The far bank, the goal: a strip along the **top** edge, this deep. |
| `bank start 0.10` | The start bank: a strip along the **bottom** edge, this deep. |
| `frog 0.50 0.62` | The frog's start position (x, y). It must be on the start bank. |
| `bag twig 2` | Objects in the starting throw bag: a kind (`twig`, `plank` or `log`) and a whole number above 0. May appear several times; the counts add up. A level may have no `bag` line. |

Everything between the two banks is water. Every level uses this layout: the frog
always crosses from the bottom of the screen to the top.

## 4. Elements

An element line is:

```
<type> <x> <y> [diameter]
```

Any number of element lines may appear. The diameter is optional for every type: when
it is left out, the default size below is used, so a level only gives a diameter when
it wants a different size.

A **throwable icon** has the kind it gives before the position:

```
icon <kind> <x> <y> [diameter]
```

For example `icon plank 0.42 0.34` gives the frog one plank when it touches the icon.

### Default sizes

All sizes are diameters in play-area widths. In a 900-pixel-wide window, 0.01 is 9
pixels. The frog is 0.04 (GDD section 2.1) and is not placed with an element line.

| Type | What it is | Default diameter | Why this size |
| --- | --- | --- | --- |
| `twig` | Thrown object, small | 0.05 | GDD section 2.2. Just bigger than the frog: room to stand, not to walk. |
| `plank` | Thrown object, medium | 0.10 | GDD section 2.2. Room for a short walk. |
| `log` | Thrown object, large | 0.18 | GDD section 2.2. Room to walk along it. |
| `lilypad` | Lily pad | 0.08 | Twice the frog: an easy landing, smaller than a plank. |
| `turtle` | Sleeping turtle | 0.07 | Clearly bigger than the frog, so it reads as an obstacle. |
| `reed` | Reed clump | 0.10 | Big enough to cast a useful ripple shadow. |
| `icon` | Throwable icon | 0.04 | The frog's size: easy to see and to touch. |
| `fly` | Food | 0.025 | Small, so it takes a deliberate move to reach. |
| `minnow` | Food | 0.03 | A little larger than a fly. |
| `breadcrumb` | Food | 0.025 | Same as a fly. |
| `snake` | Predator | 0.07 | Larger than the frog, so it is easy to spot. |
| `bird` | Predator | 0.06 | Slightly smaller than a snake. |
| `fish` | Predator | 0.06 | Same as a bird. |

These are starting values [proposed], tuned at the balancing session on 25 November.
Each class owner adds their defaults to `Balance` when they write the class: the thrown
objects, lily pad, turtle, reed and icon (Jonathan), the food (Xaryab) and the
predators (Stephan).

## 5. Errors

The loader refuses the whole file, and its error message gives the **line number** and
the reason, when:

- a keyword, element type or throwable kind is unknown;
- a line has too few or too many values, or a value is not a number;
- a position is outside the play area (x not between 0 and 1, or y not between 0 and
  0.667);
- a diameter is 0 or less;
- `turns`, `frog`, `bank start` or `bank far` is missing, or appears twice;
- the two banks overlap;
- the frog is not on the start bank;
- `turns` or a `bag` count is not a whole number above 0.

The loader does **not** check that a level can be won. Win every level yourself before
merging it.

## 6. Example

This shows the format only; it is not a balanced level.

```
# Level 1 - First crossing (format example, not balanced)

turns 40
bank far 0.10        # top strip: y from 0 to 0.10
bank start 0.10      # bottom strip: y from 0.567 to 0.667
frog 0.50 0.62

lilypad 0.50 0.49 0.08
lilypad 0.42 0.34 0.08
reed    0.20 0.30 0.10
turtle  0.75 0.30 0.09
icon plank 0.42 0.34
fly     0.30 0.45

bag twig 2
bag plank 1
```
