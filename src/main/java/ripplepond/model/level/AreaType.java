package ripplepond.model.level;

/**
 * The kinds of area in a pond, listed from the top of the play area to the bottom.
 */
public enum AreaType {

    /** The far bank, at the top of the play area. */
    FAR_BANK,

    /** Open water between the two banks. */
    WATER,

    /** The bank the frog starts on, at the bottom of the play area. */
    START_BANK
}