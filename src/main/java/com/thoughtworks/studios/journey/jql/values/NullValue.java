package com.thoughtworks.studios.journey.jql.values;

import org.neo4j.helpers.collection.IteratorUtil;

import java.util.Iterator;

public class NullValue implements JQLValue {
    public static JQLValue instance = new NullValue();

    private NullValue() {
        super();
    }

    @Override
    public Iterator<JQLValue> iterator() {
        return IteratorUtil.<JQLValue>iterator(this);
    }

    @Override
    public Object serializable() {
        return null;
    }

    @Override
    public String toString() {
        return "null-value";
    }
}
