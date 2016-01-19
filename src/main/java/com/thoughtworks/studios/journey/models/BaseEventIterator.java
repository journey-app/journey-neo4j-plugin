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
import org.neo4j.helpers.collection.PrefetchingIterator;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getSingleEndNode;

public abstract class BaseEventIterator extends PrefetchingIterator<Node> implements EventIterator {

    protected final Application app;
    protected Node currentEvent;
    protected Node currentJourney;
    private Node rewindEvent;
    private Node rewindJourney;

    public BaseEventIterator(Application app, Node startEvent) {
        this.app = app;
        this.currentEvent = startEvent;
        this.currentJourney = app.events().journeyOf(currentEvent);
    }

    @Override
    protected Node fetchNextOrNull() {
        try {
            return currentEvent;
        } finally {
            rewindEvent = currentEvent;
            rewindJourney = currentJourney;
            //noinspection ConstantConditions
            if (currentEvent != null) {
                currentEvent =  getSingleEndNode(currentEvent, RelTypes.NEXT);
                if (currentEvent == null) {
                    this.forward();
                }
            }
        }
    }

    @Override
    public void rewind() {
        currentEvent = rewindEvent;
        currentJourney = rewindJourney;
    }

}
