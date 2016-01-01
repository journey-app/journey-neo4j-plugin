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

    public abstract JQLValue values(Node journey, Node request, boolean cross);


    private static class EventPropertyCollector extends ValueCollector {
        private Application app;
        private String propertyName;

        public EventPropertyCollector(Application app, String propertyName) {
            this.app = app;
            this.propertyName = propertyName;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }

            Set<Object> values = app.requests().values(request, propertyName);
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
        public JQLValue values(Node journey, Node request, boolean cross) {
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
        public JQLValue values(Node journey, Node request, boolean cross) {
            return Values.wrapModel(app.journeys().user(journey), app.users());
        }
    }

    private static class JourneyCollector extends ValueCollector {

        private Application app;

        public JourneyCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            return Values.wrapModel(journey, app.journeys());
        }
    }

    private static class EventCollector extends ValueCollector {
        private Application app;

        private EventCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }
            return Values.wrapModel(request, app.requests());
        }
    }

    private static class EventUrlCollector extends ValueCollector {
        private final Application app;

        public EventUrlCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.requests().getUrl(request));
        }
    }

    private static class EventReferrerCollector extends ValueCollector {
        private final Application app;

        public EventReferrerCollector(Application app) {
            super();
            this.app = app;
        }


        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.requests().getReferrer(request));
        }
    }

    private static class EventActionCollector extends ValueCollector {
        private Application app;

        public EventActionCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.requests().getActionLabel(request));
        }
    }

    private static class EventTimeStampCollector extends ValueCollector {
        private Application app;

        public EventTimeStampCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            if (request == null) {
                return NullValue.instance;
            }
            return Values.wrapSingle(app.requests().getStartAt(request));
        }
    }

    private static class JourneyStartAtCollector extends ValueCollector {
        private Application app;

        public JourneyStartAtCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            return Values.wrapSingle(app.journeys().getStartAt(journey));
        }
    }

    private static class JourneyActionsCollector extends ValueCollector {
        private Application app;

        public JourneyActionsCollector(Application app) {
            this.app = app;
        }

        @Override
        public JQLValue values(Node journey, Node request, boolean cross) {
            Journeys journeys = app.journeys();
            return Values.wrapMulti(cross ? journeys.crossActions(journey) : journeys.actions(journey));
        }
    }
}
