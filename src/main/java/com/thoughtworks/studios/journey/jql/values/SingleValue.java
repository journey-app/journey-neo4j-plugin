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
