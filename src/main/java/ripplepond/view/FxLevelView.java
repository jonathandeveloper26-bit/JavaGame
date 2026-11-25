package ripplepond.view;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import ripplepond.controller.GameController;
import ripplepond.model.element.Element;
import ripplepond.model.element.Frog;
import ripplepond.model.element.LilyPad;
import ripplepond.model.element.Reed;
import ripplepond.model.element.ThrowableKind;
import ripplepond.model.element.Turtle;
import ripplepond.model.element.pickup.Pickup;
import ripplepond.model.element.predator.Predator;
import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;
import ripplepond.model.geometry.Vec2;
import ripplepond.model.level.Level;
import ripplepond.model.level.Pond;
import ripplepond.model.movement.MoveMode;
import ripplepond.model.ripple.Ripple;

/**
 * The level screen: the river, the frog's status, the throw bag, the move choice, the
 * message log and the turn buttons (GDD section 5).
 * <p>
 * The view only reads the model and draws it. Every player choice goes through
 * {@link FxInputHandler}, which in turn calls the {@link GameController}. The view listens
 * for events to write the message log.
 */
final class FxLevelView implements PondListener {

    static final double CANVAS_WIDTH = 720;
    static final double CANVAS_HEIGHT = 480;
    private static final int LOG_LINES = 3;

    private static final Color WATER = Color.web("#1d4f6b");
    private static final Color BANK = Color.web("#8c6d46");
    private static final Color FROG = Color.web("#7ed957");
    private static final Color PREDATOR = Color.web("#d64541");
    private static final Color REED = Color.web("#6b8e23");
    private static final Color TURTLE = Color.web("#2f6f4f");
    private static final Color PAD = Color.web("#9be564");
    private static final Color WOOD = Color.web("#b07a4a");
    private static final Color PICKUP = Color.web("#f4d35e");
    private static final Color RIPPLE = Color.web("#dff6ff", 0.6);
    private static final Color PREVIEW = Color.web("#ffffff", 0.9);

    private final GameController controller;
    private final Runnable onLevelOver;
    private final FxInputHandler input;
    private final Deque<String> messages = new ArrayDeque<>();

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    private final Label status = new Label();
    private final Label log = new Label();
    private final Label bagTitle = new Label("Throw bag");
    private final Label moveTitle = new Label("Move");
    private final Map<ThrowableKind, Button> bagButtons = new EnumMap<>(ThrowableKind.class);

    /**
     * Builds the level screen.
     *
     * @param controller the game being played
     * @param onLevelOver runs after a turn that wins or loses the level
     */
    FxLevelView(GameController controller, Runnable onLevelOver) {
        this.controller = controller;
        this.onLevelOver = onLevelOver;
        this.input = new FxInputHandler(controller, this);

        canvas.setOnMouseClicked(event -> {
            PixelMapping map = mapping();
            if (map.inPlayArea(event.getX(), event.getY())) {
                input.clickPlayArea(map.toFraction(event.getX(), event.getY()));
            }
        });

        VBox panel = new VBox(10, bagTitle);
        for (ThrowableKind kind : ThrowableKind.values()) {
            Button button = new Button();
            button.setOnAction(event -> input.chooseThrow(kind));
            bagButtons.put(kind, button);
            panel.getChildren().add(button);
        }
        panel.getChildren().add(moveTitle);
        panel.getChildren().add(moveButton("Walk", FxInputHandler.WALK));
        panel.getChildren().add(moveButton("Hop", FxInputHandler.HOP));
        panel.getChildren().add(moveButton("Swim", FxInputHandler.SWIM));

        Button endTurn = new Button("End turn");
        endTurn.setOnAction(event -> input.endTurn());
        Button reset = new Button("Reset");
        reset.setOnAction(event -> input.reset());
        panel.getChildren().addAll(endTurn, reset);
        panel.setPrefWidth(180);

        status.setStyle("-fx-font-size: 14px;");
        log.setStyle("-fx-font-size: 13px;");
        root.setTop(status);
        root.setCenter(canvas);
        root.setRight(panel);
        root.setBottom(log);
    }

    private Button moveButton(String text, MoveMode mode) {
        Button button = new Button(text);
        button.setOnAction(event -> input.chooseMove(mode));
        return button;
    }

    /** Returns the screen's root node for the scene. */
    Parent root() {
        return root;
    }

    /** Redraws the level and refreshes the labels and buttons from the model. */
    void refresh() {
        Level level = controller.getLevel();
        if (level == null) {
            return;
        }
        Pond pond = level.getPond();
        PixelMapping map = mapping();
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(WATER);
        g.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        drawBanks(g, map, pond);
        drawRipples(g, map, pond);
        for (Element element : pond.elementsInOrder()) {
            disc(g, map, element.getPosition(), element.getSize(), colourOf(element));
        }
        Frog frog = pond.getFrog();
        disc(g, map, frog.getPosition(), frog.getSize(), FROG);
        drawPreview(g, map, frog);
        updateLabels(level, frog);
    }

