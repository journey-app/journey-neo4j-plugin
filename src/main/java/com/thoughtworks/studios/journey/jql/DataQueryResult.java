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
