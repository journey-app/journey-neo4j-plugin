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
package com.thoughtworks.studios.journey;

import com.thoughtworks.studios.journey.cspmining.TreeNode;
import com.thoughtworks.studios.journey.jql.Stop;
import com.thoughtworks.studios.journey.models.*;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.util.*;

import static com.thoughtworks.studios.journey.TestHelper.assertIterableEquals;
import static com.thoughtworks.studios.journey.utils.CollectionUtils.list;
import static com.thoughtworks.studios.journey.utils.MapUtils.mapOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ModelTestCase {
    public static final String TEST_NAME_SPACE = "parsley";
    public static final String TEST_NAME_SPACE2 = "acme";

    protected Events events;
    protected Journeys journeys;
    protected Actions actions;
    private Transaction tx;
    protected Users users;
    protected static GraphDatabaseService db;
    protected Application app;
    protected Application app2;


    protected GraphDatabaseService createDB() {
        // return new TestGraphDatabaseFactory().newEmbeddedDatabase("/tmp/journey-test");
        return new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @Before
    public void supperSetUp() throws IOException {
        if (db == null) {
            db = createDB();
        }
        app = new Application(db, TEST_NAME_SPACE);
        app2 = new Application(db, TEST_NAME_SPACE2);

        events = app.events();
        journeys = app.journeys();
        actions = app.actions();
        users = app.users();
        try (Transaction ignored = db.beginTx()) {
            app.setupSchema();
            app2.setupSchema();
        }
        this.tx = db.beginTx();
    }

    @After
    public void supperTearDown() {
        tx.failure();
        tx.close();
    }

    protected Node setupJourney(Iterable actions, long startAt, String uid) {
        return setupJourney(actions, startAt, 10 * 1000L, uid);
    }

    protected Node setupJourney(Iterable actions, long startAt, long interval, String uid) {
        String sessionId = UUID.randomUUID().toString();
        return setupJourney(actions, startAt, interval, uid, sessionId);
    }

    protected Node setupJourney(Iterable actions, long startAt, long interval, String uid, String sessionId) {
        Node event = null;
        for (Object action : actions) {
            event = events.add(TestHelper.createEventAttributes(sessionId, (String) action, startAt, uid));
            startAt += interval;
        }
        return events.journeyOf(event);

    }


    protected Node setupJourney(Iterable actions, long startAt) {
        return setupJourney(actions, startAt, null);
    }

    protected void assertTreeNode(TreeNode node, Set<Long> expectedJourneyIds, String... expectedNames) {
        assertIterableEquals(Arrays.asList(expectedNames), Arrays.asList(node.getNames()));
        assertEquals(expectedJourneyIds, node.getJourneyIdsSet());
    }

    protected void assertNodeDeleted(Node node) {
        try {
            app.graphDB().getNodeById(node.getId());
        } catch (NotFoundException e) {
            return;
        }
        fail("node should have been deleted");
    }


    protected List<Map> buildConditions(List... conditionTriples) {
        List<Map> conditions = new ArrayList<>();
        for (List conditionTriple : conditionTriples) {
            conditions.add(mapOf("subject", conditionTriple.get(0), "verb", conditionTriple.get(1), "object", conditionTriple.get(2)));
        }
        return conditions;
    }

    protected Node action(String actionLabel) {
        return actions.findByActionLabel(actionLabel);
    }


    protected Map<String, Object> stop(String action, List<String> conditions) {
        return mapOf("action", action, "conditions", conditions);
    }

    protected Map<String, Object> stop(String action) {
        return mapOf("action", action, "conditions", list());
    }


    protected Map<String, Object> stop(String action, List<String> conditions, boolean rewind) {
        return mapOf("action", action, "conditions", conditions, "rewind", rewind);
    }

}
