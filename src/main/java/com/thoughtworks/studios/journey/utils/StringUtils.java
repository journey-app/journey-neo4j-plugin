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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RunAutomaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class StringUtils {

    public static final char WILDCARD_STRING = '*';
    public static final char WILDCARD_CHAR = '?';

    public static String unquote(String s, String quote) {
        if (s != null && (s.startsWith(quote) && s.endsWith(quote))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static ConcurrentLinkedHashMap<String, RunAutomaton> dfaCaches = new ConcurrentLinkedHashMap.Builder<String, RunAutomaton>()
            .maximumWeightedCapacity(200).build();

    public static boolean wildcardMatch(String text, String pattern) {
        return compileToAutomaton(pattern).run(text);
    }

    private static RunAutomaton compileToAutomaton(String pattern) {
        if (dfaCaches.containsKey(pattern)) {
            return dfaCaches.get(pattern);
        }

        Automaton automaton = Automaton.makeEmptyString();
        for (int i = 0; i < pattern.length(); i++) {
            char p = pattern.charAt(i);
            switch (p) {
                case WILDCARD_STRING:
                    automaton = automaton.concatenate(Automaton.makeAnyString());
                    break;
                case WILDCARD_CHAR:
                    automaton = automaton.concatenate(Automaton.makeAnyChar());
                    break;
                default:
                    automaton = automaton.concatenate(Automaton.makeChar(p));
            }
        }
        RunAutomaton run = new RunAutomaton(automaton);
        dfaCaches.put(pattern, run);
        return run;
    }
}
