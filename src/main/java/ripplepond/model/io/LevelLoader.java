package ripplepond.model.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.AreaType;
import ripplepond.model.level.InProgress;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.level.ThrowBag;
import ripplepond.model.progress.Rank;

/**
 * Reads a level file and builds a playable {@link Level} (GDD section 2.6). The format
 * is described in {@code docs/LEVEL_FORMAT.md}.
 * <p>
 * The whole file is refused at the first problem, with a {@link LevelFormatException}
 * whose message gives the line number and what is wrong. The loader does not check that
 * a level can be won.
 * <p>
 * Element types come from an {@link ElementFactory}, so a new type only needs to be
 * registered there. Throwable icons ({@code icon <kind> <x> <y>}) are added in J8.
 */
public class LevelLoader {

    /** A decimal number written with a dot: {@code 1}, {@code 0.25}, {@code .5}, {@code -2}. */
    private static final Pattern DECIMAL = Pattern.compile("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)");

    /** A whole number of digits only: {@code 3}, {@code 40}. */
    private static final Pattern WHOLE = Pattern.compile("\\d+");

    private final ElementFactory factory;

    /**
     * Creates a loader that knows every element type written so far.
     */
    public LevelLoader() {
        this(new ElementFactory());
    }

    /**
     * Creates a loader that uses a chosen factory.
     *
     * @param factory turns element type names into elements
     * @throws NullPointerException if {@code factory} is {@code null}
     */
    public LevelLoader(ElementFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    /**
     * Loads a level file from the game's resources, such as {@code levels/level1.txt}.
     *
     * @param path the path of the file inside the resources folder
     * @param rank the frog's rank, which sets its health and energy
     * @return the level, at turn 0 and in progress
     * @throws LevelFormatException if the file is missing, cannot be read or breaks the
     *                              level format
     * @throws NullPointerException if {@code path} or {@code rank} is {@code null}
     */
    public Level load(String path, Rank rank) throws LevelFormatException {
        Objects.requireNonNull(path, "path");
        InputStream stream = LevelLoader.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new LevelFormatException("level file not found: " + path);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return load(reader, rank);
        } catch (IOException e) {
            throw new LevelFormatException("could not read " + path + ": " + e.getMessage());
        }
    }

    /**
     * Loads a level from any text source. The caller closes the reader.
     *
     * @param reader the level file's text
     * @param rank   the frog's rank, which sets its health and energy
     * @return the level, at turn 0 and in progress
     * @throws LevelFormatException if the text cannot be read or breaks the level format
     * @throws NullPointerException if {@code reader} or {@code rank} is {@code null}
     */
    public Level load(Reader reader, Rank rank) throws LevelFormatException {
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(rank, "rank");
        LevelDraft draft = new LevelDraft();
        BufferedReader lines = new BufferedReader(reader);
        try {
            String text;
            int lineNumber = 0;
            while ((text = lines.readLine()) != null) {
                lineNumber++;
                readLine(draft, lineNumber, text);
            }
        } catch (IOException e) {
            throw new LevelFormatException("could not read the level: " + e.getMessage());
        }
        return draft.build(rank);
    }

    /**
     * Reads one line into the draft. Comments and blank lines are skipped.
     */
    private void readLine(LevelDraft draft, int lineNumber, String text) throws LevelFormatException {
        int comment = text.indexOf('#');
        String content = (comment >= 0 ? text.substring(0, comment) : text).trim();
        if (content.isEmpty()) {
            return;
        }
        Line line = new Line(lineNumber, content.split("\\s+"));
        switch (line.keyword()) {
            case "turns" -> draft.setTurns(line);
            case "bank" -> draft.setBank(line);
            case "frog" -> draft.setFrog(line);
            case "bag" -> draft.addToBag(line);
            case "icon" -> throw line.error("throwable icons are not available yet (J8)");
            default -> readElement(draft, line);
        }
    }

    /**
     * Reads an element line: {@code <type> <x> <y> [diameter]}.
     */
    private void readElement(LevelDraft draft, Line line) throws LevelFormatException {
        String type = line.keyword();
        if (!factory.knows(type)) {
            throw line.error("unknown keyword or element type '" + type + "'");
        }
        line.requireValues(2, 3);
        Vec2 position = line.position(1);
        double size = line.valueCount() == 3 ? line.size(3) : factory.defaultSize(type);
        draft.addElement(factory.create(type, position, size));
    }

    /**
     * One line of the file, split into words, with helpers that read its values and
     * report problems with the line number.
     */
    private record Line(int number, String[] words) {

        String keyword() {
            return words[0];
        }

        /** Number of values after the keyword. */
        int valueCount() {
            return words.length - 1;
        }

        LevelFormatException error(String message) {
            return new LevelFormatException(number, message);
        }

        void requireValues(int count) throws LevelFormatException {
            requireValues(count, count);
        }

        void requireValues(int min, int max) throws LevelFormatException {
            int count = valueCount();
            if (count < min || count > max) {
                String expected = min == max ? String.valueOf(min) : min + " or " + max;
                String values = max == 1 ? "value" : "values";
                throw error("'" + keyword() + "' needs " + expected + " " + values + ", found " + count);
            }
        }

