/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
 *
 * Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
