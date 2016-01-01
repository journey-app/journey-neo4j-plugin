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
