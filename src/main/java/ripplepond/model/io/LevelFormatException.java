package ripplepond.model.io;

/**
 * Thrown when a level file breaks the level format ({@code docs/LEVEL_FORMAT.md}).
 * <p>
 * The message says what is wrong and, when the problem is on one line, starts with that
 * line's number, for example {@code "line 7: unknown element type 'frogg'"}. A level
 * author can then find and fix the line without reading any Java.
 * <p>
 * It is a checked exception: a broken level file is something the game must expect and
 * handle (for example by showing the message), not a programming error.
 */
public class LevelFormatException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int lineNumber;

    /**
     * Creates an exception for a problem with the file as a whole, such as a missing
     * {@code frog} line.
     *
     * @param message what is wrong
     */
    public LevelFormatException(String message) {
        super(message);
        this.lineNumber = 0;
    }

    /**
     * Creates an exception for a problem on one line.
     *
     * @param lineNumber the number of the line, counting from 1
     * @param message    what is wrong on that line
     */
    public LevelFormatException(int lineNumber, String message) {
        super("line " + lineNumber + ": " + message);
        this.lineNumber = lineNumber;
    }

    /**
     * Returns the number of the line with the problem.
     *
     * @return the line number, counting from 1, or 0 when the problem is not on one line
     */
    public int getLineNumber() {
        return lineNumber;
    }
}
