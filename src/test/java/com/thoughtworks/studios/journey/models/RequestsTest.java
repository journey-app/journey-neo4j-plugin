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
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;
import static org.neo4j.helpers.collection.IteratorUtil.first;

public class RequestsTest extends ModelTestCase {
    @Test
    public void shouldCreateNodeOnAddRequest() {
        Map<String, Object> attrs = createRequestAttributesWithDigest("tb3sd", "tt25");
        attrs.put("http_method", "POST");
        attrs.put("url", "/url");
        attrs.put("start_at", 100L);
        attrs.put("finish_at", 150L);
        requests.add(attrs);
        Node node = requests.findByDigest("tt25");
        assertNotNull(node);
        assertEquals("POST", requests.getHttpMethod(node));
        assertEquals("/url", requests.getUrl(node));
        assertEquals((Long) 100L, requests.getStartAt(node));
    }

    @Test
    public void shouldSkipRequestThatDigestAlreadyExist() {
        requests.add(createRequestAttributesWithDigest("session1", "tt25"));
        requests.add(createRequestAttributesWithDigest("session2", "tt25"));
        assertEquals(1, requests.count());
    }


    @Test
    public void shouldSkipNullProperties() {
        Map<String, Object> attrs = createRequestAttributesWithDigest("session1", "tt25");
        attrs.put("url", null);
        Node req = requests.add(attrs);
        assertEquals(null, requests.getUrl(req));
    }

    @Test
    public void shouldCreateJourneyWhenAddSingleRequest() {
        Node req = requests.add(createRequestAttributes("s1"));

        assertEquals(1, journeys.count());
        Node journey = first(journeys.findAllBySessionId("s1"));
        assertEquals(requests.getStartAt(req), journeys.getStartAt(journey));
        assertEquals(journey, requests.journeyOf(req));
    }

    @Test
    public void shouldAddToSameJourneyWhenAddingMultipleRequestWithSameSessionId() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 300L));
        assertEquals(1, journeys.count());
        assertEquals(requests.journeyOf(req1), requests.journeyOf(req2));
        assertIterableEquals(iterable(req1, req2), journeys.userRequests(requests.journeyOf(req1)));
    }

    @Test
    public void shouldCreateSeparatedJourneyIfSessionIdIsMatchButUIDIsNot() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L, "u1"));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 300L, "u2"));
        assertEquals(2, journeys.count());
        assertNotEquals(requests.journeyOf(req1), requests.journeyOf(req2));
        assertIterableEquals(iterable(req1), journeys.userRequests(requests.journeyOf(req1)));
        assertIterableEquals(iterable(req2), journeys.userRequests(requests.journeyOf(req2)));
    }

    @Test
    public void shouldAttacheToSameJourneyIfSessionIdMatchButUserBecomeAnonymous() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L, "u1"));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 300L));
        assertEquals(1, journeys.count());
        assertEquals(requests.journeyOf(req1), requests.journeyOf(req2));
        assertIterableEquals(iterable(req1, req2), journeys.userRequests(requests.journeyOf(req1)));
    }


    @Test
    public void shouldAddMultipleRequestToSameJourneyWithTimeOrder() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 50L));
        Node req3 = requests.add(createRequestAttributes("s1", "a1", 200L));
        Node req4 = requests.add(createRequestAttributes("s1", "a1", 150L));
        assertIterableEquals(iterable(req2, req1, req4, req3), journeys.userRequests(requests.journeyOf(req1)));
    }

    @Test
    public void shouldUpdateJourneyRangeAfterAddRequest() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L));
        requests.add(createRequestAttributes("s1", "a1", 50L));
        requests.add(createRequestAttributes("s1", "a1", 200L));
        Node journey = requests.journeyOf(req1);
        assertEquals((Object) 50L, journeys.getStartAt(journey));
        assertEquals((Object) 200L, journeys.getFinishAt(journey));
    }

    @Test
    public void shouldCutJourneyWhenNextRequestHappenedTwoHourAfter() {
        Node req1 = requests.add(createRequestAttributes("s1", "a0", 100L));
        Node req2 = requests.add(createRequestAttributes("s1", "a1", 100L + 2 * 60 * 60 * 1000L));
        Node req3 = requests.add(createRequestAttributes("s1", "a1", 100L + 4 * 60 * 60 * 1000L + 1L));
        assertEquals(requests.journeyOf(req1), requests.journeyOf(req2));
        assertNotEquals(requests.journeyOf(req1), requests.journeyOf(req3));
        assertEquals(journeys.getSessionId(requests.journeyOf(req1)),
                journeys.getSessionId(requests.journeyOf(req2)));
        assertEquals(journeys.getSessionId(requests.journeyOf(req1)),
                journeys.getSessionId(requests.journeyOf(req3)));

    }

    @Test
    public void shouldPersistCustomProperties() {
        Node req = requests.add(createRequestAttributes("s1", "a0", 100L, mapOf("k", "v", "k2", "v2")));
        assertEquals(mapOf("k", set("v"), "k2", set("v2")), requests.properties(req));
        assertEquals(set("v"), requests.values(req, "k"));
        assertEquals(set("v2"), requests.values(req, "k2"));

    }

    @Test
    public void shouldPersistMultiValueProperties() {
        Node req = requests.add(createRequestAttributes("s1", "a0", 100L, mapOf("k", list("v1", "v2", "v3"))));
        assertEquals(mapOf("k", set("v1", "v2", "v3")), requests.properties(req));
        assertEquals(set("v1", "v2", "v3"), requests.values(req, "k"));
    }

    @Test
    public void shouldSkipNullValues() {
        Node req = requests.add(createRequestAttributes("s1", "a0", 100L, mapOf("k", "v", "k1", null)));
        assertEquals(mapOf("k", set("v")), requests.properties(req));
        assertEquals(set(), requests.values(req, "k1"));
    }


}
