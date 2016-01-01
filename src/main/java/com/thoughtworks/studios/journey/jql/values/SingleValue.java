package com.thoughtworks.studios.journey.jql.values;

import org.neo4j.helpers.collection.IteratorUtil;

import java.util.Iterator;

public class SingleValue implements JQLValue {
    public Object getWrapped() {
        return wrapped;
    }

    private Object wrapped;

    public SingleValue(Object wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SingleValue jqlValue = (SingleValue) o;

        return !(wrapped != null ? !wrapped.equals(jqlValue.wrapped) : jqlValue.wrapped != null);

    }

    @Override
    public int hashCode() {
        return wrapped != null ? wrapped.hashCode() : 0;
    }

    @Override
    public Iterator<JQLValue> iterator() {
        return IteratorUtil.<JQLValue>iterator(this);
    }

    @Override
    public String toString() {
        return "single-value:" + wrapped.toString();
    }

    @Override
    public Object serializable() {
        if(wrapped instanceof JQLValue) {
            return ((JQLValue) wrapped).serializable();
        }
        return wrapped;
    }
}
