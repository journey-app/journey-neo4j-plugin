package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.values.JQLValue;

public interface ValueTransformFn {
    JQLValue apply(JQLValue value, String... params);
}
