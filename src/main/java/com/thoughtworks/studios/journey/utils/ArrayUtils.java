package com.thoughtworks.studios.journey.utils;

import java.util.LinkedList;

public class ArrayUtils {

    public static String[] commonPrefix(String[] left, String[] right) {
        LinkedList<String> result = new LinkedList<>();
        int minLength = Math.min(left.length, right.length);
        int i = 0;
        for (; i < minLength; i++) {
            if (left[i].equals(right[i])) {
                result.add(left[i]);
            } else {
                break;
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static String[] concat(String[] left, String[] right) {
        String[] result = new String[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }
}
