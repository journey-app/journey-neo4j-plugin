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
package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.PrefetchingIterator;

import java.util.Iterator;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.*;
import static org.neo4j.helpers.collection.Iterables.skip;

/**
 * General behaviors for linked list like data structure in neo4j database base on time order.
 */
public class ChronologicalChain {
    private final RelationshipType last;
    private RelationshipType first;
    private RelationshipType next;
    private String timePropName;

    public ChronologicalChain(RelationshipType first, RelationshipType next, RelTypes last, String timePropName) {
        this.first = first;
        this.next = next;
        this.timePropName = timePropName;
        this.last = last;
    }

    public ChronologicalChain(String timePropName) {
        this(RelTypes.FIRST, RelTypes.NEXT, RelTypes.LAST, timePropName);
    }

    public void insert(Node chain, Node node) {
        if (isEmpty(chain)) {
            connectSingle(chain, first, node);
            connectSingle(chain, last, node);
            return;
        }

        Node firstNode = first(chain);
        Node lastNode = last(chain);

        if (time(node) < time(firstNode)) {
            prependNode(chain, firstNode, node);
            return;
        }

        if (time(node) >= time(lastNode)) {
            appendNode(chain, lastNode, node);
            return;
        }


        if (timeDistance(node, firstNode) > timeDistance(node, lastNode)) {
            insertWithReverseOrder(chain, node);
        } else {
            insertWithNormalOrder(chain, node);
        }
    }

    private void appendNode(Node chain, Node lastNode, Node node) {
        connectSingle(lastNode, next, node);
        connectSingle(chain, last, node);
    }

    private void prependNode(Node chain, Node firstNode, Node node) {
        connectSingle(node, next, firstNode);
        connectSingle(chain, first, node);
    }

    private long timeDistance(Node node, Node firstNode) {
        return Math.abs(time(firstNode) - time(node));
    }

    private void insertWithReverseOrder(Node chain, Node node) {
        Node firstNode = null;
        for (Node cursor : reverseNodes(chain)) {
            firstNode = cursor;
            if (time(cursor) <= time(node)) {
                insertAfter(node, cursor);
                return;
            }
        }

        if (firstNode != null) {
            prependNode(chain, firstNode, node);
        }

    }

    private long time(Node cursor) {
        return (long) cursor.getProperty(timePropName);
    }

    private void insertWithNormalOrder(Node chain, Node node) {
        Node lastNode = null;
        for (Node cursor : nodes(chain)) {
            lastNode = cursor;
            if (time(cursor) > time(node)) {
                insertBefore(node, cursor);
                return;
            }
        }

        if (lastNode != null) {
            appendNode(chain, lastNode, node);
        }
    }

    public Iterable<Node> nodes(final Node chain) {
        return nodesAfter(first(chain));
    }

    public Iterable<Node> nodesAfter(final Node startNode) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {

                return new PrefetchingIterator<Node>() {
                    Node current = startNode;

                    @Override
                    protected Node fetchNextOrNull() {
                        try {
                            return current;
                        } finally {
                            //noinspection ConstantConditions
                            if (current != null) {
                                current = getSingleEndNode(current, next);
                            }
                        }
                    }
                };
            }
        };
    }

    public Iterable<Node> reverseNodes(final Node chain) {
        return reverseNodesFrom(last(chain));
    }

    public boolean isEmpty(Node chain) {
        return first(chain) == null;
    }

    public Iterable<Node> reverseNodesFrom(final Node node) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new PrefetchingIterator<Node>() {
                    Node current = node;

                    @Override
                    protected Node fetchNextOrNull() {
                        try {
                            return current;
                        } finally {
                            //noinspection ConstantConditions
                            if (current != null) {
                                current = getSingleStartNode(current, next);
                            }
                        }
                    }
                };
            }
        };
    }

    public void revise(Node node) {
        Node point = null;
        for (Node next : skip(1, nodesAfter(node))) {
            if (time(node) > time(next)) {
                point = next;
            } else {
                break;
            }
        }

        if (point != null) {
            remove(node);
            insertAfter(node, point);
            return;
        }

        for (Node prev : skip(1, reverseNodesFrom(node))) {
            if (time(node) < time(prev)) {
                point = prev;
            } else {
                break;
            }
        }

        if (point != null) {
            remove(node);
            insertBefore(node, point);
        }

    }

    private void insertBefore(Node node, Node point) {
        Node newPrev = getSingleStartNode(point, next);
        if (newPrev != null) {
            connectSingle(newPrev, next, node);
        } else {
            Node chain = getSingleStartNode(point, first);
            connectSingle(chain, first, node);
        }

        connectSingle(node, next, point);
    }

    private void insertAfter(Node node, Node point) {
        Node newNext = getSingleEndNode(point, next);
        if (newNext != null) {
            connectSingle(node, next, newNext);
        } else {
            Node chain = getSingleStartNode(point, last);
            connectSingle(chain, last, node);
        }
        connectSingle(point, next, node);
    }

    private void remove(Node node) {
        Node oldPrev = getSingleStartNode(node, next);
        Node oldNext = getSingleEndNode(node, next);

        disconnectSingle(node, next);

        if (oldPrev == null && oldNext == null) {
            return;
        }

        if (oldPrev == null) {
            Node chain = getSingleStartNode(node, first);
            connectSingle(chain, first, oldNext);
            return;
        }

        if (oldNext == null) {
            Node chain = getSingleStartNode(node, last);
            connectSingle(chain, last, oldPrev);
            disconnectSingle(oldPrev, next);
            return;
        }

        connectSingle(oldPrev, next, oldNext);
    }

    public Node first(Node chain) {
        return getSingleEndNode(chain, first);
    }

    public Node last(Node chain) {
        return getSingleEndNode(chain, last);
    }

}
