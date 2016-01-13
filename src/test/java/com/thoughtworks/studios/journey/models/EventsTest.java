/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
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

public class EventsTest extends ModelTestCase {
    @Test
    public void shouldCreateNodeOnAddEvent() {
        Map<String, Object> attrs = createEventAttributesWithDigest("tb3sd", "tt25");
        attrs.put("http_method", "POST");
        attrs.put("url", "/url");
        attrs.put("start_at", 100L);
        attrs.put("finish_at", 150L);
        events.add(attrs);
        Node node = events.findByDigest("tt25");
        assertNotNull(node);
        assertEquals("POST", events.getHttpMethod(node));
        assertEquals("/url", events.getUrl(node));
        assertEquals((Long) 100L, events.getStartAt(node));
    }

    @Test
    public void shouldSkipEventThatDigestAlreadyExist() {
        events.add(createEventAttributesWithDigest("session1", "tt25"));
        events.add(createEventAttributesWithDigest("session2", "tt25"));
        assertEquals(1, events.count());
    }


    @Test
    public void shouldSkipNullProperties() {
        Map<String, Object> attrs = createEventAttributesWithDigest("session1", "tt25");
        attrs.put("url", null);
        Node event = events.add(attrs);
        assertEquals(null, events.getUrl(event));
    }

    @Test
    public void shouldCreateJourneyWhenAddSingleEvent() {
        Node event = events.add(createEventAttributes("s1"));
        assertEquals(1, journeys.count());
        Node journey = first(journeys.findAllBySessionId("s1"));
        assertEquals(events.getStartAt(event), journeys.getStartAt(journey));
        assertEquals(journey, events.journeyOf(event));
    }

    @Test
    public void shouldAddToSameJourneyWhenAddingMultipleEventWithSameSessionId() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L));
        Node event2 = events.add(createEventAttributes("s1", "a1", 300L));
        assertEquals(1, journeys.count());
        assertEquals(events.journeyOf(event1), events.journeyOf(event2));
        assertIterableEquals(iterable(event1, event2), journeys.events(events.journeyOf(event1)));
    }

    @Test
    public void shouldCreateSeparatedJourneyIfSessionIdIsMatchButUIDIsNot() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L, "u1"));
        Node event2 = events.add(createEventAttributes("s1", "a1", 300L, "u2"));
        assertEquals(2, journeys.count());
        assertNotEquals(events.journeyOf(event1), events.journeyOf(event2));
        assertIterableEquals(iterable(event1), journeys.events(events.journeyOf(event1)));
        assertIterableEquals(iterable(event2), journeys.events(events.journeyOf(event2)));
    }

    @Test
    public void shouldAttacheToSameJourneyIfSessionIdMatchButUserBecomeAnonymous() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L, "u1"));
        Node event2 = events.add(createEventAttributes("s1", "a1", 300L));
        assertEquals(1, journeys.count());
        assertEquals(events.journeyOf(event1), events.journeyOf(event2));
        assertIterableEquals(iterable(event1, event2), journeys.events(events.journeyOf(event1)));
    }


    @Test
    public void shouldAddMultipleEventToSameJourneyWithTimeOrder() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L));
        Node event2 = events.add(createEventAttributes("s1", "a1", 50L));
        Node event3 = events.add(createEventAttributes("s1", "a1", 200L));
        Node event4 = events.add(createEventAttributes("s1", "a1", 150L));
        assertIterableEquals(iterable(event2, event1, event4, event3), journeys.events(events.journeyOf(event1)));
    }

    @Test
    public void shouldUpdateJourneyRangeAfterAddEvent() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L));
        events.add(createEventAttributes("s1", "a1", 50L));
        events.add(createEventAttributes("s1", "a1", 200L));
        Node journey = events.journeyOf(event1);
        assertEquals((Object) 50L, journeys.getStartAt(journey));
        assertEquals((Object) 200L, journeys.getFinishAt(journey));
    }

    @Test
    public void shouldCutJourneyWhenNextEventHappenedTwoHourAfter() {
        Node event1 = events.add(createEventAttributes("s1", "a0", 100L));
        Node event2 = events.add(createEventAttributes("s1", "a1", 100L + 2 * 60 * 60 * 1000L));
        Node event3 = events.add(createEventAttributes("s1", "a1", 100L + 4 * 60 * 60 * 1000L + 1L));
        assertEquals(events.journeyOf(event1), events.journeyOf(event2));
        assertNotEquals(events.journeyOf(event1), events.journeyOf(event3));
        assertEquals(journeys.getSessionId(events.journeyOf(event1)),
                journeys.getSessionId(events.journeyOf(event2)));
        assertEquals(journeys.getSessionId(events.journeyOf(event1)),
                journeys.getSessionId(events.journeyOf(event3)));

    }

    @Test
    public void shouldPersistCustomProperties() {
        Node event = events.add(createEventAttributes("s1", "a0", 100L, mapOf("k", "v", "k2", "v2")));
        assertEquals(mapOf("k", set("v"), "k2", set("v2")), events.properties(event));
        assertEquals(set("v"), events.values(event, "k"));
        assertEquals(set("v2"), events.values(event, "k2"));
    }

    @Test
    public void shouldPersistMultiValueProperties() {
        Node event = events.add(createEventAttributes("s1", "a0", 100L, mapOf("k", list("v1", "v2", "v3"))));
        assertEquals(mapOf("k", set("v1", "v2", "v3")), events.properties(event));
        assertEquals(set("v1", "v2", "v3"), events.values(event, "k"));
    }

    @Test
    public void shouldSkipNullValues() {
        Node event = events.add(createEventAttributes("s1", "a0", 100L, mapOf("k", "v", "k1", null)));
        assertEquals(mapOf("k", set("v")), events.properties(event));
        assertEquals(set(), events.values(event, "k1"));
    }

    @Test
    public void shouldSkipEventWithUnreasonableTime() {
        assertNull(events.add(createEventAttributes("s1", "a0", dateToMillis(3000, 1, 4))));
    }
}
