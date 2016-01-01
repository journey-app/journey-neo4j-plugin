package com.thoughtworks.studios.journey.jql.values;

import com.thoughtworks.studios.journey.models.Models;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IteratorUtil;

import java.util.Iterator;

public class ModelValue implements JQLValue {
    private final Node node;
    private final Models models;

    public ModelValue(Node node, Models models) {
        this.node = node;
        this.models = models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelValue jqlValues = (ModelValue) o;

        return !(node != null ? !node.equals(jqlValues.node) : jqlValues.node != null);

    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }

    @Override
    public Object serializable() {
        return models.toHash(node);
    }

    @Override
    public Iterator<JQLValue> iterator() {
        return IteratorUtil.<JQLValue>iterator(this);
    }
}
