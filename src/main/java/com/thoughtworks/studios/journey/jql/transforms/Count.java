package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.Values;

import java.util.Collections;
import java.util.List;

public class Count implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        return Collections.singletonList(Tuple.single(Values.wrapSingle(column.size())) );
    }
}
