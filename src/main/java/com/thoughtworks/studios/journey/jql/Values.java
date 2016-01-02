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

import com.thoughtworks.studios.journey.models.Models;
import com.thoughtworks.studios.journey.jql.values.*;
import org.neo4j.graphdb.Node;

import java.util.Set;

public class Values {
    public static JQLValue wrapSingle(Object value) {
        if (value == null) {
            return NullValue.instance;
        } else {
            return new SingleValue(value);
        }
    }

    public static <T> JQLValue wrapMulti(Set<T> values) {
        if (values.isEmpty()) {
            return NullValue.instance;
        } else if (values.size() == 1) {
            return wrapSingle(values.iterator().next());
        } else {
            return new MultiValue<T>(values);
        }
    }

    public static JQLValue wrapModel(Node node, Models models) {
        if(node == null) {
            return NullValue.instance;
        }
        return new ModelValue(node, models);
    }

}
