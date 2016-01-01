package com.thoughtworks.studios.journey.utils;

import java.util.*;

public class CollectionUtils {
    @SafeVarargs
    public static <T> Set<T> set(T... items) {
        HashSet<T> result = new HashSet<>(items.length);
        Collections.addAll(result, items);
        return result;
    }

    @SafeVarargs
    public static <T> List<T> list(T... items) {
        return Arrays.asList(items);
    }

    public static List listO(Object... items) {
        return Arrays.asList(items);
    }

    public static <T> Set<T> union(Set<T> left, Set<T> right) {
        Set<T> smaller;
        Set<T> bigger;
        if (left.size() > right.size()) {
            bigger = left;
            smaller = right;
        } else {
            bigger = right;
            smaller = left;
        }

        HashSet<T> result = new HashSet<>(bigger);
        result.addAll(smaller);
        return result;
    }

}
