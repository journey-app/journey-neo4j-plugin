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
