package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.PrefetchingIterator;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getSingleEndNode;

public abstract class BaseEventIterator extends PrefetchingIterator<Node> implements EventIterator {

    protected final Application app;
    protected Node currentEvent;
    protected Node currentJourney;
    private Node rewindEvent;
    private Node rewindJourney;

    public BaseEventIterator(Application app, Node startEvent) {
        this.app = app;
        this.currentEvent = startEvent;
        this.currentJourney = app.events().journeyOf(currentEvent);
    }

    @Override
    protected Node fetchNextOrNull() {
        try {
            return currentEvent;
        } finally {
            //noinspection ConstantConditions
            if (currentEvent != null) {

                currentEvent = getSingleEndNode(currentEvent, RelTypes.NEXT);

                if (currentEvent == null) {
                    this.forward();
                }
            }
        }
    }

    @Override
    public void rewind() {
        currentEvent = rewindEvent;
        currentJourney = rewindJourney;
    }

    @Override
    public void markRewindPoint() {
        this.rewindEvent = currentEvent;
        this.rewindJourney = currentJourney;
    }
}
