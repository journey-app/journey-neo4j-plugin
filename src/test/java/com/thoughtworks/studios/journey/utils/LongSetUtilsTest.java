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
package com.thoughtworks.studios.journey.utils;

import com.thoughtworks.studios.journey.ModelTestCase;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Test;

import static com.thoughtworks.studios.journey.utils.LongSetUtils.*;
import static junit.framework.TestCase.assertEquals;


public class LongSetUtilsTest extends ModelTestCase {

    @Test
    public void testIntersect() {
        assertEquals(longSet(2, 3), intersect(longSet(2, 3, 4), longSet(1, 2, 3)));
        assertEquals(longSet(2, 3), intersect(longSet(1, 2, 3), longSet(2, 3, 4)));
        assertEquals(longSet(), intersect(longSet(), longSet(1, 2, 3)));
    }

    private LongSet longSet(long... i) {
        return new LongOpenHashSet(i);
    }

    @Test
    public void testIntersectCount() {
        assertEquals(2, intersectCount(longSet(2, 3, 4), longSet(1, 2, 3)));
        assertEquals(2, intersectCount(longSet(1, 2, 3), longSet(2, 3, 4)));
        assertEquals(0, intersectCount(longSet(), longSet(1, 2, 3)));
    }

    @Test
    public void testUnion() {
        assertEquals(longSet(1, 2, 3, 4), union(longSet(2, 3, 4), longSet(1, 2, 3)));
        assertEquals(longSet(1, 2, 3, 4), union(longSet(1, 2, 3), longSet(2, 3, 4)));
        assertEquals(longSet(1, 2, 3), union(longSet(), longSet(1, 2, 3)));
    }

    @Test
    public void testUnionCanHandleNilInput() {
        assertEquals(longSet(1,2,3), union(longSet(1,2,3), null));
        assertEquals(longSet(1,2,3), union(null, longSet(1, 2, 3)));
    }

}
