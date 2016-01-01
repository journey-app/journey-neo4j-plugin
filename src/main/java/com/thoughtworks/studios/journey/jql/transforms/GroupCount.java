package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.utils.MapUtils;

import java.util.*;

public class GroupCount implements ColumnTransformFn {
    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        Map<Tuple, Integer> counts = new LinkedHashMap<>(column.size());
        for (Tuple tuple: column) {
            MapUtils.incrementValue(counts, tuple, 1);
        }

        ArrayList<Tuple> result = new ArrayList<>();
        for (Map.Entry<Tuple, Integer> entry : counts.entrySet()) {
            Tuple key = entry.getKey();
            result.add(key.append(Values.wrapSingle(entry.getValue())));
        }
        return result;
    }
}
