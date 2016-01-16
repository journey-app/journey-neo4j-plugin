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
package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;

public class CrossJourneyEventIterator extends BaseEventIterator {

    public CrossJourneyEventIterator(Application app, Node startEvent) {
        super(app, startEvent);
    }

    public void forward() {
        currentJourney = app.journeys().next(currentJourney);
        if (currentJourney == null) {
            currentEvent = null;
        } else {
            currentEvent = app.journeys().firstEvent(currentJourney);
        }
    }

}
