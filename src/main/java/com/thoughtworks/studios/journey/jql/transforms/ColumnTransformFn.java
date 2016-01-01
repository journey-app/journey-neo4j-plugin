package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;

import java.util.List;

public interface ColumnTransformFn {
    List<Tuple> apply(List<Tuple> column, String... params);
}
