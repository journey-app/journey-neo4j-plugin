package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
    BELONGS_TO, NEXT, ACTION, JOURNEY_USER, REQUEST, FIRST, TIMED_ACTION, LAST, FIRST_JOURNEY, CUSTOM_PROPERTY, USER_TRAIT, LAST_JOURNEY, JOURNEY_ACTIONS, SUFFIX_CHILD
}