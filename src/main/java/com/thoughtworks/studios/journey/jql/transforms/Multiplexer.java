package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;

import java.util.ArrayList;
import java.util.List;

public class Multiplexer implements ColumnTransformFn {
    private ValueTransformFn valueTransform;

    public Multiplexer(ValueTransformFn valueTransform) {
        this.valueTransform = valueTransform;
    }

    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        ArrayList<Tuple> result = new ArrayList<>(column.size());
        for (Tuple tuple : column) {
            result.add(tuple.apply(valueTransform, params));
        }
        return result;
    }
}
