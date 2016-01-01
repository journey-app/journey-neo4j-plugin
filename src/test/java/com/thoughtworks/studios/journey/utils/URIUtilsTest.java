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
package com.thoughtworks.studios.journey.utils;

import org.junit.Test;

import static com.thoughtworks.studios.journey.utils.URIUtils.queryValue;
import static com.thoughtworks.studios.journey.utils.URIUtils.topDomain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class URIUtilsTest {
    @Test
    public void testTopDomain() throws Exception {
        assertNull(topDomain(null));
        assertEquals("google.com", topDomain("https://google.com/foo/bar"));
        assertNull(topDomain("http:\\sss"));
    }

    @Test
    public void testQueryValue() throws Exception {
        assertNull(queryValue(null, "foo"));
        assertNull(queryValue("http://ssss.com/?a=b", null));
        assertNull(queryValue("http://ssss.com/?a=b", "b"));
        assertEquals("b", queryValue("http://ssss.com/?a=b", "a"));

        String url = "https://www.thoughtworks.com/mingle/signup/?tag=utm_source=google&utm_medium=cpc&utm_campaign=Mingle%20-%20Display&utm_term=&utm_content=77216695484&campaignid=245538644&adgroupid=20372377004&feeditemid=&targetid=&loc_physical_ms=1007737&loc_interest_ms=&matchtype=&network=d&device=m&devicemodel=opera%2Bmini%2B7&creative=77216695484&keyword=&placement=www.vpnbook.com&target=%2Fcomputers%20%26%20electronics&param1=&param2=&random=2710191141436186542&aceid=&adposition=none&gclid=CJ3YwevL1sgCFZNgfgodAXIKaw";
        assertEquals("google", queryValue(url, "tag"));
        assertEquals("google", queryValue(url, "utm_source"));
        assertEquals("cpc", queryValue(url, "utm_medium"));
        assertEquals("Mingle - Display", queryValue(url, "utm_campaign"));
        assertNull(queryValue(url, "param2"));
    }
}
