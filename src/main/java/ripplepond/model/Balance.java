package ripplepond.model;

/**
 * Tunable game numbers. Change values here only, so balancing touches one file.
 */
public final class Balance {

    private Balance() {
    }

    /** Width of the play area, in play-area units. */
    public static final double PLAY_AREA_WIDTH = 1;

    /** Height of the play area: 2/3, so the area is exactly 3:2. */
    public static final double PLAY_AREA_HEIGHT = 2.0 / 3.0;

    /** Turns a ripple stays active. [proposed] */
    public static final int RIPPLE_LIFETIME_TURNS = 3;

    /** How far a ripple's radius grows each turn. [proposed] */
    public static final double RIPPLE_GROWTH_PER_TURN = 0.08;

    /** How far a floating object moves when a ripple pushes it. [proposed] */
    public static final double RIPPLE_PUSH_DISTANCE = 0.08;

    /** How many turns a ripple keeps spreading before it fades (GDD 3.1). */

}
