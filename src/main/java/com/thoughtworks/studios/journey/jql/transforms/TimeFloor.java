package com.thoughtworks.studios.journey.jql.transforms;

import com.thoughtworks.studios.journey.jql.Values;
import com.thoughtworks.studios.journey.jql.values.JQLValue;
import com.thoughtworks.studios.journey.jql.values.NullValue;
import com.thoughtworks.studios.journey.jql.values.SingleValue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class TimeFloor implements ValueTransformFn {
    @Override
    public JQLValue apply(JQLValue value, String... params) {
        TimeGroupingInterval interval = TimeGroupingInterval.valueOf(params[0].toUpperCase());
        DateTimeZone timezone = DateTimeZone.UTC;

        if (params.length > 1) {
            timezone = DateTimeZone.forID(params[1]);
        }

        if (value == NullValue.instance) {
            return value;
        }

        SingleValue v = (SingleValue) value;
        Long ts = (Long) v.getWrapped();
        return Values.wrapSingle(interval.floor(ts, timezone));
    }

    private enum TimeGroupingInterval {
        DAY {
            public DateTime getPreviousStart(DateTime endTime) {
                return endTime.withTime(0, 0, 0, 0);
            }
        },

        WEEK {
            public DateTime getPreviousStart(DateTime endTime) {
                return endTime.withDayOfWeek(DateTimeConstants.MONDAY).withTime(0, 0, 0, 0);
            }

        },

        MONTH {
            public DateTime getPreviousStart(DateTime endTime) {
                return endTime.withDayOfMonth(1).withTime(0, 0, 0, 0);
            }
        };

        public abstract DateTime getPreviousStart(DateTime endTime);

        public Long floor(Long timestamp, DateTimeZone timeZone) {
            return getPreviousStart(new DateTime(timestamp, timeZone)).getMillis();
        }

    }
}
