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
package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.utils.MapUtils;

import java.util.*;

public class GroupCount implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        Map<Tuple, Integer> counts = new LinkedHashMap<>(column.size());
        for (Tuple tuple: column) {
            MapUtils.incrementValue(counts, tuple, 1);
        }

        ArrayList<Tuple> result = new ArrayList<>();
        for (Map.Entry<Tuple, Integer> entry : counts.entrySet()) {
            Tuple key = entry.getKey();
            result.add(key.append(Values.wrapSingle(entry.getValue())));
        }
        return result;
    }
}
