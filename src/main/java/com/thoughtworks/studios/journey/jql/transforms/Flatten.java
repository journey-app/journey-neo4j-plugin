package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Flatten implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        ArrayList<Tuple> result = new ArrayList<>(column.size());
        for (Tuple tuple : column) {
            for (Tuple flattened : tuple) {
                result.add(flattened);
            }
        }
        return result;
    }
}
