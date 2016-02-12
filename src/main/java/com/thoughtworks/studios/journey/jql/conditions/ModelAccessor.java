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
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.Iterables;

import java.util.HashMap;
import java.util.Map;

public enum ModelAccessor {
    START_ACTIVE_AT {
        @Override
        public Value get(Application app, Node journey) {
            Node user = app.journeys().user(journey);
            return new IntValue(app.users().getStartActiveAt(user));
        }
    },

    LAST_ACTIVE_AT {
        @Override
        public Value get(Application app, Node journey) {
            Node user = app.journeys().user(journey);
            return new IntValue(app.users().getLastActiveAt(user));
        }
    },

    IDENTIFIER {
        @Override
        public Value get(Application app, Node journey) {
            Node user = app.journeys().user(journey);
            return new StringValue(app.users().getIdentifier(user));
        }
    },

    FIRST_ACTION {
        @Override
        public Value get(Application app, Node journey) {
            Node firstEvent = app.journeys().firstEvent(journey);
            if (firstEvent == null) {
                return new StringValue("");
            }
            Node firstAction = app.events().action(firstEvent);
            return new StringValue(app.actions().getActionLabel(firstAction));
        }
    },

    START_AT {
        @Override
        public Value get(Application app, Node journey) {
            return new IntValue(app.journeys().getStartAt(journey));
        }
    },

    FINISH_AT {
        @Override
        public Value get(Application app, Node journey) {
            return new IntValue(app.journeys().getFinishAt(journey));
        }
    },

    LENGTH {
        @Override
        public Value get(Application app, Node journey) {
            return new IntValue(app.journeys().length(journey));
        }
    },

    ACTIONS {
        @Override
        public Value get(Application app, Node journey) {
            return new SetValue(app.journeys().actions(journey));
        }
    },
    ANONYMOUS_ID {
        @Override
        public Value get(Application app, Node journey) {
            Node user = app.journeys().user(journey);
            return new StringValue(app.users().getAnonymousId(user));
        }
    };


    private static Map<String, ModelAccessor> registry = new HashMap<>(3);

    static {
        registry.put("user.start_active_at", START_ACTIVE_AT);
        registry.put("user.last_active_at", LAST_ACTIVE_AT);
        registry.put("user.identifier", IDENTIFIER);
        registry.put("user.anonymous_id", ANONYMOUS_ID);
        registry.put("first_action", FIRST_ACTION);
        registry.put("start_at", START_AT);
        registry.put("finish_at", FINISH_AT);
        registry.put("length", LENGTH);
        registry.put("actions", ACTIONS);
    }

    public static ModelAccessor forField(String field) {
        return registry.get(field);
    }

    public abstract Value get(Application app, Node journey);

}
