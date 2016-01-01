package com.thoughtworks.studios.journey.utils;

import java.util.*;

public class MapUtils {
    public static <S, T> Map<S, T> mapOf() {
        return new HashMap<>();
    }

    public static <S, T> Map<S, T> mapOf(S k, T v) {
        Map<S, T> values = new HashMap<>();
        values.put(k, v);
        return values;
    }

    public static <S, T> Map<S, T> mapOf(S k1, T v1, S k2, T v2) {
        Map<S, T> values = new HashMap<>();
        values.put(k1, v1);
        values.put(k2, v2);
        return values;
    }

    public static <S, T> Map<S, T> mapOf(S k1, T v1, S k2, T v2, S k3, T v3) {
        Map<S, T> values = new HashMap<>();
        values.put(k1, v1);
        values.put(k2, v2);
        values.put(k3, v3);
        return values;
    }

    public static <S, T> Map<S, T> mapOf(S k1, T v1, S k2, T v2, S k3, T v3, S k4, T v4) {
        Map<S, T> values = new HashMap<>();
        values.put(k1, v1);
        values.put(k2, v2);
        values.put(k3, v3);
        values.put(k4, v4);
        return values;
    }

    public static <S, T> Map<S, T> mapOf(S k1, T v1, S k2, T v2, S k3, T v3, S k4, T v4, S k5, T v5) {
        Map<S, T> values = new HashMap<>();
        values.put(k1, v1);
        values.put(k2, v2);
        values.put(k3, v3);
        values.put(k4, v4);
        values.put(k5, v5);
        return values;
    }


    public static <S> void incrementValue(Map<S, Integer> map, S key, int step) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + step);
        } else {
            map.put(key, step);
        }
    }

    public static <K, V> Map<K, V> merge(Map<K, V> left, Map<K, V> right) {
        Map<K, V> map = new HashMap<>();
        map.putAll(left);
        map.putAll(right);
        return map;
    }

}
