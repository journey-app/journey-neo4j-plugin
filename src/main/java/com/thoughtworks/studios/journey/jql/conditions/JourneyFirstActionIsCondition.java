package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

public class JourneyFirstActionIsCondition extends QueryCondition {
    private String actionLabel;

    public JourneyFirstActionIsCondition(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    @Override
    public Iterable<Node> filter(final Application app, Iterable<Node> journeys) {
        final Node expectedAction = app.actions().findByActionLabel(this.actionLabel);
        return Iterables.filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node journey) {
                Iterable<Node> requests = app.journeys().userRequests(journey);
                Node firstRequest = Iterables.first(requests);
                if(firstRequest == null) {
                    return false;
                }
                Node firstAction = app.requests().action(firstRequest);
                return firstAction.equals(expectedAction);
            }
        }, journeys);
    }
}
