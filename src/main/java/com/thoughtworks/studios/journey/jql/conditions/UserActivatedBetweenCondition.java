/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
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
