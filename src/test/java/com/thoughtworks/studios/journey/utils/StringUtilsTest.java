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

import static com.thoughtworks.studios.journey.utils.StringUtils.unquote;
import static com.thoughtworks.studios.journey.utils.StringUtils.wildcardMatch;
import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testUnquote() throws Exception {
        assertEquals("foo", unquote("'foo'", "'"));
        assertEquals("foo", unquote("`foo`", "`"));
        assertEquals("fo`o", unquote("`fo`o`", "`"));
        assertEquals("`fo`o", unquote("`fo`o", "`"));
        assertNull(unquote(null, "'"));
    }

    @Test
    public void testWildcardMatch() throws Exception {
        // no wildcard
        assertTrue(wildcardMatch("no wildcard", "no wildcard"));
        assertFalse(wildcardMatch("none wildcard", "no"));

        // just wildcard
        assertTrue(wildcardMatch("any text", "*"));
        assertTrue(wildcardMatch("", "*"));
        assertFalse(wildcardMatch("ab", "?"));
        assertTrue(wildcardMatch("a", "?"));
        assertFalse(wildcardMatch("", "?"));

        // prefixed
        assertTrue(wildcardMatch("prefixed-text", "prefixed*"));
        assertFalse(wildcardMatch("prefix-text", "prefixed*"));

        // suffixed
        assertTrue(wildcardMatch("text-s", "*s"));
        assertFalse(wildcardMatch("text-st", "*s"));


        // '?' replacement
        assertTrue(wildcardMatch("abcd", "ab?d"));
        assertTrue(wildcardMatch("abcd", "abc?"));
        assertTrue(wildcardMatch("abcd", "?bcd"));
        assertFalse(wildcardMatch("abcd", "a?d"));

        // * replacement
        assertTrue(wildcardMatch("abcd", "a*d"));
        assertTrue(wildcardMatch("abcd", "*d"));
        assertFalse(wildcardMatch("abcd", "*e"));

        // * match should be greedy
        assertTrue(wildcardMatch("ccd", "*cd"));

        // mixed
        assertTrue(wildcardMatch("abefcdgiescdfimde", "ab*cd?i*de"));

        // repeated star
        assertTrue(wildcardMatch("abefcdgiescdfimde", "ab**cd?i*de"));

        // a lot of backtracing
        assertFalse(wildcardMatch("aabbbbaababbabababaabbbbabbabbaabbbab", "a*b*a*b*aaaa*ab*abaaa*ab"));

        // pattern longer than text
        assertTrue(wildcardMatch("a", "?*"));
        assertTrue(wildcardMatch("a", "*?*"));

        assertFalse(wildcardMatch(
                "aabbbbaababbabababaabbbbabbabbaabbbab" +
                        "bbabaabbaaaababababbababbabbbbabaaabaa" +
                        "abaabbaaaabbbbabaaabbbbbabbbaabbbbbaba" +
                        "abababaaabaaababaababbaaabaabbabaababb" +
                        "abababaaababbabbabaabbbbabbbbabaabbaab" +
                        "abaaabababbab",
        "a*b*a*b*aaaa*abaaa**b*a***b*a*bb****ba*ba*b******a********a**baba*ab***a***bbba*b**a*b*ba*a*aaaa*ab"));
    }
}
