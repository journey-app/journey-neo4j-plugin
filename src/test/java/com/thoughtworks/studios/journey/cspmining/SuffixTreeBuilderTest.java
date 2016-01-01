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
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.function.Function;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.thoughtworks.studios.journey.TestHelper.dateToMillis;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.*;

public class SuffixTreeBuilderTest extends ModelTestCase {
    @Test
    public void testBuildTreeForEachAction() throws Exception {
        setupJourney(iterable("a", "b", "d", "a", "c"), dateToMillis(2014, 12, 6), "100");
        setupJourney(iterable("e", "a", "e", "b", "c", "a", "c"), dateToMillis(2014, 12, 7), "200");
        setupJourney(iterable("b", "a", "b", "f", "a", "e", "c"), dateToMillis(2014, 12, 7), "300");
        setupJourney(iterable("a", "f", "b", "a", "c", "f", "c"), dateToMillis(2014, 12, 7), "400");

        SuffixTreeBuilder suffixTreeBuilder = new SuffixTreeBuilder(app, JourneyQuery.Builder.query(app).build(), TreeCategory.GLOBAL, 4);
        Map<String, SuffixTree> forest = suffixTreeBuilder.build();
        assertEquals(6, forest.size());
        assertEquals(set("a", "c", "b", "f", "e", "d"), forest.keySet());

        SuffixTree atree = forest.get("a");
        assertEquals(4, atree.getJourneyCount());
        List<TreeNode> treeNodes = toList(atree.depthFirstNodes(true));
        assertEquals(6, treeNodes.size());
    }

    @Test
    public void testBuildSingleTree() {
        Node j1 = setupJourney(list("3"), 0l);
        Node j2 = setupJourney(list("5", "8", "3", "4", "2"), 100l);
        Node j3 = setupJourney(list("2", "5", "8", "3", "4", "2"), 200l);
        Node j4 = setupJourney(list("2", "5", "3", "4", "7"), 300l);
        Node j5 = setupJourney(list("7", "5", "3", "5", "4"), 400l);
        SuffixTreeBuilder builder = new SuffixTreeBuilder(app, JourneyQuery.Builder.query(app).build(), TreeCategory.GLOBAL, 4);
        SuffixTree tree = builder.build().get("3");

        List<TreeNode> treeNodes = toList(tree.depthFirstNodes(true));
        assertEquals(4, treeNodes.size());

        assertTreeNode(treeNodes.get(0), set(j3.getId(), j2.getId()), "2");
        assertTreeNode(treeNodes.get(1), set(j4.getId()), "7");
        assertTreeNode(treeNodes.get(2), Collections.<Long>emptySet(), "4");
        assertTreeNode(treeNodes.get(3), set(j5.getId()), "5", "4");
    }


    @Test
    public void canLoadBackBuiltTrees() {
        setupJourney(iterable("a", "b", "d", "a", "c"), dateToMillis(2014, 12, 6), "100");
        setupJourney(iterable("e", "a", "e", "b", "c", "a", "c"), dateToMillis(2014, 12, 7), "200");
        setupJourney(iterable("b", "a", "b", "f", "a", "e", "c"), dateToMillis(2014, 12, 7), "300");
        setupJourney(iterable("a", "f", "b", "a", "c", "f", "c"), dateToMillis(2014, 12, 7), "400");
        new SuffixTreeBuilder(app, JourneyQuery.Builder.query(app).build(), TreeCategory.GLOBAL, 4).build();
        Iterable<SuffixTree> forest = toList(SuffixTree.findByCategory(app, TreeCategory.GLOBAL));
        assertEquals(6, count(forest));
        assertEquals(set(1l, 2l, 4l), new HashSet<>(toList(map(getJouneyCountFun(), forest))));
    }

    private Function<SuffixTree, Long> getJouneyCountFun() {
        return new Function<SuffixTree, Long>() {
            @Override
            public Long apply(SuffixTree suffixTree) {
                return suffixTree.getJourneyCount();
            }
        };
    }

}
