package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.utils.IterableUtils;
import org.neo4j.graphdb.Node;

import java.util.Iterator;

public class StoppingCondition {

    public static StoppingCondition eval(Application app, String stoppingExpression) {
        String[] parts = stoppingExpression.split(":");
        String action = parts[0];
        int qualifier = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        boolean rewind = parts.length > 2 && "<<".equals(parts[2]);
        return new StoppingCondition(app, action, qualifier, rewind);
    }

    private final int qualifier;
    private final boolean rewind;
    private final ActionMatcher actionMatcher;


    public StoppingCondition(Application app, String action, int qualifier, boolean rewind) {
        this.actionMatcher = new ActionMatcher(app, action);
        this.qualifier = qualifier;
        this.rewind = rewind;
    }


    public StopMatchResult match(Iterable<Node> iterable) {
        return match(iterable.iterator());
    }

    public StopMatchResult match(Iterator<Node> requestIterator) {
        int hitNeeded = qualifier;
        IterableUtils.RewindableIterator<Node> iterator = IterableUtils.rewindable(requestIterator);
        boolean matched = false;
        while (iterator.hasNext()) {
            Node request = iterator.next();
            if (actionMatcher.match(request)) {
                hitNeeded--;
                if (hitNeeded == 0) {
                    matched = true;
                    break;
                }
            }
        }

        return new StopMatchResult(matched,
                    matched ? iterator.lastVisited() : null,
                    rewind ? iterator.rewind() : iterator.original());
    }


    public class StopMatchResult {
        private final boolean matched;
        private Node last;
        private final Iterator<Node> iterator;

        public StopMatchResult(boolean matched, Node last, Iterator<Node> iterator) {
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

    private class ActionMatcher {
        private final Application app;
        private boolean alwaysMatch = false;
        private Node action;

        public ActionMatcher(Application app, String actionLabel) {
            this.app = app;
            if (actionLabel.equals("*")) {
                this.alwaysMatch = true;
            } else {
                this.action = app.actions().findByActionLabel(actionLabel);
            }
        }

        public boolean match(Node request) {
            return alwaysMatch || (action != null && app.requests().action(request).equals(action));
        }
    }
}
