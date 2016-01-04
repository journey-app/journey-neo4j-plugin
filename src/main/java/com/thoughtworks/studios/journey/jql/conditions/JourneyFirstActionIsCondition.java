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
                Iterable<Node> events = app.journeys().events(journey);
                Node firstEvent = Iterables.first(events);
                if(firstEvent == null) {
                    return false;
                }
                Node firstAction = app.events().action(firstEvent);
                return firstAction.equals(expectedAction);
            }
        }, journeys);
    }
}
