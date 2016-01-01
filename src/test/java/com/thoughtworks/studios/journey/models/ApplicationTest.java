package com.thoughtworks.studios.journey.models;

import com.thoughtworks.studios.journey.ModelTestCase;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static org.junit.Assert.assertEquals;

public class ApplicationTest extends ModelTestCase {
    @Test
    public void testDeleteData() {
        Node journey = setupJourney(list("a1", "a2"), 200L, "u1");
        app.destroyData();
        assertNodeDeleted(journey);
    }
}