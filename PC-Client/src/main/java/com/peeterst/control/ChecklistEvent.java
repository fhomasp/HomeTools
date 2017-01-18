package com.peeterst.control;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 23/06/13
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
public class ChecklistEvent extends Event {

    public static final EventType<ChecklistEvent> CTRL_CHANGED = new EventType(ANY, "CTRL_CHANGED");
    public static final EventType<ChecklistEvent> PROCESS_CLOSE = new EventType(ANY, "PROCESS_CLOSE");
    public static final EventType<ChecklistEvent> CTRL_CHANGED_EXTERNAL = new EventType(ANY, "CTRL_CHANGED");
    public static final EventType<ChecklistEvent> ITEM_DELETED = new EventType(ANY, "ITEM_DELETED");
    public static final EventType<ChecklistEvent> CHECKLIST_DELETED = new EventType(ANY, "CHECKLIST_DELETED");
    public static final EventType<ChecklistEvent> CHECKLIST_ADD = new EventType(ANY, "CHECKLIST_ADD");
    public static final EventType<ChecklistEvent> ITEM_ADD = new EventType(ANY, "ITEM_ADD");

    private Object source;

    public ChecklistEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public ChecklistEvent(Object o, EventTarget eventTarget, EventType<? extends Event> eventType) {
        super(o, eventTarget, eventType);
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }
}
