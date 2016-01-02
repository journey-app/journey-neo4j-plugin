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
package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.values.JQLValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DistinctBy implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        int index = Integer.valueOf(params[0]);
        HashSet<JQLValue> set = new HashSet<>();
        ArrayList<Tuple> result = new ArrayList<>();
        for (Tuple tuple : column) {
            JQLValue by = tuple.get(index);
            if(set.contains(by)) {
                continue;
            }
            set.add(by);
            result.add(tuple);
        }
        return result;
    }
}
