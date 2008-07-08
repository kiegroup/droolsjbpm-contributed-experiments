package org.drools.jpdl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.ExceptionHandler;

public class JpdlConnection extends ConnectionImpl {

    private static final long serialVersionUID = 1L;

    protected String condition;
    private Map<String, Event> events;
    private List<ExceptionHandler> exceptionHandlers;
    
    public JpdlConnection(Node from, String fromType, Node to, String toType) {
        super(from, fromType, to, toType);
    }

    public Map<String, Event> getEvents() {
        return events;
    }

    public boolean hasEvents() {
        return (events != null) && (events.size() > 0);
    }

    public Event getEvent(String eventType) {
        Event event = null;
        if (events != null) {
            event = (Event) events.get(eventType);
        }
        return event;
    }

    public boolean hasEvent(String eventType) {
        boolean hasEvent = false;
        if (events != null) {
            hasEvent = events.containsKey(eventType);
        }
        return hasEvent;
    }

    public Event addEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException(
                "can't add a null event to a graph element");
        }
        if (event.getEventType() == null) {
            throw new IllegalArgumentException(
                "can't add an event without an eventType to a graph element");
        }
        if (events == null) {
            events = new HashMap<String, Event>();
        }
        events.put(event.getEventType(), event);
        return event;
    }

    public Event removeEvent(Event event) {
        Event removedEvent = null;
        if (event == null) {
            throw new IllegalArgumentException(
                "can't remove a null event from a graph element");
        }
        if (event.getEventType() == null) {
            throw new IllegalArgumentException(
                "can't remove an event without an eventType from a graph element");
        }
        if (events != null) {
            removedEvent = (Event) events.remove(event.getEventType());
        }
        return removedEvent;
    }
    
    public List getExceptionHandlers() {
        return exceptionHandlers;
    }
      
    public ExceptionHandler addExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler == null) {
            throw new IllegalArgumentException(
                "can't add a null exceptionHandler to a connection");
        }
        if (exceptionHandlers == null) {
            exceptionHandlers = new ArrayList<ExceptionHandler>();
        }
        exceptionHandlers.add(exceptionHandler);
        return exceptionHandler;
    }

    public void removeExceptionHandler(ExceptionHandler exceptionHandler) {
        if (exceptionHandler == null) {
            throw new IllegalArgumentException(
                "can't remove a null exceptionHandler from a connection");
        }
        if (exceptionHandlers != null) {
            exceptionHandlers.remove(exceptionHandler);
        }
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

}
