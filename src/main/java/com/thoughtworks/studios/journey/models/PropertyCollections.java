/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
