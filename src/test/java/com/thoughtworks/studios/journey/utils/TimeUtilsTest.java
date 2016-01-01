package com.thoughtworks.studios.journey.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {
    @Test
    public void testParseHourToMSec() {
        assertEquals(3600000l, TimeUtils.hoursToMSec(1));
    }

    @Test
    public void timezoneByMinutesOffset() {
        assertEquals("-07:00", TimeUtils.timeZoneByMinutesOffset(-420).getName(0));
    }


}
