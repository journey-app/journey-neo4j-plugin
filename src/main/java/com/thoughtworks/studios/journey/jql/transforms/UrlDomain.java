package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.jql.values.SingleValue;
import com.thoughtworks.studios.journey.utils.URIUtils;

public class UrlDomain implements ValueTransformFn {

    @Override
    public JQLValue apply(JQLValue value, String... params) {
        if (value == NullValue.instance) {
            return value;
        }

        if (value instanceof SingleValue) {
            String url = (String) ((SingleValue) value).getWrapped();
            return Values.wrapSingle(URIUtils.topDomain(url));
        }

        throw new RuntimeException("Can not apply function url_query to " + value.toString());
    }
}
