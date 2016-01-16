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
package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.EventIterator;
import org.neo4j.graphdb.Node;

import java.util.Iterator;
import java.util.List;

public class Stop {
    private final Application app;
    private Node action = null;
    private boolean matchAny = false;
    private JourneyQuery query;
    private boolean rewind = false;

    public Stop(Application app, String action, List<String> conditions) {
        this(app, action, conditions, false);
    }

    public Stop(Application app, String action, List<String> conditions, boolean rewind) {
        this.app = app;
        this.rewind = rewind;
        this.query = JourneyQuery.Builder.query(app).
                conditions(conditions).
                build();

        if (action.equals("*")) {
            this.matchAny = true;
        } else {
            this.action = app.actions().findByActionLabel(action);
        }
    }

    public JourneyQuery journeyQuery() {
        return query;
    }


    public MatchResult match(EventIterator iterator) {
        boolean matched = false;
        Node matchedEvent = null;

        iterator.markRewindPoint();
        while (iterator.hasNext()) {
            Node event = iterator.next();
            if (match(event)) {
                matchedEvent = event;
                matched = true;
                break;
            }
        }

        if (rewind) {
            iterator.rewind();
        }

        return new MatchResult(matched, matchedEvent, iterator);
    }

    private boolean match(Node event) {
        return matchAny || app.events().action(event).equals(action);
    }

    public class MatchResult {
        private final boolean matched;
        private Node last;
        private final Iterator<Node> iterator;

        public MatchResult(boolean matched, Node last, Iterator<Node> iterator) {
            this.matched = matched;
            this.last = last;
            this.iterator = iterator;
        }

        public boolean matched() {
            return matched;
        }

        public Iterator<Node> iterator() {
            return iterator;
        }

        public Node last() {
            return last;
        }
    }

}
