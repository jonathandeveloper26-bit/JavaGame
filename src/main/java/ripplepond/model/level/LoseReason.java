package ripplepond.model.level;

/**
 * Why a level was lost (GDD section 3.2). An empty throw bag is not a reason: the frog
 * may still walk or hop to the far bank.
 */
public enum LoseReason {

    /** The frog's health reached 0. */
    NO_HEALTH,

    /** The frog's energy reached 0 before it reached the far bank. */
    NO_ENERGY,

    /** The last turn allowed was played and the frog is not on the far bank. */
    TURN_LIMIT
}
