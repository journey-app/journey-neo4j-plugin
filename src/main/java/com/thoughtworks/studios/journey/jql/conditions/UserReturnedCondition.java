package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

public class UserReturnedCondition extends QueryCondition {
    private final long threshold;

    public UserReturnedCondition(long threshold) {
        this.threshold = threshold;
    }

    @Override
    public Iterable<Node> filter(final Application app, Iterable<Node> journeys) {
        return Iterables.filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node journey) {
                Node user = app.journeys().user(journey);
                if(user == null) {
                    return false;
                }
                return app.users().getLastActiveAt(user) - app.users().getStartActiveAt(user) >= threshold;
            }
        }, journeys);
    }
}
