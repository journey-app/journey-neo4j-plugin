package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

public class JourneyStartsAfterCondition extends QueryCondition {
    private long after;

    public JourneyStartsAfterCondition(long after) {
        this.after = after;
    }

    @Override
    public Query luceneQuery(Application app) {
        return NumericRangeQuery.newLongRange(
                Journeys.PROP_START_AT,
                after,
                Long.MAX_VALUE,
                true,
                false);
    }
}
