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


import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class LongSetUtils {

    public static int intersectCount(LongSet left, LongSet right) {
        LongSet smaller;
        LongSet bigger;
        int count = 0;

        if (left.size() > right.size()) {
            bigger = left;
            smaller = right;
        } else {
            bigger = right;
            smaller = left;
        }

        LongIterator smallerIterator = smaller.iterator();
        while (smallerIterator.hasNext()) {
            long next = smallerIterator.nextLong();
            if (bigger.contains(next)) {
                count++;
            }
        }

        return count;
    }


    public static LongSet intersect(LongSet left, LongSet right) {
        LongSet smaller;
        LongSet bigger;
        if (left.size() > right.size()) {
            bigger = left;
            smaller = right;
        } else {
            bigger = right;
            smaller = left;
        }

        LongSet result = new LongOpenHashSet(smaller);
        result.retainAll(bigger);
        return result;
    }


    public static LongSet union(LongSet left, LongSet right) {
        if(left == null) {
            return right;
        }
        if(right == null) {
            return left;
        }
        LongSet smaller;
        LongSet bigger;
        if (left.size() > right.size()) {
            bigger = left;
            smaller = right;
        } else {
            bigger = right;
            smaller = left;
        }

        LongSet result = new LongOpenHashSet(bigger);
        result.addAll(smaller);
        return result;
    }

}
