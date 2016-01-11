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

public class StringUtils {

    public static final char WILDCARD_STRING = '*';
    public static final char WILDCARD_CHAR = '?';

    public static String unquote(String s, String quote) {
        if (s != null && (s.startsWith(quote) && s.endsWith(quote))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    //todo: DFA based algorithm should be better solution O(n+m) instead of current worst case O(m*n)
    public static boolean wildcardMatch(String text, String pattern) {
        if (pattern.indexOf(WILDCARD_STRING) == -1 && pattern.indexOf('?') == -1) {
            return text.equals(pattern);
        }

        if (pattern.indexOf(WILDCARD_CHAR) == -1 && pattern.indexOf('*') == pattern.length() - 1) {
            return text.startsWith(pattern.substring(0, pattern.length() - 1));
        }

        int i = 0;
        int j = 0;
        int star = -1;
        int mark = -1;
        while (i < text.length()) {
            if (j < pattern.length()
                    && (pattern.charAt(j) == WILDCARD_CHAR || pattern.charAt(j) == text.charAt(i))) {
                i++;
                j++;
            } else if (j < pattern.length() && pattern.charAt(j) == WILDCARD_STRING) {
                star = j;
                j++;
                mark = i;
            } else if (star != -1) {
                j = star + 1;
                i = ++mark;
            } else {
                return false;
            }
        }
        while (j < pattern.length() && pattern.charAt(j) == WILDCARD_STRING) {
            j++;
        }
        return j == pattern.length();
    }
}
