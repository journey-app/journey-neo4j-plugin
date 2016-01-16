package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class CrossJourneyEventIteratorTest extends ModelTestCase {
    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j1 = setupJourney(iterable("a0", "a1", "a1"), dateToMillis(2015, 1, 1), 100L, "u0", "s0");
        j2 = setupJourney(iterable("a1", "a2"), dateToMillis(2015, 1, 2), 100L, "u1", "s1");
        j3 = setupJourney(iterable("a2", "a3", "a0"), dateToMillis(2015, 1, 3), 100L, null, "s2");
        j4 = setupJourney(iterable("a0", "a1"), dateToMillis(2015, 1, 4), 100L, "u1", "s1");
    }

    @Test
    public void testShouldGoThroughAllEventsBelongToSameUser() {
        assertEquals(list("a0", "a1", "a1"), mapLabels(iterator(j1)));
        assertEquals(list("a1", "a2", "a0", "a1"), mapLabels(iterator(j2)));
        assertEquals(list("a0", "a1"), mapLabels(iterator(j4)));
    }

    private EventIterator iterator(Node journey) {
        return journeys.eventIterator(journey, true);
    }

    @Test
    public void testForwardToNextJourney() {
        EventIterator iterator = iterator(j2);
        assertTrue(iterator.hasNext());
        assertEquals("a1", events.getActionLabel(iterator.next()));
        iterator.forward();
        assertTrue(iterator.hasNext());
        assertEquals("a0", events.getActionLabel(iterator.next()));

        iterator.forward();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testRewindIterator() {
        EventIterator iterator = iterator(j2);
        iterator.next();
        iterator.markRewindPoint();
        iterator.next();
        iterator.next();
        iterator.rewind();
        assertEquals("a2", events.getActionLabel(iterator.next()));
    }

}