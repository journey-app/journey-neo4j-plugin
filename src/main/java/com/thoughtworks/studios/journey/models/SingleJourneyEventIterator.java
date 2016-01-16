package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;

public class SingleJourneyEventIterator extends BaseEventIterator{
    public SingleJourneyEventIterator(Application app, Node event) {
        super(app, event);
    }

    @Override
    public void forward() {
        currentEvent = null;
        currentJourney = null;
    }
}
