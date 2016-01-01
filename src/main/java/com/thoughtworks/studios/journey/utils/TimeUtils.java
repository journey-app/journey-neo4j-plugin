package com.thoughtworks.studios.journey.utils;

import org.joda.time.DateTimeZone;

public class TimeUtils {
    public static long hoursToMSec(int hours) {
        return hours * 3600000;
    }

    public static DateTimeZone timeZoneByMinutesOffset(int tzOffsetInMinutes) {
        int hours = tzOffsetInMinutes / 60;
        int minutes = tzOffsetInMinutes % 60;
        return DateTimeZone.forOffsetHoursMinutes(hours, minutes);
    }
}
