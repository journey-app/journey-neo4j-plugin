package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;

public class CrossJourneyEventIterator extends BaseEventIterator {

    public CrossJourneyEventIterator(Application app, Node startEvent) {
        super(app, startEvent);
    }

    public void forward() {
        currentJourney = app.journeys().next(currentJourney);
        if (currentJourney == null) {
            currentEvent = null;
        } else {
            currentEvent = app.journeys().firstEvent(currentJourney);
        }
    }

}
