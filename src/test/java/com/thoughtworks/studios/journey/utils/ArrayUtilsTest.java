package com.thoughtworks.studios.journey.utils;

import org.junit.Test;

import static com.thoughtworks.studios.journey.utils.ArrayUtils.commonPrefix;
import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTest {

    @Test
    public void findingCommonPrefixOfTwoArray() {
        assertArrayEquals(new String[]{"a", "b"},
                commonPrefix(
                        new String[]{"a", "b", "c", "d"},
                        new String[]{"a", "b", "d"}));

        assertArrayEquals(new String[0],
                commonPrefix(
                        new String[]{"a", "b", "c", "d"},
                        new String[]{"b", "a" }));

        assertArrayEquals(new String[0],
                commonPrefix(
                        new String[]{"f", "b", "a", "b"},
                        new String[]{"d", "b", "a" }));



        assertArrayEquals(new String[0],
                commonPrefix(
                        new String[]{"a", "b", "c", "d"},
                        new String[]{}));


    }


}
