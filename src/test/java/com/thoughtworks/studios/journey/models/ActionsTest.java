package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.utils.MapUtils;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ActionsTest extends ModelTestCase {

    @Test
    public void importShouldCreateActions() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 50L));
        Node req3 = requests.add(createRequestAttributes("s1", "a1", 200L));

        assertEquals("a0", requests.getActionLabel(req1));
        assertEquals("a1", requests.getActionLabel(req2));
        assertEquals("a1", requests.getActionLabel(req3));
        assertNotEquals(requests.action(req2), requests.action(req1));
        assertEquals(requests.action(req2), requests.action(req3));
    }

    @Test
    public void allShouldExcludeIgnoredActions() {
        requests.add(createRequestAttributes("s1", "a0", dateToMillis(2014, 12, 5)));
        requests.add(createRequestAttributes("s1", "a1", dateToMillis(2014, 12, 6)));
        requests.add(createRequestAttributes("s1", "a2", dateToMillis(2014, 12, 6)));

        actions.ignore(actions.findByActionLabel("a1"));
        assertIterableEquals(list(action("a0"), action("a2")), actions.allExcludeIgnored());
        actions.unIgnore(action("a1"));
        assertIterableEquals(list(action("a0"), action("a1"), action("a2")), actions.allExcludeIgnored());
    }

    @Test
    public void tesGetHttpMethodOfAnAction() {
        requests.add(createRequestAttributes("s1", "a2", dateToMillis(2014, 12, 7), "http://foo.bar.com", "POST", "d2", "u2", MapUtils.<String, Object>mapOf()));

        Node a2 = actions.findByActionLabel("a2");
        assertEquals("POST", actions.getHttpMethod(a2));
    }
}
