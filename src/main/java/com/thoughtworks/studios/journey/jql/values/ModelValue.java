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
