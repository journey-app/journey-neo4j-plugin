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

import org.neo4j.function.Function;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.schema.IndexCreator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.PagingIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.neo4j.helpers.collection.Iterables.filter;
import static org.neo4j.helpers.collection.Iterables.map;
import static org.neo4j.helpers.collection.IteratorUtil.singleOrNull;

public class GraphDbUtils {

    // Destroy all nodes with a label in batches. This method manage the transaction internally. So
    // should not be wrapped into transactions.
    public static void destroyAll(GraphDatabaseService graphDb, Label label) {
        List<Long> nodeIds = new ArrayList<>();
        try (Transaction ignored = graphDb.beginTx()) {
            ResourceIterator<Node> nodes = graphDb.findNodes(label);
            while (nodes.hasNext()) {
                nodeIds.add(nodes.next().getId());
            }
        }

        PagingIterator<Long> pager = new PagingIterator<>(nodeIds.iterator(), 1000);
        while (pager.hasNext()) {
            try (Transaction tx = graphDb.beginTx()) {
                Iterator<Long> page = pager.nextPage();
                while (page.hasNext()) {
                    Long id = page.next();
                    Node node = graphDb.getNodeById(id);
                    for (Relationship relationship : node.getRelationships()) {
                        relationship.delete();
                    }
                    node.delete();
                }
                tx.success();
            }
        }
    }


    public static void dropAllIndex(GraphDatabaseService graphDb, Label label) {
        Schema schema = graphDb.schema();
        Iterable<IndexDefinition> indexes = schema.getIndexes(label);
        for (IndexDefinition index : indexes) {
            index.drop();
        }
    }

    public static void createIndexIfNotExists(GraphDatabaseService graphDb, Label label, String prop) {
        Schema schema = graphDb.schema();
        IndexCreator indexCreator = schema.indexFor(label);

        if (!indexExists(schema, label, prop)) {
            indexCreator.on(prop).create();
        }
    }

    private static boolean indexExists(Schema schema, Label label, String property) {
        for (IndexDefinition index : schema.getIndexes(label)) {
            for (String key : index.getPropertyKeys()) {
                if (key.equals(property)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static Index<Node> legacyIndex(GraphDatabaseService graphDb, Label label) {
        IndexManager indexManager = graphDb.index();
        String indexName = legacyIndexName(label);
        return indexManager.forNodes(indexName);
    }

    private static String legacyIndexName(Label label) {
        return label.name() + ":indexes";
    }

    public static boolean connectSingle(Node left, RelationshipType relType, Node right) {
        Relationship rel = left.getSingleRelationship(relType, Direction.OUTGOING);
        if (rel != null) {
            if (rel.getEndNode() == right) {
                return false; // already connected
            }
            rel.delete();
        }
        left.createRelationshipTo(right, relType);
        return true;
    }

    public static void disconnectSingle(Node start, RelationshipType relType) {
        Relationship rel = start.getSingleRelationship(relType, Direction.OUTGOING);
        if (rel != null) {
            rel.delete();
        }
    }

    public static void connectUnique(Node left, RelationshipType reltype, Node right) {
        if (singleRelBetween(left, right, Direction.OUTGOING, reltype) == null) {
            left.createRelationshipTo(right, reltype);
        }
    }

    public static Object propertyValueOrNull(Node node, String propertyName) {
        return node.hasProperty(propertyName) ? node.getProperty(propertyName) : null;
    }

    public static Iterable<Relationship> relsBetween(final Node left, final Node right, final Direction direction, RelationshipType... types) {

        Iterable<Relationship> relationships = (types.length == 0) ? left.getRelationships(direction) : left.getRelationships(direction, types);
        return filter(new Predicate<Relationship>() {
            @Override
            public boolean accept(Relationship item) {
                return item.getOtherNode(left).equals(right);
            }
        }, relationships);

    }

    public static Relationship singleRelBetween(final Node left, final Node right, final Direction direction, RelationshipType... types) {
        return singleOrNull(relsBetween(left, right, direction, types));
    }

    public static Node getSingleEndNode(Node start, RelationshipType relType) {
        Relationship relationship = start.getSingleRelationship(relType, Direction.OUTGOING);
        return relationship == null ? null : relationship.getEndNode();
    }

    public static Node getSingleStartNode(Node end, RelationshipType relType) {
        Relationship relationship = end.getSingleRelationship(relType, Direction.INCOMING);
        return relationship == null ? null : relationship.getStartNode();
    }

    public static Iterable<Node> getEndNodes(Node start, RelationshipType relType) {
        Iterable<Relationship> relationships = start.getRelationships(relType, Direction.OUTGOING);
        return map(new Function<Relationship, Node>() {

            @Override
            public Node apply(Relationship relationship) {
                return relationship.getEndNode();
            }
        }, relationships);
    }


    public static Iterable<Node> getEndNodes(Node start) {
        Iterable<Relationship> relationships = start.getRelationships(Direction.OUTGOING);
        return map(new Function<Relationship, Node>() {

            @Override
            public Node apply(Relationship relationship) {
                return relationship.getEndNode();
            }
        }, relationships);
    }


    public static int getRelationshipsCount(Node start, RelationshipType relType, Direction direction) {
        Iterable<Relationship> relationships = start.getRelationships(relType, direction);
        int size = 0;
        for (Relationship r : relationships) {
            size++;
        }
        return size;
    }

    public static Iterable<Node> getStartNodes(Node start, RelationshipType relType) {
        Iterable<Relationship> relationships = start.getRelationships(relType, Direction.INCOMING);
        return map(new Function<Relationship, Node>() {

            @Override
            public Node apply(Relationship relationship) {
                return relationship.getStartNode();
            }
        }, relationships);
    }

    public static void increaseLongProperty(Node node, String property) {
        if (node.hasProperty(property)) {
            node.setProperty(property, (long) node.getProperty(property) + 1);
        } else {
            node.setProperty(property, 1L);
        }

    }

}
