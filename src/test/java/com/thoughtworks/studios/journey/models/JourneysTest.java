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
import org.apache.lucene.search.NumericRangeQuery;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.function.Function;
import org.neo4j.graphdb.Node;
import org.neo4j.index.lucene.QueryContext;

import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.*;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.*;

public class JourneysTest extends ModelTestCase {

    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j1 = setupJourney(iterable("a0", "a1"), 0L, 100L, "ur0", "s0");
        j2 = setupJourney(iterable("a1", "a2"), 100L, 100L, "ur1", "s1");
        j3 = setupJourney(iterable("a2", "a3", "a0"), 200L, 100L, null, "s2");
        j4 = setupJourney(iterable("a0", "a1"), 100L + Journeys.CUT_TOLERANT + 200L, 100L, "ur1", "s1");
    }

    @Test
    public void toHashWithEnoughRequestLimit() {
        Map<String, Object> hash = journeys.toHash(j1, 2, 0);
        assertEquals(j1.getId(), hash.get("id"));
        assertEquals(true, hash.get("reached_last"));
        assertIterableEquals(list("a0", "a1"), map(pluck("action_label"), (Iterable) hash.get("requests")));
    }

    @Test
    public void reindexShouldRemoveOldValue() {
        j1.setProperty(Journeys.PROP_START_AT, 20L);
        journeys.reindex(j1);

        assertIterableEquals(list(j1), journeys.query(startAtQuery(20L)));
        assertIterableEquals(list(), journeys.query(startAtQuery(0L)));
        assertEquals(set(j1, j2, j4), toSet(journeys.query(includesActionQuery("a1"))));
    }

    private QueryContext includesActionQuery(String actionLabel) {
        Node action = actions.findByActionLabel(actionLabel);
        return new QueryContext(
                NumericRangeQuery.newLongRange(
                        Journeys.IDX_PROP_ACTION_IDS, 1, action.getId(), action.getId(), true, true));
    }

    private QueryContext startAtQuery(long at) {
        return new QueryContext(NumericRangeQuery.newLongRange(Journeys.PROP_START_AT, 1, at, at, true, true));
    }

    @Test
    public void testToHash() {
        users.addTrait(journeys.user(j1), "company", "acme");
        Map<String, Object> hash = journeys.toHash(j1, 100, 0);
        assertEquals(j1.getId(), hash.get("id"));
        assertEquals("s0", hash.get("session_id"));
        assertEquals(0L, hash.get("start_at"));
        assertEquals(100L, hash.get("finish_at"));
        assertEquals(true, hash.get("reached_last"));
        assertEquals(mapOf("uid", "ur0", "anonymous_id", null, "traits", mapOf("company", set("acme"))), hash.get("user"));
    }

    @Test
    public void toHashWithRequestLimit() {
        Map<String, Object> hash = journeys.toHash(j3, 2, 0);
        assertEquals(j3.getId(), hash.get("id"));
        assertEquals(false, hash.get("reached_last"));
        assertIterableEquals(list("a2", "a3"), map(pluck("action_label"), (Iterable) hash.get("requests")));
    }

    @Test
    public void toHashWithRequestLimitAndOffset() {
        Map<String, Object> hash = journeys.toHash(j3, 2, 1);
        assertEquals(j3.getId(), hash.get("id"));
        assertEquals(true, hash.get("reached_last"));
        assertIterableEquals(list("a3", "a0"), map(pluck("action_label"), (Iterable) hash.get("requests")));
    }


    @Test
    public void requestsShouldListAllActions() {
        assertIterableEquals(iterable("a2", "a3", "a0"),
                map(app.requests().getActionLabelFn(), journeys.userRequests(j3)));
    }


    @Test
    public void requestsShouldNotListIgnoredAction() {
        actions.ignore(actions.findByActionLabel("a3"));
        assertIterableEquals(iterable("a2", "a0"),
                map(app.requests().getActionLabelFn(), journeys.userRequests(j3)));

    }

    @Test
    public void shouldBeAbleToTrackFirstJourney() {
        assertTrue(journeys.isFirstJourney(j1));
        assertTrue(journeys.isFirstJourney(j2));
        assertTrue(journeys.isFirstJourney(j3));
        assertFalse(journeys.isFirstJourney(j4));
    }

    @Test
    public void getPrefixForARequest() {
        Node a0 = last(journeys.userRequests(j3));
        Node a2 = first(journeys.userRequests(j3));
        assertIterableEquals(iterable("a3", "a2"), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(a0)));
        assertIterableEquals(iterable(), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(a2)));
        actions.ignore(actions.findByActionLabel("a2"));
        assertIterableEquals(iterable("a3"), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(a0)));
    }

    @Test
    public void getPrefixForAnAction() {
        assertIterableEquals(iterable("a3", "a2"), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(j3, "a0")));
        assertIterableEquals(iterable("a2"), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(j3, "a3")));
        assertIterableEquals(iterable(), map(app.requests().getActionLabelFn(), journeys.reversedPrefixFor(j3, "a2")));
    }

    @Test
    public void getSuffixForARequest() {
        Node a0 = last(journeys.userRequests(j3));
        Node a2 = first(journeys.userRequests(j3));
        assertIterableEquals(iterable(), map(app.requests().getActionLabelFn(), journeys.suffixFor(a0)));
        assertIterableEquals(iterable("a3", "a0"), map(app.requests().getActionLabelFn(), journeys.suffixFor(a2)));

        actions.ignore(actions.findByActionLabel("a0"));
        assertIterableEquals(iterable("a3"), map(app.requests().getActionLabelFn(), journeys.suffixFor(a2)));
    }

    @Test
    public void testFindJourneyByIds() {
        assertIterableEquals(list(j1, j3, j2), journeys.findByIds(new String[]{
                String.valueOf(j1.getId()), String.valueOf(j3.getId()), String.valueOf(j2.getId())}));
        assertIterableEquals(list(), journeys.findByIds(new String[0]));
    }


    @Test
    public void testJourneyFromSameUserShouldChainTogether() {
        Node journey1 = setupJourney(iterable("a0", "a1"), 0L, 100L, "u", "j1");
        Node journey2 = setupJourney(iterable("a0", "a1"), 500L, 100L, "u", "j2");
        Node journey3 = setupJourney(iterable("a0", "a1"), 200L, 400L, "u", "j3");
        Node user = users.findByIdentifier("u");
        assertIterableEquals(list(journey1, journey3, journey2), users.journeys(user));

        assertEquals(journey3, journeys.next(journey1));
        assertEquals(journey2, journeys.next(journey3));
    }

    @Test
    public void testJourneyChainShouldBeRevisedWhenStartAtChange() {
        Node journey1 = setupJourney(iterable("a0", "a1"), 500L, 100L, "u", "j1");
        Node journey2 = setupJourney(iterable("a0", "a1"), 600L, 100L, "u", "j2");
        Node user = users.findByIdentifier("u");
        assertIterableEquals(list(journey1, journey2), users.journeys(user));

        requests.add(createRequestAttributes("j2", "a3", 400L, "u"));
        assertEquals(400L, journeys.getStartAt(journey2).longValue());
        assertIterableEquals(list(journey2, journey1), users.journeys(user));
    }

    @Test
    public void testCrossJourneyIterator() {
        Function<Node, String> getLabel = new Function<Node, String>() {
            @Override
            public String apply(Node node) throws RuntimeException {
                return requests.getActionLabel(node);
            }
        };
        assertIteratorEquals(list("a1", "a2", "a0", "a1").iterator(), map(getLabel, journeys.userRequestsCrossJourneys(j2)));
        assertIteratorEquals(list("a0", "a1").iterator(), map(getLabel, journeys.userRequestsCrossJourneys(j1)));
    }

    @Test
    public void getGetActions() {
        assertEquals(set("a0", "a1"), journeys.actions(j1));
        assertEquals(set("a0", "a1"), journeys.actions(setupJourney(list("a0", "a0", "a0", "a1"), 100L, "user10")));

    }

    @Test
    public void testGetActionsCrossJourneys() {
        assertEquals(set("a2", "a1", "a1", "a0"), journeys.crossActions(j2));
    }
}
