package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;

public class JourneyStartsBeforeCondition extends QueryCondition {
    private long before;

    public JourneyStartsBeforeCondition(Long before) {
        this.before = before;
    }

    @Override
    public Query luceneQuery(Application app) {
        return NumericRangeQuery.newLongRange(
                Journeys.PROP_START_AT,
                Long.MIN_VALUE,
                before,
                true,
                false);
    }
}
