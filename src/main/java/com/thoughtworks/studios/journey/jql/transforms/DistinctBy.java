package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.values.JQLValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DistinctBy implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        int index = Integer.valueOf(params[0]);
        HashSet<JQLValue> set = new HashSet<>();
        ArrayList<Tuple> result = new ArrayList<>();
        for (Tuple tuple : column) {
            JQLValue by = tuple.get(index);
            if(set.contains(by)) {
                continue;
            }
            set.add(by);
            result.add(tuple);
        }
        return result;
    }
}
