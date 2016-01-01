package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.jql.values.SingleValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class ToDate implements ValueTransformFn {
    @Override
    public JQLValue apply(JQLValue value, String... params) {
        if (value == NullValue.instance) {
            return value;
        }

        SingleValue v = (SingleValue) value;
        DateTime dateTime = new DateTime(v.getWrapped(), DateTimeZone.UTC);
        return Values.wrapSingle(dateTime);
    }
}
