package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

import java.util.Iterator;

public class JourneyAtLeastNActionsCondition extends QueryCondition {
    private int n;

    public JourneyAtLeastNActionsCondition(int n) {
        this.n = n;
    }

    @Override
    public Iterable<Node> filter(final Application app, final Iterable<Node> journeys) {
        return Iterables.filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node node) {
                Iterable<Node> requests = app.journeys().userRequests(node);
                Iterator<Node> iterator = requests.iterator();
                for (int i = 0; i < n; i++) {
                    if (iterator.hasNext()) {
                        iterator.next();
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }, journeys);
    }
}
