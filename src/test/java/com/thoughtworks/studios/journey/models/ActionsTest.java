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
