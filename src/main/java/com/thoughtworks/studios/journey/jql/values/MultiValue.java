package com.thoughtworks.studios.journey.jql.values;

import com.thoughtworks.studios.journey.jql.Values;
import org.neo4j.function.Function;
import org.neo4j.helpers.collection.Iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class MultiValue<T> implements JQLValue {
    private Set<T> wrapped;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiValue that = (MultiValue) o;

        return !(wrapped != null ? !wrapped.equals(that.wrapped) : that.wrapped != null);

    }

    @Override
    public int hashCode() {
        return wrapped != null ? wrapped.hashCode() : 0;
    }

    public MultiValue(Set<T> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Iterator<JQLValue> iterator() {
        return Iterables.map(new Function<Object, JQLValue>() {
            @Override
            public JQLValue apply(Object o) {
                return Values.wrapSingle(o);
            }
        }, wrapped).iterator();
    }

    @Override
    public Object serializable() {

        ArrayList<Object> result = new ArrayList<>(wrapped.size());
        for (Object w : wrapped) {
            if (w instanceof JQLValue) {
                result.add(((JQLValue) w).serializable());
            } else {
                result.add(w);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "multi-value:" + wrapped.toString();
    }
}
