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
