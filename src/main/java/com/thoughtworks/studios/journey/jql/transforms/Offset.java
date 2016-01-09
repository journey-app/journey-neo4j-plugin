package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import org.neo4j.helpers.collection.Iterables;

public class Offset implements ColumnTransformFn {
    @Override
    public Iterable<Tuple> apply(Iterable<Tuple> column, String... params) {
        Integer offset = Integer.valueOf(params[0]);
        return Iterables.skip(offset, column);
    }
}
