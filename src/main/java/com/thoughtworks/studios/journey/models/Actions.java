package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;

import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.*;
import static com.thoughtworks.studios.journey.utils.IterableUtils.toIterable;
import static org.neo4j.helpers.collection.Iterables.filter;
import static org.neo4j.helpers.collection.Iterables.first;

public class Actions implements Models {
    private static final String PROP_ACTION_LABEL = "label";
    private static final String PROP_IGNORED = "ignored";
    private final Application app;
    private final GraphDatabaseService graphDb;


    public Actions(Application application) {
        this.app = application;
        graphDb = this.app.graphDB();
    }

    public void setupSchema() {
        createIndexIfNotExists(graphDb, getLabel(), PROP_ACTION_LABEL);
    }

    public Label getLabel() {
        return app.nameSpacedLabel("Action");
    }

    @Override
    public Map<String, Object> toHash(Node action) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("label", getActionLabel(action));
        if (isIgnored(action)) {
            result.put("ignored", true);
        }
        return result;
    }

    public String getActionLabel(Node action) {
        return (String) action.getProperty(PROP_ACTION_LABEL);
    }

    public Node findOrCreateByActionLabel(String actionLabel) {
        Node exist = findByActionLabel(actionLabel);
        if (exist != null) {
            return exist;
        } else {
            Node fresh = graphDb.createNode(getLabel());
            fresh.setProperty(PROP_ACTION_LABEL, actionLabel);
            return fresh;
        }
    }

    public String getHttpMethod(Node action) {
        Node firstRequest = first(requests(action));
        return app.requests().getHttpMethod(firstRequest);
    }

    public Node findByActionLabel(String actionLabel) {
        return graphDb.findNode(getLabel(), PROP_ACTION_LABEL, actionLabel);
    }


    public void addRequest(Node action, Node request) {
        connectSingle(request, RelTypes.ACTION, action);
    }


    public Iterable<Node> allExcludeIgnored() {
        return filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node action) {
                return !isIgnored(action);
            }
        }, all());
    }

    public Iterable<Node> all() {
        return toIterable(graphDb.findNodes(getLabel()));
    }

    public Iterable<Node> allIgnored() {
        return filter(new Predicate<Node>() {
            @Override
            public boolean accept(Node action) {
                return isIgnored(action);
            }
        }, all());
    }


    public Iterable<Node> requests(Node action) {
        return getStartNodes(action, RelTypes.ACTION);
    }

    public void ignore(Node action) {
        action.setProperty(PROP_IGNORED, true);
    }

    public boolean isIgnored(Node action) {
        return action.hasProperty(PROP_IGNORED);
    }

    public void unIgnore(Node action) {
        action.removeProperty(PROP_IGNORED);
    }

}
