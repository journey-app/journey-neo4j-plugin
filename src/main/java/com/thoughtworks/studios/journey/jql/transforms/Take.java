package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Take implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        int index = Integer.parseInt(params[0]);
        ArrayList<Tuple> result = new ArrayList<>(column.size());
        for (Tuple tuple : column) {
            result.add(tuple.take(index));
        }
        return result;
    }
}
