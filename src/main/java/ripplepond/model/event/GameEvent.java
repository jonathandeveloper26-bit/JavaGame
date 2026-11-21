package ripplepond.model.event;

import ripplepond.model.element.Element;

/** An event published by the pond, describing what happened and to which element. */
public class GameEvent {
    private final EventType type;
    private final Element subject;
    private final double amount;

    /** Creates a new event with its type, subject, and amount. */
    public GameEvent(EventType type, Element subject, double amount) {
        this.type = type;
        this.subject = subject;
        this.amount = amount;
    }

    /** Returns the type of the event. */
    public EventType getType() {
        return type;
    }

    /** Returns the element associated with the event. */
    public Element getSubject() {
        return subject;
    }

    /** Returns the amount associated with the event. */
    public double getAmount() {
        return amount;
    }
}