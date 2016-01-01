package com.thoughtworks.studios.journey.jql.conditions;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import com.thoughtworks.studios.journey.jql.QueryCondition;
import com.thoughtworks.studios.journey.utils.MatchNothingLuceneQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Node;

public class JourneyIncludesActionCondition extends QueryCondition {
    private String actionLabel;

    public JourneyIncludesActionCondition(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    @Override
    public Query luceneQuery(Application app) {
        Node action = app.actions().findByActionLabel(actionLabel);
        if(action == null) {
            return new MatchNothingLuceneQuery();
        }
        Long actionId = action.getId();
        return NumericRangeQuery.newLongRange(
                Journeys.IDX_PROP_ACTION_IDS,
                actionId,
                actionId,
                true,
                true);
    }
}
