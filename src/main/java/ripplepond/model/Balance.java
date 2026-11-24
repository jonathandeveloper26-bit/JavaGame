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

    public static final int RIPPLE_TURNS = 3;
    /** Diameter of the frog, in play-area widths. [proposed] */
    public static final double FROG_SIZE = 0.04;

    /** Diameter of a thrown twig, in play-area widths. [proposed] */
    public static final double TWIG_SIZE = 0.05;

    /** Diameter of a thrown plank, in play-area widths. [proposed] */
    public static final double PLANK_SIZE = 0.10;

    /** Diameter of a thrown log, in play-area widths. [proposed] */
    public static final double LOG_SIZE = 0.18;

    /** Default diameter of a lily pad in a level file, in play-area widths. [proposed] */
    public static final double LILY_PAD_SIZE = 0.08;

    /** Default diameter of a turtle in a level file, in play-area widths. [proposed] */
    public static final double TURTLE_SIZE = 0.07;

    /** Default diameter of a reed clump in a level file, in play-area widths. [proposed] */
    public static final double REED_SIZE = 0.10;

    /** Default diameter of a throwable icon in a level file, in play-area widths. [proposed] */
    public static final double ICON_SIZE = 0.04;

    /** Default diameter of a fly in a level file, in play-area widths. [proposed] */
    public static final double FLY_SIZE = 0.025;

    /** Default diameter of a small minnow in a level file, in play-area widths. [proposed] */
    public static final double MINNOW_SIZE = 0.03;

    /** Default diameter of a breadcrumb in a level file, in play-area widths. [proposed] */
    public static final double BREADCRUMB_SIZE = 0.025;

    /** Moves are charged per this distance travelled, in play-area widths (GDD 4.2). [proposed] */
    public static final double MOVE_UNIT = 0.10;

    /** Energy a walk costs per {@link #MOVE_UNIT} travelled. [proposed] */
    public static final double WALK_ENERGY_PER_UNIT = 1;

    /** Energy a hop costs per {@link #MOVE_UNIT} travelled: twice a walk. [proposed] */
    public static final double HOP_ENERGY_PER_UNIT = 2;

    /** Energy a swim costs per {@link #MOVE_UNIT} travelled: four times a walk. [proposed] */
    public static final double SWIM_ENERGY_PER_UNIT = 4;

    /** The furthest the frog can walk in one move, in play-area widths. [proposed] */
    public static final double WALK_MAX_DISTANCE = 0.10;

    /** The furthest the frog can swim in one move, in play-area widths. [proposed] */
    public static final double SWIM_MAX_DISTANCE = 0.10;

    /** XP every completed level gives, before health and energy are counted (GDD 4.3). [proposed] */
    public static final int XP_PER_LEVEL = 50;

    /** XP for each point of health the frog has left at the end of a level. [proposed] */
    public static final int XP_PER_HEALTH_LEFT = 2;

    /** XP for each whole point of energy the frog has left at the end of a level. [proposed] */
    public static final int XP_PER_ENERGY_LEFT = 1;

    /** Health a fly restores (GDD 2.5). [proposed] */
    public static final int FLY_HEALTH = 2;

    /** Energy a fly restores (GDD 2.5). [proposed] */
    public static final double FLY_ENERGY = 4;

    /** Energy a small minnow restores (GDD 2.5). [proposed] */
    public static final double MINNOW_ENERGY = 5;

    /** Health a breadcrumb restores (GDD 2.5). [proposed] */
    public static final int BREADCRUMB_HEALTH = 3;
}
