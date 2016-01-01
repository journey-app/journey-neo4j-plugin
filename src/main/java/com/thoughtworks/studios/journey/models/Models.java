package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Map;

public interface Models {
    void setupSchema();

    Label getLabel();

    Map<String, Object> toHash(Node node);

}
