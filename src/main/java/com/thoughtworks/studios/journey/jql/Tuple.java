/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
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
package com.thoughtworks.studios.journey.jql;

import com.thoughtworks.studios.journey.jql.transforms.ValueTransformFn;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Tuple implements Iterable<Tuple> {
    private final JQLValue[] values;

    public Tuple(int size) {
        this.values = new JQLValue[size];
    }

    public Tuple(JQLValue[] values) {
        this.values = values;
    }

    public Object[] serializations() {
        Object[] serializations = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            JQLValue value = values[i];
            serializations[i] = value.serializable();
        }
        return serializations;
    }

    public boolean isAllNull() {
        for (JQLValue value : values) {
            if (value != NullValue.instance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        if (!Arrays.equals(values, tuple.values)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    public static Tuple single(JQLValue value) {
        return new Tuple(new JQLValue[]{value});
    }

    @Override
    public Iterator<Tuple> iterator() {
        if (values.length > 1) {
            throw new DataQueryError("can not flatten a tuple with more than one elements");
        }
        List<Tuple> list = new ArrayList<>();
        for (JQLValue v : values[0]) {
            list.add(Tuple.single(v));
        }
        return list.iterator();
    }

    public Tuple append(JQLValue value) {
        JQLValue[] output = new JQLValue[values.length + 1];
        System.arraycopy(values, 0, output, 0, values.length);
        output[values.length] = value;
        return new Tuple(output);
    }

    public Tuple apply(ValueTransformFn fn, String... params) {
        JQLValue[] output = new JQLValue[values.length];
        for (int i = 0; i < values.length; i++) {
            JQLValue value = values[i];
            output[i] = fn.apply(value, params);
        }
        return new Tuple(output);
    }

    public void set(int index, JQLValue value) {
        values[index] = value;
    }

    @Override
    public String toString() {
        return "(" + StringUtils.join(values) + ')';
    }

    public Tuple take(int index) {
        if (index < 0 || index > values.length - 1) {
            throw new IllegalArgumentException("Index is out of tuple boundary");
        }
        return Tuple.single(values[index]);
    }

    public Tuple drop(int index) {
        if (index < 0 || index > values.length - 1) {
            throw new IllegalArgumentException("Index is out of tuple boundary");
        }

        JQLValue[] output = new JQLValue[values.length - 1];
        System.arraycopy(values, 0, output, 0, index);
        System.arraycopy(values, index + 1, output, index, values.length - index - 1);
        return new Tuple(output);
    }

    public JQLValue get(int index) {
        if (index < 0 || index > values.length - 1) {
            throw new IllegalArgumentException("Index is out of tuple boundary");
        }
        return values[index];
    }
}
