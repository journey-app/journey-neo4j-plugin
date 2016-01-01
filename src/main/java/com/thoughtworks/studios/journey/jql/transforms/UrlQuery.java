package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.jql.values.SingleValue;
import com.thoughtworks.studios.journey.utils.URIUtils;

public class UrlQuery implements ValueTransformFn {

    @Override
    public JQLValue apply(JQLValue value, String... params) {
        String field = params[0];

        if (value == NullValue.instance) {
            return value;
        }

        if (value instanceof SingleValue) {
            String queryValue = URIUtils.queryValue((String) ((SingleValue) value).getWrapped(), field);
            return Values.wrapSingle(queryValue);
        }

        throw new RuntimeException("Can not apply function url_query to " + value.toString());
    }
}
