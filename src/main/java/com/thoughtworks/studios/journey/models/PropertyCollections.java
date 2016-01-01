package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.utils.GraphDbUtils;
import org.apache.commons.lang.NotImplementedException;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.thoughtworks.studios.journey.utils.IterableUtils.toIterable;

public abstract class PropertyCollections implements Models {
    private static final String PROP_NAME = "name";
    private static final String REL_PROP_VALUE = "value";
    protected Application app;

    public abstract Label getLabel();

    protected abstract RelationshipType propertyRelType();

    @Override
    public void setupSchema() {
        GraphDbUtils.createIndexIfNotExists(app.graphDB(), getLabel(), PROP_NAME);
    }

    @Override
    public Map<String, Object> toHash(Node property) {
        throw new NotImplementedException();
    }

    public PropertyCollections(Application application) {
        app = application;
    }

    private Node findOrCreate(String propertyName) {
        Node property = find(propertyName);
        if (property != null) {
            return property;
        }
        Node node = app.graphDB().createNode(getLabel());
        node.setProperty(PROP_NAME, propertyName);
        return node;
    }

    public String getName(Node property) {
        return (String) property.getProperty(PROP_NAME);
    }

    public Iterable<Node> all() {
        return toIterable(app.graphDB().findNodes(getLabel()));
    }

    public Node find(String name) {
        return app.graphDB().findNode(getLabel(), PROP_NAME, name);
    }

    public Set<Object> getProperty(Node node, String propertyName) {
        Set<Object> result = new HashSet<>();
        Node property = find(propertyName);
        if (property == null) {
            return result;
        }
        Iterable<Relationship> relationships = node.getRelationships(propertyRelType(), Direction.OUTGOING);
        for (Relationship relationship : relationships) {
            if (property.equals(relationship.getEndNode())) {
                result.add(relationship.getProperty(REL_PROP_VALUE));
            }
        }
        return result;
    }

    public Map<String, Set> properties(Node node) {
        HashMap<String, Set> result = new HashMap<>();
        Iterable<Relationship> relationships = node.getRelationships(propertyRelType(), Direction.OUTGOING);
        for (Relationship relationship : relationships) {
            String name = getName(relationship.getEndNode());
            if (!result.containsKey(name)) {
                result.put(name, new HashSet(1));
            }
            Set values = result.get(name);
            //noinspection unchecked
            values.add(relationship.getProperty(REL_PROP_VALUE));
        }
        return result;
    }

    public void setProperty(Node node, String propertyName, Object value) {
        Set<Object> existingValues = getProperty(node, propertyName);
        if (!existingValues.contains(value)) {
            Node property = findOrCreate(propertyName);
            writePropertyValue(node, property, value);
        }
    }

    private void writePropertyValue(Node node, Node property, Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof Iterable) {
            for (Object v : (Iterable) value) {
                writePropertyValue(node, property, v);
            }
            return;
        }
        Relationship rel = node.createRelationshipTo(property, propertyRelType());
        rel.setProperty(REL_PROP_VALUE, value);
    }


    public Set sampleValues(Node trait, int sampleLimit) {
        HashSet<Object> result = new HashSet<>();
        Iterable<Relationship> relationships = trait.getRelationships();
        Iterable<Relationship> limited = Iterables.limit(sampleLimit, relationships);
        for (Relationship relationship : limited) {
            result.add(relationship.getProperty(REL_PROP_VALUE));
        }
        return result;
    }
}
