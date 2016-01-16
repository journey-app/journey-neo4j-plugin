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
import com.thoughtworks.studios.journey.jql.Stop;
import com.thoughtworks.studios.journey.utils.CollectionUtils;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class StopTest extends ModelTestCase {

    @Test
    public void testStopWithAnAction() {
        Stop.MatchResult match = matchResult(iterable("a0", "a1", "a2", "a3"), stop("a2"));
        assertTrue(match.matched());
        assertEquals("a2", events.getActionLabel(match.last()));
        assertEquals("a3", events.getActionLabel(match.iterator().next()));
    }

    @Test
    public void testNoMatchingWhenActionNotFound() {
        Stop.MatchResult failedMatch = matchResult(iterable("a2", "a3"), stop("a0"));
        assertFalse(failedMatch.matched());
        assertNull(failedMatch.last());
    }

    @Test
    public void testNoMatchingWhenActionExistFromOtherJourney() {
        setupJourney(iterable("a0"), 0L);
        Stop.MatchResult failedMatch = matchResult(iterable("a2", "a3"), stop("a0"));
        assertFalse(failedMatch.matched());
        assertNull(failedMatch.last());
    }


    @Test
    public void testStopWithAnyAction() {
        Stop.MatchResult match = matchResult(iterable("a0", "a1", "a2", "a3"), stop("*"));
        assertTrue(match.matched());
        assertEquals("a0", events.getActionLabel(match.last()));
        assertEquals("a1", events.getActionLabel(match.iterator().next()));
    }

    @Test
    public void testStopWithRewind() {
        Stop.MatchResult match = matchResult(iterable("a0", "a1", "a2"), stop("*", CollectionUtils.<String>list(), true));
        assertTrue(match.matched());
        assertEquals("a0", events.getActionLabel(match.last()));
        assertEquals("a0", events.getActionLabel(match.iterator().next()));
    }

    private Stop.MatchResult matchResult(Iterable<Object> actions, Map<String, Object> stopAttr) {
        Node journey = setupJourney(actions, dateToMillis(2014, 7, 7, 10));
        return Stop.build(app, stopAttr).match(journeys.eventIterator(journey, true));
    }

}