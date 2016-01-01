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
package com.thoughtworks.studios.journey.jql;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.studios.journey.jql.transforms.ColumnTransformFn;

import java.util.ArrayList;
import java.util.List;

public class DataQueryResult {
    private List<List<Tuple>> columns;
    private String errors;

    public DataQueryResult(int numOfColumns) {
        columns = new ArrayList<>(numOfColumns);
        for (int i = 0; i < numOfColumns; i++) {
            columns.add(new ArrayList<Tuple>());
        }
    }

    public List<List<Tuple>> data() {
        return columns;
    }

    @JsonProperty("data")
    public List<List<Object[]>> jsonData() {
        ArrayList<List<Object[]>> result = new ArrayList<>(columns.size());
        for (List<Tuple> column : columns) {
            List<Object[]> serializableColumn = new ArrayList<>();
            for (Tuple tuple : column) {
                serializableColumn.add(tuple.serializations());
            }
            result.add(serializableColumn);
        }
        return result;
    }

    public void apply(ColumnTransformFn fn) {
        for (int i = 0; i < columns.size(); i++) {
            List<Tuple> column = columns.get(i);
            columns.set(i, fn.apply(column));
        }
    }

    public void addRows(Tuple[] row) {
        for (int i = 0; i < row.length; i++) {
            List<Tuple> column = columns.get(i);
            column.add(row[i]);
        }
    }

    public String getErrors() {
        return errors;
    }

    public static DataQueryResult error(Exception e) {
        DataQueryResult result = new DataQueryResult(0);
        result.errors = e.getClass().getSimpleName() + ": " + e.getMessage();
        return result;
    }
}
