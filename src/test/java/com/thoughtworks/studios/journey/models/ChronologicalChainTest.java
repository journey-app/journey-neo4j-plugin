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
package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.Iterables.iterable;

public class ChronologicalChainTest extends ModelTestCase {

    private ChronologicalChain chainHelper;
    private Node chain;
    private Node a;
    private Node b;
    private Node c;
    private Node d;

    @Before
    public void setup() {
        chain = db.createNode();
        a = createNodeAt("a", 100L);
        b = createNodeAt("b", 200L);
        c = createNodeAt("c", 300L);
        d = createNodeAt("d", 400L);
        chainHelper = new ChronologicalChain("at");
    }

    @Test
    public void testIsEmpty() {
        assertTrue(chainHelper.isEmpty(chain));
        chainHelper.insert(chain, a);
        assertFalse(chainHelper.isEmpty(chain));
    }

    @Test
    public void nodesShouldBeInTimeOrder() {
        assertIterableEquals(iterable(), chainHelper.nodes(chain));
        assertIterableEquals(iterable(), chainHelper.reverseNodes(chain));

        chainHelper.insert(chain, c);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, d);

        assertIterableEquals(iterable(a, b, c, d), chainHelper.nodes(chain));
        assertIterableEquals(iterable(d, c, b, a), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseSingleNodeChain() {
        chainHelper.insert(chain, a);
        a.setProperty("at", 1000L);
        assertIterableEquals(iterable(a), chainHelper.nodes(chain));
        assertIterableEquals(iterable(a), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseNodesLeftToMiddle() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        a.setProperty("at", 350L);
        chainHelper.revise(a);
        assertIterableEquals(iterable(b, c, a, d), chainHelper.nodes(chain));
        assertIterableEquals(iterable(d, a, c, b), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseNodesLeftToRight() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        a.setProperty("at", 800L);
        chainHelper.revise(a);
        assertIterableEquals(iterable(b, c, d, a), chainHelper.nodes(chain));
        assertIterableEquals(iterable(a, d, c, b), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseNodesRightToLeft() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        d.setProperty("at", 0L);
        chainHelper.revise(d);
        assertIterableEquals(iterable(d, a, b, c), chainHelper.nodes(chain));
        assertIterableEquals(iterable(c, b, a, d), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseNodesRightToMiddle() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        d.setProperty("at", 250L);
        chainHelper.revise(d);
        assertIterableEquals(iterable(a, b, d, c), chainHelper.nodes(chain));
        assertIterableEquals(iterable(c, d, b, a), chainHelper.reverseNodes(chain));
    }


    @Test
    public void testReviseNodeMiddleToMiddleRight() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        b.setProperty("at", 350L);
        chainHelper.revise(b);
        assertIterableEquals(iterable(a, c, b, d), chainHelper.nodes(chain));
        assertIterableEquals(iterable(d, b, c, a), chainHelper.reverseNodes(chain));
    }

    @Test
    public void testReviseNodeMiddleToMiddleLeft() {
        chainHelper.insert(chain, a);
        chainHelper.insert(chain, b);
        chainHelper.insert(chain, c);
        chainHelper.insert(chain, d);

        b.setProperty("at", 350L);
        chainHelper.revise(c);
        assertIterableEquals(iterable(a, c, b, d), chainHelper.nodes(chain));
        assertIterableEquals(iterable(d, b, c, a), chainHelper.reverseNodes(chain));
    }


    private Node createNodeAt(String name, long value) {
        Node node = db.createNode();
        node.setProperty("at", value);
        node.setProperty("name", name);
        return node;
    }
}
