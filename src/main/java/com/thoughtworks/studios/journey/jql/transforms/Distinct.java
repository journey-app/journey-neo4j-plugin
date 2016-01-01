package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Distinct implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        LinkedHashSet<Tuple> set = new LinkedHashSet<>(column);
        return new ArrayList<>(set);
    }
}
