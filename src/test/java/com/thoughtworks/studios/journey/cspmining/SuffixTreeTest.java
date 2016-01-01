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
import com.thoughtworks.studios.journey.utils.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.assertEquals;
import static org.neo4j.helpers.collection.Iterables.*;

public class SuffixTreeTest extends ModelTestCase {

    private SuffixTree tree;

    @Before
    public void setup() {
        tree = new SuffixTree(app, "c", 100);
    }

    @Test
    public void simpleCasesTree() {
        tree.addSuffix(sequence("a", "b"), 1);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(1, nodes.size());
        assertTreeNode(nodes.get(0), set(1l), "a", "b");
    }

    @Test
    public void shouldBeAbleToDeleteTree() {
        tree.addSuffix(sequence("a", "c", "b"), 1);
        tree.addSuffix(sequence("a", "c", "c"), 2);
        tree.addSuffix(sequence("a", "d"), 3);
        tree.destroy();
        assertIterableEquals(iterable(), SuffixTree.findByCategory(app, TreeCategory.GLOBAL));
        Iterable<Node> childNodes = app.getAllNodesWithLabel(SuffixTree.TREE_NODE_LABEL);
        assertIterableEquals(iterable(), childNodes);
    }

    @Test
    public void shouldBeAbleToDeleteOneLevelTree() {
        tree.addSuffix(sequence("a", "c", "b"), 1);
        tree.destroy();
        assertIterableEquals(iterable(), SuffixTree.findByCategory(app, TreeCategory.GLOBAL));
        Iterable<Node> childNodes = app.getAllNodesWithLabel(SuffixTree.TREE_NODE_LABEL);
        assertIterableEquals(iterable(), childNodes);
    }

    @Test
    public void shouldPushingDownNewBranchesOnUpTreeWithAddSuperSeq() {
        tree.addSuffix(sequence("a", "b"), 1);
        tree.addSuffix(sequence("a", "b", "c"), 2);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(2, nodes.size());
        assertTreeNode(nodes.get(0), set(2l), "c");
        assertTreeNode(nodes.get(1), set(1l), "a", "b");
    }

    @Test
    public void childNodesShouldHaveJourneyCount() {
        tree.addSuffix(sequence("a", "b"), 1);
        tree.addSuffix(sequence("a", "b"), 1);
        tree.addSuffix(sequence("a", "b"), 2);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(2l, nodes.get(0).getJourneyCount());
    }

    @Test
    public void testDepthFirstTraverseWithoutOrderedExpanding() {
        tree.addSuffix(sequence("a", "b", "c"), 1);
        tree.addSuffix(sequence("a", "c", "d"), 2);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(false));
        assertTreeNode(nodes.get(0), set(1l), "b", "c");
        assertTreeNode(nodes.get(1), set(2l), "c", "d");
        assertTreeNode(nodes.get(2), emptyLongSet(), "a");
    }

    @Test
    public void shouldReplaceNodeWithCommonPrefixAndBranchingOnUpTreeWithOverlapSuffix() {
        tree.addSuffix(sequence("a", "b"), 1);
        tree.addSuffix(sequence("a", "c", "c"), 2);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(3, nodes.size());
        assertTreeNode(nodes.get(0), set(1l), "b");
        assertTreeNode(nodes.get(1), set(2l), "c", "c");
        assertTreeNode(nodes.get(2), emptyLongSet(), "a");
    }

    private Set<Long> emptyLongSet() {
        return CollectionUtils.set();
    }

    @Test
    public void shouldPushDownWholeBranchWhenHaveOverlapAndOriginalChildHasDescendants() {
        tree.addSuffix(sequence("a", "c", "b"), 1);
        tree.addSuffix(sequence("a", "c", "c"), 2);
        tree.addSuffix(sequence("a", "d"), 3);

        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(5, nodes.size());

        assertTreeNode(nodes.get(0), set(1l), "b");
        assertTreeNode(nodes.get(1), set(2l), "c");
        assertTreeNode(nodes.get(2), emptyLongSet(), "c");
        assertTreeNode(nodes.get(3), set(3l), "d");
        assertTreeNode(nodes.get(4), emptyLongSet(), "a");
    }

    @Test
    public void shouldSetJourneyIdForMiddleNodeThatIsAnEndNode() {
        tree.addSuffix(sequence("a", "b"), 1);
        tree.addSuffix(sequence("a", "c"), 2);
        tree.addSuffix(sequence("a"), 3);

        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(3, nodes.size());

        assertTreeNode(nodes.get(0), set(1l), "b");
        assertTreeNode(nodes.get(1), set(2l), "c");
        assertTreeNode(nodes.get(2), set(3l), "a");
    }

    @Test
    public void complexUpTree() {
        tree.addSuffix(sequence("r", "o", "m", "a", "n", "e"), 1);
        tree.addSuffix(sequence("r", "o", "m", "a", "n", "u", "s"), 2);
        tree.addSuffix(sequence("r", "o", "m", "u", "l", "u", "s"), 3);
        tree.addSuffix(sequence("r", "u", "b", "e", "n", "s"), 4);
        tree.addSuffix(sequence("r", "u", "b", "e", "r"), 5);
        tree.addSuffix(sequence("r", "u", "b", "i", "c", "o", "n"), 6);
        tree.addSuffix(sequence("r", "u", "b", "i", "c", "u", "n", "d", "u", "s"), 7);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertEquals(13, nodes.size());
        assertTreeNode(nodes.get(0), set(1l), "e");
        assertTreeNode(nodes.get(1), set(2l), "u", "s");
        assertTreeNode(nodes.get(2), emptyLongSet(), "a", "n");
        assertTreeNode(nodes.get(3), set(3l), "u", "l", "u", "s");
        assertTreeNode(nodes.get(4), emptyLongSet(), "o", "m");
        assertTreeNode(nodes.get(5), set(4l), "n", "s");
        assertTreeNode(nodes.get(6), set(5l), "r");
        assertTreeNode(nodes.get(7), emptyLongSet(), "e");
        assertTreeNode(nodes.get(8), set(6l), "o", "n");
        assertTreeNode(nodes.get(9), set(7l), "u", "n", "d", "u", "s");
        assertTreeNode(nodes.get(10), emptyLongSet(), "i", "c");
        assertTreeNode(nodes.get(11), emptyLongSet(), "u", "b");
        assertTreeNode(nodes.get(12), emptyLongSet(), "r");
    }

    @Test
    public void testCaseForSuffixEndOnAPreviouslyExtractedCommonNode() {
        tree.addSuffix(sequence("a", "b", "c", "d"), 0l);
        tree.addSuffix(sequence("a", "b"), 1l);
        tree.addSuffix(sequence("a", "c", "b"), 3l);
        List<TreeNode> nodes = toList(tree.depthFirstNodes(true));
        assertTreeNode(nodes.get(0), set(0l), "c", "d");
        assertTreeNode(nodes.get(1), set(1l), "b");
        assertTreeNode(nodes.get(2), set(3l), "c", "b");
        assertTreeNode(nodes.get(3), emptyLongSet(), "a");

    }


    @Test
    public void shouldPathThroughUpAndDownTreeNode() {
        tree.addSuffix(sequence("d", "e"), 1);
        tree.addSuffix(sequence("d", "e", "f"), 1);

        TreeNode f = first(tree.depthFirstNodes(true));
        assertEquals(list("c", "d", "e", "f"), tree.pathTo(f));

    }


    private List<String> sequence(String... actions) {
        ArrayList<String> result = new ArrayList<>(actions.length);
        Collections.addAll(result, actions);
        return result;
    }

}
