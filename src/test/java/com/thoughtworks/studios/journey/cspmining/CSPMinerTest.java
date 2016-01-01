/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
package com.thoughtworks.studios.journey.cspmining;

import com.thoughtworks.studios.journey.ModelTestCase;
import com.thoughtworks.studios.journey.jql.JourneyQuery;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSPMinerTest extends ModelTestCase {

    private SuffixTreeBuilder builder;
    private Node j0;
    private Node j1;
    private Node j2;
    private Node j3;
    private Node j4;

    @Before
    public void setup() {
        j0 = setupJourney(list("3"), 0l);
        j1 = setupJourney(list("5", "8", "3", "4", "2"), 0l);
        j2 = setupJourney(list("2", "5", "8", "3", "4", "2"), 0l);
        j3 = setupJourney(list("2", "5", "3", "4", "7"), 0l);
        j4 = setupJourney(list("7", "5", "3", "5", "4"), 0l);
        builder = new SuffixTreeBuilder(app, JourneyQuery.Builder.query(app).build(), TreeCategory.GLOBAL, 4);
    }

    @Test
    public void suffixPatternShouldNotOverCountTheSubPattern() {
        app.destroyData(journeys);
        setupJourney(list("7", "5", "5", "5", "5", "5"), 0l);
        Set<Pattern> patterns = suffixMining("5", 1f);
        assertEquals(list("5", "5", "5", "5", "5"), patterns.iterator().next().getActions());
        assertEquals(1, patterns.size());
    }


    @Test
    public void suffixMineShouldRollUpSupportCorrectly() {
        app.destroyData(journeys);
        setupJourney(list("r", "a", "x"), 0l);
        setupJourney(list("r", "a", "y"), 0l);
        setupJourney(list("r", "a", "z"), 0l);
        setupJourney(list("r", "a", "b", "c", "d", "e"), 0l);
        setupJourney(list("r", "a", "b", "c", "d"), 0l);
        setupJourney(list("r", "a", "b", "c"), 0l);
        setupJourney(list("r", "a", "b"), 0l);
        setupJourney(list("r", "e"), 0l);

        Set<Pattern> patterns = suffixMining("r", 7f / 8f);
        assertEquals(1, patterns.size());
        assertEquals(list("r", "a"), patterns.iterator().next().getActions());


    }

    private Pattern pattern(List<String> actions, Set<Node> journeys) {
        HashSet<Long> journeyIds = new HashSet<>(journeys.size());
        for (Node journey : journeys) {
            journeyIds.add(journey.getId());
        }
        return new Pattern(actions, journeyIds);
    }

    @Test
    public void mineSuffixPatternWithSupport() {
        Set<Pattern> patterns3 = suffixMining("3", 0.4f);
        assertEquals(1, patterns3.size());
        assertTrue(patterns3.contains(pattern(list("3", "4", "2"), set(j1, j2))));
        Set<Pattern> patterns2 = suffixMining("2", 0.4f);
        assertEquals(1, patterns2.size());
        assertTrue(patterns2.contains(pattern(list("2", "5"), set(j2, j3))));
    }

    private Set<Pattern> suffixMining(String rootAction, float threshold) {
        SuffixTree tree = builder.build().get(rootAction);
        return new CSPMiner(tree, threshold, true).suffixPatterns();
    }
}
