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
