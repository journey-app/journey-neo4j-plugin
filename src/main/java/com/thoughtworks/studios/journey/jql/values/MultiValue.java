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
