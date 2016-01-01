package com.thoughtworks.studios.journey.jql;

import org.junit.Test;
import org.neo4j.helpers.collection.Iterables;

import static com.thoughtworks.studios.journey.TestHelper.t;
import static com.thoughtworks.studios.journey.TestHelper.v;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TupleTest {
    @Test
    public void testEquality() {
        assertEquals(t(v(1)), t(v(1)));
        assertEquals(t(v(1, 2)), t(v(1, 2)));
        assertNotEquals(t(v(1)), t(v(1, 2)));
        assertEquals(t(v()), t(v()));
        assertNotEquals(t(v(1)), t(v()));
    }

    @Test
    public void testNull() {
        assertTrue(t(v(), v()).isAllNull());
        assertFalse(t(v(), v(3)).isAllNull());
    }

    @Test
    public void testIterator() {
        assertEquals(list(t(v(1)), t(v(2))), Iterables.toList(t(v(1, 2))));
    }

    @Test
    public void testTake() {
        assertEquals(t(v(1)), t(v(1), v(2)).take(0));
        assertEquals(t(v(2)), t(v(1), v(2)).take(1));
    }


    @Test
    public void testDrop() {
        assertEquals(t(v(2), v(3)), t(v(1), v(2), v(3)).drop(0));
        assertEquals(t(v(1), v(3)), t(v(1), v(2), v(3)).drop(1));
        assertEquals(t(v(1), v(2)), t(v(1), v(2), v(3)).drop(2));
    }

}
