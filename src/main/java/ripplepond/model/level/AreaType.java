package ripplepond.model.level;

/**
 * The kinds of area in a pond, from the top of the play area to the bottom.
 */
public enum AreaType {

    /** The bank the frog starts on. */
    START_BANK,

    /** Open water, where the frog can swim once its rank allows it. */
    WATER,

    /** The bank the frog must reach to win. */
    FAR_BANK
}