        /** The value at an index, counting the keyword as 0. */
        String word(int index) {
            return words[index];
        }

        double decimal(int index) throws LevelFormatException {
            String word = word(index);
            if (!DECIMAL.matcher(word).matches()) {
                throw error("'" + word + "' is not a number (use a dot for decimals)");
            }
            return Double.parseDouble(word);
        }

        int wholeAboveZero(int index) throws LevelFormatException {
            String word = word(index);
            if (!WHOLE.matcher(word).matches()) {
                throw error("'" + word + "' is not a whole number above 0");
            }
            int value;
            try {
                value = Integer.parseInt(word);
            } catch (NumberFormatException e) {
                throw error("'" + word + "' is too large");
            }
            if (value <= 0) {
                throw error("'" + word + "' is not a whole number above 0");
            }
            return value;
        }

        /** Reads x at {@code index} and y after it, and checks both are in the play area. */
        Vec2 position(int index) throws LevelFormatException {
            double x = decimal(index);
            double y = decimal(index + 1);
            if (x < 0 || x > Pond.WIDTH || y < 0 || y > Pond.HEIGHT) {
                throw error("position (" + word(index) + ", " + word(index + 1)
                        + ") is outside the play area: x must be from 0 to 1, y from 0 to "
                        + String.format(java.util.Locale.ROOT, "%.4f", Pond.HEIGHT));
            }
            return new Vec2(x, y);
        }

        double size(int index) throws LevelFormatException {
            double size = decimal(index);
            if (!(size > 0)) {
                throw error("diameter '" + word(index) + "' must be greater than 0");
            }
            return size;
        }
    }

    /**
     * Everything read so far. Settings that must appear exactly once remember the line
     * that set them, so a second one can say where the first was.
     */
    private static final class LevelDraft {

        private int turns;
        private int turnsLine;
        private double farBank;
        private int farBankLine;
        private double startBank;
        private int startBankLine;
        private Vec2 frog;
        private int frogLine;
        private final ThrowBag bag = new ThrowBag();
        private final List<Element> elements = new ArrayList<>();

        void setTurns(Line line) throws LevelFormatException {
            line.requireValues(1);
            requireFirst(line, turnsLine);
            turns = line.wholeAboveZero(1);
            turnsLine = line.number();
        }

        void setBank(Line line) throws LevelFormatException {
            line.requireValues(2);
            String which = line.word(1);
            double depth = line.decimal(2);
            if (!(depth > 0)) {
                throw line.error("bank depth '" + line.word(2) + "' must be greater than 0");
            }
            switch (which) {
                case "far" -> {
                    requireFirst(line, farBankLine);
                    farBank = depth;
                    farBankLine = line.number();
                }
                case "start" -> {
                    requireFirst(line, startBankLine);
                    startBank = depth;
                    startBankLine = line.number();
                }
                default -> throw line.error("unknown bank '" + which + "': use 'far' or 'start'");
            }
        }

        void setFrog(Line line) throws LevelFormatException {
            line.requireValues(2);
            requireFirst(line, frogLine);
            frog = line.position(1);
            frogLine = line.number();
        }

        void addToBag(Line line) throws LevelFormatException {
            line.requireValues(2);
            ThrowableKind kind = kindNamed(line, line.word(1));
            int count = line.wholeAboveZero(2);
            for (int i = 0; i < count; i++) {
                bag.add(kind);
            }
        }

        void addElement(Element element) {
            elements.add(element);
        }

        /** Refuses a setting that already appeared on an earlier line. */
        private static void requireFirst(Line line, int earlierLine) throws LevelFormatException {
            if (earlierLine != 0) {
                String name = line.keyword().equals("bank") ? "bank " + line.word(1) : line.keyword();
                throw line.error("'" + name + "' appears twice (first on line " + earlierLine + ")");
            }
        }

        private static ThrowableKind kindNamed(Line line, String name) throws LevelFormatException {
            for (ThrowableKind kind : ThrowableKind.values()) {
                if (kind.name().toLowerCase(java.util.Locale.ROOT).equals(name)) {
                    return kind;
                }
            }
            throw line.error("unknown throwable kind '" + name + "': use twig, plank or log");
        }

        /** Checks the whole file and builds the level. */
        Level build(Rank rank) throws LevelFormatException {
            requirePresent(turnsLine, "turns");
            requirePresent(farBankLine, "bank far");
            requirePresent(startBankLine, "bank start");
            requirePresent(frogLine, "frog");
            if (farBank + startBank > Pond.HEIGHT) {
                throw new LevelFormatException(startBankLine,
                        "the two banks overlap: together they are deeper than the play area");
            }
            Frog theFrog = new Frog(frog, rank);
            Pond pond = new Pond(Pond.WIDTH, Pond.HEIGHT, farBank, startBank, theFrog);
            if (pond.areaAt(frog) != AreaType.START_BANK) {
                throw new LevelFormatException(frogLine, "the frog must start on the start bank");
            }
            for (Element element : elements) {
                pond.add(element);
            }
            return new Level(pond, bag, turns, new InProgress());
        }

        private static void requirePresent(int line, String name) throws LevelFormatException {
            if (line == 0) {
                throw new LevelFormatException("the '" + name + "' line is missing");
            }
        }
    }
}
