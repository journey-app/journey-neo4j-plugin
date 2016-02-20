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

import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.models.Application;
import com.thoughtworks.studios.journey.models.Journeys;
import org.neo4j.graphdb.Node;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ValueCollector {

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("event\\.(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAIT_PATTERN = Pattern.compile("user\\.(.*)", Pattern.CASE_INSENSITIVE);

    public static ValueCollector eval(Application app, String expression) {
        if (expression.equals("event")) {
            return new EventCollector(app);
        }

        if (expression.equals("user")) {
            return new UserCollector(app);
        }

        if (expression.equals("user.identifier")) {
            return new UIDCollector(app);
        }

        if (expression.equals("user.first_event.url")) {
            return new UserFirstEventURLCollector(app);
        }

        if (expression.equals("user.first_event.referrer")) {
            return new UserFirstEventReferrerCollector(app);
        }

        if (expression.equals("user.first_event.action")) {
            return new UserFirstEventActionCollector(app);
        }

        Matcher traitMatcher = TRAIT_PATTERN.matcher(expression);
        if (traitMatcher.matches()) {
            return new UserTraitCollector(app, traitMatcher.group(1));
        }

        if (expression.equals("journey")) {
            return new JourneyCollector(app);
        }

        if (expression.equals("journey.actions")) {
            return new JourneyActionsCollector(app);
        }

        if (expression.equals("journey.start_at")) {
            return new JourneyStartAtCollector(app);
        }

        if (expression.equals("event.url")) {
            return new EventUrlCollector(app);
        }

        if (expression.equals("event.action")) {
            return new EventActionCollector(app);
        }

        if (expression.equals("event.timestamp") || expression.equals("event.at")) {
            return new EventTimeStampCollector(app);
        }

        if (expression.equals("event.referrer")) {
            return new EventReferrerCollector(app);
        }

        Matcher propertyMatcher = PROPERTY_PATTERN.matcher(expression);
        if (propertyMatcher.matches()) {
            return new EventPropertyCollector(app, propertyMatcher.group(1));
        }

        throw new DataQueryError("'" + expression + "' is not a valid collector");
    }

    public abstract JQLValue values(Node journey, Node event, boolean cross);


    private static class EventPropertyCollector extends ValueCollector {
        private Application app;
        private String propertyName;

        public EventPropertyCollector(Application app, String propertyName) {
            this.app = app;
            this.propertyName = propertyName;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }

            Set<Object> values = app.events().values(event, propertyName);
            return Values.wrapMulti(values);
        }
    }

    private static class UserTraitCollector extends ValueCollector {
        private Application app;
        private String traitName;

        public UserTraitCollector(Application app, String value) {
            this.app = app;
            this.traitName = value;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Node user = app.journeys().user(journey);
            Set<Object> values = app.users().getTraitValue(user, traitName);
            return Values.wrapMulti(values);
        }
    }


    private static class UserCollector extends ValueCollector {
        private Application app;

        public UserCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            return Values.wrapModel(app.journeys().user(journey), app.users());
        }
    }

    private static class JourneyCollector extends ValueCollector {

        private Application app;

        public JourneyCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            return Values.wrapModel(journey, app.journeys());
        }
    }

    private static class EventCollector extends ValueCollector {
        private Application app;

        private EventCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }
            return Values.wrapModel(event, app.events());
        }
    }

    private static class EventUrlCollector extends ValueCollector {
        private final Application app;

        public EventUrlCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.events().getUrl(event));
        }
    }

    private static class EventReferrerCollector extends ValueCollector {
        private final Application app;

        public EventReferrerCollector(Application app) {
            super();
            this.app = app;
        }


        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.events().getReferrer(event));
        }
    }

    private static class EventActionCollector extends ValueCollector {
        private Application app;

        public EventActionCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.events().getActionLabel(event));
        }
    }

    private static class EventTimeStampCollector extends ValueCollector {
        private Application app;

        public EventTimeStampCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            if (event == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.events().getStartAt(event));
        }
    }

    private static class JourneyStartAtCollector extends ValueCollector {
        private Application app;

        public JourneyStartAtCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            return Values.wrapSingle(app.journeys().getStartAt(journey));
        }
    }

    private static class JourneyActionsCollector extends ValueCollector {
        private Application app;

        public JourneyActionsCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Journeys journeys = app.journeys();
            return Values.wrapMulti(cross ? journeys.crossActions(journey) : journeys.actions(journey));
        }
    }

    private static class UIDCollector extends ValueCollector {
        private Application app;

        public UIDCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Node user = app.journeys().user(journey);
            return Values.wrapSingle(app.users().getIdentifier(user));
        }
    }

    private static class UserFirstEventURLCollector extends ValueCollector {
        private Application app;

        public UserFirstEventURLCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Node user = app.journeys().user(journey);
            Node firstEvent = app.users().firstEvent(user);
            return Values.wrapSingle(app.events().getUrl(firstEvent));
        }
    }

    private static class UserFirstEventReferrerCollector extends ValueCollector {
        private Application app;

        public UserFirstEventReferrerCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Node user = app.journeys().user(journey);
            Node firstEvent = app.users().firstEvent(user);
            return Values.wrapSingle(app.events().getReferrer(firstEvent));        }
    }

    private static class UserFirstEventActionCollector extends ValueCollector {
        private Application app;
        public UserFirstEventActionCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node event, boolean cross) {
            Node user = app.journeys().user(journey);
            Node firstEvent = app.users().firstEvent(user);
            return Values.wrapSingle(app.events().getActionLabel(firstEvent));
        }
    }
}
