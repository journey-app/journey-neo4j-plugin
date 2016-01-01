package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.utils.ArrayUtils;

public class CurryValueFn implements ValueTransformFn {
    private final ValueTransformFn fn;
    private final String[] params;

    public CurryValueFn(ValueTransformFn fn, String[] params) {
        this.fn = fn;
        this.params = params;
    }

    @Override
    public JQLValue apply(JQLValue value, String... params) {
        return fn.apply(value, ArrayUtils.concat(this.params, params));
    }
}
