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
