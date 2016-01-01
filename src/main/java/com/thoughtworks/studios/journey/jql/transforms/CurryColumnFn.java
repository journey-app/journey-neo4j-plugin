package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Tuple;
import com.thoughtworks.studios.journey.utils.ArrayUtils;

import java.util.List;

public class CurryColumnFn implements ColumnTransformFn {
    private final ColumnTransformFn original;
    private final String[] params;

    public CurryColumnFn(ColumnTransformFn original, String[] params) {
        this.original = original;
        this.params = params;
    }

    @Override
    public List<Tuple> apply(List<Tuple> column, String... params) {
        return original.apply(column, ArrayUtils.concat(this.params, params));
    }
}
