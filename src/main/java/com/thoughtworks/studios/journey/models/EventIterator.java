package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Node;

import java.util.Iterator;

public interface EventIterator extends Iterator<Node> {
    void forward();
    void rewind();

    void markRewindPoint();
}
