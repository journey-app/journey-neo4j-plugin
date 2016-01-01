package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.Iterables;

public class UserActivatedBetweenCondition extends QueryCondition {
    private final Long after;
    private final Long before;

    public UserActivatedBetweenCondition(long after, long before) {
        this.after = after;
        this.before = before;
    }

    @Override
    public Query luceneQuery(Application app) {
        return NumericRangeQuery.newLongRange(
                Journeys.PROP_START_AT,
                Long.valueOf(after),
                Long.MAX_VALUE,
                true,
                false);
    }

    @Override
    public Iterable<Node> filter(final Application app, Iterable<Node> journeys) {
        return Iterables.filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node journey) {
                Node user = app.journeys().user(journey);
                if (user == null) {
                    return false;
                }
                Long firstActiveAt = app.users().getStartActiveAt(user);
                return firstActiveAt >= after && firstActiveAt < before;
            }
        }, journeys);
    }
}
