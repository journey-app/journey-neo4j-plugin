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
