package com.thoughtworks.studios.journey.models;

import org.neo4j.graphdb.Label;

public class CustomProperties extends PropertyCollections {
    public CustomProperties(Application application) {
        super(application);
    }

    public Label getLabel() {
        return app.nameSpacedLabel("CustomProperty");
    }

    protected RelTypes propertyRelType() {
        return RelTypes.CUSTOM_PROPERTY;
    }
}
