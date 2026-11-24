package ripplepond.model.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ripplepond.model.event.GameEvent;
import ripplepond.model.event.PondListener;

/**
 * One playable level: the pond, the throw bag, the turn count and the current state.
 */
public class Level {

    private final Pond pond;
    private final ThrowBag bag;
    private final int turnLimit;
    private LevelState state;
    private int turn;
    private final List<PondListener> listeners = new ArrayList<>();

    /**
     * Creates a level at turn 0.
     *
     * @param pond      the pond the level is played in
     * @param bag       the objects the player can throw
     * @param turnLimit the number of turns allowed
     * @param state     the state the level starts in, normally {@code new InProgress()}
     * @throws NullPointerException if {@code pond}, {@code bag} or {@code state} is
     *                              {@code null}
     */
    public Level(Pond pond, ThrowBag bag, int turnLimit, LevelState state) {
        this.pond = Objects.requireNonNull(pond, "pond");
        this.bag = Objects.requireNonNull(bag, "bag");
        this.turnLimit = turnLimit;
        this.state = Objects.requireNonNull(state, "state");
        this.turn = 0;
    }

    /**
     * Plays one turn by handing it to the current state, and keeps the state the turn
     * ends in (State pattern). While the level is in progress, the state runs the turn
     * engine and then the end rules; a won or lost level refuses the turn.
     * <p>
     * The turn counter goes up before the state plays the turn, so the end rules see
     * the turn that has just been played when they check the turn limit. A refused turn
     * is not counted.
     *
     * @param action what the player asked for this turn
     * @throws NullPointerException  if {@code action} is {@code null}
     * @throws IllegalStateException if the level is already won or lost
     */
    public void playTurn(TurnAction action) {
        Objects.requireNonNull(action, "action");
        if (state.isOver()) {
            throw new IllegalStateException("the level is over: " + state);
        }
        turn++;
        state = state.playTurn(this, action);
    }

    /** Returns the current state: in progress, won or lost. */
    public LevelState getState() {
        return state;
    }

    /** Returns the pond the level is played in. */
    public Pond getPond() {
        return pond;
    }

    /** Returns the throw bag. */
    public ThrowBag getBag() {
        return bag;
    }

    /** Returns the number of turns played so far. */
    public int getTurn() {
        return turn;
    }

    /** Returns the number of turns allowed. */
    public int getTurnLimit() {
        return turnLimit;
    }

    /**
     * Registers a listener to receive every event this level publishes.
     *
     * @param listener the listener to add
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public void addListener(PondListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Sends an event to every registered listener, in the order they were added.
     *
     * @param event the event to send
     * @throws NullPointerException if {@code event} is {@code null}
     */
    public void publish(GameEvent event) {
        Objects.requireNonNull(event, "event");
        for (PondListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}