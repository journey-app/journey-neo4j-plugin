package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public class UserTraits extends PropertyCollections{
    public UserTraits(Application application) {
        super(application);
    }

    @Override
    public Label getLabel() {
        return app.nameSpacedLabel("UserTrait");
    }

    @Override
    protected RelationshipType propertyRelType() {
        return RelTypes.USER_TRAIT;
    }
}
