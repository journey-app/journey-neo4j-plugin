/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
 *
 * Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