    /** Called after a turn: redraws, then tells the screen owner if the level is over. */
    void afterTurn() {
        refresh();
        Level level = controller.getLevel();
        if (level != null && level.getState().isOver()) {
            onLevelOver.run();
        }
    }

    /**
     * Writes a line to the message log, keeping the last few.
     *
     * @param text the message; blank messages are ignored
     */
    void say(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        messages.addLast(text);
        while (messages.size() > LOG_LINES) {
            messages.removeFirst();
        }
        log.setText(String.join("\n", messages));
    }

    /** Listens to the level's events and writes what happened to the log. */
    @Override
    public void onEvent(GameEvent event) {
        say(describe(event));
    }

    private static String describe(GameEvent event) {
        return switch (event.getType()) {
            case OBJECT_THROWN -> "Splash! Something landed on the water.";
            case OBJECT_PUSHED -> "A ripple pushed something.";
            case TURTLE_WOKE -> "A turtle woke up.";
            case FROG_MOVED -> "The frog moved.";
            case FROG_HURT -> "Ouch! The frog lost " + (int) event.getAmount() + " HP.";
            case FOOD_EATEN -> "The frog ate something tasty.";
            case ICON_GATHERED -> "The frog found a new object for the bag.";
            case RANK_GAINED -> "The frog reached a new rank!";
            default -> null;
        };
    }

    private void updateLabels(Level level, Frog frog) {
        int xp = controller.getSession().getProgress().getXp();
        status.setText(String.format(
                "Health %d/%d    Energy %.0f/%d    Rank %d    XP %d    Turn %d of %d",
                frog.getHealth(), frog.getRank().maxHealth(),
                frog.getEnergy(), frog.getRank().maxEnergy(),
                frog.getRank().ordinal() + 1, xp,
                level.getTurn(), level.getTurnLimit()));

        ThrowBagView.update(level.getBag(), bagButtons);
    }

    private void drawBanks(GraphicsContext g, PixelMapping map, Pond pond) {
        g.setFill(BANK);
        PixelMapping.Point farTop = map.toPixels(new Vec2(0, 0));
        PixelMapping.Point farBottom = map.toPixels(new Vec2(Pond.WIDTH, pond.getFarBankDepth()));
        g.fillRect(farTop.x(), farTop.y(), farBottom.x() - farTop.x(), farBottom.y() - farTop.y());

        PixelMapping.Point startTop = map.toPixels(new Vec2(0, Pond.HEIGHT - pond.getStartBankDepth()));
        PixelMapping.Point startBottom = map.toPixels(new Vec2(Pond.WIDTH, Pond.HEIGHT));
        g.fillRect(startTop.x(), startTop.y(), startBottom.x() - startTop.x(),
                startBottom.y() - startTop.y());
    }

    private void drawRipples(GraphicsContext g, PixelMapping map, Pond pond) {
        g.setStroke(RIPPLE);
        g.setLineWidth(2);
        for (Ripple ripple : pond.ripples()) {
            PixelMapping.Point c = map.toPixels(ripple.getSource());
            double d = 2 * ripple.getRadius() * map.scale();
            g.strokeOval(c.x() - d / 2, c.y() - d / 2, d, d);
        }
    }

    private void drawPreview(GraphicsContext g, PixelMapping map, Frog frog) {
        FxInputHandler.Pending pending = input.pending();
        g.setStroke(PREVIEW);
        g.setLineWidth(2);
        if (pending.throwTarget() != null && pending.throwKind() != null) {
            Vec2 at = pending.throwTarget();
            disc(g, map, at, pending.throwKind().size(), PREVIEW.deriveColor(0, 1, 1, 0.35));
        }
        if (pending.moveTarget() != null) {
            PixelMapping.Point from = map.toPixels(frog.getPosition());
            PixelMapping.Point to = map.toPixels(pending.moveTarget());
            g.strokeLine(from.x(), from.y(), to.x(), to.y());
            double r = 6;
            g.strokeOval(to.x() - r, to.y() - r, 2 * r, 2 * r);
        }
    }

    private void disc(GraphicsContext g, PixelMapping map, Vec2 centre, double diameter, Color fill) {
        PixelMapping.Point p = map.toPixels(centre);
        double d = diameter * map.scale();
        g.setFill(fill);
        g.fillOval(p.x() - d / 2, p.y() - d / 2, d, d);
    }

    /**
     * Picks the colour of an element. The view is the one place that maps types to
     * colours; the model never knows about colours.
     */
    private static Color colourOf(Element element) {
        if (element instanceof Predator) {
            return PREDATOR;
        }
        if (element instanceof Reed) {
            return REED;
        }
        if (element instanceof Turtle) {
            return TURTLE;
        }
        if (element instanceof LilyPad) {
            return PAD;
        }
        if (element instanceof Pickup) {
            return PICKUP;
        }
        return WOOD;
    }

    private PixelMapping mapping() {
        return new PixelMapping(CANVAS_WIDTH, CANVAS_HEIGHT);
    }
}
