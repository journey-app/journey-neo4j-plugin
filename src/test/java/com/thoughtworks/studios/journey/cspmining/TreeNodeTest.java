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
import com.thoughtworks.studios.journey.models.RelTypes;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.HashSet;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.set;
import static org.junit.Assert.*;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class TreeNodeTest extends ModelTestCase {

    private TreeNode treeNode;

    @Before
    public void setup() {
        treeNode = new TreeNode(app.graphDB().createNode());
    }

    @Test
    public void testShouldTakeNeo4jNodesAndGiveBackTreeNodes() throws Exception {
        Node n1 = app.graphDB().createNode();
        assertNotNull(TreeNode.factoryFun().apply(n1));
    }

    @Test
    public void testGetJourneyIdsSet() {
        treeNode.addJourneyId(1l);
        treeNode.addJourneyId(2l);
        treeNode.addJourneyId(1l);
        treeNode.setNames(new String[]{});
        assertTreeNode(treeNode, set(1l, 2l));
        assertEquals(2l, treeNode.getJourneyCount());
    }

    @Test
    public void testSetNames() {
        treeNode.setNames(new String[]{"c", "d"});
        assertTreeNode(treeNode, new HashSet<Long>(), "c", "d");
    }

    @Test
    public void testEquality() {
        Node neonode = app.graphDB().createNode();
        assertEquals(new TreeNode(neonode), new TreeNode(neonode));
        assertNotEquals(treeNode, new TreeNode(neonode));
    }

    @Test
    public void testAddChild() {
        RelTypes childRel = RelTypes.SUFFIX_CHILD;
        TreeNode child1 = new TreeNode(app.graphDB().createNode());
        TreeNode child2 = new TreeNode(app.graphDB().createNode());

        treeNode.addChild(child1);
        treeNode.addChild(child2);
        assertIterableEquals(iterable(child1, child2), treeNode.children());

        assertEquals(treeNode, child1.getParent());
        assertEquals(treeNode, child2.getParent());
        assertNull(treeNode.getParent());

        assertFalse(child1.hasChildren());
        assertFalse(child2.hasChildren());
        assertTrue(treeNode.hasChildren());
    }

    @Test
    public void testMaintainingDepthInfoOnBranchPushDown() {
        RelTypes childRel = RelTypes.SUFFIX_CHILD;
        TreeNode child1 = new TreeNode(app.graphDB().createNode());
        TreeNode child11 = new TreeNode(app.graphDB().createNode());
        TreeNode child111 = new TreeNode(app.graphDB().createNode());

        treeNode.addChild(child11);
        child11.addChild(child111);
        treeNode.addChild(child1);
        child1.addChild(child11);

    }

    @Test
    public void testLookupPath() {
        RelTypes childRel = RelTypes.SUFFIX_CHILD;
        TreeNode child1 = new TreeNode(app.graphDB().createNode());
        TreeNode child2 = new TreeNode(app.graphDB().createNode());
        TreeNode child11 = new TreeNode(app.graphDB().createNode());
        TreeNode child111 = new TreeNode(app.graphDB().createNode());

        treeNode.addChild(child1);
        treeNode.addChild(child2);
        child1.addChild(child11);
        child11.addChild(child111);

        assertIterableEquals(list(child1), child1.pathToRoot());
        assertIterableEquals(list(child11, child1), child11.pathToRoot());
        assertIterableEquals(list(child111, child11, child1), child111.pathToRoot());
        assertIterableEquals(list(), treeNode.pathToRoot());

    }

}
